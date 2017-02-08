package TestDisruptor;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * Created by i311352 on 2/6/2017.
 */
public class LongEventProducerWithTranslator {
    private final RingBuffer<LongEvent> ringBuffer;
    public LongEventProducerWithTranslator(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    private static final EventTranslatorOneArg<LongEvent, ByteBuffer> TRANSLATOR_ONE_ARG =
            new EventTranslatorOneArg<LongEvent, ByteBuffer>() {
                @Override
                public void translateTo(LongEvent event, long sequence, ByteBuffer arg0) {
                    event.set(arg0.getLong(0));
                }
            };
    public void onData(ByteBuffer bb) {
        ringBuffer.publishEvent(TRANSLATOR_ONE_ARG, bb);
    }
}
