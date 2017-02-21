package http;

import elasticsearch.ElasticRestClient;
import http.worker.SearchWorker;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.elasticsearch.client.RestClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;


/**
 * Created by i311352 on 2/13/2017.
 */
public class DefaultServerInitializer extends ChannelInitializer<SocketChannel> {
    private final Config conf;

    //private final EventExecutorGroup executor;
    private final ExecutorService executor;
    private final ConfigurableApplicationContext applicationContext;

    public DefaultServerInitializer(Config conf, ConfigurableApplicationContext applicationContext) {
        this.conf = conf;
        this.applicationContext = applicationContext;

        this.executor = new SearchWorker(conf.getTaskThreadPoolSize(), "SearchWorkder", new ArrayBlockingQueue(500));

//                new DefaultEventExecutorGroup(
//                conf.getTaskThreadPoolSize());
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("httpDecoder", new HttpRequestDecoder());

        pipeline.addLast("httpAggregator", new HttpObjectAggregator(conf.getClientMaxBodySize()));
        pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
        pipeline.addLast("httpMyResponseHandler", new HttpSearchResponseHandler());

        pipeline.addLast("httpSearchDecoder", new SearchQueryDecoder());

        RestClient restClient = applicationContext.getBean("elasticRestClient", RestClient.class);

        pipeline.addLast("httpSearchHandler", new HttpSearchHandler(this.executor, restClient));
    }
}
