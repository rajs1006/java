package com.test.tools.testutil.utils;

import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.Delta;
import difflib.DiffAlgorithm;
import difflib.Patch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the implementation of Comparision Algorithm
 *
 * @author sraj on 01-Feb-19
 */
public class testDiffUtil implements DiffAlgorithm {


    /**
     * {@inheritDoc}
     * <p>
     * Return empty diff if get the error while procession the difference.
     */
    public Patch diff(List<?> original, List<?> revised) {
        return buildRevision(original, revised);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Return empty diff if get the error while procession the difference.
     */
    public Patch diff(Object[] original, Object[] revised) {
        return diff(Arrays.asList(original), Arrays.asList(revised));
    }


    /**
     * {@inheritDoc}
     *
     * @return List of changes {@link ChangeDelta}
     */
    public static Patch buildRevision(List<?> orig, List<?> rev) {
        if (orig == null)
            throw new IllegalArgumentException("original sequence is null");
        if (rev == null)
            throw new IllegalArgumentException("revised sequence is null");
        // Collecting common items
        List<Object> commons = orig.stream().filter(rev::contains).collect(Collectors.toList());
        // Removing common items from Both the lists
        orig.removeAll(commons);
        rev.removeAll(commons);
        // Initializing Patch
        Patch patch = new Patch();
        // Initialize Chunks
        Chunk original = new Chunk(0, orig);
        Chunk revised = new Chunk(0, rev);
        Delta delta = new ChangeDelta(original, revised);

        patch.addDelta(delta);
        return patch;
    }

}
