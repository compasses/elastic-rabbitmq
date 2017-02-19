package http;

import com.google.gson.Gson;
import http.elasticaction.RestRequest;
import http.elasticaction.RestResponse;
import http.exception.BadRequestException;
import http.message.DecodedSearchRequest;
import http.searchcommand.RestCommand;
import http.searchcommand.SearchRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * Created by i311352 on 2/13/2017.
 */
public class HttpSearchHandler extends SimpleChannelInboundHandler<DecodedSearchRequest> {
    private final static Gson gson = new Gson();
    private final static Logger logger = LoggerFactory.getLogger(HttpSearchHandler.class);

    private final EventExecutorGroup executor;
    private RestClient client;

    public HttpSearchHandler(EventExecutorGroup group, RestClient client) {
        this.executor = group;
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DecodedSearchRequest searchRequest) throws Exception {
        logger.info("Got search request " + searchRequest.getQueryMeta().toString());
        //throw new BadRequestException(new Exception("adads"));

        RestCommand command = new RestCommand(client, new RestRequest());

        //SearchRequest request = new SearchRequest();
        //        Callable<? extends Object> callable = new Callable<Object>() {
        //            @Override
        //            public Object call() throws Exception {
        //                return null;
        //            }
        //        };

        final Future<? extends Object> future = executor.submit(new SearchRequest(command));

        future.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                logger.info("Got Response for request: " + searchRequest);
                if (future.isSuccess()) {
                    RestResponse restResponse = new RestResponse();

                    restResponse.setBody(future.get());
                    restResponse.setHttpRequest(searchRequest.getHttpRequest());
                    ctx.writeAndFlush(restResponse);
                } else {
                    ctx.fireExceptionCaught(future.cause());
                }
            }
        });
//
//        RestResponse restResponse = new RestResponse();
//        restResponse.setBody(gson.toJson(searchRequest.getQueryMeta()));
//        restResponse.setHttpRequest(searchRequest.getHttpRequest());
//        ctx.writeAndFlush(restResponse);
    }
}
