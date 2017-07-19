package com.rui.model;

import com.rui.ngram.WordTag;
import com.rui.parameters.AbstractParas;

/**
 *  HMM实现的抽象类。
 */
public abstract class AbstractHMM {

    //返回最可能的标注序列
    public abstract WordTag[] tag(String  sentences);

    //返回k个最可能的标注序列
    public abstract WordTag[][] tagTopK(String  sentences,int k);

    //viterbi回溯解码
    protected abstract int[] decode(int lastIndex,int[][] records);

    //匹配【词，词性】
    protected abstract WordTag[] matching(String[]words,int[]tagIds);

}
