package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/18/2016.
 */
public class ESDeleteResponse extends ESBaseResponse {
    protected Boolean found;
    @SerializedName("_shards")
    protected JsonObject shards;

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public JsonObject getShards() {
        return shards;
    }

    public void setShards(JsonObject shards) {
        this.shards = shards;
    }
}
