package com.rui.model;

import com.rui.dictionary.DictFactory;
import com.rui.parameter.AbstractParas;

import java.io.*;

/**
 * HMM接口
 */
public abstract class HMM implements Serializable {

    //HMM的参数对象
    protected AbstractParas hmmParas;

    //返回k个最可能的标注序列
    public abstract int[][] decode(String sentence, int k);

    //获得第[ranking]大的概率
    protected abstract void forward(String sentence, int ranking);

    //viterbi回溯
    protected abstract int[] backTrack(int ranking, int... lastIndexs);

    //HMM序列化
    public void writeHMM(String path) throws FileNotFoundException,IOException {
        ObjectOutputStream oos = null;
        oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(this);
        oos.close();
    }


    public AbstractParas getHmmParas() {
        return hmmParas;
    }
}


