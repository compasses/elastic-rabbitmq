package http.worker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * Created by i311352 on 2/20/2017.
 */
public class WorkerThread implements ThreadFactory {

    private int threadCount;
    private String name;
    private List<String> states;


    public WorkerThread(String name) {
        this.name = name;
        this.threadCount = 1;
        this.states = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name + "_" + threadCount);
        threadCount ++;

        states.add(String.format("Created thread %d with name %s on %s \n", t.getId(), t.getName(), new Date()));

        return t;
    }

    public String getStats()
    {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> it = this.states.iterator();
        while (it.hasNext())
        {
            buffer.append(it.next());
        }
        return buffer.toString();
    }
}
