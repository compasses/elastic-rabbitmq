package elasticsearch.esapi.resp;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by I311352 on 10/17/2016.
 */

/**
 * no version in query response
 */
public class Hits extends ESBaseResponse {
    @SerializedName("_score")
    private String score;
    @SerializedName("_source")
    private JsonObject source;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public JsonObject getSource() {
        return source;
    }

    public void setSource(JsonObject source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Hits{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", score='" + score + '\'' +
                ", routing='" + routing + '\'' +
                ", source=" + source +
                '}';
    }
}
