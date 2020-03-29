package de.funkedigital.autotagging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AutoTaggingAppProperty {

    private static final Logger LOG = LoggerFactory.getLogger(AutoTaggingAppProperty.class);
}
