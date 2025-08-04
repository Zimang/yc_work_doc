package com.desaysv.sceneengine.thread;

public class PriorityRunnable implements Runnable  {

    private final Priority priority; //任务优先级
    private final Runnable runnable; //任务真正执行者
    /*package*/static long seq; //任务唯一标示

    public Priority getPriority() {
        return priority;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public static long getSEQ() {
        return seq;
    }

    public PriorityRunnable(Priority priority, Runnable runnable)  {
        this.priority = priority == null ? Priority.NORMAL : priority; 
        this.runnable = runnable; 
    }

    @Override
    public final void run()  {
        this.runnable.run(); 
    }
}

