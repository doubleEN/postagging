package com.rui.model;

import com.rui.parameters.AbstractParas;
import com.rui.wordtag.WordTag;

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

    //通过标注的id获取标注的字符串形式
    public String getTagOnId(int id) {
        return this.hmmParas.getTagOnId(id);
    }

    //HMM序列化
    public void writeHMM(String path) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}


