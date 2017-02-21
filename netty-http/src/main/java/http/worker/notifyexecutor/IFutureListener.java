package http.worker.notifyexecutor;

import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

/**
 * Created by i311352 on 2/20/2017.
 */
public interface IFutureListener<V> {

    /**
     * The task was computed successfully.
     * @param result
     */
    public void onSuccess(V result);

    /**
     * called when future state is canceled.
     */
    public void onCancel(RunnableFuture<V> cancelledFuture);

    /**
     * Called when there was an error while executing
     * this future.
     * @param e
     * @param future the future that fails
     */
    public void onError(Throwable e, Future<V> future);
}
