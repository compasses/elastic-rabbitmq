package http;

import http.exception.BadRequestException;
import http.message.DecodedSearchRequest;
import http.message.QueryMeta;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by i311352 on 2/13/2017.
 */
public class SearchQueryDecoder extends SimpleChannelInboundHandler<FullHttpRequest> {
    private long orderNumber;
    private static Logger logger = LoggerFactory.getLogger(SearchQueryDecoder.class);


    public SearchQueryDecoder() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        DecoderResult result = httpRequest.decoderResult();
        if (!result.isSuccess()) {
            throw new BadRequestException(result.cause());
        }
        logger.info("Get search request: " + httpRequest.uri());

        // only decode get path is enough
        Map<String, List<String>> requestParameters;
        QueryStringDecoder stringDecoder = new QueryStringDecoder(httpRequest.uri());
        requestParameters = stringDecoder.parameters();

        QueryMeta meta = new QueryMeta();

        for(Map.Entry<String, List<String>> entry : requestParameters.entrySet()) {
            if (entry.getKey().equals("options[]")) {
                // add filters
                List<String> filters = entry.getValue();
                filters.forEach(filter -> {
                    String[] typeVal = filter.split(":");
                    meta.addMeta(typeVal[0], typeVal[1]);
                });
            } else if (entry.getKey().equals("orderby")) {
                meta.setOrderBy(entry.getValue().get(0));
            } else {
                logger.warn("Unknown query parameter, ignore it:" + entry.toString());
            }
        }

        DecodedSearchRequest searchRequest = new DecodedSearchRequest(httpRequest, meta, orderNumber++);
        ctx.fireChannelRead(searchRequest);
    }
}
