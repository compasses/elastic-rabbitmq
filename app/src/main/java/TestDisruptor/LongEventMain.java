package TestDisruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by i311352 on 2/6/2017.
 */
public class LongEventMain {
    public static void main(String[] args) throws Exception {
        Executor executor = Executors.newCachedThreadPool();

        LongEventFactory eventFactory = new LongEventFactory();
        int bufferSize = 1024;

//        Disruptor<LongEvent> disruptor = new Disruptor<LongEvent>(eventFactory, bufferSize, executor);
//        disruptor.handleEventsWith(new LongEventHandler());
//        disruptor.start();

        Disruptor<LongEvent> disruptor = new Disruptor<>(LongEvent::new, bufferSize, executor);
        disruptor.handleEventsWith((event, sequence, endOfBatch) -> {System.out.println("Event: " + event);
            System.out.println("CurrentThreadName:" + Thread.currentThread().getName());
        });
        disruptor.start();

        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();
        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            ringBuffer.publishEvent((event, sequence, buffer) -> event.set(buffer.getLong(0)), bb);

           // producer.onData(bb);
            //Thread.sleep(1000);
        }
    }
}
