package com.rui.parameter;

import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.util.GlobalParas;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 模型参数类测试
 */
public class AbstractParasTest {

    /*
    平滑概率无法有效断言
     */
    @Test
    public void calc2Gram() throws Exception {

        AbstractParas paras=new BigramParas(GlobalParas.UNK_MAXPROB);

        paras.addCorpus("学习/v 自然/n 语言/n 处理/n 。/w",new PeopleDailyWordTagStream());
        paras.addCorpus("学习/v nlp/yn 。/w",new PeopleDailyWordTagStream());

        paras.calcProbs();

        int tag_v=paras.getDictionary().getTagId("v");
        int tag_n=paras.getDictionary().getTagId("n");
        int tag_yn=paras.getDictionary().getTagId("yn");
        assertEquals(0.25,paras.getProbPi(tag_v),0.001);
        assertEquals(0.375,paras.getProbPi(tag_n),0.001);
        assertEquals(0.125,paras.getProbPi(tag_yn),0.001);

        assertEquals(0.5,paras.getProbA(true,tag_v,tag_n),0.1);

        int word_xuexi=paras.getDictionary().getWordId("学习");
        //发射概率进行了+1平滑，发射计数矩阵中，v-->学习：（2+1）/（2+6）=0.375
        assertEquals(0.375,paras.getProbB(tag_v,word_xuexi),0.001);
    }

    @Test
    public void calc3Gram() throws Exception {

        AbstractParas paras=new TrigramParas(GlobalParas.UNK_MAXPROB);

        paras.addCorpus("学习/v 自然/n 语言/n 处理/n 。/w",new PeopleDailyWordTagStream());
        paras.addCorpus("学习/v 科学/n 知识/tn",new PeopleDailyWordTagStream());

        paras.calcProbs();

        int tag_v=paras.getDictionary().getTagId("v");
        int tag_n=paras.getDictionary().getTagId("n");
        int tag_w=paras.getDictionary().getTagId("w");
        assertEquals(0.25,paras.getProbPi(tag_v),0.001);
        assertEquals(0.5,paras.getProbPi(tag_n),0.001);

        //两个标注状态转移到第三个状态
        assertEquals(0.5,paras.getProbA(true,tag_v,tag_n,tag_n),0.01);

        int word_xuexi=paras.getDictionary().getWordId("学习");
        //发射概率进行了+1平滑，发射计数矩阵中，v-->学习：（2+1）/（2+7）=0.333...
        assertEquals(0.333,paras.getProbB(tag_v,word_xuexi),0.001);
    }


}