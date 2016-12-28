package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/17/2016.
 */
public class ESQueryResponse {
    @SerializedName("_scroll_id")
    protected String scrollId;

    protected Long took;

    @SerializedName("timed_out")
    protected Boolean timeOut;

    @SerializedName("_shards")
    protected JsonObject shards;

    @SerializedName("hits")
    protected Hit hit;

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public Boolean getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Boolean timeOut) {
        this.timeOut = timeOut;
    }

    public JsonObject getShards() {
        return shards;
    }

    public void setShards(JsonObject shards) {
        this.shards = shards;
    }

    public Hit getHit() {
        return hit;
    }

    public void setHit(Hit hit) {
        this.hit = hit;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    @Override
    public String toString() {
        return "ESQueryResponse{" +
                "scrollId='" + scrollId + '\'' +
                ", took=" + took +
                ", timeOut=" + timeOut +
                ", shards=" + shards +
                ", hit=" + hit +
                '}';
    }
}
