package http.worker.notifyexecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Created by i311352 on 2/20/2017.
 */
public interface INotifyingExecutorService extends ExecutorService {

    @Override
    public <T> INotifyingFuture<T> submit(Callable<T> task);

    @Override
    public INotifyingFuture<Void> submit(Runnable task);

    @Override
    public <T> INotifyingFuture<T> submit(Runnable task, T result);
}