package com.desaysv.sceneengine.thread;

import android.os.Handler;
import android.os.Looper; 

public final class ThreadDispatcher  {

    private final PriorityExecutor mExecutor; 
    private final Handler mHandler; 

    private static class ThreadDispatcherInstance  {
        private static final ThreadDispatcher INSTANCE = new ThreadDispatcher();
    }

    private ThreadDispatcher()  {
        mExecutor = new PriorityExecutor(true); 
        mHandler = new Handler(Looper.getMainLooper()); 
    }

    public static ThreadDispatcher getInstance()  {
        return ThreadDispatcherInstance.INSTANCE;
    }

    public void execute(Runnable runnable)  {
        mExecutor.execute(runnable); 
    }

    public void post(Runnable runnable)  {
        mHandler.post(runnable); 
    }

    public void postDelay(Runnable runnable, long delay)  {
        mHandler.postDelayed(runnable, delay); 
    }

    public void tryCancelTask(Runnable runnable)  {
        mHandler.removeCallbacks(runnable); 
        mExecutor.getQueue().remove(runnable); 
    }
}

