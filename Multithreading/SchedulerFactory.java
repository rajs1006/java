package com.test.tools.testutil.runner;

import org.apache.commons.lang3.StringUtils;
import org.junit.runners.model.RunnerScheduler;

/**
 * This class works as factory class to instantiate Scheduler framework.
 *
 * @param <T> Type of count ie, String, Integer, Long
 * @author sraj 26-Oct-2018
 */
public class SchedulerFactory<T> {

    /**
     * Return the instance of scheduler.
     *
     * @param runner      Instance of {@link Scheduler}
     * @param threadCount number of threads
     * @param <T>         Type of count ie, String, Integer, Long
     * @return Instance of {@link RunnerScheduler}
     */
    public synchronized static <T> RunnerScheduler getInstance(Class<? extends Scheduler> runner, T threadCount) {
        String className = runner.getSimpleName();
        if (SchedulerService.class.getSimpleName().equals(className)) {
            return SchedulerService.init(getThreadCount(threadCount));
        }
        return ExecutorService.init(getThreadCount(threadCount));
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
