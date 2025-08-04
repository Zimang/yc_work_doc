package com.desaysv.sceneengine.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger; 

public class SvThreadFactory implements ThreadFactory  {

    private final AtomicInteger mCount = new AtomicInteger(1); 

    @Override
    public Thread newThread(Runnable runnable)  {
        return new Thread(runnable, "thread-#"  +  mCount.getAndIncrement()); 
    }
}

