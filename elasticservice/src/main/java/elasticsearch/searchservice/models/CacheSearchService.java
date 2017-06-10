package elasticsearch.searchservice.models;

import org.springframework.stereotype.Service;

/**
 * Created by i311352 on 5/13/2017.
 */
@Service
public interface CacheSearchService {
    QueryResp query(String body);
    Integer count(String body);
}
