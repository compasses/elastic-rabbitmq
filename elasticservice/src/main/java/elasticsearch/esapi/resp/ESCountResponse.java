package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by i325622 on 10/19/16.
 */
public class ESCountResponse {
    @SerializedName("count")
    protected Long count;
    @SerializedName("_shards")
    protected JsonObject _shards;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public JsonObject getShards() {
        return _shards;
    }

    public void setShards(JsonObject shards) {
        this._shards = shards;
    }

}
