package rabbitmq;

import org.springframework.amqp.core.Queue;

/**
 * Created by I311352 on 12/28/2016.
 */
public class MQParsedConfig {
    public final static String exchangeName = "SharedExchange";
    public final static int inital_maxInterval = 10;
    public final static int multiplier = 2;
    public final static int max_retry = 4;
    public final static int max_interval = 50;
    public final static int conncurrentConsumers = 1;
    public final static String[] routingKeys = {"Product.CREATE.#", "SKU.CREATE.#", "CatalogSKU.LIST.#", "" +
            "Category.ASSOCIATE.#"};
}
