package rabbitmq;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jca.cci.connection.SingleConnectionFactory;
import sync.listener.ESMessageListener;

/**
 * Created by I311352 on 12/28/2016.
 */

public class ListenerJobBuilder {
    // just everything under default
    public static MQListenerAdmin buildMQListenerAdmin(ConfigurableApplicationContext context) {
        MessageListenerProxy listenerProxy = new MessageListenerProxy(new ESMessageListener(context),
                "Elastic_Queue");
        return new MQListenerAdmin(getConnectionFactory(), listenerProxy);
    }

    public static ConnectionFactory getConnectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("10.128.165.206");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }
}
