package com.rui.stream;

import com.rui.ngram.AbstractWordTag;
import com.rui.ngram.WordTag;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 */
public class PeopleDailyWordTagStreamTest {
    WordTagStream tagStream;
    String []sentences;

    @Before//Before在每个Test前执行
    public void setUp() throws Exception {
        this.tagStream = new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/test/testCount.txt");
        this.sentences=new String[]{"标注/v     汉语/n","我/ 爱/v NLP/ne"};
    }

    @Test
    public void segSentence() throws Exception {
        AbstractWordTag[] wt = this.tagStream.segSentence(this.sentences[0]);
        assertEquals("标注/v", wt[0].getWordTag());
    }

    @Test
    public void openReadStream() throws Exception {
        assertNotNull(tagStream.br);//判非空
    }

    @Test
    public void next() throws Exception {
        AbstractWordTag[]wt;
        while ((wt=tagStream.readLine())!=null){
            System.out.println(Arrays.toString(wt));
        }
        assertNull(wt);//判空
    }

    @Test
    public void close() throws Exception {
        AbstractWordTag[]wt;
        wt=tagStream.readLine();
        System.out.println(Arrays.toString(wt));
        tagStream.close();
        wt=tagStream.readLine();//Stream closed
        System.out.println(Arrays.toString(wt));
    }
}