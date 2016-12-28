package elasticsearch;


import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Created by i325622 on 10/20/16.
 */
public class ElasticTransportClient {

    private static Logger logger = Logger.getLogger(ElasticTransportClient.class);

    private TransportClient client;

    public ElasticTransportClient() {

//        try {
//
//            client = TransportClient.builder()
//                    .settings(Settings.EMPTY)
//                    .build()
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ESConstants.clusterIPAdress), 9200));
//
//        } catch (UnknownHostException e) {
//            logger.error("init TransportClient fail.", e);
//        }

    }

    public TransportClient getClient() {
        return client;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }
}

