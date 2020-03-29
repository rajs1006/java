package de.funkedigital.autotagging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@EnableAspectJAutoProxy
@Configuration
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AutoTaggingAppProperty.class)
public class AutoTaggingApp {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingApp.class);


    private AutoTaggingAppProperty appProperties;

    @Autowired
    public AutoTaggingApp(AutoTaggingAppProperty appProperties) {
        this.appProperties = appProperties;
    }

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(AutoTaggingApp.class, args);
        LOG.debug("main() ctx = {}", ctx);

        if (args != null && args.length > 0) {
            LOG.debug("Command line arguments: {}", String.join(",", args));
        } else {
            LOG.debug("No command line arguments");
        }
        LOG.debug("Active profiles: {}", Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
    }
}
