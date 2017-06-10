package elasticsearch.searchservice.models;

import java.util.List;

/**
 * Created by i311352 on 5/2/2017.
 */
public class Meta {
    private String key;
    private List<String> value;
    private String operator;

    public Meta() {}
    public Meta(String key, List<String> value, String operator) {
        this.key = key;
        this.value = value;
        this.operator = operator;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
