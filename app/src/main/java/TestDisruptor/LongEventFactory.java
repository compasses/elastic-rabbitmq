package TestDisruptor;

import com.lmax.disruptor.EventFactory;

/**
 * Created by i311352 on 2/6/2017.
 */
public class LongEventFactory implements EventFactory<LongEvent> {
    public LongEvent newInstance() {
        return new LongEvent();
    }
}
