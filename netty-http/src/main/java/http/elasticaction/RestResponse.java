package http.elasticaction;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by i311352 on 2/15/2017.
 */
public class RestResponse {

    private HttpRequest httpRequest;
    private Object body;

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
}
