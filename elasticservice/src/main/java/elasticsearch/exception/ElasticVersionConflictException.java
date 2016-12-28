package elasticsearch.exception;

/**
 * Created by I311352 on 10/25/2016.
 */
public class ElasticVersionConflictException extends RuntimeException {
    public ElasticVersionConflictException(String message) {
        super(message);
    }

    public ElasticVersionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
