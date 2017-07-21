package com.rui.model;

import com.rui.ngram.WordTag;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;

import java.util.Arrays;

/**
 * 一阶HMM实现。
 */
public class FirstOrderHMM implements AbstractHMM {

    public static void main(String[] args) {
        BigramParas paras = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44, 55310);
        paras.calcProbs();
//
//        System.out.println("probA:");
//        for (double[] p : paras.getPA()) {
//            System.out.println(Arrays.toString(p));
//        }
//        System.out.println("probPi:");
//        System.out.println(Arrays.toString(paras.getPpi()));
//
//        System.out.println("smoothA:");
//        for (double[] p : paras.getPSA()) {
//            System.out.println(Arrays.toString(p));
//        }
//
//        System.out.println("probB:");
//        for (double[] p : paras.getPB()) {
//            System.out.println(Arrays.toString(p));
//        }

        AbstractHMM hmm = new FirstOrderHMM(paras);

        WordTag[] wts = hmm.tag("迈向 一九九八年 ！");
        System.out.println(Arrays.toString(wts));
    }


    @Override
    public WordTag[][] tagTopK(String sentence, int k) {
        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();
        //索引数组差概率数组一位
        double[][] sentencesProb = new double[tagSize][wordLen];
        int[][] maxIndexs = new int[tagSize][wordLen];

        //带入初始状态计算第一个观察态概率，不用记录最大值索引
        for (int i = 0; i < tagSize; ++i) {
            sentencesProb[i][0] = this.hmmParas.getProbPi(i) * this.hmmParas.getProbB(i, this.hmmParas.getWordId(words[0]));
        }

        //外层循环：t(i)-->t(i+1)-->w(i+1)
        for (int wordIndex = 1; wordIndex < wordLen; ++wordIndex) {

            for (int nextTag = 0; nextTag < tagSize; ++nextTag) {
                double maxProb = -1.0;
                double prob = -1.0;
                int maxIndex = -1;

                for (int preTag = 0; preTag < tagSize; ++preTag) {
                    if ((prob = sentencesProb[preTag][wordIndex - 1] * this.hmmParas.getProbSmoothA(preTag, nextTag)) > maxProb) {
                        maxProb = prob;
                        maxIndex = preTag;
                    }
                }

                sentencesProb[nextTag][wordIndex] = maxProb * this.hmmParas.getProbB(nextTag, this.hmmParas.getWordId(words[wordIndex]));
                maxIndexs[nextTag][wordIndex - 1] = maxIndex;
            }
        }

        //取出最后一列的最大概率
        double[] maxProbs = new double[tagSize];
        for (int i = 0; i < tagSize; ++i) {
            maxProbs[i] = sentencesProb[i][wordLen - 1];
        }

        WordTag[][] wts = new WordTag[k][tagSize];

        //通过最大的k个概率的索引，回溯得到标注
        for (int i = 0; i < k; ++i) {
            double maxP = -1.0;
            int maxIndex = -1;

            for (int j = 0; j < tagSize; ++j) {
                if (maxProbs[j] >= maxP) {
                    maxP = maxProbs[j];
                    maxIndex = j;
                }
            }
            maxProbs[maxIndex] = -1.0;
            int[] tagIds = this.decode(maxIndex, maxIndexs);
            wts[i] = this.matching(words, tagIds);
        }

        return wts;
    }

    @Override
    public WordTag[] tag(String sentences) {
        return this.tagTopK(sentences, 1)[0];

    }

    @Override
    public int[] decode(int lastIndex, int[][] records) {
        int wordLen = records[0].length;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 1] = lastIndex;
        int maxRow = lastIndex;

        for (int col = wordLen - 2; col >= 0; --col) {
            maxRow = records[maxRow][col];
            tagIds[col] = maxRow;
        }
        return tagIds;
    }

    @Override
    public WordTag[] matching(String[] words, int[] tagIds) {
        int wordLen = words.length;
        WordTag[] wts = new WordTag[wordLen];
        for (int index = 0; index < wordLen; ++index) {
            wts[index] = new WordTag(words[index], this.hmmParas.getTagOnId(tagIds[index]));
        }
        return wts;
    }

    protected AbstractParas hmmParas;

    public FirstOrderHMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

}
