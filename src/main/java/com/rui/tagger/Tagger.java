package com.rui.tagger;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.parameters.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Random;

/**
 *
 */
public class Tagger {

    public static void main(String[] args) {

//        AbstractParas paras = new TrigramParas();
//        PeopleDailyWordTagStream stream = new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt");
//        WordTag[] wts = null;
//        Random random = new Random(11);
//        while ((wts = stream.readSentence()) != null) {
//            int num = random.nextInt(4);
//            if (num == 1) {
//                paras.addHoldOut(wts);
//            } else {
//                paras.addCorpus(wts);
//            }
//        }
//        paras.calcProbs();
//        HMM hmm=new SecondOrderHMM(paras);
//        Tagger tagger=new Tagger(hmm);
//        System.out.println(Arrays.toString(tagger.tag("学好 自然 语言 处理 ， 实现 台湾 统一  。")));

        AbstractParas paras=new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
        HMM hmm=new SecondOrderHMM(paras);
        hmm.writeHMM("/home/mjx/桌面/TriGram.bin");
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
