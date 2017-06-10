package elasticsearch.searchservice;

import elasticsearch.searchservice.dsl.DSLGenerateService;
import elasticsearch.searchservice.models.CacheSearchService;
import elasticsearch.searchservice.models.QueryParam;
import elasticsearch.searchservice.models.QueryResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by i311352 on 5/2/2017.
 */
@Service
public class SearchServiceImpl implements SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Autowired
    private CacheSearchService cacheSearchService;

    @Autowired
    private DSLGenerateService dslGenerateService;

    @Override
    public QueryResp doCount(QueryParam queryFromPHP) {
        if (queryFromPHP.getChannelId() == null) {
            logger.warn("Must have channelId for elasticsearch ");
            return null;
        }

        logger.info("start do count");
        queryFromPHP.setCount(true);
        String body = dslGenerateService.fromQueryParam(queryFromPHP);
        logger.info("count DSL \r\n " + body);
        Integer count = cacheSearchService.count(body);
        logger.info("count done " + count);
        return new QueryResp(count.longValue());
    }

    @Override
    public QueryResp doSearch(QueryParam queryFromPHP) {
        if (queryFromPHP.getChannelId() == null) {
            logger.warn("Must have channelId for elasticsearch ");
            return null;
        }

        logger.info("start do search");
        String body = dslGenerateService.fromQueryParam(queryFromPHP);
        logger.info("search DSL \r\n" + body);
        return cacheSearchService.query(body);
    }
}
