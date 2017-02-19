package http;

import com.google.gson.Gson;
import http.elasticaction.RestResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by i311352 on 2/14/2017.
 */
public class HttpSearchResponseHandler extends ChannelOutboundHandlerAdapter {
    Gson gson = new Gson();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse) {
            super.write(ctx, msg, promise);
            return;
        }

        RestResponse response = (RestResponse) msg;

        // Build the response object
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.copiedBuffer((char[]) response.getBody(), CharsetUtil.UTF_8));

        httpResponse.headers().set(CONTENT_TYPE, "application/json");
        httpResponse.headers().set(CONTENT_LENGTH,
                httpResponse.content().readableBytes());

        HttpRequest httpRequest = response.getHttpRequest();

        // keep alive check
//        Boolean keepAlive = isKeepAlive(httpRequest);
//        if (keepAlive) {
//            // Add keep alive header as per
//            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
//            httpResponse.headers().set(CONNECTION,
//                    HttpHeaders.Values.KEEP_ALIVE);
//        } else {
//            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
//        }

        ReferenceCountUtil.release(httpRequest);
        ctx.writeAndFlush(httpResponse, promise);
        ctx.close();
    }
}
