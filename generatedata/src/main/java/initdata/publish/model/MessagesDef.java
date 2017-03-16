package initdata.publish.model;

/**
 * Created by I311352 on 11/24/2016.
 */
public class MessagesDef {
    public String body;
    public String routingKey;

    public String getBody() {
        return body;

    }
    public void setBody(String body) {
        this.body = body;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}