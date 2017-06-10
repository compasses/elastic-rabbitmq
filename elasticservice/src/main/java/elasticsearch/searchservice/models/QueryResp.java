package elasticsearch.searchservice.models;


import com.google.gson.JsonObject;
import elasticsearch.esapi.resp.ESQueryResponse;
import elasticsearch.esapi.resp.Hits;
import elasticsearch.searchservice.models.ESResp.ESSearchResp;
import elasticsearch.searchservice.models.ESResp.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i311352 on 5/4/2017.
 */
public class QueryResp {
    Long total;
    List<Long> productIds;

    public QueryResp() {
    }

    public QueryResp(Long total) {
        this.total = total;
    }

    public QueryResp(ESQueryResponse response) {
        total = response.getHit().getTotal();
        productIds = new ArrayList<>();
        for (Hits hitResult : response.getHit().getHits()) {
            JsonObject object = hitResult.getSource();
            Long id = object.get("id").getAsLong();
            productIds.add(id);
        }
    }

    public QueryResp(ESSearchResp resp) {
        total = resp.getHits().getTotal();
        productIds = new ArrayList<>();
        for (HitResult hitResult : resp.getHits().getHits()) {
            productIds.add(hitResult.get_source().getId());
        }
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
