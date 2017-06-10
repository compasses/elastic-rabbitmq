package elasticsearch.searchservice.models.ESResp;

import java.util.List;

/**
 * Created by i311352 on 5/4/2017.
 */
public class Hit {
    Long total;
    List<HitResult> hits;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<HitResult> getHits() {
        return hits;
    }

    public void setHits(List<HitResult> hits) {
        this.hits = hits;
    }
}