package elasticsearch.exception;

/**
 * Created by I311352 on 9/30/2016.
 */
public class ElasticQueryException extends RuntimeException {
    public ElasticQueryException(String message) {
        super(message);
    }

    public ElasticQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
