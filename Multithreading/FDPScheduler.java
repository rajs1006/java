package com.test.tools.testutil.runner;

import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Parent class for all scheduler
 *
 * @author sraj 26-Oct-2018
 */
public abstract class FDPScheduler implements RunnerScheduler {

    /**
     * manages consolidated view of exceptions
     */
    protected final FDPException fde = new FDPException();

    /**
     * Return number of threads base on Thread numbers obtained
     * by the Env parameters or picked using maximum available processors
     *
     * @param threadCount count of thread received.
     * @return number of threads to initialize the pool.
     */
    protected static int getNumThreads(Integer threadCount) {
        int numThreads;
        if (threadCount != 0) {
            numThreads = Math.max(2, threadCount);
        } else {
            Runtime runtime = Runtime.getRuntime();
            numThreads = Math.max(2, runtime.availableProcessors());
        }
        return numThreads;
    }

    /**
     * This method run the last task and makes sure that no asynchronous task is yet to run
     * and also calls {@link FDPException} to maintain a consolidated view of all the errors
     * occurred while execution.
     */
    protected void finished(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                fde.add(e);
            }
        }
        fde.throwIfNotEmpty();
    }
}
