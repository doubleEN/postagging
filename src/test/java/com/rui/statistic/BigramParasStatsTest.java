package com.rui.statistic;

import com.rui.dictionary.AbstractDictionary;
import com.rui.ngram.AbstractWordTag;
import com.rui.ngram.WordTag;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by mjx on 17-7-12.
 */
public class BigramParasStatsTest {

    AbstractParasStats stats = new BigramParasStats();

    @Before
    public void pre() {
        AbstractWordTag[] wordTags = new AbstractWordTag[]{
                new WordTag("我/n"),
                new WordTag("爱/v"),
                new WordTag("nlp/n")
        };
        stats.countParas(wordTags);
    }

    @Test
    public void testDict() {
        AbstractDictionary dictionary = stats.getDictionary();

        assertEquals(0, dictionary.getTagId().get("n").intValue());
        assertEquals(1, dictionary.getTagId().get("v").intValue());
        assertEquals(0, dictionary.getWordId().get("我").intValue());
        assertEquals(1, dictionary.getWordId().get("爱").intValue());
        assertEquals(2, dictionary.getWordId().get("nlp").intValue());


    }

    @Test
    public void countParasFromWTS() throws Exception {
//        System.out.println(Arrays.toString(stats.getNumPi()));
        //数组检查
        assertArrayEquals(new int[]{0, 1}, stats.getNumMatA()[0][0]);
        assertArrayEquals(new int[]{1, 0, 1}, stats.getNumMatB()[0]);
        assertArrayEquals(new int[]{2, 1}, stats.getNumPi());
        System.out.println("matA_1:");
        for (int [] m:stats.getNumMatA()[0]){
            System.out.println(Arrays.toString(m));
        }
    }

    @Test
    public void countParasFromFile() throws Exception {
        stats.countParas("/home/mjx/桌面/PoS/test/testCount.txt");//我/n 爱/v 	自然语言处理/ln

        assertArrayEquals(new int[]{0, 2, 0}, stats.getNumMatA()[0][0]);
        assertArrayEquals(new int[]{2, 0, 1, 0}, stats.getNumMatB()[0]);
        assertArrayEquals(new int[]{3, 2, 1}, stats.getNumPi());

        System.out.println("matA_2:");
        for (int [] m:stats.getNumMatA()[0]){
            System.out.println(Arrays.toString(m));
        }
    }

}