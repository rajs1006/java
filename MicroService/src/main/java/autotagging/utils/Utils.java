package de.funkedigital.autotagging.utils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static String getFormattedDate(Date executionDate) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(executionDate);
    }

    /**
     * convert mili seconds to readable format
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
     * Read fle in chunks
     */
    public static Map<Integer, List<String>> readFileInChunks(String fileName, Character escapeChar, int startChunk, int limit)
            throws IOException {

        int endChunk = startChunk;
        List<String> lines = new ArrayList<>();

        try (RandomAccessFile aFile = new RandomAccessFile(fileName, "r");
             FileChannel inChannel = aFile.getChannel()) {

            MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, startChunk, inChannel.size() - startChunk);
            buffer.load();

            int len = buffer.limit();
            char[] cList = new char[len + 1];

            for (int i = 0; i < len; i++) {
                char c = (char) buffer.get();
                if (c == escapeChar) {
                    lines.add(String.valueOf(cList));
                    cList = new char[len];

                    if (lines.size() == limit || i == (len - 1)) {
                        endChunk = i + startChunk + 1;
                        break;
                    }
                } else {
                    cList[i] = c;
                }
            }
        }

        Map<Integer, List<String>> returnVal = new HashMap<>();
        if (lines.size() != 0) {
            returnVal.put(endChunk, lines);
        }

        return returnVal;
    }

    /**
     * Return next time after addition to last completion time.
     *
     * @param last      last completion time
     * @param fieldType {@link Calendar#SECOND}
     * @param interval  adding time interval to fieldtype
     * @return next time.
     */
    public static Date getNextExecution(Date last, @NotNull int fieldType, @NotNull int interval) {
        Calendar nextExecutionTime = new GregorianCalendar();
        Date lastCompletionTime = last;

        if (lastCompletionTime != null) {
            nextExecutionTime.setTime(lastCompletionTime);
            //you can get the value from wherever you want
            nextExecutionTime.add(fieldType, interval);
        } else {
            nextExecutionTime.setTime(new Date());
        }
        return nextExecutionTime.getTime();
    }
}
