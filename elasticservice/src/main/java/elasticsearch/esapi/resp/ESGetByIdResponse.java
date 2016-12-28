package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/26/2016.
 */
public class ESGetByIdResponse extends ESBaseResponse {
    private Boolean found;
    @SerializedName("_source")
    private JsonObject object;

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return super.toString() + "ESGetByIdResponse{" +
                "found=" + found +
                ", object=" + object +
                '}';
    }
}
