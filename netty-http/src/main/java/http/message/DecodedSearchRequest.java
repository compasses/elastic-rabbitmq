package http.message;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by i311352 on 2/13/2017.
 */
public class DecodedSearchRequest {
    private final HttpRequest httpRequest;
    private final long orderNumber;

    private QueryMeta queryMeta;

    public DecodedSearchRequest(HttpRequest request, QueryMeta meta, long orderNumber) {
        this.httpRequest = request;
        this.queryMeta = meta;
        this.orderNumber = orderNumber;
    }
    public QueryMeta getQueryMeta() {
        return queryMeta;
    }

    public void setQueryMeta(QueryMeta queryMeta) {
        this.queryMeta = queryMeta;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public long getOrderNumber() {
        return orderNumber;
    }

    @Override
    public String toString() {
        return "DecodedSearchRequest{" +
                "httpRequest=" + httpRequest +
                ", orderNumber=" + orderNumber +
                ", queryMeta=" + queryMeta +
                '}';
    }
}
