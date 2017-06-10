package elasticsearch.searchservice.dsl;

import elasticsearch.searchservice.models.QueryParam;
import org.springframework.stereotype.Service;

/**
 * Created by i311352 on 5/3/2017.
 */
@Service
public interface DSLGenerateService {
    String fromQueryParam(QueryParam param);
}
