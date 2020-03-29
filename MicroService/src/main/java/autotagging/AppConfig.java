package de.funkedigital.autotagging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@EnableAspectJAutoProxy
@Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppProperty.class)
public class AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    /**
     * This is autowired in constructor {@link AppProperty}
     */
    private AppProperty appProperties;

    @Autowired
    public AppConfig(AppProperty appProperties) {
        this.appProperties = appProperties;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AppConfig.class, args);
        LOG.debug("main() ctx = {}", ctx);

        if (args != null && args.length > 0) {
            LOG.debug("Command line arguments: {}", String.join(",", args));
        } else {
            LOG.debug("No command line arguments");
        }
        LOG.debug("Active profiles: {}", Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
    }

    /**
     * @return the resource folder path
     */
    @Bean(name = "resourcePath")
    public String resourcePath() {
        return this.getClass().getResource("/").getPath();
    }
}
