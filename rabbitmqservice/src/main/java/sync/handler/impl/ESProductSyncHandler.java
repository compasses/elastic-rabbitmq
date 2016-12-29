package sync.handler.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import elasticsearch.ESQueryBodyBuilder;
import elasticsearch.constant.ESConstants;
import elasticsearch.esapi.resp.ESDeleteResponse;
import elasticsearch.esapi.resp.ESGetByIdResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import sync.common.ESHandleMessage;
import sync.common.OperateAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by I311352 on 10/17/2016.
 */

public class ESProductSyncHandler extends ESAbstractHandler {
    public static final Logger logger = Logger.getLogger(ESProductSyncHandler.class);
    public static final String RESERVEDCODE = "product_code_reserved_for_shipping";
    private static final String CATEGORY_LEVEL_FIELD = "internalOrder";


    @Override
    public boolean handleCreate(ESHandleMessage message) {
        // save product in index
        JsonObject product = message.getJsonElement().getAsJsonObject();
        // handle default product code, no need save to ES
        if (product.get("code") != null && product.get("code").getAsString().equals(RESERVEDCODE)) {
            logger.info("Got reserved product code no need save product");
            return true;
        }

        Long productId = product.get("id").getAsLong();
        Long newVersion = product.get("version").getAsLong();
        HashMap<String, String> param = new HashMap<>();
        ESGetByIdResponse response = loadDocumentWithVersion(productId, ESConstants.PRODUCT_TYPE);
        if (response == null || response.getFound() != true) {
            logger.info("The first come message body");
            param.put("version", "-1");
        } else {
            // check the version
            Long currentVersion = response.getObject().get("version") == null ? -1 :
                    response.getObject().get("version").getAsLong();
            if (currentVersion >= newVersion) {
                // need ignore it
                logger.info("Older Version come in last.");
                message.setNeedSendEvent(false);
                return true;
            } else {
                param.put("version", response.getVersion().toString());
            }
        }

        JsonArray skus = product.get("skus").getAsJsonArray();
        JsonArray skuIds = retrieveSKUIds(product, skus);
        product.remove("skus");
        product.add("skuIds", skuIds);

        // add tenantId to product document
        product.addProperty("tenantId", "123");
        // process product categories
        product = handleCategories(product);

        hotDocumentService.update(ESConstants.STORE_INDEX, ESConstants.PRODUCT_TYPE,
                productId, param, product, this.conflictFunction());

        message.setNeedSendEvent(true);
        return true;
    }

    @Override
    public boolean handleUpdate(ESHandleMessage message) {
        // same as create product
        return handleCreate(message);
    }

    @Override
    public boolean handleDelete(ESHandleMessage message) {
        Long productId = jsonParser.parse(new String(message.getMsgBody())).getAsLong();
        logger.info("Handle Delete Product msg id is ." + productId);

        HashMap<String, String> params = new HashMap<>();
        ESDeleteResponse response = documentService.Delete(ESConstants.STORE_INDEX, ESConstants.PRODUCT_TYPE,
                productId, params);

        if (response.getFound() != true) {
            logger.warn("Delete product not found id:" + productId);
        }

        return true;
    }

    @Override
    public boolean postHandleMessage(ESHandleMessage message) {
        if (!message.isNeedSendEvent()) {
            return true;
        }

        Long  productId;
        if (message.getAction() == OperateAction.DELETE) {
            productId = jsonParser.parse(new String(message.getMsgBody())).getAsLong();
            logger.info("Handle Delete id is ." + productId);
        } else {
            productId = message.getJsonElement().getAsJsonObject()
                             .get("id").getAsLong();
        }

        ArrayList<Long> productIds = new ArrayList<>();
        productIds.add(productId);
        message.setProductIds(productIds);

        return super.postHandleMessage(message);
    }

    public JsonObject handleCategories(JsonObject product) {
        if (product.get("category").isJsonNull()) {
            product.add("categories", new JsonArray());
            return product;
        }

        JsonObject category = product.get("category").getAsJsonObject();
        if (category != null) {
            String levelInfo;

            if (category.get(CATEGORY_LEVEL_FIELD) == null ||
                    category.get(CATEGORY_LEVEL_FIELD).isJsonNull() ||
                    category.get(CATEGORY_LEVEL_FIELD).getAsString().isEmpty()) {
                logger.warn("Product Message with category, but miss internal order");
                Long categoryId = category.get("id").getAsLong();
                ESGetByIdResponse response = loadCategoriesInfo(categoryId);
                if (response == null || response.getFound() == false) {
                    logger.warn("product update may lost categories info");
                    return product;
                }

                levelInfo = response.getObject().get(CATEGORY_LEVEL_FIELD) == null ? "" :
                        response.getObject().get(CATEGORY_LEVEL_FIELD).getAsString();
                if (StringUtils.isEmpty(levelInfo)) {
                    logger.warn("illegal message, without internalOrder:" + categoryId);
                    return product;
                }
            } else {
                levelInfo = category.get(CATEGORY_LEVEL_FIELD).getAsString();
            }

            String[] ids = levelInfo.split("\\.");
            JsonArray longIds = new JsonArray();
            Arrays.stream(ids).forEach(strId -> {
                longIds.add(new JsonPrimitive(Long.parseLong(StringUtils.trim(strId))));
            });

            product.add("categories", longIds);
        }
        return product;
    }

    private ESGetByIdResponse loadCategoriesInfo(Long categoryId) {
        String fields = new ESQueryBodyBuilder.SourceFieldsListBuilder().addField(CATEGORY_LEVEL_FIELD).build();
        return documentService.loadSourceSpecifyField(ESConstants.STORE_INDEX, ESConstants.PRODUCT_PROPERTY,
                categoryId, null, fields);
    }

    private JsonArray retrieveSKUIds(JsonObject product, JsonArray skus) {
        JsonArray skuIds = new JsonArray();
        JsonArray preSKUIds = product.get("skuIds") == null? null : product.get("skuIds").getAsJsonArray();

        // change skus to just skuIds array
        for (int i = 0; i < skus.size(); ++i) {
            if (preSKUIds == null || preSKUIds.contains(skus.get(i).getAsJsonObject().get("id"))) {
                skuIds.add(skus.get(i).getAsJsonObject().get("id"));
            }
        }
        return skuIds;
    }
}
