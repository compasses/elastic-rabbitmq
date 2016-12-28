package elasticsearch.esapi;

import com.google.gson.JsonObject;
import elasticsearch.constant.ESConstants;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.ElasticsearchHostsSniffer;
import org.elasticsearch.client.sniff.HostsSniffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by I311352 on 11/17/2016.
 */

@Component
public class ClusterService {
    private static final Logger logger = Logger.getLogger(ClusterService.class);
    private final RestClient client;
    private final ElasticsearchHostsSniffer elasticsearchHostsSniffer;
    private static Task task = null;

    @Autowired
    public ClusterService(RestClient client) {
        this.client = client;
        this.elasticsearchHostsSniffer = new ElasticsearchHostsSniffer(client);
    }

    public List<HttpHost> getNodesInfo() {
        try {
            return elasticsearchHostsSniffer.sniffHosts();
        } catch (IOException e) {
            logger.error("Cannot get elastic search nodes.." + e);
        }
        return null;
    }

    public JsonObject getNodesJvmInfo() {
        return null;
    }

    public void startSniffer() {
        this.task = new Task(elasticsearchHostsSniffer, client, ESConstants.SNIFFER_INTERVAL);
    }

    // sniffer implementation
    private static class Task implements Runnable {
        private final HostsSniffer hostsSniffer;
        private final RestClient restClient;
        //private NodeStatusService nodeStatusService = null;
        private int lastCheckHttpHosts = 0;

        private final long sniffIntervalMillis;
        private final ScheduledExecutorService scheduledExecutorService;
        private final AtomicBoolean running = new AtomicBoolean(false);
        private ScheduledFuture<?> scheduledFuture;

        private Task(HostsSniffer hostsSniffer, RestClient restClient, int sniffIntervalMillis) {
            this.hostsSniffer = hostsSniffer;
            this.restClient = restClient;
            this.sniffIntervalMillis = sniffIntervalMillis;
            this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
            //this.nodeStatusService = null;// ApplicationContextHolder.getBean(NodeStatusService.class);
            scheduleNextRun(0);
        }

        synchronized void scheduleNextRun(long delayMillis) {
            if (scheduledExecutorService.isShutdown() == false) {
                try {
                    if (scheduledFuture != null) {
                        //regardless of when the next sniff is scheduled, cancel it and schedule a new one with updated delay
                        this.scheduledFuture.cancel(false);
                    }
                    logger.debug("scheduling next sniff in " + delayMillis + " ms");
                    this.scheduledFuture = this.scheduledExecutorService.schedule(this, delayMillis, TimeUnit.MILLISECONDS);
                } catch(Exception e) {
                    logger.error("error while scheduling next sniffer task", e);
                }
            }
        }

        @Override
        public void run() {
            sniff(null, sniffIntervalMillis);
        }

        void sniff(HttpHost excludeHost, long nextSniffDelayMillis) {
            List<HttpHost> sniffedHosts = null;
            if (running.compareAndSet(false, true)) {
                try {
                    sniffedHosts = hostsSniffer.sniffHosts();
                    logger.debug("sniffed hosts: " + sniffedHosts);
                    if (excludeHost != null) {
                        sniffedHosts.remove(excludeHost);
                    }
                    if (sniffedHosts.isEmpty()) {
                        logger.warn("no hosts to set, hosts will be updated at the next sniffing round");
                    } else {
                        this.restClient.setHosts(sniffedHosts.toArray(new HttpHost[sniffedHosts.size()]));
                    }
                } catch (Exception e) {
                    logger.error("error while sniffing nodes", e);
                } finally {
                    scheduleNextRun(nextSniffDelayMillis);
                    running.set(false);
                    // exception happen
                    if (sniffedHosts == null) {
                        // rest client is round robin check next time
                        lastCheckHttpHosts --;
                        if (lastCheckHttpHosts >= 1) {
                            return;
                        }
                    }
                   // nodeStatusService.checkStatus(sniffedHosts);
                    lastCheckHttpHosts = sniffedHosts.size();
                }
            }
        }

        synchronized void shutdown() {
            scheduledExecutorService.shutdown();
            try {
                if (scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    return;
                }
                scheduledExecutorService.shutdownNow();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
