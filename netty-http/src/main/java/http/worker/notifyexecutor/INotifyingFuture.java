package http.worker.notifyexecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RunnableFuture;

/**
 * Created by i311352 on 2/20/2017.
 */
public interface INotifyingFuture<V> extends RunnableFuture<V> {

    /**
     * Sets this listener to a {@link INotifyingFuture}. When the future is done
     * or canceled the listener gets notified.<br>
     * @param listener
     * @param the executor that executes the shiet.
     */
    public void setListener(IFutureListener<V> listener, ExecutorService executor);

    /**
     * Sets this listener to a {@link INotifyingFuture}. When the future is done
     * or canceled the listener gets notified.<br>
     * <b>Attention</b>: Be aware of the fact that everything that is done in that
     * listener is executed in same thread as the original task that this listener listens
     * to. Only use this method when you are sure that no long running task is performed
     * by the listener. When you want the listener's tasks to be performed asynchronous
     * use {@link #setListener(IFutureListener, ExecutorService)} instead.
     * @param listener
     */
    public void setListener(IFutureListener<V> listener);
}
