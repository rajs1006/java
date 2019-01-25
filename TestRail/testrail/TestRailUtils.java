package com.test.tools.testutil.testrail;

import com.test.tools.testutil.utils.CommonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nonnull;
import javax.naming.InvalidNameException;

import static com.test.tools.testutil.runner.EnvironmentFrameworkMethod.FOR;
import static com.test.tools.testutil.runner.EnvironmentFrameworkMethod.ON;

/**
 * This is a UTIL class to perform multiple tasks.
 * <p>
 * Created BY s.raj@funkedigital.de on 11-May-18
 */
public class TestRailUtils extends CommonUtils {

    /**
     * Covert time from milisecond to TestRail acceptable format.
     * <p>
     * This method return 1 second if execution completes before 1 second because of TestRail API limitation.
     *
     * @param millis Time in miliseconds
     * @return Test rail formated time
     */
    public static String milliSecToTimeSpan(long millis) {
        long second = (millis / 1000) % 60;
        long minute = (millis / (1000 * 60)) % 60;
        long hour = (millis / (1000 * 60 * 60)) % 24;

        if (hour != 0 && minute != 0 && second != 0) {
            second += 1l;
        }

        return String.format("%02dh %02dm %02ds", hour, minute, second);
    }

    /**
     * This method splits the Method name and return Name, Publication and Stage.
     *
     * @param frameworkMethodName In the format of <b>Name + "for" + publication + "on" + stage</b>
     * @return Array of {Name, Publication, Stage}
     */
    @Nonnull
    public static String[] getMethodDescription(@Nonnull String frameworkMethodName) throws InvalidNameException {
        String[] data = new String[3];
        try {
            @Nonnull String[] first = frameworkMethodName.split(FOR);
            String[] second;
            if (first.length > 1) {
                second = first[1].split(ON);
                data[0] = first[0];
                data[1] = second[0];
            } else {
                second = first[0].split(ON);
                data[0] = second[0];
                data[1] = "Standalone";
            }
            data[2] = second[1];
            return data;
        } catch (Exception e) {
            throw new InvalidNameException(String.format(" Invalid method name %s  : %s ", frameworkMethodName, ExceptionUtils.getMessage(e)));
        }
    }

}
