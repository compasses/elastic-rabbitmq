package rabbitmq;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import sync.common.MQMessageConst;

/**
 * Created by I311352 on 12/28/2016.
 */
public class MessageListenerProxy implements MessageListener {
    private static final Logger logger = Logger.getLogger(MessageListenerProxy.class);
    private final MessageListener delegate;
    private final String jobName;

    public MessageListenerProxy(MessageListener listener, String jobName) {
        this.delegate = listener;
        this.jobName = jobName;
    }

    private void beforeOnMessage(Message message) {
        logger.info("Get message");
        this.markJobThread(message);
    }

    @Override
    public void onMessage(Message message) {
        try {
            beforeOnMessage(message);
            delegate.onMessage(message);
        } finally {
            afterOnMessage(message);
        }
    }

    private void afterOnMessage(Message message) {
        logger.info("Finish message");
        this.unmarkJobThread();
    }

    private void markJobThread(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thread_EventJob_").append(this.jobName).append("_tenant_");
        Object tenantId = message.getMessageProperties().getHeaders()
                                 .get(MQMessageConst.H_TENANTID);
        sb.append(tenantId);
        Thread.currentThread().setName(sb.toString());
    }

    private void unmarkJobThread() {
        Thread.currentThread().setName("Thread_EventJob_Idle_Parking");
    }
}