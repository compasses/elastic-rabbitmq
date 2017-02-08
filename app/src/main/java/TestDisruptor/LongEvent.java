package TestDisruptor;

import javafx.event.Event;

/**
 * Created by i311352 on 2/6/2017.
 */
public class LongEvent{
    private long value;

    public void set(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LongEvent{" +
                "value=" + value +
                '}';
    }
}

