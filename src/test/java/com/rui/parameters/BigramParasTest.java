package com.rui.parameters;

import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 一阶HMM参数测试
 */
public class BigramParasTest {

    AbstractParas paras = new BigramParas();

    //测试标注集大小
    @Test
    public void getSizeOfTags() throws Exception {
        paras.addCorpus("我/n 爱/v 自然语言处理/ln");
        paras.addCorpus("我/n 爱/v nlp/yn");
        paras.addCorpus("武汉/n 的/c 天气/n 热/a");
        paras.addCorpus("学/v 好/a 数理化/n");
        paras.addCorpus("递归/ln 神经网络/ln");
        paras.calcProbs();

        assertEquals(6, paras.getSizeOfTags());
    }

    //测试初始概率
    @Test
    public void getProbPi() throws Exception {
        paras.addCorpus("我/n 爱/v 自然语言处理/ln");
        paras.addCorpus("我/n 爱/v nlp/yn");
        paras.addCorpus("武汉/n 的/c 天气/n 热/a");
        paras.addCorpus("学/v 好/a 数理化/n");
        paras.addCorpus("递归/ln 神经网络/ln");
        paras.calcProbs();

        int idN = paras.getTagId("n");

        assertEquals(0.333333, paras.getProbPi(idN), 0.0001);
    }

    //测试混淆概率
    @Test
    public void getProbB() throws Exception {
        paras.addCorpus("我/n 爱/v 自然语言处理/ln");
        paras.addCorpus("我/n 爱/v nlp/yn");
        paras.addCorpus("武汉/n 的/c 天气/n 热/a");
        paras.addCorpus("学/v 好/a 数理化/n");
        paras.addCorpus("递归/ln 神经网络/ln");
        paras.calcProbs();

        int word_NLP_id = paras.getWordId("nlp");
        int tag_yn_id = paras.getTagId("yn");

        assertEquals(1.0, paras.getProbB(tag_yn_id, word_NLP_id),0.1);
    }

    //测试未平滑的转移概率
    @Test
    public void getProbA() throws Exception {
        paras.addCorpus("我/n 爱/v 自然语言处理/ln");
        paras.addCorpus("我/n 爱/v nlp/yn");
        paras.addCorpus("武汉/n 的/c 天气/n 热/a");
        paras.addCorpus("学/v 好/a 数理化/n");
        paras.addCorpus("递归/ln 神经网络/ln");
        paras.calcProbs();

        int tag_n_id = paras.getTagId("n");
        int tag_v_id = paras.getTagId("v");

        assertEquals(0.5, paras.getProbA(tag_n_id, tag_v_id),0.0001);

    }

    //测试平滑的转移概率
    @Test
    public void getProbSmoothA() throws Exception {

        AbstractParas paras=new BigramParas("/home/mjx/桌面/PoS/test/testSet2.txt");

        int tag_v_id = paras.getTagId("v");
        int tag_yn_id = paras.getTagId("n");

        assertEquals(0.19359, paras.getProbSmoothA(tag_v_id, tag_yn_id),0.000001);
    }

}