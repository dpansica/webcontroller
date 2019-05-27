package it.solutionsexmachina.webcontroller.servlet.utils;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

public class ProducerConsumerThread {

    private Map<String, Thread> channelProducers = new HashMap<>();
    private Map<String, Thread> channelConsumers = new HashMap<>();
    public List<AsyncContext> connections = new CopyOnWriteArrayList<AsyncContext>();

    private BlockingQueue<AjaxCallAction> inputsQueue = new LinkedBlockingDeque<>();
    private BlockingQueue<String> outputsQueue = new LinkedBlockingDeque<>();

    public void sendInputOnChannel(String channelName, AjaxCallAction input, AsyncContext context) throws InterruptedException {

        if (channelConsumers.get(channelName) == null) {
            Thread thread = new Thread(() -> {
                try {
                    consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            channelConsumers.put(channelName, thread);
        }

        if (channelProducers.get(channelName) == null) {
            Thread thread = new Thread(() -> {
                try {
                    produce();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            channelProducers.put(channelName, thread);
        }

        context.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                connections.remove(asyncEvent.getAsyncContext());
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {

            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {

            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {

            }
        });

        inputsQueue.put(input);
        connections.add(context);
    }

    public void produce() throws InterruptedException {
        synchronized (this) {

            AjaxCallAction call = inputsQueue.take();

            outputsQueue.put(call.callBeanMethod());

            notify();
        }
    }

    public void consume() throws InterruptedException {
        synchronized (this) {
            wait();

            String result = outputsQueue.take();

            for (AsyncContext context : connections) {
                try {
                    PrintWriter writer = context.getResponse().getWriter();

                    writer.print(result);
                    writer.flush();
                    writer.close();

                    context.complete();
                } catch (Throwable e) {
                    e.printStackTrace();
                    connections.remove(context);
                }
            }

        }
    }


} 