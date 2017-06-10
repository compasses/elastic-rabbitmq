package elasticsearch.searchservice;


import elasticsearch.searchservice.models.QueryParam;
import elasticsearch.searchservice.models.QueryResp;
import org.springframework.stereotype.Service;

/**
 * Created by i311352 on 5/2/2017.
 */
@Service
public interface SearchService {
    QueryResp doSearch(QueryParam queryFromPHP);
    QueryResp doCount(QueryParam queryFromPHP);
}
