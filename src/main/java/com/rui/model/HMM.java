package com.rui.model;

import com.rui.dictionary.DictFactory;
import com.rui.parameter.AbstractParas;

import java.io.*;

/**
 * HMM抽象类
 */
public abstract class HMM implements Serializable {

    /**
     * HMM的参数对象
     */
    protected AbstractParas hmmParas;

    /**
     * 返回k个最可能的标注序列
     * @param sentence 未标注的句子
     * @param k 最可能的标注序列个数
     * @return k个最可能的标注序列的id序列
     */
    public abstract int[][] decode(String sentence, int k);

    /**
     * 获得句子可能的标注中，第[ranking]大的概率
     * @param sentence 未标注的句子
     * @param ranking 指定的概率排名
     */
    protected abstract void forward(String sentence, int ranking);

    /**
     * viterbi回溯得标注id
     * @param ranking 指定的概率排名
     * @param lastIndexs 计算指定排名句子概率的最后一个词对应的标注id
     * @return 指定的概率排名下的标注序列
     */
    protected abstract int[] backTrack(int ranking, int... lastIndexs);

    /**
     * HMM序列化
     * @param path 指定的序列化路径
     */
    public void writeHMM(String path) throws IOException {
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(this);
        oos.close();
    }

    /**
     * 获取HMM的参数对象
     * @return HMM的参数对象
     */
    public AbstractParas getHmmParas() {
        return hmmParas;
    }
}


