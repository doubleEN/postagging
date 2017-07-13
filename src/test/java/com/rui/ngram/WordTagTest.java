package com.rui.ngram;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class WordTagTest {
    IWordTagGenerator generator=new PeopleDailyNewsWordTagGenerator();
    //抽象方法 separateWordTag() 在构造器中分割word与tag

    @Test
    public void testWord_Tag() throws Exception {

        assertEquals("v", generator.separateWordTag("吃/v").getTag());

        assertEquals("中国", generator.separateWordTag("中国/n").getWord());
    }


}