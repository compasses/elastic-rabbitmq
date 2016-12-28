package elasticsearch.esapi.resp;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by I311352 on 10/17/2016.
 */
public class Hit {
    private Long total;

    @SerializedName("max_score")
    private String maxScore;
    private List<Hits> hits;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public String getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(String maxScore) {
        this.maxScore = maxScore;
    }

    public List<Hits> getHits() {
        return hits;
    }

    public void setHits(List<Hits> hits) {
        this.hits = hits;
    }

    @Override
    public String toString() {
        return "Hit{" +
                "total=" + total +
                ", maxScore='" + maxScore + '\'' +
                ", hits=" + hits +
                '}';
    }
}
