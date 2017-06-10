package elasticsearch.searchservice;

import elasticsearch.ElasticRestClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.rest.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Created by i311352 on 5/2/2017.
 */
@Service
public class ESService {
    private static final Logger logger = LoggerFactory.getLogger(ESService.class);

    private static String baseURL;
    private static String searchProductURL;
    private static String countProductURL;

    @Autowired
    ElasticRestClient client;

    public ESService(String url) {
        this.setBaseURL(url);
        this.setSearchProductURL(url+"/storessearch/product/_search");
        this.setCountProductURL(url+"/storessearch/product/_count");
        logger.info("Initial ESService done, url: " + url);
    }

    public void setBaseURL(String url) {
        this.baseURL = url;
    }

    public void setSearchProductURL(String searchURL) {
        ESService.searchProductURL = searchURL;
    }

    public static void setCountProductURL(String countProductURL) {
        ESService.countProductURL = countProductURL;
    }

//    public RestResponse postSearch(byte[] body) {
//        // only return ids
//        map.put("_source", "id");
//        try {
//           return client.post(searchProductURL, body, map);
//        } catch (RestClientException e) {
//            //logger.warn("DO search failed " + e);
//            return null;
//        }
//    }
//
//    public RestResponse postSearchCount(byte[] body) {
//        try {
//            return client.post(countProductURL, body, map);
//        } catch (RestClientException e) {
//            //logger.warn("DO search failed " + e);
//            return null;
//        }
//    }
}
