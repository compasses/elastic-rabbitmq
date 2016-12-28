package elasticsearch.esapi.resp;

import com.google.gson.JsonArray;

/**
 * Created by I311352 on 11/8/2016.
 */
public class ESBulkResponse {
    protected Long took;
    protected Boolean errors;
    protected JsonArray items;

    public Long getTook() {
        return took;
    }

    public void setTook(Long took) {
        this.took = took;
    }

    public Boolean getErrors() {
        return errors;
    }

    public void setErrors(Boolean errors) {
        this.errors = errors;
    }

    public JsonArray getItems() {
        return items;
    }

    public void setItems(JsonArray items) {
        this.items = items;
    }
}
