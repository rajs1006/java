package com.test.tools.testutil.runner;

import org.apache.commons.lang3.StringUtils;
import org.junit.runners.model.RunnerScheduler;

/**
 * This class works as factory class to instantiate Scheduler framework.
 *
 * @param <T> Type of count ie, String, Integer, Long
 * @author sraj 26-Oct-2018
 */
public class FDPSchedulerFactory<T> {

    /**
     * Return the instance of scheduler.
     *
     * @param runner      Instance of {@link FDPScheduler}
     * @param threadCount number of threads
     * @param <T>         Type of count ie, String, Integer, Long
     * @return Instance of {@link RunnerScheduler}
     */
    public synchronized static <T> RunnerScheduler getInstance(Class<? extends FDPScheduler> runner, T threadCount) {
        String className = runner.getSimpleName();
        if (FDPSchedulerService.class.getSimpleName().equals(className)) {
            return FDPSchedulerService.init(getThreadCount(threadCount));
        }
        return FDPExecutorService.init(getThreadCount(threadCount));
    }

    /**
     * Convert any type of value to integer.
     *
     * @param threadCount Count of thread
     * @param <T>         Type of count ie, String, Integer, Long
     * @return Intgere value
     */
    private static <T> Integer getThreadCount(T threadCount) {
        String count = String.valueOf(threadCount);
        if (!StringUtils.isEmpty(count)) {
            return Integer.valueOf(count);
        }
        return 0;
    }
}
