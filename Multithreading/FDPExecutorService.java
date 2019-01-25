package com.test.tools.testutil.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * This class works as scheduler for {@link ExecutorService} to execute process in parallelly
 *
 * @author sraj 26-Oct-2018
 */
public class FDPExecutorService extends FDPScheduler {

    /**
     * Instance of {@link FDPExecutorService}
     */
    private static FDPExecutorService fdpExecutor = null;

    /**
     * Instance of {@link ExecutorService}
     */
    private static ExecutorService threadExecutor = null;

    /**
     * Return singelton instance of {@link FDPExecutorService}
     *
     * @param threadCount Number of threads needed to initialize the pool.
     * @return Instance of {@link FDPExecutorService}
     */
    public static FDPExecutorService init(Integer threadCount) {
        setUpExecutor(getNumThreads(threadCount));
        if (fdpExecutor == null) {
            fdpExecutor = new FDPExecutorService();
        }
        return fdpExecutor;
    }

    /**
     * This method instantiate the {@link ExecutorService} using Thread numbers obtained
     * by the Env parameters or picked using maximum available processors
     * and also create {@link ForkJoinPool.ForkJoinWorkerThreadFactory}.
     *
     * @param threadCount Number of threads needed to initialize the pool.
     * @return instance of {@link ExecutorService}
     */
    private static void setUpExecutor(Integer threadCount) {
        threadExecutor = Executors.newWorkStealingPool(threadCount);
    }

    /**
     * This schedule the statement to be run.
     *
     * @param childStatement Statement to be executed
     */
    @Override
    public void schedule(Runnable childStatement) {
        threadExecutor.submit(childStatement);
    }

    /**
     * This method run the last task and amkes sure that no asynchronous task is yet to run
     * and also calls {@link FDPException} to maintain a consolidated view of all the errors
     * occurred while execution.
     */
    @Override
    public void finished() {
        super.finished(threadExecutor);
    }

}
