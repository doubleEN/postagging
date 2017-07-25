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
        BigramParas paras = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 44, 55310);

        FirstOrderHMM hmm = new FirstOrderHMM(paras);

        WordTag[] wts = hmm.tag("迈向 一九九八年 ！");

        System.out.println(Arrays.toString(wts));
    }

    @Override
    public WordTag[][] tagTopK(String sentence, int k) {

        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();
        WordTag[][] wts = new WordTag[k][wordLen];

        //记录k次viterbi解码中计算得到的句子概率
        double[][] rankProbs = new double[k][tagSize];
        //解码用到中间数组
        int[][][] indexs = new int[k][tagSize][wordLen];

        //计算句子概率
        for (int i = 1; i <= k; ++i) {
            rankProbs[i - 1] = this.probsTopK(words, i, indexs[i - 1]);
        }

        //找到k个概率最大的句子并解码
        for (int rank = 1; rank <= k; ++rank) {
            int index_i = -1;
            int index_j = -1;
            double currProb = -0.1;

            //找到当前概率最大的索引
            for (int i = 0; i < k; ++i) {
                for (int j = 0; j < tagSize; ++j) {
                    if (rankProbs[i][j] > currProb) {
                        currProb = rankProbs[i][j];
                        index_i = i;
                        index_j = j;
                    }
                }
            }

            rankProbs[index_i][index_j] = -1;

            int[] tagIds = this.decode(index_j, indexs[index_i]);
            wts[rank - 1] = this.matching(words, tagIds);
        }

        return wts;
    }

    public double[] probsTopK(String[] words, int ranking, int[][] toolArr) {
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();
        //索引数组差概率数组一位
        double[][] sentencesProb = new double[tagSize][wordLen];
        int[][] indexs = new int[tagSize][wordLen];

        //带入初始状态计算第一个观察态概率，不用记录最大值索引
        for (int i = 0; i < tagSize; ++i) {
            sentencesProb[i][0] = this.hmmParas.getProbPi(i) * this.hmmParas.getProbB(i, this.hmmParas.getWordId(words[0]));
        }

        //外层循环：t(i)-->t(i+1)-->w(i+1)
        for (int wordIndex = 1; wordIndex < wordLen; ++wordIndex) {

            for (int nextTag = 0; nextTag < tagSize; ++nextTag) {
                int index = -1;

                //同一个转移后状态接受的概率存入数组
                double[] probs = new double[tagSize];

                for (int preTag = 0; preTag < tagSize; ++preTag) {
                    probs[preTag] = sentencesProb[preTag][wordIndex - 1] * this.hmmParas.getProbSmoothA(preTag, nextTag);
                }

                double[] midProbs = Arrays.copyOf(probs, tagSize);
                Arrays.sort(midProbs);

                for (int i = 0; i < tagSize; ++i) {
                    if (probs[i] == midProbs[tagSize - ranking]) {
                        index = i;
                        break;
                    }
                }

                sentencesProb[nextTag][wordIndex] = midProbs[tagSize - ranking] * this.hmmParas.getProbB(nextTag, this.hmmParas.getWordId(words[wordIndex]));
                indexs[nextTag][wordIndex - 1] = index;
            }
        }

        //取出最后一列的最大概率并返回
        double[] maxProbs = new double[tagSize];
        for (int i = 0; i < tagSize; ++i) {
            maxProbs[i] = sentencesProb[i][wordLen - 1];
        }

        return maxProbs;
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
