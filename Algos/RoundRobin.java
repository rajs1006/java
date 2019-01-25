package de.funke.tools.testutil.utils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class returns the key from list of keys using round robin implementation
 * <p>
 * #Created By Sraj
 */
public class RoundRobin {

    protected static final Logger LOG = LoggerFactory.getLogger(RoundRobin.class);

    private static Set<String> usedElement = new LinkedHashSet<>();

    private static int elementSize = 0;

    private final Multiset<String> counter;

    private final Iterator<String> elements;

    /**
     * This initializes the list.
     *
     * @param elements List of elements.
     */
    public RoundRobin(final List<String> elements) {
        elementSize = elements.size();
        this.counter = HashMultiset.create();
        this.elements = Iterables.cycle(elements).iterator();
    }

    /**
     * This method returns round robin key when the Instance of {@link RoundRobin} class is
     * called Statically (Instantiated only once)
     *
     * @return key selected with round robin implementation
     */
    public String getOne() {
        final String element = this.elements.next();
        this.counter.add(element);

        return element;
    }

    /**
     * This method returns round robin key when the Instance of {@link RoundRobin} class is
     * called non-statically (Instantiated every time a method is called.)
     * <p>
     * This method uses static fields to maintain the last reference.
     *
     * @return key selected with round robin implementation
     */
    public String select() {
        String element;
        if (usedElement.size() == elementSize) {
            usedElement.clear();
        }
        do {
            element = getOne();
        } while (usedElement.size() > this.counter.size() - 1);
        usedElement.add(element);
        LOG.info("Server key : " + element);
        return element;
    }
}
