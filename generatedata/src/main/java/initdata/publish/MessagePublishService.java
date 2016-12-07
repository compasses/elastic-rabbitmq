package initdata.publish;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import initdata.publish.model.Messages;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by I311352 on 11/23/2016.
 */

@Service
public class MessagePublishService {
    private static final Logger logger = Logger.getLogger(MessagePublishService.class);
    private static final String EXCHANGE_NAME = "SharedExchange";
    private static boolean USE_DEFAULT = true;

    private static final String DEFAULT_RABBIT_PROPERTIES= "/rabbit/rabbitmq.properties";
    private static final String DEFAULT_MESSAGE_SOURCE = "/messages/source.json";

    private Channel channel;
    private String rabbitProperties;
    private String messageSource;

    public MessagePublishService() {
        this.rabbitProperties = DEFAULT_RABBIT_PROPERTIES;
        this.messageSource = DEFAULT_MESSAGE_SOURCE;
    }

    public void publish() {
        logger.info("Publish Configuration: rabbit.cfg:" + this.rabbitProperties +
                " message source: " + this.messageSource);
        if (!createChannel()) {
            logger.error("Channel Create Failed, Do Nothing");
            return;
        }
        logger.info("Creaet RabbitMQ Channel Success, Start to publish message");
        try {
            Messages messages = loadMessages();
            logger.info("messages count " + messages.getMessages().size() +
                    " header " + messages.getMessageHeader());
            publishMessage(messages);
        } catch (Exception e) {
            logger.error("Load messages exception: " + e);
        }

        logger.info("finish publish job");
    }

    private void publishMessage(Messages messages) {
        Map<String, Object> mapHeader = new HashMap<>(messages.getMessageHeader());

        messages.getMessages().forEach( msg -> {
            try {
                logger.info("Publish message: routingKey " + msg.getRoutingKey() + " body " + msg.getBody());
                mapHeader.put("X-Message-ID", java.util.UUID.randomUUID().toString());

                this.channel.basicPublish(EXCHANGE_NAME, msg.getRoutingKey() + "." + mapHeader.get("X-Tenant-ID"),
                        new AMQP.BasicProperties.Builder()
                                .contentType("application/json")
                                .headers(mapHeader)
                                .build(),
                                msg.getBody().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        });
    }

    private Messages loadMessages() throws Exception{
        logger.info("Start to load messages " + this.messageSource);
        Gson gson = new Gson();
        JsonReader reader;
        if (this.messageSource == DEFAULT_MESSAGE_SOURCE) {
            InputStream inputStream = this.getClass().getResourceAsStream(this.messageSource);
            reader = new JsonReader(new InputStreamReader(inputStream));
        } else {
            reader = new JsonReader(new FileReader(this.messageSource));
        }

        return gson.fromJson(reader, Messages.class);
    }

    private boolean createChannel() {
        try {
            logger.info("Get rabbitMQ Channel START!");
            InputStream inputStream = null;
            Properties props = new Properties();
            String rabbitusr = null;
            String rabbitpwd = null;
            logger.info("Use the specific rabbit config file!" + this.getRabbitProperties());
            inputStream = new FileInputStream(this.getRabbitProperties());
            props.load(inputStream);
            rabbitusr = props.get("MQ_DB_USR").toString();
            rabbitpwd = props.get("MQ_DB_PWD").toString();
            inputStream.close();

            String rabbithost = props.get("MQ_HOST").toString();
            String rabbitport = props.get("MQ_PORT").toString();

            ConnectionFactory rabbitFactory = new ConnectionFactory();
            rabbitFactory.setHost(rabbithost);
            rabbitFactory.setUsername(rabbitusr);
            rabbitFactory.setPassword(rabbitpwd);
            Long port = Long.valueOf(rabbitport);
            rabbitFactory.setPort(port.intValue());
            logger.info("RabbitMQ connection: " + rabbithost + rabbitport + rabbitusr + rabbitpwd);
            Connection connection = rabbitFactory.newConnection();
            this.channel = connection.createChannel();
            this.channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
            return true;
        } catch (Exception e) {
            logger.error("Exception during getChannel " + e);
        }
        return false;
    }

    public String getRabbitProperties() {
        return rabbitProperties;
    }

    public void setRabbitProperties(String rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
        USE_DEFAULT = false;
    }

    public String getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(String messageSource) {
        this.messageSource = messageSource;
    }
}
