package de.funke.tools.testutil.utils;

import difflib.Delta;
import difflib.DiffAlgorithm;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.MyersDiff;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class compares String and return difference.
 * <p>
 * Created BY : sraj on 31-Aug-2018
 */
public class FileComparator<T> {


    private static final String MULTIPLE_REGEX_SPLIT_CHAR = ";";
    private static final String REGEX_PATTERN_SPLIT_CHAR = "==";
    protected static final String EMPTY = "";

    /**
     * Original Document.
     */
    @Nonnull
    private final T original;

    /**
     * Revised Document.
     */
    @Nonnull
    private final T revised;

    /**
     * Pattern to be ignored. format should be : <!-==-->;<lastbuilddate>==<\/lastbuilddate>
     * <ul>
     * <li><b>;(Semicolon)</b> : To separate diff patterns.</li>
     * <li><b>== (Double equal)</b> : To separate start Regex/String and end Regex/String</li>
     * </>
     */
    private final String regexIgnoredPattern;

    /**
     * Pattern to be ignored. format should be : [0-9]{5,15}==12345;\/resources\/[0-9]*=/resources/12345
     * <ul>
     * <li><b>;(Semicolon)</b> : To separate diff regex patterns.</li>
     * <li><b>== (Double equal)</b> : To separate old Regex/String and new Regex/String</li>
     * </>
     */
    private final String regexReplacedPattern;

    /**
     * This contains the list of all the changes {@link Delta}
     */
    private List<Delta> deltas;

    /**
     * Algorithm to be used for comparision.
     */
    private final DiffAlgorithm diffAlgorithm;


    /**
     * Constructor to initialize variable for further use.
     *
     * @param original             Base document to perform the comparision.
     * @param revised              Revised document which needs to be compared.
     * @param regexIgnoredPattern  <b>;(Semicolon)</b> separated list of patterns to be <b>ignored</b> while doc comparision.
     * @param regexReplacedPattern <b>;(Semicolon)</b> separated list of patterns to be <b>replaced</b> while doc comparision.
     */
    public FileComparator(T original, T revised, String regexIgnoredPattern, String regexReplacedPattern) {
        this.original = original;
        this.revised = revised;
        this.regexIgnoredPattern = regexIgnoredPattern;
        this.regexReplacedPattern = regexReplacedPattern;
        this.diffAlgorithm = new MyersDiff();
    }

    /**
     * Constructor to initialize variable for further use.
     *
     * @param original             Base document to perform the comparision.
     * @param revised              Revised document which needs to be compared.
     * @param regexIgnoredPattern  <b>;(Semicolon)</b> separated list of patterns to be <b>ignored</b> while doc comparision.
     * @param regexReplacedPattern <b>;(Semicolon)</b> separated list of patterns to be <b>replaced</b> while doc comparision.
     */
    public FileComparator(T original, T revised, String regexIgnoredPattern, String regexReplacedPattern, DiffAlgorithm diffAlgo) {
        this.original = original;
        this.revised = revised;
        this.regexIgnoredPattern = regexIgnoredPattern;
        this.regexReplacedPattern = regexReplacedPattern;
        this.diffAlgorithm = diffAlgo;
    }

    /**
     * This method is mandatory to be called to populate deltas.
     * <p>
     * This method basically servers as one stop initializer of deltas for diff type of changes and so needs
     * to be called explicitly.
     *
     * @throws Exception IOException File Reader related exception.
     */
    public void initDelta() throws Exception {
        deltas = getDeltas();
    }


    /**
     * This method return difference of {@link Delta.TYPE#CHANGE}, diff in both files.
     *
     * @return List of messages containing position from original and revised doc if diff is found.
     */
    public List<String> getChangesFromOriginal() {
        return getChunksByType(Delta.TYPE.CHANGE);
    }

    /**
     * This method return difference of {@link Delta.TYPE#INSERT}, present in {@link #revised} doc but not
     * in {@link #original}
     *
     * @return List of messages containing position from original and revised doc if diff is found.
     */
    public List<String> getInsertsFromOriginal() {
        return getChunksByType(Delta.TYPE.INSERT);
    }

    /**
     * This method return difference of {@link Delta.TYPE#DELETE}, present in {@link #original} doc but not
     * in {@link #revised}
     *
     * @return List of messages containing position from original and revised doc if diff is found.
     */
    public List<String> getDeletesFromOriginal() {
        return getChunksByType(Delta.TYPE.DELETE);
    }

    /**
     * @param type Type of difference {@link Delta.TYPE#CHANGE}/{@link Delta.TYPE#INSERT}/{@link Delta.TYPE#DELETE}
     * @return List of messages containing position from original and revised doc if diff is found.
     */
    private List<String> getChunksByType(Delta.TYPE type) {
        final List<String> listOfChanges = new ArrayList<>();
        for (Delta delta : deltas) {
            if (delta.getType().equals(type)) {
                List<?> o = delta.getOriginal().getLines();
                List<?> r = delta.getRevised().getLines();
                for (int i = 0; i < Math.max(o.size(), r.size()); i++) {
                    listOfChanges.add(String.format("Original : %s \n Revised : %s \n", checkSize(o, i), checkSize(r, i)));
                }
            }
        }
        return listOfChanges;
    }

    /**
     * Return object at index if index is valid else return empty.
     *
     * @param l     List
     * @param index Index of list
     * @return Empty of object at Index
     */
    private Object checkSize(List<?> l, int index) {
        if (index < l.size()) {
            return l.get(index);
        }
        return StringUtils.stripToEmpty("");
    }

    /**
     * This method process the document using {@link DiffUtils} and return the difference.
     *
     * @return List of object of {@link Delta} containing the difference
     * @throws Exception IOException File Reader related exception.
     */
    protected List<Delta> getDeltas() throws Exception {
        final List<String> originalFileLines = fileToLines(original);
        final List<String> revisedFileLines = fileToLines(revised);

        final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines, this.diffAlgorithm);
        return patch.getDeltas();
    }

    /**
     * This method receives the Document and process it by Replacement {@link #replaceRegex(String)} and
     * ignoring patterns {@link #ignoreRegex(BufferedReader, String)}.
     *
     * @param file String formatted Document
     * @return list of lines after processing
     * @throws Exception IOException File Reader related exception.
     */
    protected List<String> fileToLines(T file) throws Exception {
        String f = file.toString();
        // Replace all the patterns before processing, it replaces all the Regex with some fixed value.
        f = replaceRegex(f);
        // Process the document line by line.
        final List<String> lines = new ArrayList<>();
        try (final BufferedReader in = new BufferedReader(new StringReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                // Ignore line or multiple lines like comments of multiple line or a text between opening and closing tags.
                boolean isIgnoredPattern = ignoreRegex(in, line);
                if (!isIgnoredPattern) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * This method splits the Multiple regex patterns.
     *
     * <b>IMP:</b> split pattern is <code>; (Semicolon)</code>
     *
     * @param regexPattern The regex pattern needed to be split.
     * @return Array of patterns.
     */
    @Nonnull
    protected String[] splitRegexPattern(String regexPattern) {
        return ArrayUtils.nullToEmpty(StringUtils.split(regexPattern, MULTIPLE_REGEX_SPLIT_CHAR));
    }

    /**
     * This method splits the regex from ie; start tot end.
     *
     * <b>IMP:</b> split pattern is <code>== (Double equal)</code>
     *
     * @param regexPattern The regex pattern needed to be split.
     * @return Array of patterns.
     */
    @Nonnull
    protected String[] splitRegex(String regexPattern) {
        return regexPattern.split(REGEX_PATTERN_SPLIT_CHAR);
    }

    /**
     * This method replaces the regular expression pattern in document from a fixed value.
     * These patterns are passed in <code>,(comma)</code> separated list {@link #regexReplacedPattern}.
     * <p>
     * Using {@link String#replaceAll(String, String)} is performed to achieve regex based replacement.
     *
     * @param document String formatted Document
     * @return Document, with replaced values as per {@link #regexReplacedPattern}
     */
    @Nonnull
    protected String replaceRegex(String document) {
        final String[] replacePattern = splitRegexPattern(regexReplacedPattern);
        for (String replaceRegex : replacePattern) {
            // Split regex pattern
            String[] r = splitRegex(replaceRegex);
            String r0 = r[0];
            String r1 = r.length > 1 ? r[1] : EMPTY;
            if (!StringUtils.isEmpty(r1)) {
                document = document.replaceAll(r0, r1);
            }
        }
        return document;
    }


    /**
     * This method process lines from the  {@link BufferedReader} and looks for {@link #regexIgnoredPattern}
     * to ignore particular line.
     * <p>
     * {@link #regexIgnoredPattern} can contain <b>single</b> pattern as well as <b>start and end</b> pattern.
     * <ul>
     * <li>For Single pattern it ignores the single line which contains that pattern</li>
     * <li>For Start/End pattern it ignores the all the line between Start and End patterns, inclusively</li>
     * </ul>
     *
     * @param in   instance of {@link BufferedReader} to skip lines.
     * @param line current line from {@code in}
     * @return True/False whether to add the line or not
     * @throws Exception File Reader related exception.
     */
    protected boolean ignoreRegex(BufferedReader in, String line) throws Exception {
        String l = line.trim();
        // Check.
        final String[] ignoredPattern = splitRegexPattern(regexIgnoredPattern);
        for (String ignoreRegex : ignoredPattern) {
            // Split regex pattern
            String[] i = splitRegex(ignoreRegex);
            String i0 = i[0];
            String i1 = i.length > 1 ? i[1] : EMPTY;
            // Check for matching pattern.
            if (l.startsWith(i0) || l.matches(i0)) {
                if (!StringUtils.isEmpty(i1)) {
                    while (l != null && !(l.trim().endsWith(i1) || l.trim().matches(i1))) {
                        l = in.readLine();
                    }
                }
                return true;
            }
        }
        return false;
    }

}
