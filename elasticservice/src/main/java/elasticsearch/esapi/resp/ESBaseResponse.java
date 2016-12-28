package elasticsearch.esapi.resp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/18/2016.
 */
public class ESBaseResponse {
    @SerializedName("_index")
    protected String index;
    @SerializedName("_type")
    protected String type;
    @SerializedName("_id")
    protected String id;
    @SerializedName("_version")
    protected Long version;
    @SerializedName("_routing")
    protected String routing;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    @Override
    public String toString() {
        return "ESBaseResponse{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", version=" + version +
                ", routing='" + routing + '\'' +
                '}';
    }
}
