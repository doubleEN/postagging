package com.rui.model;

import com.rui.parameters.AbstractParas;
import com.rui.parameters.TrigramParas;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.util.Arrays;

/**
 *
 */
public class SecondOrderHMM extends HMM {

    public static void main(String[] args) {
        AbstractParas paras = new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 44, 50000);
        HMM hmm = new SecondOrderHMM(paras);
        Tagger tagger = new Tagger(hmm);
        WordTag[] wts = tagger.tag("中共中央  总书记  、  国家  主席  江  泽民");
        System.out.println(Arrays.toString(wts));
    }

    //记录k次viterbi解码中计算得到的句子概率
    private double[][][] rankProbs;

    //解码时的辅助数组
    private int[][][][] indexs;

    public SecondOrderHMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    @Override
    public int[][] decode(String sentence, int k) {

        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getSizeOfTags();

        //存储最可能的标注集
        int[][] tagIds = new int[k][wordLen];

        this.rankProbs = new double[k][tagSize][tagSize];
        //解码用到中间数组
        this.indexs = new int[k][tagSize][tagSize][wordLen];

        //计算需要的句子概率
        for (int rank = 1; rank <= k; ++rank) {
            this.forward(sentence, rank);
        }

        WordTag[][] wts = new WordTag[k][wordLen];
        //找到k个概率最大的句子并解码
        for (int rank = 1; rank <= k; ++rank) {
            int index_rank = -1;
            int index_i = -1;
            int index_j = -1;
            double currProb = -0.1;

            //找到当前概率最大的索引
            for (int everyRank = 0; everyRank < k; ++everyRank) {
                for (int i = 0; i < tagSize; ++i) {
                    for (int j = 0; j < tagSize; ++j) {

                        if (this.rankProbs[everyRank][i][j] > currProb) {
                            currProb = this.rankProbs[everyRank][i][j];
                            index_rank = everyRank;
                            index_i = i;
                            index_j = j;
                        }
                    }
                }
            }

            this.rankProbs[index_rank][index_i][index_j] = -1;

            //第rank大的概率为第index_rank次句子概率计算中的索引[index_i,index_j]
            tagIds[rank - 1] = this.backtrack(index_rank, index_i, index_j);
        }
        return tagIds;
    }

    @Override
    protected void forward(String sentence, int ranking) {
        String[] words = sentence.split("\\s+");
        int tagSize = this.hmmParas.getSizeOfTags();
        int wordLen = words.length;

        //用以记录中间最大概率的数组
        double[][][] sentenceProb = new double[tagSize][tagSize][wordLen];
        //回溯解码用的中间数组:索引包含了j与k，存储的值是i
        int[][][] indexs = new int[tagSize][tagSize][wordLen];

        //处理t_1上的状态
        for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
            //第三个状态tag_k固定的情况下，不同的tag_j组合是一样的
            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
                sentenceProb[tag_j][tag_k][0] = this.hmmParas.getProbPi(tag_k) * this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[0]));
                if (sentenceProb[tag_j][tag_k][0]!=0){
                    System.out.println("t_1:"+sentenceProb[tag_j][tag_k][0]+"--"+tag_k+":"+this.hmmParas.getTagOnId(tag_k));
                }
            }
        }
        //处理t_2上的状态
        for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
                //第三个状态tag_k固定的情况下，不同的tag_j组合是一样的
                sentenceProb[tag_j][tag_k][1] = sentenceProb[0][tag_j][0] * this.hmmParas.getProbSmoothA(tag_j, tag_k) * this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[1]));
                if (sentenceProb[tag_j][tag_k][1]!=0){
                    System.out.println("t_2:"+sentenceProb[tag_j][tag_k][1]+"--"+tag_k+":"+this.hmmParas.getTagOnId(tag_k)+"--上一个标注："+tag_j+":"+this.hmmParas.getTagOnId(tag_j));
                }
            }
        }

        //O(wordLen*tagSize*tagSize*tagSize)
        for (int wordIndex = 2; wordIndex < wordLen; ++wordIndex) {
            //probs记录了第三个状态固定的情况下，前两个状态不同取值下的概率
            double[] probs = new double[tagSize];
//          //midProbs
            double[] midProbs = new double[tagSize];

            //排列无重复
            //三维数组先固定二维
            //一个三元：tag_i,tag_j,tag_k
            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
                //再固定三维
                for (int tag_k = 0; tag_k < tagSize; ++tag_k) {

                    boolean flag=false;
                    for (int tag_i = 0; tag_i < tagSize; ++tag_i) {
                        probs[tag_i] = sentenceProb[tag_i][tag_j][wordIndex - 1] * this.hmmParas.getProbSmoothA(tag_i, tag_j, tag_k);
//                        System.out.println(this.hmmParas.getTagOnId(tag_i)+":"+probs[tag_i]);
                        midProbs[tag_i] = probs[tag_i];
                        if (sentenceProb[tag_i][tag_j][wordIndex - 1]!=0){
                            flag=true;
                        }
                    }

                    Arrays.sort(midProbs);
                    int i = -1;
                    if (flag){
                        System.out.println(Arrays.toString(probs));
                        System.out.println(Arrays.toString(midProbs));
                    }
                    for (int row = 0; row < tagSize; ++row) {
                        if (probs[row] == midProbs[tagSize - ranking]) {
                            i = row;
                            break;
                        }
                    }

                    sentenceProb[i][tag_k][wordIndex] = midProbs[tagSize - ranking] * this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[wordIndex]));

                    indexs[tag_j][tag_k][wordIndex - 1] = i;
                }
            }
        }

        //取出最后一列的概率并返回，最后一列由两个隐藏态组成
        double[][] maxProbs = new double[tagSize][tagSize];
        for (int i = 0; i < tagSize; ++i) {
            for (int j = 0; j < tagSize; ++j) {
                maxProbs[i][j] = sentenceProb[i][j][wordLen - 1];
            }
        }
        this.indexs[ranking - 1] = indexs;
        this.rankProbs[ranking - 1] = maxProbs;
    }

    @Override
    protected int[] backtrack(int ranking, int... lastIndexs) {
        if (lastIndexs.length != 2) {
            System.err.println("回溯参数不合法。");
            return null;
        }
        int wordLen = this.indexs[ranking][0][0].length;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 1] = lastIndexs[0];
        tagIds[wordLen - 2] = lastIndexs[1];
        int max_k = lastIndexs[0];
        int max_j = lastIndexs[1];

        for (int col = wordLen - 3; col >= 0; --col) {
            int max_i = this.indexs[ranking][max_j][max_k][col];
            max_k = max_j;
            max_j = max_i;
            tagIds[col] = max_i;
        }
        return tagIds;
    }
}
