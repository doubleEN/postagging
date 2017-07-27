package com.rui.model;

import com.rui.wordtag.WordTag;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * 一阶HMM标注测试
 */
public class FirstOrderHMMTest {
    static HMM hmm;

    //输入并训练参数，构造HMM模型
    @BeforeClass
    public static void setUp() throws Exception {
        //构造参数
        AbstractParas hmmParas = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);

        //模型传入参数
        hmm = new FirstOrderHMM(hmmParas);
    }

    //测试两种标注最可能的结果是否一致
    @Test
    public void tag_tagTopK() throws Exception {

        //待标注句子
        String unTagSentence="学好 自然 语言 处理 ， 实现 台湾 统一  。 ";

        //两种标注接口进行标注
    }

    @Test
    public void tagTopK() throws Exception {

        //待标注句子
        String unknowSentence="学好 自然 语言 处理 ， 实现 台湾 统一  。 ";

    }

}