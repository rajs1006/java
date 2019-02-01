package de.funke.util;

import de.funke.tools.testutil.utils.FileComparator;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class compares JSoup document and return difference. This class extends {@link FileComparator<Document>}
 * <p>
 * Created BY : sraj on 31-Aug-2018
 */
public class JSoupDocComparator extends FileComparator<Document> {

    /**
     * CSS query Pattern to be removed from document. format should be : head>title;article[class^=\"teaser\"]>a"
     * <ul>
     * <li><b>;(Semicolon)</b> : To separate diff CSS queries patterns.</li>
     * <li><b>== (Double equal)</b> : To separate Select CSS Query from the attribute name</li>
     * </>
     */
    protected final String cssRemovedPattern;


    /**
     * This method is constructor and calls super constructor along with populating {@link #cssRemovedPattern} to
     * perform document alteration based on css query.
     * <p>
     * {@inheritDoc}
     */
    public JSoupDocComparator(Document original, Document revised, String regexIgnoredPattern, String regexReplacedPattern, String cssRemovedPattern) {
        super(original, revised, regexIgnoredPattern, regexReplacedPattern);
        this.cssRemovedPattern = cssRemovedPattern;
    }


    /**
     * {@inheritDoc}
     */
    protected List<String> fileToLines(Document file) throws Exception {
        return super.fileToLines(cssQueryIgnored(file));
    }

    /**
     * This method removes the result of {@link Document#select(String)} fetched by using {@link #cssRemovedPattern}
     * <p>
     * These patterns are passed in <code>;(semicolon)</code> separated list {@link #cssRemovedPattern} and then
     * further split by <code>==(Double equal)</code> to get the attribute of from that CSS query.
     * if there is not pattern after <code>==(Double equal)</code> that means the text() of that element will be
     * processed as empty else the attribute.
     *
     * @param document JSoup Document
     */
    @Nonnull
    private Document cssQueryIgnored(Document document) {
        String[] cssIgnoredPatterns = splitRegexPattern(cssRemovedPattern);
        for (String cssQuery : cssIgnoredPatterns) {
            String[] r = splitRegex(cssQuery);
            String r0 = r[0];
            String r1 = r.length > 1 ? r[1] : EMPTY;
            Elements elements = document.select(r0);
            for (Element element : elements) {
                if (!StringUtils.isEmpty(r1)) {
                    if (!element.attr(r1).isEmpty()) {
                        element.attr(r1, EMPTY);
                    }
                } else {
                    element.empty();
                }
            }
        }
        return document;
    }
}
