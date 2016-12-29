package sync.common;

import com.google.gson.JsonElement;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by I311352 on 10/17/2016.
 */

public class ESHandleMessage {
    private Long tenantId;
    private String type;
    private OperateAction action;
    private byte[] msgBody;
    private JsonElement jsonElement;

    private boolean needSendEvent = true;
    private boolean isEventGenerated = false;

    private List<Long> productIds = null;
    private Long channelId = null;

    public ESHandleMessage() {
    }

    public ESHandleMessage(Long tenantId, String indexType, String action, byte[] msgBody) {
        this.tenantId = tenantId;
        this.type = indexType;
        this.action = OperateAction.valueOf(action);
        this.msgBody = msgBody;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OperateAction getAction() {
        return action;
    }

    public void setAction(OperateAction action) {
        this.action = action;
    }

    public byte[] getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(byte[] msgBody) {
        this.msgBody = msgBody;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public boolean isNeedSendEvent() {
        return needSendEvent;
    }

    public boolean isProductIdsEmpty() {
        return CollectionUtils.isEmpty(this.productIds);
    }

    public void setNeedSendEvent(boolean needSendEvent) {
        this.needSendEvent = needSendEvent;
    }

    public JsonElement getJsonElement() {
        return jsonElement;
    }

    public void setJsonElement(JsonElement jsonElement) {
        this.jsonElement = jsonElement;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public boolean isEventGenerated() {
        return isEventGenerated;
    }

    public void setEventGenerated(boolean eventGenerated) {
        isEventGenerated = eventGenerated;
    }

    @Override
    public String toString() {
        return "ESHandleMessage{" +
                "tenantId=" + tenantId +
                ", type='" + type + '\'' +
                ", action=" + action +
                ", needSendEvent=" + needSendEvent +
                ", isEventGenerated=" + isEventGenerated +
                ", productIds=" + productIds +
                ", channelId=" + channelId +
                '}';
    }
}
