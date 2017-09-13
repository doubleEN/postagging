package com.rui.model;

import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.tagger.Tagger;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;


public class HMM1st extends HMM {

    private int[][][] backTrackTool;

    private double[][] allProbs;


    public HMM1st(AbstractParas paras) {
        this.hmmParas = paras;
    }

    public int[][] decode(String sentence, int k) {

        //处理k大于标注集大小的边界问题
        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();
        if (k > sizeOfTags) {
            return this.decode(sentence, sizeOfTags);
        }

        String[] words = sentence.trim().split("\\s+");
        int lenOfSentence = words.length;

        //解码回溯的索引数组：比句子长度短1的，k，标注集大小
        this.backTrackTool = new int[lenOfSentence - 1][k][sizeOfTags];
        //记录序列上最有一个节点的k次最有概率值
        this.allProbs = new double[k][sizeOfTags];

        this.forward(sentence, k);

        int[][] bestKSequence = new int[k][lenOfSentence];


        for (int rank = 0; rank < k; ++rank) {

            double no_KProb = Math.log(0);

            int rankIndex = -1, tagIndex = -1;


            for (int i = 0; i < k; ++i) {
                for (int j = 0; j < sizeOfTags; ++j) {
                    if (this.allProbs[i][j] >= no_KProb) {
                        rankIndex = i;
                        tagIndex = j;
                        no_KProb = this.allProbs[i][j];
                    }
                }
            }

            bestKSequence[rank] = this.backTrack(rankIndex, tagIndex);
        }

        return bestKSequence;
    }

    protected void forward(String sentence, int ranks) {
        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();
        String[] words = sentence.trim().split("\\s+");
        int lenOfSentence = words.length;

        //三维：句子长度，k，标注集大小，该三维数组用以记录viterbi产生的中间概率，这个概率是发射过后的概率，而不仅仅是转移后的概率
        double[][][] midProb = new double[lenOfSentence][ranks][sizeOfTags];

        //计算初始的发射概率，不用记录最大概率索引
        for (int tagIndex = 0; tagIndex < sizeOfTags; ++tagIndex) {
            //句首的未登录词处理，log(1)=0
            double launchProb = 0;
            if (this.hmmParas.getDictionary().getWordId(words[0]) != null) {
                //发射概率
                launchProb = Math.log(this.hmmParas.getProbB(tagIndex, this.hmmParas.getDictionary().getWordId(words[0])));
            }
            double val = Math.log(this.hmmParas.getProbPi(tagIndex)) + launchProb;
            //为k次最优赋予相同的初始发射概率
            for (int rank = 0; rank < ranks; ++rank) {
                midProb[0][rank][tagIndex] = val;
            }
        }

        //句子在时序上遍历
        for (int wordIndex = 1; wordIndex < lenOfSentence; ++wordIndex) {
            //遍历k次最优
            for (int rank = 0; rank < ranks; ++rank) {

                //将要转移的下一个隐藏状态
                for (int currTag = 0; currTag < sizeOfTags; ++currTag) {
                    double[] tempArr = new double[sizeOfTags];


                    //转移前的隐藏状态
                    for (int preTag = 0; preTag < sizeOfTags; ++preTag) {
                        tempArr[preTag] = midProb[wordIndex - 1][rank][preTag] + Math.log(this.hmmParas.getProbSmoothA(preTag, currTag));
                    }

                    //获得下一个隐藏状态一定的情况下，概率第k大的转移前状态的索引和概率
                    int indexOfBestK = -1;
                    double bestKProb = -1;
                    //用以找到第k优概率的排序数组
                    double[] sortedArr = Arrays.copyOf(tempArr, sizeOfTags);
                    Arrays.sort(sortedArr);
                    //k次计算句子的概率的步骤都一样，不一样的是每次转移概率取的第k大的概率
                    for (int i = 0; i < sizeOfTags; ++i) {
                        if (tempArr[i] == sortedArr[sizeOfTags - rank - 1]) {
                            indexOfBestK = i;
                            bestKProb = tempArr[i];
                            break;
                        }
                    }
                    //回溯用中间数组含义：第rank次最优，时序wordIndex上的currTag状态的最大转移概率对应的上一个隐藏状态是indexOfBestK
                    this.backTrackTool[wordIndex - 1][rank][currTag] = indexOfBestK;

                    //状态发射时的未登录词处理
                    double launchProb = 0;
                    if (this.hmmParas.getDictionary().getWordId(words[wordIndex]) != null) {
                        launchProb = Math.log(this.hmmParas.getProbB(currTag, this.hmmParas.getDictionary().getWordId(words[wordIndex])));
                    }

                    //状态转移和发射的概率积
                    midProb[wordIndex][rank][currTag] = bestKProb + launchProb;
                }

                this.allProbs[rank] = midProb[wordIndex][rank];
            }
        }

    }

    @Override
    public int[] backTrack(int rank, int... lastTagIndexs) {
        if (lastTagIndexs.length != 1) {
            logger.severe("回溯参数不合法。");
            System.exit(1);
        }
        int wordLen = this.backTrackTool.length + 1;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 1] = lastTagIndexs[0];
        int maxRow = lastTagIndexs[0];

        for (int col = wordLen - 2; col >= 0; --col) {
            maxRow = this.backTrackTool[col][rank][maxRow];
            tagIds[col] = maxRow;
        }
        return tagIds;
    }
}
