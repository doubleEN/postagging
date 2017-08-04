package com.rui.tagger;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.parameters.TrigramParas;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

/**
 *
 */
public class Tagger {

    public static void main(String[] args) {

        AbstractParas paras=new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);

        HMM hmm=new SecondOrderHMM(paras);

        hmm.writeHMM("/home/mjx/桌面/2HMM.bin");

        Tagger tager = new Tagger("/home/mjx/桌面/2HMM.bin");
        System.out.println(Arrays.toString(tager.tag("学好 自然 语言 处理 ， 实现 台湾 统一  。")));
    }

    private HMM hmm;

    public Tagger(HMM hmm) {
        this.hmm = hmm;
    }

    public Tagger(String HMMPath) {
        this.hmm = this.readHMM(HMMPath);
    }

    //返回最可能的标注序列
    public WordTag[] tag(String sentences) {
        return tagTopK(sentences, 1)[0];
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

    //词与标注配对
    private WordTag[] matching(String[] words, int[] tagIds) {
        int wordLen = words.length;
        WordTag[] wts = new WordTag[wordLen];
        for (int index = 0; index < wordLen; ++index) {
            wts[index] = new WordTag(words[index], this.hmm.getTagOnId(tagIds[index]));
        }
        return wts;
    }

    //模型反序列化
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
