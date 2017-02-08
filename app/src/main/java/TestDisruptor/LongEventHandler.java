package TestDisruptor;


import com.lmax.disruptor.EventHandler;

/**
 * Created by i311352 on 2/6/2017.
 */
public class LongEventHandler implements EventHandler<LongEvent> {
    public void handle(LongEvent event) {
        System.out.println("Event: " + event);
    }

    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Event: " + event);
    }
}
