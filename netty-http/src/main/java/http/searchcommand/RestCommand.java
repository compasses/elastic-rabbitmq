package http.searchcommand;

import com.netflix.hystrix.*;
import http.elasticaction.RestRequest;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by i311352 on 2/16/2017.
 */
public class RestCommand extends HystrixCommand<char[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCommand.class);

    private RestClient client;
    private RestRequest restRequest;

    public RestCommand(RestClient client, RestRequest restRequest) {
        super(getSetter(client.toString()));
        this.client = client;
        this.restRequest = restRequest;
    }

    private static HystrixCommand.Setter getSetter(String key) {
        LOGGER.info("Command key:" + key);
        String uuid = UUID.randomUUID().toString();
        key += uuid;

        HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory.asKey(key);
        HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(key);
        HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
        commandProperties.withExecutionTimeoutInMilliseconds(3000)
                         .withCircuitBreakerErrorThresholdPercentage(60);

        HystrixThreadPoolProperties.Setter threadPoolPropertiesDefaults = HystrixThreadPoolProperties.Setter();
        threadPoolPropertiesDefaults.withCoreSize(15)
                                    .withMaxQueueSize(300)
                                    .withQueueSizeRejectionThreshold(90);

        return Setter.withGroupKey(groupKey).andCommandKey(commandKey).andCommandPropertiesDefaults(commandProperties)
                     .andThreadPoolPropertiesDefaults(threadPoolPropertiesDefaults);
    }

    @Override
    protected char[] run() throws Exception {
        LOGGER.info("Going to get request: " + "stores/product/_search?size=1000");
        Response response = this.client.performRequest("GET", "stores/product/_search?size=1000");
        return IOUtils.toCharArray(response.getEntity().getContent());
    }

    @Override
    protected char[] getFallback() {
        String string = new String("Just FallBack");
        return string.toCharArray();
    }
}
