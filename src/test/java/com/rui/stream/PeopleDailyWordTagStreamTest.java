package com.rui.stream;

import com.rui.wordtag.WordTag;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 */
public class PeopleDailyWordTagStreamTest {
    WordTagStream PeopleDailyTagStream;

    @Before
    public void setUp() throws Exception {
        String s="s";
        this.PeopleDailyTagStream = new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/test/testCount.txt");
    }

    // 流迭代返回每个句子
    @Test
    public void next() throws Exception {
        WordTag[] wts;
        while ((wts = PeopleDailyTagStream.readSentence()) != null) {
            System.out.println(Arrays.toString(wts));
        }
    }

    //抽象方法：segSentence()
    @Test
    public void segSentence() throws Exception {
        String[] sentences = new String[]{
                "我/n 爱/v NLP/ne"
        };
        WordTag[] wt = this.PeopleDailyTagStream.segSentence(sentences[0]);

        assertEquals("NLP", wt[2].getWord());
    }

    // 打开流操作,在构造器中打开了流
    @Test
    public void openReadStream() throws Exception {
        assertNotNull(PeopleDailyTagStream.br);//判非空
    }

    //测试关闭流
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void close3() throws Exception {
        WordTag[] wt;
        wt = PeopleDailyTagStream.readSentence();
        //[我/n, 爱/v, 自然语言处理/ln]
        System.out.println(Arrays.toString(wt));

        //关闭异常
        PeopleDailyTagStream.close();

        thrown.expect(NullPointerException.class);
        wt = PeopleDailyTagStream.readSentence();

    }
}