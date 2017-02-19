package http.message;

import java.util.HashMap;

/**
 * Created by i311352 on 2/13/2017.
 */
public class QueryMeta {

    private String orderBy;
    private HashMap<String, String> filters = new HashMap<String, String>();

    public HashMap addMeta(String type, String value) {
        filters.put(type, value);
        return filters;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "QueryMeta{" +
                "orderBy='" + orderBy + '\'' +
                ", filters=" + filters +
                '}';
    }
}
