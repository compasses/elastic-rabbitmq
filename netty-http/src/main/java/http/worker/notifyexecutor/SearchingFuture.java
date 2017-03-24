package http.worker.notifyexecutor;

import http.worker.SearchWorker;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by i311352 on 2/20/2017.
 */

public class SearchingFuture<V> extends FutureTask<V> implements INotifyingFuture<V> {

    private static final ExecutorService DEFAULT_EXECUTOR = new SearchWorker(30, "SearchWorker", new LinkedBlockingDeque(100));;

    private IFutureListener<V> listener = null;
    private ExecutorService executor = null;
    private final AtomicBoolean executed = new AtomicBoolean();

    public SearchingFuture(Callable<V> callable) {
        super(callable);
        setExecutor(DEFAULT_EXECUTOR);
    }

    public SearchingFuture(Runnable runnable, V result) {
        super(runnable,result);
        setExecutor(DEFAULT_EXECUTOR);
    }


    @Override
    protected void done() {
        if(listener == null){
            return;
        }
        notifyListenerOnce();
    }

    /**
     * Atomically executes the task only one time.
     */
    protected void notifyListenerOnce(){
        if(!this.executed.getAndSet(true)){
            notifyListener();
        }
    }

    protected void notifyListener() {
        this.executor.submit(new TaskCompletionRunner<V>(delegateFuture(),this.listener));
    }

    /**
     * @return the future that was processed.
     */
    protected RunnableFuture<V> delegateFuture(){
        return this;
    }

    @Override
    public void setListener(IFutureListener<V> listener, ExecutorService executor) {
        setExecutor(executor);
        setListener(listener);
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void setListener(IFutureListener<V> listener) {
        this.listener = listener;
        /*
         * Probably the task was already executed. If so, call done() manually.
         */
        runWhenDone();
    }

    protected void runWhenDone(){
        if(isDone()){
            notifyListenerOnce();
        }
    }

    private static class TaskCompletionRunner<V> implements Runnable{

        private final IFutureListener<V> listener;
        private final RunnableFuture<V> future;

        public TaskCompletionRunner(RunnableFuture<V> future, IFutureListener<V> listener) {
            this.future = future;
            this.listener = listener;
        }

        @Override
        public void run() {
            if (this.future.isCancelled()) {
                this.listener.onCancel(this.future);
            } else {
                try {
                    this.listener.onSuccess(this.future.get());
                } catch (InterruptedException e) {
                    this.listener.onError(e, this.future);
                } catch (ExecutionException e) {
                    this.listener.onError(e, this.future);
                }
            }
        }
    }
}