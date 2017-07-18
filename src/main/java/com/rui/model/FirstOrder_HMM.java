package com.rui.model;

import com.rui.ngram.WordTag;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;

import java.util.Arrays;

/**
 *
 */
public class FirstOrder_HMM extends AbstractHMM {

    public static void main(String[] args) {
        BigramParas paras=new BigramParas();
        paras.addCorpus("/home/mjx/桌面/PoS/test/testCount.txt");
        paras.calcProbs(true);

        System.out.println("probA:");
        for (double[] p : paras.getPA()) {
            System.out.println(Arrays.toString(p));
        }
        System.out.println("probPi:");
        System.out.println(Arrays.toString(paras.getPpi()));

        System.out.println("smoothA:");
        for (double[] p : paras.getPSA()) {
            System.out.println(Arrays.toString(p));
        }

        System.out.println("probB:");
        for (double[] p : paras.getPB()) {
            System.out.println(Arrays.toString(p));
        }

        FirstOrder_HMM hmm=new FirstOrder_HMM(paras);

        WordTag[] wts=hmm.predict("爱 我");
//        System.out.println(Arrays.toString(wts));
    }

    public FirstOrder_HMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    @Override
    public WordTag[] predict(String sentence) {

        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();
        //索引数组差概率数组一位
        double[][] sentencesProb = new double[tagSize][wordLen];
        int[][] maxIndexs = new int[tagSize][wordLen];

        //带入初始状态计算第一个观察态概率，不用记录最大值索引
        for (int i = 0; i < tagSize; ++i) {
            sentencesProb[i][0] = this.hmmParas.getPi(i) * this.hmmParas.getProbB(i, words[0]);
        }

        //外层循环：t(i)-->t(i+1)-->w(i+1)
        for (int wordIndex = 1; wordIndex < wordLen; ++wordIndex) {

            for (int nextTag = 0; nextTag < tagSize; ++nextTag) {
                double maxProb = -1.0;
                double prob = -1.0;
                int maxIndex = -1;

                for (int preTag = 0; preTag < tagSize; ++preTag) {
                    System.out.println("索引："+preTag);
                    if ((prob = sentencesProb[preTag][wordIndex-1] * this.hmmParas.getProbSmoothA(preTag,nextTag)) > maxProb) {
                        maxProb = prob;
                        System.out.println("最大索引："+preTag);
                        maxIndex = preTag;
                    }
                }

                sentencesProb[nextTag][wordIndex]=maxProb*this.hmmParas.getProbB(nextTag,words[wordIndex]);
                maxIndexs[nextTag][wordIndex-1] = maxIndex;
            }
        }

        System.out.println("句子概率：");

        for (double[] p:sentencesProb){
            System.out.println(Arrays.toString(p));
        }
        System.out.println("句子索引：");

        for (int[] i:maxIndexs){
            System.out.println(Arrays.toString(i));
        }

        return new WordTag[0];
    }
}
