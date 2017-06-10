package elasticsearch.searchservice.models;

import java.util.List;

/**
 * Created by i311352 on 5/2/2017.
 */

public class Collection {
    private String conditionType;
    private List<Meta> conditions;

    public Collection() {}
    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public List<Meta> getConditions() {
        return conditions;
    }

    public void setConditions(List<Meta> conditions) {
        this.conditions = conditions;
    }
}