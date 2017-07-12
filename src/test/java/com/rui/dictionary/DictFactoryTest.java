package com.rui.dictionary;

import com.rui.ngram.WordTag;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by mjx on 17-7-11.
 */
public class DictFactoryTest {

    DictFactory factory ;

//    @Before
//    public void setUp() throws Exception {
//    }

    @Before
    public void addIndex() throws Exception {
        factory= new DictFactory();
        WordTag[] wordTags = new WordTag[]{
                new WordTag("中国人/n"),
                new WordTag("爱/v"),
                new WordTag("吃/v"),
                new WordTag("火锅/n"),
        };
        factory.addIndex(wordTags);
    }

    @Test
    public void getWordId() throws Exception {
        Map<String, Integer> map = factory.getWordId();
        int id0 = map.get("中国人");
        int id3 = map.get("火锅");
    }

    @Test
    public void getTagId() throws Exception {
        Map<String, Integer> map = factory.getTagId();
        int id0 = map.get("n");
        int id1 = map.get("v");
        assertEquals (1,id1);
        assertEquals (0,id0);
    }

    @Test
    public void getTagDict() throws Exception {
        Map<Integer,String > map = factory.getTagDict();
        String tag0 = map.get(0);
        String tag1 = map.get(1);

        System.out.println(tag1);

        assertEquals ("n",tag0);
        assertEquals ("v",tag1);
    }

    @Test
    public void getWordDict() throws Exception {
        Map<Integer,String > map = factory.getWordDict();
        String tag1 = map.get(1);
        String tag2 = map.get(2);
        assertEquals ("爱",tag1);
        assertEquals ("吃",tag2);
    }

}