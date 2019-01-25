public void stress() {
        final List<String> urls = collectedUrls2publication.keySet().stream().collect(Collectors.toList());
        LOG.info("stress(): urls.size = {} , Map size {} ", urls.size(), collectedUrls2publication.size());
        // Scheduler.
        final RunnerScheduler scheduler = FDPSchedulerFactory.getInstance(FDPExecutorService.class, threadCount);
        try {
            while (threadCount-- != 0) {
                sleep(waitMillisBetweenRequests);
                try (IntStream i = IntStream.range(0, threadCount)) {
                    i.parallel().forEach(count ->
                            scheduler.schedule(
                                    webdriverEnabled ? new WebdriverClientLoader(urls) : new HttpClientLoader(urls))
                    );
                }
            }
        } finally {
            scheduler.finished();
        }
        LOG.info("Total URL request processed {}", loadedUrls.intValue());
    }

