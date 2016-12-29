package sync.handler.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import elasticsearch.ESQueryBodyBuilder;
import elasticsearch.constant.ESConstants;
import elasticsearch.esapi.resp.*;
import org.apache.log4j.Logger;
import sync.common.ESHandleMessage;
import sync.common.OperateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by I311352 on 10/21/2016.
 */
public class ESProductListSyncHandler extends ESAbstractHandler {
    private static final Logger logger = Logger.getLogger(ESProductListSyncHandler.class);
    private static final String CHANNELIDS_KEY = "channelIds";

    @Override
    public boolean handleList(ESHandleMessage message) {
        JsonArray jsonObjectArr = message.getJsonElement().getAsJsonArray();
        //no need bulk request
        if (jsonObjectArr.size() < 100) {
            for (int i = 0; i < jsonObjectArr.size(); i++) {
                doList(jsonObjectArr.get(i).getAsJsonObject(), message.getAction());
            }
            aggregateListProduct(message);
            return true;
        }

        for (int i = 0; i < jsonObjectArr.size(); i++) {
            doBulkList(jsonObjectArr.get(i).getAsJsonObject(), message.getAction());
        }
        aggregateListProduct(message);
        return true;
    }

    @Override
    public boolean handleUnlist(ESHandleMessage message) {
        JsonArray jsonObjectArr = message.getJsonElement().getAsJsonArray();
        //no need bulk request
        if (jsonObjectArr.size() < 100) {
            for (int i = 0; i < jsonObjectArr.size(); i++) {
                doList(jsonObjectArr.get(i).getAsJsonObject(), message.getAction());
            }
            aggregateListProduct(message);
            return true;
        }

        for (int i = 0; i < jsonObjectArr.size(); i++) {
            doBulkList(jsonObjectArr.get(i).getAsJsonObject(), message.getAction());
        }
        aggregateListProduct(message);
        return true;
    }

    private void doBulkList(JsonObject jsonObject, OperateAction action) {
        Long channelId = jsonObject.get("channelId").getAsLong();
        Long productId = jsonObject.get("sourceId").getAsLong();
        JsonArray skuIds = jsonObject.get("listIds").getAsJsonArray();

        if (skuIds.size() == 0) {
            // nothing need update
            return;
        }

        ESQueryResponse response = querySKUChannelIds(skuIds, productId);
        List<Hits> hits = response.getHit().getHits();

        if (hits.size() != skuIds.size()) {
            // some sku not created yet
            logger.warn("sku not created but list message is come");
            hits = buildHitForMessMsg(hits, skuIds);
        }

        String body = buildBulkUpdateBody(hits, channelId, productId, action);
        if (body == null) {
            // nothing to do
            return;
        }

        ESBulkResponse bulkResp = documentService.doBulkRequest(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE, null, body);
        if (bulkResp == null || bulkResp.getErrors()) {
            // let's retry, the process is a merge process
            Long try_times = jsonObject.get("try_times") == null ? 0L : jsonObject.get("try_times").getAsLong();
            // should less than 10 times
            if (try_times > 10) {
                throw new IllegalStateException("build request failed, try_times" + try_times);
            }
            jsonObject.addProperty("try_times", ++try_times);
            doBulkList(jsonObject, action);
        }
    }

    private void doList(JsonObject jsonObject, OperateAction action) {
        Long channelId = jsonObject.get("channelId").getAsLong();
        Long productId = jsonObject.get("sourceId").getAsLong();
        JsonArray skuIds = jsonObject.get("listIds").getAsJsonArray();

        if (skuIds.size() == 0) {
            // nothing need update
            return;
        }
        JsonObject queryIds = new JsonObject();
        queryIds.add("ids", skuIds);

        ESMultiGetResponse multiGetResponse = documentService.multiGet(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE,
                null, queryIds.toString());

        if (multiGetResponse == null) {
            throw new IllegalStateException("ES exception, new retry later");
        }

        ArrayList<ESGetByIdResponse> responses = multiGetResponse.getDocs();
        HashMap<String, String> param = new HashMap<>();
        param.put("parent", productId.toString());

        responses.forEach(hit -> {
            JsonObject sku;
            Long skuId = Long.parseLong(hit.getId());
            if (hit.getFound()) {
                sku = hit.getObject();
                param.put("version", hit.getVersion().toString());
            } else {
                sku = new JsonObject();
                sku.addProperty("id", skuId);
                sku.addProperty("parentId", productId);
                sku.addProperty("version", 0);
                param.put("version", "-1");
            }

            // do action
            if (action == OperateAction.LIST) {
                list(skuId, productId, channelId, param, sku);
            } else {
                unlist(skuId, productId, channelId, param, sku);
            }

        } );
    }

    private void list(Long skuId, Long productId, Long channelId, HashMap param, JsonObject sku) {
        // just save
        JsonArray channels = sku.get(CHANNELIDS_KEY) == null ? null : sku.get(CHANNELIDS_KEY).getAsJsonArray();

        if (channels == null) {
            channels = new JsonArray();
        } else {
            for (int i = 0; i < channels.size(); ++i) {
                if (channels.get(i).getAsLong() == channelId) {
                    // listed before, DO NOTHING
                    return ;
                }
            }
        }
        channels.add(new JsonPrimitive(channelId));

        JsonObject updateSKU;
        if (sku.get("version").getAsInt() == 0) {
            updateSKU = sku;
        } else {
            updateSKU = new JsonObject();
        }

        updateSKU.add("channelIds", channels);
        hotDocumentService.update(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE, skuId, param,
                updateSKU, this.conflictFunction());
    }

    private void unlist(Long skuId, Long productId, Long channelId, HashMap param, JsonObject sku) {
        // just save
        JsonArray channels = sku.get(CHANNELIDS_KEY) == null ? null : sku.get(CHANNELIDS_KEY).getAsJsonArray();
        boolean found = false;
        if (channels == null) {
            // unlist message, but no channeIds found, need reentrant
            throw new IllegalStateException("need reject the message temporary");
        } else {
            for (int i = 0; i < channels.size(); ++i) {
                if (channels.get(i).getAsLong() == channelId) {
                    channels.remove(i);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            // no need unlist
            return;
        }

        JsonObject updateSKU;
        if (sku.get("version").getAsInt() == 0) {
            updateSKU = sku;
        } else {
            updateSKU = new JsonObject();
        }

        updateSKU.add("channelIds", channels);
        hotDocumentService.update(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE, skuId, param,
                updateSKU, this.conflictFunction());
    }

    @Override
    protected BiFunction<JsonObject, JsonObject, JsonObject> conflictFunction() {
        return this::handleConflict;
    }

    private JsonObject handleConflict(JsonObject currentObj, JsonObject newObj) {
        JsonArray currentChannels = currentObj.get(CHANNELIDS_KEY) == null ?
                                    null : currentObj.get(CHANNELIDS_KEY).getAsJsonArray();
        if (currentChannels == null || currentChannels.size() == 0) {
            return newObj;
        }

        // do the merge
        JsonArray newChannels = newObj.get(CHANNELIDS_KEY) == null ?
                                null : newObj.get(CHANNELIDS_KEY).getAsJsonArray();
        currentChannels.forEach(channelId -> {
            if (!newChannels.contains(channelId)) {
                newChannels.add(channelId);
            }
        });

        JsonObject changedObj = new JsonObject();
        changedObj.add(CHANNELIDS_KEY, newChannels);
        return changedObj;
    }

    @Override
    public boolean postHandleMessage(ESHandleMessage message) {
        JsonArray jsonObjectArr = message.getJsonElement().getAsJsonArray();
        Long channelId = null;

        if( jsonObjectArr.size() > 0 ){
            JsonObject jsonObject=  jsonObjectArr.get(0).getAsJsonObject();
            channelId = jsonObject.get("channelId").getAsLong();
        }

        List<Long> productIds = new ArrayList<>();
        for (int i = 0; i < jsonObjectArr.size(); i++) {
            productIds.add(jsonObjectArr.get(i).getAsJsonObject().get("sourceId").getAsLong());
        }

        message.setProductIds(productIds);
        message.setChannelId(channelId);
        return super.postHandleMessage(message);
    }


    /**
     *
     * doing the product merge process data
     */
    public void aggregateListProduct(ESHandleMessage message) {
        JsonArray jsonObjectArr = message.getJsonElement().getAsJsonArray();
        for (int i = 0; i < jsonObjectArr.size(); i++) {
            doProductList(jsonObjectArr.get(i).getAsJsonObject());
        }
    }

    private void doProductList(JsonObject jsonObject) {
        Long productId = jsonObject.get("sourceId").getAsLong();
        JsonArray skuIds = jsonObject.get("listIds").getAsJsonArray();

        ESGetByIdResponse response = loadProductChannelListInfo(productId);
        if (response.getFound() != true) {
            logger.error("Product not exist! need redo again later");
            throw new IllegalStateException("need reentrant");
        }

        ESQueryResponse skuChannelResponse = querySKUChannelIds(skuIds, productId);
        Hit hitResult = skuChannelResponse.getHit();
        if (hitResult.getTotal() == 0) {
            logger.warn("No need merge channelsInfo, since no sku list info found");
            return;
        }

        JsonArray skuChannels = new JsonArray();
        hitResult.getHits().forEach (
                hits -> {
                    if (hits.getSource() != null && hits.getSource().get(CHANNELIDS_KEY) != null) {
                        addToArray(skuChannels, hits.getSource().get(CHANNELIDS_KEY).getAsJsonArray());
                    }
                }
        );

        if (skuChannels.size() == 0) {
            // nothing need change
            return;
        }

        JsonObject productChannels = response.getObject();
        // merge the channels info
        HashMap<String, String> params = new HashMap<>();
        params.put("version", response.getVersion().toString());

        if (productChannels != null && productChannels.get(CHANNELIDS_KEY) != null) {
            JsonArray channles = productChannels.get(CHANNELIDS_KEY).getAsJsonArray();
            channles.forEach(id -> {
                if (!skuChannels.contains(id)) {
                    skuChannels.add(id);
                }
            });
        }

        JsonObject updateDoc = new JsonObject();
        updateDoc.add(CHANNELIDS_KEY, skuChannels);
        hotDocumentService.update(ESConstants.STORE_INDEX, ESConstants.PRODUCT_TYPE, productId,
                params, updateDoc, this.conflictFunction());
    }

    private void addToArray(JsonArray skuChannels, JsonArray newAdded) {
        newAdded.forEach(id -> {
            if (!skuChannels.contains(id)) {
                skuChannels.add(id);
            }
        });
    }

    private ESGetByIdResponse loadProductChannelListInfo(Long productId) {
        String fields = new ESQueryBodyBuilder.SourceFieldsListBuilder().addField(CHANNELIDS_KEY).build();
        return documentService.loadSourceSpecifyField(ESConstants.STORE_INDEX, ESConstants.PRODUCT_TYPE,
                productId, null, fields);
    }

    private ESQueryResponse querySKUChannelIds(JsonArray skuIds, Long productId) {
        JsonObject query = ESQueryBodyBuilder.getIdsQueryJsonObject(ESConstants.SKU_TYPE, skuIds);
        JsonArray sourceList = new JsonArray();
        sourceList.add(new JsonPrimitive(CHANNELIDS_KEY));
        sourceList.add(new JsonPrimitive("id"));
        query.add("_source", sourceList);

        HashMap<String, String> param = new HashMap<>();
        //param.put("parent", productId.toString());
        //param.put("refresh", "true");

        return documentService.query(ESConstants.STORE_INDEX, ESConstants.SKU_TYPE, param, query.toString());
    }


    /**
     *  build body like below
     *  action_and_meta_data\n
        optional_source\n
        action_and_meta_data\n
        optional_source\n
     ....
     * @param hits
     * @param channelId
     * @param productId
     * @param action
     * @return
     */
    private String buildBulkUpdateBody(List<Hits> hits, Long channelId, Long productId, OperateAction action) {
        JsonArray updateActionArray = new JsonArray();
        JsonArray updateDocArray = new JsonArray();

        for (Hits hit : hits) {
            JsonObject source = hit.getSource();
            Long skuId = source.get("id").getAsLong();
            JsonArray channels = source.get(CHANNELIDS_KEY) == null ? null : source.get(CHANNELIDS_KEY).getAsJsonArray();
            JsonArray result = doUpdateChannelIds(channelId, channels, action);
            if (result == null) {
                //nothing update
                continue;
            } else {
                // action meta data
                JsonObject innerMeta = new JsonObject();
                innerMeta.addProperty("_id", skuId.toString());
                innerMeta.addProperty("parent", productId.toString());
                JsonObject outerObj = new JsonObject();
                outerObj.add("update", innerMeta);
                updateActionArray.add(outerObj);
                // optional source
                JsonObject innerSource = new JsonObject();
                innerSource.add(CHANNELIDS_KEY, result);

                boolean doc_as_upsert = false;
                if (source.get("needInsert") != null && source.get("needInsert").getAsBoolean()) {
                    innerSource.addProperty("id", skuId);
                    innerSource.addProperty("version", 0);
                    innerSource.addProperty("parentId", productId);
                    doc_as_upsert = true;
                }
                JsonObject outerSource = new JsonObject();
                outerSource.add("doc", innerSource);
                if (doc_as_upsert) {
                    outerSource.addProperty("doc_as_upsert", true);
                }
                updateDocArray.add(outerSource);
            }
        }

        if (updateActionArray.size() != updateDocArray.size()) {
            //illegal state happen, mess message
            throw new IllegalStateException("bulk illegal state on body:" + updateDocArray.toString());
        }

        if (updateActionArray.size() == 0) {
            // nothing update
            return null;
        }

        StringBuilder body = new StringBuilder(50);
        for (int i = 0; i < updateActionArray.size(); ++i) {
            body.append(updateActionArray.get(i).toString());
            body.append("\r\n");
            body.append(updateDocArray.get(i).toString());
            body.append("\r\n");
        }

        return body.toString();
    }

    private JsonArray doUpdateChannelIds(Long channelId, JsonArray channels, OperateAction action) {
        if (action == OperateAction.LIST) {
            return listChannelIdsArray(channelId, channels);
        } else {
            return unlistChannelIdsArray(channelId, channels);
        }
    }

    private JsonArray listChannelIdsArray(Long channelId, JsonArray channels) {
        if (channels == null) {
            channels = new JsonArray();
            channels.add(new JsonPrimitive(channelId));
            return channels;
        }
        // repeat list product
        if (channels.contains(new JsonPrimitive(channelId))) {
            return null;
        } else {
            channels.add(new JsonPrimitive(channelId));
            return channels;
        }
    }

    private JsonArray unlistChannelIdsArray(Long channelId, JsonArray channels) {
        // no need unlist
        if (channels == null) {
            return null;
        }

        if (channels.contains(new JsonPrimitive(channelId))) {
            channels.remove(new JsonPrimitive(channelId));
            return channels;
        } else {
            // unlist before; nothing to do
            return null;
        }
    }

    private List<Hits> buildHitForMessMsg(List<Hits> hit, JsonArray skuIds) {
        skuIds.forEach(item -> {
            Long skuId = item.getAsLong();
            boolean found = false;
            for (Hits h : hit) {
                if (h.getSource().get("id").getAsLong() == skuId) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Hits newH = new Hits();
                JsonObject newSource = new JsonObject();
                newSource.addProperty("id", skuId);
                newSource.addProperty("version", 0);
                newSource.addProperty("needInsert", true);
                newH.setSource(newSource);
                hit.add(newH);
            }
        });

        return hit;
    }
}
