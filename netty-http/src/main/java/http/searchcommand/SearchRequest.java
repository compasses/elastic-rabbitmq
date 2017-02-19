package http.searchcommand;

import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by i311352 on 2/16/2017.
 */
public class SearchRequest implements Callable<char[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestCommand.class);

    RestCommand command;
    public SearchRequest(RestCommand command) {
        this.command = command;
    }

    @Override
    public char[] call() throws Exception {
        LOGGER.info("Start request: " + command.toString());
        return command.execute();
    }
}
