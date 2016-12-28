package elasticsearch.esapi;

import elasticsearch.constant.ESConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * Created by I311352 on 9/30/2016.
 */

@Component
public class IndexService {
    private static final String CURRENT_INDEX_VERSION = ESConstants.STORE_INDEX + "_v1";
    private static final Logger logger = Logger.getLogger(IndexService.class);
    private static volatile boolean initialized = false;
    private final RestClient client;

    @Autowired
    public IndexService(RestClient client) {
        this.client = client;

        if (!initialized) {
            CreateIndexIfNotExist();
            initialized = true;
        }
    }

    public void CreateIndexIfNotExist() {
        if (indexExist(CURRENT_INDEX_VERSION)) {
            logger.info("Index Exist, index name:" + CURRENT_INDEX_VERSION );
            addAlias(CURRENT_INDEX_VERSION, ESConstants.STORE_INDEX);
            return;
        }

        InputStream inputStream = null;
        try {
            String INDEX_MAPING_FILE = "/elasticsearch/stores-product-mapping.json";
            inputStream = this.getClass().getResourceAsStream(INDEX_MAPING_FILE);
            String mapInfo = IOUtils.toString(inputStream);
            createIndex(CURRENT_INDEX_VERSION, mapInfo);
        } catch (IOException e) {
            logger.error("Could not read mapping file for creating index", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public Boolean indexExist(String indexName) {
        try {
            Response response = client.performRequest("HEAD", indexName);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return true;
            } else if (statusCode == 404) {
                return false;
            }
            logger.error("Error checking index existence: {}" + response.getStatusLine().getReasonPhrase());

        } catch (IOException e) {
            logger.error("Failed to verify the index existence ", e);
        }

        return true;
    }

    public void createIndex(String indexName, String requestBody) {
        try {
            HttpEntity entity = new StringEntity(requestBody);
            Response response = client.performRequest("PUT",
                    indexName,
                    new Hashtable<>(),
                    entity);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode > 299) {
                logger.warn("Error while creating an index: {}" + response.getStatusLine().getReasonPhrase());
            }

            addAlias(indexName, ESConstants.STORE_INDEX);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to converting the request body into an http entity");
        } catch (IOException e) {
            logger.warn("Failed to creating new index.");
        }

        logger.info("Create index successful, indexName:" + indexName);
    }

    public void refreshIndex(String indexName){
        try {
            client.performRequest("POST",
                    indexName +  "/_refresh");
        } catch (UnsupportedEncodingException e) {
            logger.warn("Failed to refresh the store index");
        } catch (IOException e) {
            logger.warn("Failed to refresh the store index.");
        }
        logger.info("Refresh index successful, indexName:" + indexName);
    }

    private void addAlias(String indexName, String alias) {
        try {
            Response response = client.performRequest("PUT",
                    indexName+"/_alias/"+alias);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode > 299) {
                logger.warn("Error while addAlias an index: {}" + response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            logger.warn("Failed to addAlias." + indexName + alias);
        }
    } 

    public void reIndex(String newIndex, String oldIndex) {

    }
}
