package initdata.publish.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by I311352 on 11/24/2016.
 */
public class Messages {
    private Map<String, String> messageHeader;
    private List<MessagesDef> messages;
    public Map<String, String> getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(Map<String, String> messageHeader) {
        this.messageHeader = messageHeader;
    }

    public List<MessagesDef> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagesDef> messages) {
        this.messages = messages;
    }

    class MessageHeader {
        @SerializedName("X-User-ID")
        private Long userId;
        @SerializedName("X-Employee-ID")
        private Long employeeId;
        @SerializedName("X-Tenant-ID")
        private Long tenantId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        @Override
        public String toString() {
            return "MessageHeader{" +
                    "userId=" + userId +
                    ", employeeId=" + employeeId +
                    ", tenantId=" + tenantId +
                    '}';
        }
    }
}
