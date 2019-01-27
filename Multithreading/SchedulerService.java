package com.test.tools.testutil.runner;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

import static java.util.concurrent.ForkJoinTask.inForkJoinPool;

/**
 * This class schedules the  Classes within the Suite to run in parallel.
 * <p>
 * Created by Sraj , 08-Aug-18
 */
public class SchedulerService extends Scheduler {

    /**
     * Add tasks to be executed
     */
    private final Deque<ForkJoinTask<?>> asyncTasks = new LinkedList<>();
    /**
     * Maintains the last executed child instance to decide about the thread to be assigned.
     */
    private Runnable lastScheduledChild;

    /**
     * Instantiate the object
     */
    private static SchedulerService fdpScheduler = null;

    /**
     * Maintains the pool
     */
    private static ForkJoinPool forkJoinPool = null;

    /**
     * Return singelton instance of {@link SchedulerService}
     *
     * @param threadCount Number of threads needed to initialize the pool.
     * @return Instance of {@link SchedulerService}
     */
    public static SchedulerService init(Integer threadCount) {
        setUpForkJoinPool(getNumThreads(threadCount));
        if (fdpScheduler == null) {
            fdpScheduler = new SchedulerService();
        }
        return fdpScheduler;
    }

    /**
     * This method instantiate the {@link ForkJoinPool} using Thread numbers obtained
     * by the Env parameters or picked using maximum available processors
     * and also create {@link ForkJoinPool.ForkJoinWorkerThreadFactory}.
     *
     * @return instance of {@link ForkJoinPool}
     */
    private static void setUpForkJoinPool(Integer threadCount) {
        ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = pool -> {
            if (pool.getPoolSize() >= pool.getParallelism()) {
                return null;
            } else {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("JUnit-" + thread.getName());
                return thread;
            }
        };
        forkJoinPool = new ForkJoinPool(threadCount, threadFactory, null, false);
    }


    /**
     * This schedule the statement to be run.
     *
     * @param childStatement Statement to be executed
     */
    @Override
    public void schedule(Runnable childStatement) {
        if (lastScheduledChild != null) {
            // Execute previously scheduled child asynchronously ...
            if (inForkJoinPool()) {
                asyncTasks.addFirst(ForkJoinTask.adapt(lastScheduledChild).fork());
            } else {
                asyncTasks.addFirst(forkJoinPool.submit(lastScheduledChild));
            }
        }
        // We schedule the childStatement in finished method()
        lastScheduledChild = childStatement;
    }

    /**
     * This method run the last task and amkes sure that no asynchronous task is yet to run
     * and also calls {@link Exception} to maintain a consolidated view of all the errors
     * occurred while execution.
     */
    @Override
    public void finished() {
        if (lastScheduledChild != null) {
            if (inForkJoinPool()) {
                // Execute the last scheduled child in the current thread
                try {
                    lastScheduledChild.run();
                } catch (Throwable t) {
                    fde.add(t);
                }
            } else {
                // Submit the last scheduled child to the ForkJoinPool too,
                // because all tests should run in the worker threads
                asyncTasks.addFirst(forkJoinPool.submit(lastScheduledChild));
            }
            // Make sure all asynchronously executed children are done, before we return
            for (ForkJoinTask<?> task : asyncTasks) {
                // Because we have added all tasks via addFirst into asyncTasks,
                // task.join() is able to steal tasks from other worker threads,
                // if there are tasks, which have not been started yet
                // from other worker threads
                try {
                    task.join();
                } catch (Throwable t) {
                    fde.add(t);
                }
            }
            super.finished(forkJoinPool);
        }
    }
}
