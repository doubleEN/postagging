package com.rui.model;

import com.rui.parameters.AbstractParas;
import com.rui.wordtag.WordTag;

/**
 * HMM接口。
 */
public abstract class HMM {
    protected AbstractParas hmmParas;


    //返回k个最可能的标注序列
    public  abstract  int[][] viterbi(String sentences, int k);

    //获得第[ranking]大的概率
    protected abstract void forward(String sentence,int ranking);

    //viterbi回溯解码
    protected abstract int[] decode(int lastIndex, int ranking);

    public String getTagOnId(int id){
        return this.hmmParas.getTagOnId(id);
    }

}
