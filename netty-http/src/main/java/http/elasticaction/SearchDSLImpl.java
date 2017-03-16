package http.elasticaction;

import http.message.QueryMeta;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by i311352 on 2/21/2017.
 */
public class SearchDSLImpl implements SearchDSL<QueryBuilder> {

    private final QueryMeta meta;

    public SearchDSLImpl(QueryMeta meta) {
        this.meta = meta;
    }

    @Override
    public QueryBuilder getDSL() {
        MatchAllQueryBuilder builders = matchAllQuery();
        return builders;
    }
}
