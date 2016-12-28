package rabbitmq;

import org.aopalliance.aop.Advice;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Created by I311352 on 12/28/2016.
 */

public class MQListenerAdmin {
    private static final Logger logger = Logger.getLogger(MQListenerAdmin.class);
    private final ConnectionFactory mqFactory;;
    private final RabbitAdmin amqpAdmin;
    private final SimpleMessageListenerContainer container;
    private Boolean startSucceeded = false;
    private Queue queue;
    private final MessageListener listener;

    public MQListenerAdmin(ConnectionFactory mqFactory, MessageListener listener) {
        container = new SimpleMessageListenerContainer();
        this.mqFactory = mqFactory;
        amqpAdmin = new RabbitAdmin(mqFactory);
        this.listener = listener;
    }

    public void start() {
        declareQueue();

        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setAutoStartup(false);
        container.setChannelTransacted(false);
        container.setConcurrentConsumers(MQParsedConfig.conncurrentConsumers);
        container.setConnectionFactory(mqFactory);
        container.setPrefetchCount(1);
        container.setQueueNames(queue.getName());
        // container.setTransactionManager(new
        // RabbitTransactionManager(mqFactory));
        container.setMessageListener(listener);
        container.setDefaultRequeueRejected(true);
        container.start();
        startSucceeded = true;
    }

    private void declareQueue() {
        amqpAdmin.declareExchange(new TopicExchange(MQParsedConfig.exchangeName));
        queue = new Queue("ElasticRabbit");
        amqpAdmin.declareQueue(queue);

        logger.info("Using queue name:" + queue.getName());

        String[] routingKeys = MQParsedConfig.routingKeys;
        for (String routingKey : routingKeys) {
            amqpAdmin.declareBinding(new Binding(queue.getName(),
                    Binding.DestinationType.QUEUE, MQParsedConfig.exchangeName, routingKey, null));
        }
    }

}
