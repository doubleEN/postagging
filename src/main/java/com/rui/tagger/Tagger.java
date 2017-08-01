package com.rui.tagger;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.wordtag.WordTag;

import java.util.Arrays;

/**
 *
 */
public class Tagger {

    public static void main(String[] args) {
        AbstractParas paras=new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
        HMM hmm=new FirstOrderHMM(paras);
        Tagger tagger=new Tagger(hmm);
        WordTag[][] wts=tagger.tagTopK("学好 自然 语言 处理 ， 实现 台湾 统一  。 ",3);
        for (WordTag[] wt:wts){
            System.out.println(Arrays.toString(wt));
        }
        WordTag[] wt=tagger.tag("学好 自然 语言 处理 ， 实现 台湾 统一  。 ");
    }

    private HMM hmm;

    public Tagger(HMM hmm) {
        this.hmm = hmm;
    }

    //返回最可能的标注序列
    public WordTag[] tag(String sentences) {
        return tagTopK(sentences,1)[0];
    }

    //返回k个最可能的标注序列
    public WordTag[][] tagTopK(String sentences, int k) {
        String[] words = sentences.split("\\s+");
        int wordLen = words.length;

        WordTag[][] wts = new WordTag[k][wordLen];

        int[][] tagIds = this.hmm.decode(sentences, k);
        for (int i = 0; i < k; ++i) {
            wts[i] = this.matching(words, tagIds[i]);
        }
        return wts;
    }

    private WordTag[] matching(String[] words, int[] tagIds) {
        int wordLen = words.length;
        WordTag[] wts = new WordTag[wordLen];
        for (int index = 0; index < wordLen; ++index) {
            wts[index] = new WordTag(words[index], this.hmm.getTagOnId(tagIds[index]));
        }
        return wts;
    }
}
