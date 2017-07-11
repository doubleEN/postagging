package com.rui.ngram;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mjx on 17-7-10.
 */
public class WordTagTest {
    WordTag wordTag;

    @Before
    public void setUp() throws Exception {
       wordTag  = new WordTag("中国/n");
    }

    @Test
    public void getWord() throws Exception {
        assertEquals("中国", wordTag.getWord());
    }

    @Test
    public void getTag() throws Exception {
        assertEquals("n", wordTag.getTag());

    }

}