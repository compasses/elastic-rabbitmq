package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/18/2016.
 */
public class ESSaveResponse extends ESBaseResponse {
    protected Boolean created;
    @SerializedName("_shards")
    protected JsonObject shards;

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public JsonObject getShards() {
        return shards;
    }

    public void setShards(JsonObject shards) {
        this.shards = shards;
    }
}
