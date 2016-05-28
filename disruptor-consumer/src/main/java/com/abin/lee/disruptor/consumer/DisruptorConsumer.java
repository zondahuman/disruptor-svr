package com.abin.lee.disruptor.consumer;

import com.abin.lee.disruptor.common.handler.TradeTransactionInDBHandler;
import com.abin.lee.disruptor.model.TradeTransaction;
import com.lmax.disruptor.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: tinkpad
 * Date: 16-5-19
 * Time: 下午11:45
 * To change this template use File | Settings | File Templates.
 */
public class DisruptorConsumer {

    public static void createConsumer() throws InterruptedException {
        int BUFFER_SIZE = 1024;
        int THREAD_NUMBERS = 4;
        EventFactory<TradeTransaction> eventFactory = new EventFactory<TradeTransaction>() {
            public TradeTransaction newInstance() {
                return new TradeTransaction();
            }
        };
        RingBuffer<TradeTransaction> ringBuffer = RingBuffer.createSingleProducer(eventFactory, BUFFER_SIZE);

        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBERS);

        WorkHandler<TradeTransaction> workHandlers = new TradeTransactionInDBHandler();
        /*
         * 这个类代码很简单的，亲自己看哈！~
         */
        WorkerPool<TradeTransaction> workerPool = new WorkerPool<TradeTransaction>(ringBuffer, sequenceBarrier, new IgnoreExceptionHandler(), workHandlers);

        workerPool.start(executor);

        //下面这个生产8个数据，图简单就写到主线程算了
        for (int i = 0; i < 8; i++) {
            long seq = ringBuffer.next();
            ringBuffer.get(seq).setPrice(Math.random() * 9999);
            ringBuffer.publish(seq);
        }

        Thread.sleep(10000000);
        workerPool.halt();
        executor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        createConsumer();
    }


}
