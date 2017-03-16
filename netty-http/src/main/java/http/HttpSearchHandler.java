package http;

import com.google.gson.Gson;
import http.elasticaction.RestRequest;
import http.elasticaction.RestResponse;
import http.elasticaction.SearchDSLImpl;
import http.message.DecodedSearchRequest;
import http.searchcommand.RestCommand;
import http.searchcommand.SearchRequest;
import http.worker.SearchWorker;
import http.worker.notifyexecutor.IFutureListener;
import http.worker.notifyexecutor.SearchingFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import querydsl.TemplateGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;


/**
 * Created by i311352 on 2/13/2017.
 */
public class HttpSearchHandler extends SimpleChannelInboundHandler<DecodedSearchRequest> {
    private final static Gson gson = new Gson();
    private final static Logger logger = LoggerFactory.getLogger(HttpSearchHandler.class);

    private final SearchWorker executor;
    private RestClient client;

    public HttpSearchHandler(ExecutorService group, RestClient client) {
        this.executor = (SearchWorker) group;
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DecodedSearchRequest searchRequest) throws Exception {
        logger.info("Got search request " + searchRequest.getQueryMeta().toString());
        //throw new BadRequestException(new Exception("adads"));

        RestCommand command = new RestCommand(client, new TemplateGenerator(searchRequest.getQueryMeta()));

        //SearchRequest request = new SearchRequest();
        //        Callable<? extends Object> callable = new Callable<Object>() {
        //            @Override
        //            public Object call() throws Exception {
        //                return null;
        //            }
        //        };

        if (ctx.executor().inEventLoop()) {
            logger.info("In event loop");
        }
        logger.info("Current info" + this.executor.monitorInfo());

        SearchingFuture searchingFuture = (SearchingFuture) executor.doSearch(new SearchRequest(command));//(SearchingFuture) new SearchingFuture<>(new SearchRequest(command));
        searchingFuture.setListener(new IFutureListener() {
            @Override
            public void onSuccess(Object result) {
                logger.info("Got Response for request: ");
                RestResponse restResponse = new RestResponse();

                restResponse.setBody(result);
                restResponse.setHttpRequest(searchRequest.getHttpRequest());
                ctx.writeAndFlush(restResponse);
            }

            @Override
            public void onCancel(RunnableFuture cancelledFuture) {

            }

            @Override
            public void onError(Throwable e, java.util.concurrent.Future future) {
                ctx.fireExceptionCaught(e);
            }
        });

//        final Future<? extends Object> future = executor.submit(new SearchRequest(command));
//
//        future.addListener(new GenericFutureListener<Future<Object>>() {
//            @Override
//            public void operationComplete(Future<Object> future) throws Exception {
//                logger.info("Got Response for request: ");
//                if (future.isSuccess()) {
//                    RestResponse restResponse = new RestResponse();
//
//                    restResponse.setBody(future.get());
//                    restResponse.setHttpRequest(searchRequest.getHttpRequest());
//                    ctx.writeAndFlush(restResponse);
//                } else {
//                    ctx.fireExceptionCaught(future.cause());
//                }
//                if (ctx.executor().inEventLoop()) {
//                    logger.info("In event loop");
//                } else {
//                    logger.info("Not in event loop");
//                }
//            }
//        });
//
//        RestResponse restResponse = new RestResponse();
//        restResponse.setBody(gson.toJson(searchRequest.getQueryMeta()));
//        restResponse.setHttpRequest(searchRequest.getHttpRequest());
//        ctx.writeAndFlush(restResponse);
    }
}
