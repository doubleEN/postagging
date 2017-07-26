package com.rui.model;

import com.rui.ngram.WordTag;
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
    static AbstractHMM hmm;

    //输入模型并训练参数，构造HMM模型
    @BeforeClass
    public static void setUp() throws Exception {
        //构造参数
        AbstractParas hmmParas = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,55310);

        //模型传入参数
        hmm = new FirstOrderHMM(hmmParas);
    }

    //测试两种标注最可能的结果是否一致
    @Test
    public void tag_tagTopK() throws Exception {

        //待标注句子
        String unTagSentence="学好 自然 语言 处理 ， 实现 台湾 统一  。 ";

        //两种标注接口进行标注
        WordTag[]tag=hmm.tag(unTagSentence);
        WordTag[][]tagK=hmm.tagTopK(unTagSentence,3);

        //两种最可能标注是否一致
        String maxTag1=Arrays.toString(tag);
        String maxTag2=Arrays.toString(tagK[0]);

        assertEquals(maxTag1,maxTag2);
    }

    @Test
    public void tagTopK() throws Exception {

        //待标注句子
        String unknowSentence="学好 自然 语言 处理 ， 实现 台湾 统一  。 ";

        //两种标注接口进行标注
        WordTag[][]tagK=hmm.tagTopK(unknowSentence,3);

        //两种最可能标注是否一致
        String maxTag2=Arrays.toString(tagK[0]);

        int no=1;
        for (WordTag[] wt:tagK){
            System.out.println("No."+no+""+Arrays.toString(wt));
            ++no;
        }

        String sentence="[学好/v, 自然/a, 语言/n, 处理/vn, ，/w, 实现/v, 台湾/ns, 统一/vn, 。/w]";
        assertEquals(sentence,maxTag2);
    }

}