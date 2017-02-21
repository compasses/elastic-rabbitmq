package http.worker;



import http.searchcommand.SearchRequest;
import http.worker.notifyexecutor.INotifyingFuture;
import http.worker.notifyexecutor.SearchingFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by i311352 on 2/20/2017.
 */
public class SearchWorker extends ThreadPoolExecutor {
    private final static Logger logger = LoggerFactory.getLogger(SearchWorker.class);
    private static SearchWorker instance;

    public SearchWorker(int size, String name, BlockingQueue blockingQueue) {
        super(size, size, 0L, TimeUnit.MILLISECONDS, blockingQueue, new WorkerThread(name));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        logger.info("start to exectue: " + t);
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        logger.info("after to exectue: " + t);
    }

    public INotifyingFuture doSearch(SearchRequest request) {
        SearchingFuture future = new SearchingFuture(request);
        future.setExecutor(this);
        future.run();
        return future;
    }
}
