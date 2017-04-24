package elasticsearch.exportimport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I311352 on 11/26/2016.
 */

@Service
public class MigrationESDataTo1612Service  {
    private static final Logger logger = LoggerFactory.getLogger(MigrationESDataTo1612Service.class);
    private final RestClient client = new ElasticRestClient().createInstance();
    private final Gson gson = new Gson();

    //bulk operation
    private final Integer STEP_SIZE = 500;
    private final String UPGRADE_TAG = "upgradeTag";
    private final int UPGRADE_VERSION = 1612;

    // aggregate fields
    private final String TENANTID = "tenantId";
    private final String CHANNELIDS = ESConstants.CHANNEL_LIST_IDS_KEY_1701;

    public JsonObject initSourceFilter() {
        JsonObject source = new JsonObject();
        JsonArray sourceFilter = new JsonArray();
        sourceFilter.add(new JsonPrimitive("id"));
        sourceFilter.add(new JsonPrimitive("skuIds"));
        source.add("_source", sourceFilter);
        source.addProperty("size", STEP_SIZE);
        return source;
    }

    public void exec() {
        logger.info("Start to upgrade ES data to 1612");
        Integer round = 0;
        Long totalCount = 0L;

        HashMap<String, String> param = new HashMap<>();
        param.put("scroll", "1m");
        JsonObject source = initSourceFilter();

        logger.info("going to update ES index max result window");
        updateIndexMaxResultWindow();

        // get scroll info
        ESQueryResponse response = searchAll(param, source.toString());
        String scrollId = response.getScrollId();

        // scroll post body
        JsonObject scollSource = new JsonObject();
        scollSource.addProperty("scroll", "1m");
        scollSource.addProperty("scroll_id", scrollId);

        do {
            if (response == null || response.getHit().getHits().size() == 0) {
                logger.warn("Failed to get response, do nothing");
                break;
            }

            // do update
            totalCount += doUpdate(response);
            // check finish or not
            if (response.getHit().getHits().size() < STEP_SIZE) {
                break;
            }

            // start new round update
            round ++;
            logger.info("Start new round update round = " + round.toString());
            response = searchScroll(new HashMap<>(), scollSource.toString());
        } while (true);

        logger.info("Upgrade finish, use round total = " + round.toString() + " product updates = " + totalCount);
        // need flush
        flush();
        postCheckUpdate(totalCount);
        deleteScroll(scrollId);
    }

    public void postCheckUpdate(Long size) {
        if (size <= 0) {
            return;
        }

        // post check should be ok, during upgrade should be less 10000 new created product.
        if (size >= 10000) {
            size = 9999L;
        }

        logger.info("Post check update result");
        String body = "{  \"query\": {\"bool\" : {  \"must_not\" : {\"term\" : {  \"upgradeTag\" : 1612}  }}  }}";
        HashMap<String, String> param = new HashMap<>();
        param.put("size", size.toString());

        ESQueryResponse res = searchAll(param, body);
        if (res.getHit().getTotal() > 0) {
            logger.warn("Some product miss update, recover it totalCount=" + res.getHit().getTotal());

            // do it again
            doUpdate(res);
        }

        logger.info("Upgrade to 1612 end.");
    }

    public void flush() {
        try {
            client.performRequest(
                    "POST",
                    ESConstants.STORE_INDEX + "/_flush?wait_if_ongoing=true");
        } catch (IOException e) {
            logger.error("Error refresh request " + e);
        }
    }

    /**
     * Use bulk api
     *   { "update" : {"_id" : "4"} }
         { "doc" : {"field" : "value"}}
     * @param response
     * @return
     */
    public Integer doUpdate(ESQueryResponse response) {
        List<Hits> hits = response.getHit().getHits();

        // use bulk API
        JsonArray updateActionArray = new JsonArray();
        JsonArray updateDocArray = new JsonArray();

        for (int i = 0; i < hits.size(); ++i) {
            Hits hit = hits.get(i);
            Long tenantId = Long.parseLong(hit.getRouting());
            Long productId = hit.getSource().get("id").getAsLong();

            JsonArray skuIds = hit.getSource().get("skuIds") == null ?
                    null : hit.getSource().get("skuIds").getAsJsonArray();
            JsonArray channelIds;
            if (skuIds != null) {
                channelIds = getSKUChannelIds(tenantId, skuIds);
            } else {
                channelIds = new JsonArray();
            }

            // action meta data
            JsonObject actionMeta = new JsonObject();
            JsonObject actionInnerObj = new JsonObject();
            actionInnerObj.addProperty("_id", productId);
            actionInnerObj.addProperty("routing", tenantId);
            actionMeta.add("update", actionInnerObj);
            updateActionArray.add(actionMeta);

            // doc source
            JsonObject sourceObj = new JsonObject();
            JsonObject sourceInnerObj = new JsonObject();
            sourceInnerObj.add(CHANNELIDS, channelIds);
            sourceInnerObj.addProperty(TENANTID, tenantId);
            sourceInnerObj.addProperty(UPGRADE_TAG, UPGRADE_VERSION);
            sourceObj.add("doc", sourceInnerObj);
            updateDocArray.add(sourceObj);
        }

        assert updateActionArray.size() == updateDocArray.size();

        StringBuilder body = new StringBuilder(1024*1024*20);
        for (int i = 0; i < updateActionArray.size(); ++i) {
            body.append(updateActionArray.get(i).toString());
            body.append("\r\n");
            body.append(updateDocArray.get(i).toString());
            body.append("\r\n");
        }

        ESBulkResponse bulkResponse = doBulkRequest(body.toString());
        if (bulkResponse == null || bulkResponse.getErrors()) {
            logger.warn("Error happen in bulk request, need extra action");
        }

        return hits.size();
    }

    public ESBulkResponse doBulkRequest(String body) {
        try {
            HttpEntity requestBody = new StringEntity(body);
            Response response = client.performRequest(
                    "POST",
                    ESConstants.STORE_INDEX + "/" + ESConstants.PRODUCT_TYPE + "/_bulk",
                    new HashMap<String, String>(),
                    requestBody);

            ESBulkResponse esResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESBulkResponse.class);
            return esResponse;
        } catch (IOException e) {
            logger.error("Error bulk request " + e);
        }

        return null;
    }

    public JsonArray getSKUChannelIds(Long tenantId, JsonArray skuIds) {
        // get sku list info
        JsonObject queryIds = new JsonObject();
        queryIds.add("ids", skuIds);
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("routing", tenantId.toString());

        ESMultiGetResponse response = multiGet(param, queryIds.toString());
        if (response == null) {
            logger.warn("Query sku info failed, no need do any update");
            return new JsonArray();
        }

        ArrayList<ESGetByIdResponse> responses = response.getDocs();
        JsonArray channelIds = new JsonArray();
        for (int i = 0; i < responses.size(); ++i) {
            ESGetByIdResponse res = responses.get(i);
            if (res.getFound()) {
                JsonObject sku = res.getObject();
                JsonArray channels = sku.get(CHANNELIDS) == null ? null : sku.get(CHANNELIDS).getAsJsonArray();
                if (channels != null && channels.size() > 0) {
                    channels.forEach( channelId -> {
                        if (!channelIds.contains(channelId)) {
                            channelIds.add(channelId);
                        }
                    });
                }
            } else {
                logger.warn("Product with skuIds, but sku body missing");
            }
        }
        return channelIds;
    }

    public ESMultiGetResponse multiGet(HashMap<String, String> params, String requestBody) {
        try {
            HttpEntity requestEntity = new StringEntity(requestBody);
            Response response = client.performRequest(
                    "POST",
                    ESConstants.STORE_INDEX + "/" + ESConstants.SKU_TYPE + "/_mget",
                    params,
                    requestEntity);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.warn("Not found" + response.toString());
                return null;
            }
            String resStr = IOUtils.toString(response.getEntity().getContent());
            return gson.fromJson(resStr, ESMultiGetResponse.class);
        } catch (IOException e) {
            logger.error("Failed to get document with type  " + e);
        }

        return null;
    }


    public ESQueryResponse searchAll(Map<String, String> params, String body) {
        try {
            HttpEntity entity = new StringEntity(body);
            Response response = client.performRequest(
                    "GET",
                    ESConstants.STORE_INDEX + "/" + ESConstants.PRODUCT_TYPE + "/_search",
                    params,
                    entity);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}", response.getStatusLine().getReasonPhrase());
                return null;
            }

            ESQueryResponse esQueryResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESQueryResponse.class);
            return esQueryResponse;
        } catch (IOException e) {
            logger.error("update failed " + e);
        }

        return null;
    }

    public ESQueryResponse searchScroll(Map<String, String> params, String body) {
        try {
            HttpEntity entity = new StringEntity(body);
            Response response = client.performRequest(
                    "POST",
                    "/_search/scroll",
                    params,
                    entity);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}", response.getStatusLine().getReasonPhrase());
                return null;
            }

            ESQueryResponse esQueryResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESQueryResponse.class);
            return esQueryResponse;
        } catch (IOException e) {
            logger.error("update failed " + e);
        }

        return null;
    }

    public void deleteScroll(String scrollId) {
        JsonArray scrollIds = new JsonArray();
        scrollIds.add(new JsonPrimitive(scrollId));
        JsonObject source = new JsonObject();
        source.add("scroll_id", scrollIds);

        try {
            HttpEntity entity = new StringEntity(source.toString());
            client.performRequest(
                    "DELETE",
                    "/_search/scroll",
                    new HashMap<String, String>(),
                    entity);
        } catch (IOException e) {
            logger.error("deleteScroll failed " + e);
        }
    }

    public void updateIndexMaxResultWindow() {
        final String body = "{\"index\": {\"max_result_window\" : 500000}}";
        try {
            HttpEntity entity = new StringEntity(body.toString());
            client.performRequest(
                    "PUT",
                    ESConstants.STORE_INDEX + "/_settings",
                    new HashMap<String, String>(),
                    entity);
        } catch (IOException e) {
            logger.error("updateIndexMaxResultWindow failed " + e);
        }
    }
}
