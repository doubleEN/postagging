package com.rui.model;

import com.rui.wordtag.WordTag;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import jdk.nashorn.internal.runtime.WithObject;

import java.util.Arrays;

/**
 * 一阶HMM实现。
 */
public class FirstOrderHMM extends HMM {
    public static void main(String[] args) {
        BigramParas paras = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 44, 55310);

        FirstOrderHMM hmm = new FirstOrderHMM(paras);

        int[][] wts = hmm.decode("迈向 一九九八年 ！",4);

        for (int[]w:wts){

            System.out.println(Arrays.toString(w));
        }
    }

    //记录k次viterbi解码中计算得到的句子概率
    private double[][]rankProbs;

    //解码时的辅助数组
    private int[][][]indexs;

    public FirstOrderHMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    @Override
    public int[][] decode(String sentence, int k) {

        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();

        int[][] tagIds=new int[k][wordLen];

        this.rankProbs = new double[k][tagSize];
        //解码用到中间数组
        this.indexs = new int[k][tagSize][wordLen];

        //计算需要的句子概率
        for (int rank= 1; rank <= k; ++rank) {
            this.forward(sentence,rank);
        }

        WordTag[][] wts = new WordTag[k][wordLen];
        //找到k个概率最大的句子并解码
        for (int rank = 1; rank <= k; ++rank) {
            int index_i = -1;
            int index_j = -1;
            double currProb = -0.1;

            //找到当前概率最大的索引
            for (int i = 0; i < k; ++i) {
                for (int j = 0; j < tagSize; ++j) {
                    if (this.rankProbs[i][j] > currProb) {
                        currProb = this.rankProbs[i][j];
                        index_i = i;
                        index_j = j;
                    }
                }
            }

            this.rankProbs[index_i][index_j] = -1;

            //index_i为第index_i次前向算法，index_j为index_i次前向算法中的某个概率索引
            tagIds[rank-1] = this.backtrack(index_j, index_i);

        }
        return tagIds;
    }

    @Override
    public void forward(String sentence,int ranking) {
        String[] words=sentence.split("\\s+");
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
        this.indexs[ranking-1]=indexs;
        this.rankProbs[ranking - 1]=maxProbs;
    }

    @Override
    public int[] backtrack(int lastIndex, int ranking) {
        int wordLen = this.indexs[ranking][0].length;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 1] = lastIndex;
        int maxRow = lastIndex;

        for (int col = wordLen - 2; col >=0; --col) {
            maxRow = this.indexs[ranking][maxRow][col];
            tagIds[col] = maxRow;
        }
        return tagIds;
    }


}
