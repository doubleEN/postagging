package com.rui.tagger;

import com.rui.evaluation.Estimator;
import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * 标注类。
 */
public class Tagger {

    /**
     * 隐马尔科夫模型
     */
    private HMM hmm;

    /**
     * @param hmm 隐马尔科夫模型
     */
    public Tagger(HMM hmm) {
        this.hmm = hmm;
    }

    /**
     * @param HMMPath 序列化隐马尔科夫模型路径
     */
    public Tagger(String HMMPath) {
        this.hmm = this.readHMM(HMMPath);
    }

    /**
     * 返回最可能的标注序列
     * @param sentences 未标注句子
     * @return 标注结果
     */
    public WordTag[] tag(String sentences) {

        return tagTopK(sentences, 1)[0];
    }

    /**
     * 返回k个最可能的标注序列
     * @param sentences 未标注句子
     * @param k 得到k个局部最优标注，其中排名第一的标注是全局最优
     * @return k个局部最优标注
     */
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

    /**
     * 词与标注配对
     * @param words 单词序列
     * @param tagIds 标注序列
     * @return [Word/Tag]数组
     */
    private WordTag[] matching(String[] words, int[] tagIds) {
        int wordLen = words.length;
        WordTag[] wts = new WordTag[wordLen];
        for (int index = 0; index < wordLen; ++index) {
            wts[index] = new WordTag(words[index], this.hmm.getHmmParas().getDictionary().getTag(tagIds[index]));
        }
        return wts;
    }

    /**
     * 模型反序列化
     * @param path 列化模型序的路径
     * @return HMM对象
     */
    private HMM readHMM(String path) {
        HMM hmm = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            hmm = (HMM) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return hmm;
    }
}
