package elasticsearch;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;

/**
 * Created by I311352 on 9/30/2016.
 */
public class ClusterFailureListener extends RestClient.FailureListener {
    private static final Logger logger = Logger.getLogger(ClusterFailureListener.class);

    @Override
    public void onFailure(HttpHost host) {
        logger.error("Node down! {}:{}" + host.getHostName() + host.getPort());
    }
}
