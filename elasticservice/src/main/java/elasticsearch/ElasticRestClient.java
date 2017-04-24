package elasticsearch;

import elasticsearch.constant.ESConstants;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by I311352 on 9/30/2016.
 */

@Component
public class ElasticRestClient extends AbstractFactoryBean<RestClient> {
    private static Logger logger = Logger.getLogger(ElasticRestClient.class);
    private ClusterFailureListener clusterFailureListener;
    private volatile RestClient restClient;
    private static List<String> hosts = new ArrayList<>();
    private Sniffer sniffer;

    static {
        hosts.add("10.128.161.107:9200");
    }

    @Override
    public Class<?> getObjectType() {
        return RestClient.class;
    }


    @Override
    public RestClient createInstance() throws Exception {
        if (this.restClient != null) {
            return this.restClient;
        }

        HttpHost[] addresses = new HttpHost[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            addresses[i] = HttpHost.create(hosts.get(i));
        }

        this.restClient = RestClient
                .builder(addresses)
                .setMaxRetryTimeoutMillis(ESConstants.RESTCLIENT_TIMEOUT)
                .build();

//        this.sniffer = Sniffer.builder(this.restClient)
//                                 .setSniffIntervalMillis(60000).build();

        return this.restClient;
    }

    @Override
    protected void destroyInstance(RestClient instance) throws Exception {
        try {
            logger.info("Closing sniffer");
            instance.close();
            this.sniffer.close();
        } catch (IOException e) {
            logger.warn("Error during close sniffer", e);
        }
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }
}
