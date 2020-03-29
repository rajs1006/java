package de.funkedigital.autotagging.interceptors;

import de.funkedigital.autotagging.exceptions.RepositoryException;
import de.funkedigital.autotagging.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * This class intercepts {@link RepositoryException} to maintain Fail-Safe operation.
 * <p>
 * This is used to intercept if somehow Database is down and CRUD operations are performed in that time.
 * so it saves the data for those operations in FILE and we process them after sometime.
 *
 * @author sraj
 */
@Configuration
@Aspect
public class FailSafeInterceptor {

    private static Logger LOG = LoggerFactory.getLogger(FailSafeInterceptor.class);

    /**
     * Path of resource folder
     */
    @Autowired
    private String resourcePath;


    @AfterThrowing(pointcut = "execution(* de.funkedigital.autotagging.repositories..*(..)))",
            throwing = "rex")
    public void afterThrowing(RepositoryException rex) {
        LOG.debug("Running afterThrowing {} : {}", rex.getFileName(), Thread.currentThread().getName());
        try {
            File fileUrl = new File(this.resourcePath.concat(rex.getFileName()));
            if (fileUrl != null) {
                FileUtils.writeStringToFile(fileUrl, rex.getObjectToString() + "\n",
                        Constants.STRING_ENCODING, Boolean.TRUE);
            }
        } catch (IOException ie) {
            LOG.error("Failed to write data {} to file {}", rex.getObjectToString(), rex.getFileName());
        }
    }
}
