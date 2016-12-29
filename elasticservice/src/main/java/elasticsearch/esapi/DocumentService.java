package elasticsearch.esapi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import elasticsearch.esapi.resp.*;
import elasticsearch.exception.ElasticAPIException;
import elasticsearch.exception.ElasticQueryException;
import elasticsearch.exception.ElasticVersionConflictException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I311352 on 10/3/2016.
 */

@Component
public class DocumentService {
    private static final Logger logger = Logger.getLogger(DocumentService.class);

    private final RestClient client;
    private final Gson gson;
    private final IndexService indexService;

    @Autowired
    public DocumentService(RestClient client, Gson gson, IndexService indexService) {
        this.client = client;
        this.gson = gson;
        this.indexService = indexService;
    }

    public boolean isDocExist(String index, String type, Long sourceId, HashMap param) {
        Map<String, String> params = addTenantId2Param(param);
        try {
            Response response = client.performRequest("HEAD", index + "/" + type + "/" + sourceId, params);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return true;
            } else if (statusCode == 404) {
                return false;
            }

            throw new ElasticQueryException("Exception for Doc Exist query");
        } catch (IOException e) {
            logger.warn("Failed to verify the index existence ", e);
            throw new ElasticAPIException("Call indexExist HEAD exception:"+e.getMessage());
        }
    }

    public ESBulkResponse doBulkRequest(String index, String type, Map<String, String> params, String body) {
        params = addTenantId2Param(params);
        try {
            HttpEntity requestBody = new StringEntity(body);
            Response response = client.performRequest(
                    "POST",
                    index + "/" + type + "/_bulk",
                    params,
                    requestBody);

            ESBulkResponse esResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESBulkResponse.class);
            return esResponse;
        } catch (IOException e) {
            logger.error("Failed to delete document with type [" + type +  "]" + e);
        }

        return null;
    }
    public ESDeleteResponse Delete(String index, String type, Long sourceId, Map<String, String> params) {
        params = addTenantId2Param(params);
        try {
            Response response = client.performRequest(
                    "DELETE",
                    index + "/" + type + "/" + sourceId,
                    params);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}" + response.getStatusLine().getReasonPhrase());
                throw new ElasticAPIException("Could not index a document, status code is " + statusCode);
            }

            ESDeleteResponse deleteResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESDeleteResponse.class);
            return deleteResponse;
        } catch (IOException e) {
            logger.error("Failed to delete document with type [" + type + "] id ["
                    + sourceId + "]: ",e);
        }
        return null;
    }

    public ESSaveResponse update(String index, String type, Long sourceId, Map<String, String> params, HttpEntity requestBody) {
        params = addTenantId2Param(params);

        // for real-time fetch
        //params.put("refresh", "true");
        try {
            Response response = client.performRequest(
                    "POST",
                    index + "/" + type + "/" + sourceId + "/_update",
                    params,
                    requestBody);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}" +
                        response.getStatusLine().getReasonPhrase());
                throw new ElasticAPIException("Could not index a document, status code is " + statusCode);
            }

            ESSaveResponse esResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESSaveResponse.class);
            return esResponse;
        } catch (ResponseException rex) {
            logger.warn("Got elasticsearch exception " + rex);
            Response res = rex.getResponse();
            if (res.getStatusLine().getStatusCode() == 409) {
                logger.warn("Conflict on store object");
                throw new ElasticVersionConflictException("type:" + type + " params:" + params.toString()
                        + " exception:" + rex);
            }
        }catch (IOException e) {
            logger.error("Failed to update document with type [" + type + "] id ["+sourceId+"]");
        }

        return null;
    }

    public ESSaveResponse Store(String index, String type, Long sourceId, Map<String, String> params, HttpEntity requestBody) {
        params = addTenantId2Param(params);
        // for real-time fetch
        //params.put("refresh", "true");
        try {
            Response response = client.performRequest(
                    "POST",
                    index + "/" + type + "/" + sourceId,
                    params,
                    requestBody);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}" + response.getStatusLine().getReasonPhrase());
                throw new ElasticAPIException("Could not index a document, status code is " + statusCode);
            }

            ESSaveResponse esQueryResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESSaveResponse.class);
            return esQueryResponse;
        } catch (ResponseException rex) {
            logger.warn("Got elasticsearch exception " + rex);
            Response res = rex.getResponse();
            if (res.getStatusLine().getStatusCode() == 409) {
                logger.warn("Conflict on store object");
                throw new ElasticVersionConflictException(index+type);
            }
        } catch (IOException e) {
            logger.error("Failed to store document with type [" + type + "] id [" + sourceId + "]: ",e);
        }
        return null;
    }

    public ESMultiGetResponse multiGet(String index, String type, HashMap<String, String> params, String requestBody) {
        Map<String, String> param = addTenantId2Param(params);

        try {
            HttpEntity requestEntity = new StringEntity(requestBody);
            Response response = client.performRequest(
                    "POST",
                    index + "/" + type + "/_mget",
                    param,
                    requestEntity);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.warn("Not found" + response.toString());
                return null;
            }

            logger.info("Got response :{}" + response.getEntity().toString());
            String resStr = IOUtils.toString(response.getEntity().getContent());
            return gson.fromJson(resStr, ESMultiGetResponse.class);
        } catch (IOException e) {
            logger.error("Failed to get document with type [" + type + "] ",e);
        }

        return null;
    }

    public Long count(String index, String type, String query) {
        Map<String, String> params = addTenantId2Param(null);
        try {
            Response response = client.performRequest(
                    "GET",
                    index + "/" + type + "/_count",
                    params,
                    new StringEntity(query));

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode !=200 ) {
                logger.warn("Problem while indexing a document: {}" + response.getStatusLine().getReasonPhrase());
                throw new ElasticAPIException("Could not index a document, status code is " + statusCode);
            }

            ESCountResponse countResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESCountResponse.class);
            if (countResponse != null) {
               return countResponse.getCount();
            }
        } catch (IOException e) {
            logger.error("Failed to count document with type ["+ type + "]: ",e);
        }
        return null;
    }

    public JsonObject loadSourceObjectById(String index, String type, Long sourceId, HashMap param) {
        Map<String, String> params = addTenantId2Param(param);
        try {
            Response response = client.performRequest(
                    "GET",
                    index + "/" + type + "/" + sourceId + "/_source",
                    params);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.warn("Problem while indexing a document: {}" + response.getStatusLine().getReasonPhrase());
                return null;
            }
            return gson.fromJson(IOUtils.toString(response.getEntity().getContent()), JsonObject.class);
        } catch (IOException e) {
            logger.error("Failed to get document with type [" + type + "] id [" + sourceId + "]: ",e);
        }
        return null;
    }

    public ESGetByIdResponse loadSourceSpecifyField(String index, String type, Long id, HashMap param,
                                                    String sourceFields) {
        Map<String, String> params = addTenantId2Param(param);
        if (sourceFields != null && !sourceFields.isEmpty()) {
            params.put("_source", sourceFields);
        }

        return loadSourceById(index, type, id, (HashMap) params);
    }

    public ESGetByIdResponse loadSourceById(String index, String type, Long id, HashMap param) {
        Map<String, String> params = addTenantId2Param(param);
        try {
            Response response = client.performRequest(
                    "GET",
                    index + "/" + type + "/" + id,
                    params);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.warn("Not found" + response.toString());
                return null;
            }

            logger.info("Got response :{}" + response.getEntity().toString());
            String resStr = IOUtils.toString(response.getEntity().getContent());
            return gson.fromJson(resStr, ESGetByIdResponse.class);
        } catch (ResponseException rex) {
            logger.warn("Got elasticsearch exception " + rex);
            try {
                String resStr = IOUtils.toString(rex.getResponse().getEntity().getContent());
                return gson.fromJson(resStr, ESGetByIdResponse.class);
            } catch (IOException e) {
                logger.error("Failed to get document with type [" + type +  "] id ["+id+"]: ",e);
            }
        } catch (IOException e) {
            logger.error("Failed to get document with type ["+type+ "] id ["+id+"]: ",e);
        }
        return null;
    }


    public ESQueryResponse query(String index, String type, Map<String, String> params, String body) {
        params = addTenantId2Param(params);

        HttpEntity requestBody = new StringEntity(body, Charset.defaultCharset());
        try {
            Response response = client.performRequest(
                    "GET",
                    index + "/" + type + "/_search",
                    params,
                    requestBody);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Problem while indexing a document: {}" + response.getStatusLine().getReasonPhrase());
                throw new ElasticAPIException("Could not index a document, status code is " + statusCode);
            }

            ESQueryResponse esQueryResponse = gson.fromJson(IOUtils.toString(response.getEntity().getContent()),
                    ESQueryResponse.class);

            return esQueryResponse;
        } catch (IOException e) {
            logger.error("Failed to update document with type [" + type + "] body [" + body + "]: ",e);
        }
        return null;
    }


    public List<JsonObject> queryObjects(String index, String type, Map<String, String> params, String body) {
        List<JsonObject> objects = new ArrayList<>();
        ESQueryResponse response = this.query(index, type, params, body);
        if (response != null && response.getHit() != null && CollectionUtils.isNotEmpty(response.getHit().getHits())) {
            response.getHit().getHits().forEach(hit->{
                objects.add(hit.getSource());
            });
        }
        return objects;
    }

    private Map<String, String> addTenantId2Param(Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        params.put("routing", "1");// UserInfoContextHolder.getTenantId().toString()
        return params;
    }

}
