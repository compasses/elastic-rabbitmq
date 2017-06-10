package elasticsearch.searchservice.models;

import java.util.List;

/**
 * Created by i311352 on 5/2/2017.
 */
public class QueryParam {
    private List<Meta> filter;
    private Collection collection;
    private String orderByField;
    private String orderByOrder;
    private Long channelId;
    private Integer pageSize;
    private Integer offSet;
    private Boolean isCount = Boolean.FALSE;

    public List<Meta> getFilter() {
        return filter;
    }

    public void setFilter(List<Meta> filter) {
        this.filter = filter;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public String getOrderByField() {
        return orderByField;
    }

    public void setOrderByField(String orderByField) {
        this.orderByField = orderByField;
    }

    public String getOrderByOrder() {
        return orderByOrder;
    }

    public void setOrderByOrder(String orderByOrder) {
        this.orderByOrder = orderByOrder;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffSet() {
        return offSet;
    }

    public void setOffSet(Integer offSet) {
        this.offSet = offSet;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Boolean getCount() {
        return isCount;
    }

    public void setCount(Boolean count) {
        isCount = count;
    }
}
