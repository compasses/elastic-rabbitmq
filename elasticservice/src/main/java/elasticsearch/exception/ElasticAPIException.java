package elasticsearch.exception;

/**
 * Created by I311352 on 9/30/2016.
 */
public class ElasticAPIException extends RuntimeException {
    public ElasticAPIException(String message) {
        super(message);
    }

    public ElasticAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
