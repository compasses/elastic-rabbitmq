package http.elasticaction;

/**
 * Created by i311352 on 2/13/2017.
 */

@FunctionalInterface
public interface SearchDSL<R, T> {
    R getDSL(T t);
}
