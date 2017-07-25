package com.rui.model;

import com.rui.ngram.WordTag;

/**
 * HMM接口。
 */
public interface AbstractHMM {

    //返回最可能的标注序列
    WordTag[] tag(String sentences);

    //返回k个最可能的标注序列
    WordTag[][] tagTopK(String sentences, int k);

    //viterbi回溯解码
    int[] decode(int lastIndex, int[][] records);

    //匹配[词，词性]
    WordTag[] matching(String[] words, int[] tagIds);

    //获得第[ranking]大的概率
    double[] probsTopK(String[] words, int ranking,int[][] toolArr);

}
