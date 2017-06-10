package elasticsearch.searchservice.models;

import com.google.common.base.Optional;
import com.google.common.cache.*;
import elasticsearch.esapi.DocumentService;
import elasticsearch.esapi.resp.ESQueryResponse;
import elasticsearch.searchservice.ESService;
import elasticsearch.searchservice.models.ESResp.ESCountResp;
import elasticsearch.searchservice.models.ESResp.ESSearchResp;
import org.elasticsearch.rest.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by i311352 on 5/9/2017.
 */
@Service
public class CacheSearchServiceImpl implements CacheSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ESService.class);
    @Autowired
    private DocumentService esService;

    private LoadingCache<String, Optional<QueryResp>> queryCache = CacheBuilder.newBuilder()
                                                                               .maximumSize(1000)
                                                                               .expireAfterWrite(10, TimeUnit.SECONDS)
                                                                               .recordStats()
                                                                               .removalListener(new RemovalListener<String, Optional<QueryResp>>() {
              @Override
              public void onRemoval(RemovalNotification<String, Optional<QueryResp>> notification) {
                  logger.info("search countCache expired, remove key ");
                  logger.info("search countCache states : " + queryCache.stats().toString());
              }
            })
                                                                               .build(new CacheLoader<String, Optional<QueryResp>>() {
            @Override
            public Optional<QueryResp> load(String s) throws IOException {
                return queryFromES(s);
            }
       });

    private LoadingCache<String, Optional<Integer>> countCache = CacheBuilder.newBuilder()
                                                                             .maximumSize(1000)
                                                                             .expireAfterWrite(10, TimeUnit.SECONDS)
                                                                             .recordStats()
                                                                             .removalListener(new RemovalListener<String, Optional<Integer>>() {
           @Override
           public void onRemoval(RemovalNotification<String, Optional<Integer>> notification) {
               logger.info("search countCache expired, remove key ");
               logger.info("search countCache states : " + countCache.stats().toString());
           }
        })
                                                                             .build(new CacheLoader<String, Optional<Integer>>() {
           @Override
           public Optional<Integer> load(String s) throws IOException {
               return countFromES(s);
           }
        });

    public QueryResp query(String body) {
        try {
            Optional<QueryResp> t = queryCache.get(body);
            if (t.isPresent()) {
                return t.get();
            }
        } catch (ExecutionException e) {
            logger.error("countCache error " + e.getMessage());
        }

        return null;
    }

    public Integer count(String body) {
        try {
            Optional<Integer> t = countCache.get(body);
            if (t.isPresent()) {
                return t.get();
            }
        } catch (ExecutionException e) {
            logger.error("countCache error " + e.getMessage());
        }

        return 0;
    }

    private Optional<QueryResp> queryFromES(String body) {
        logger.info("Call ES to query");
        try {


            ESQueryResponse restResponse = esService.query("searchindex", "product", null, body);
            if (restResponse == null) {
                return Optional.absent();
            }

            logger.info("search result total count " + restResponse.getHit().getTotal());
            return Optional.fromNullable(new QueryResp(restResponse));
        } catch (RestClientException e) {
            logger.error("search is " + e);
        }
        return Optional.absent();
    }

    private Optional<Integer> countFromES(String body) {
        logger.info("Call ES to count");
        try {
            Long count = esService.count("searchindex", "product",  body);
            if (count == null) {
                return Optional.absent();
            }

            logger.info("count result: " + count);
            return Optional.fromNullable(Integer.getInteger(count.toString()));
        } catch (RestClientException e) {
            logger.error("search is " + e);
        }
        return Optional.absent();
    }

//    private Optional<QueryResp> queryFromES(String body) {
//        logger.info("Call ES to query");
//        try {
//            RestResponse restResponse = esService.postSearch(body.getBytes());
//            if (restResponse == null) {
//                return Optional.absent();
//            }
//
//            ESSearchResp resp = restResponse.entity(ESSearchResp.class);
//            logger.info("search result total count " + resp.getHits().getTotal());
//            return Optional.fromNullable(new QueryResp(resp));
//        } catch (RestClientException e) {
//            logger.error("search is " + e);
//        }
//        return Optional.absent();
//    }
//
//    private Optional<Integer> countFromES(String body) {
//        logger.info("Call ES to count");
//        try {
//            RestResponse restResponse = esService.postSearchCount(body.getBytes());
//            if (restResponse == null) {
//                return Optional.absent();
//            }
//
//            ESCountResp resp = restResponse.entity(ESCountResp.class);
//            logger.info("count result: " + resp.getCount());
//            return Optional.fromNullable(resp.getCount());
//        } catch (RestClientException e) {
//            logger.error("search is " + e);
//        }
//        return Optional.absent();
//    }

}
