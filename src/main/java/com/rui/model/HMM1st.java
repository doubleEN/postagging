package com.rui.model;

import com.rui.parameter.AbstractParas;

import static com.rui.util.GlobalParas.logger;

/**
 * 一阶隐马尔科夫。
 */
public class HMM1st extends HMM {

    /**
     * 回溯中间索引数组
     */
    private int[][][][] backTrackTool;

    /**
     * 序列上最后一个节点上每个隐藏状态对应的k个最优概率值
     */
    private double[][] lastLayerProbs;


    public HMM1st(AbstractParas paras) {
        this.hmmParas = paras;
    }

    public int[][] decode(String sentence, int topK) {

        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();

        String[] words = sentence.trim().split("\\s+");
        int lenOfSentence = words.length;

        //解码回溯的索引数组
        this.backTrackTool = new int[lenOfSentence - 1][topK][sizeOfTags][2];
        //记录序列上最后一个节点每个隐藏状态的k次最优概率值
        this.lastLayerProbs = new double[topK][sizeOfTags];

        this.forward(sentence, topK);

        int[][] bestKSequence = new int[topK][lenOfSentence];

        for (int rank = 0; rank < topK; ++rank) {
            double KProb = Math.log(0);
            int rankIndex = -1, tagIndex = -1;

            for (int i = 0; i < topK; ++i) {
                for (int j = 0; j < sizeOfTags; ++j) {
                    if (this.lastLayerProbs[i][j] >= KProb) {
                        rankIndex = i;
                        tagIndex = j;
                        KProb = this.lastLayerProbs[i][j];
                    }
                }
            }

            bestKSequence[rank] = this.backTrack(rankIndex, tagIndex);
            this.lastLayerProbs[rankIndex][tagIndex] = Math.log(0);
        }

        return bestKSequence;
    }

    protected void forward(String sentence, int topK) {
        String[] words = sentence.trim().split("\\s+");
        int lenOfSentence = words.length;
        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();

        //三维：句子长度，k，标注集大小，该三维数组用以记录viterbi产生的中间概率，这个概率是发射过后的概率，而不仅仅是转移后的概率
        double[][][] midProb = new double[lenOfSentence][topK][sizeOfTags];

        //计算初始的发射概率，不用记录最大概率索引
        for (int tagIndex = 0; tagIndex < sizeOfTags; ++tagIndex) {
            //句首的未登录词处理，log(1)=0
            double launchProb = 0;
            if (this.hmmParas.getDictionary().getWordId(words[0]) != null) {
                //发射概率
                launchProb = Math.log(this.hmmParas.getProbB(false, tagIndex, this.hmmParas.getDictionary().getWordId(words[0])));
            } else {
                launchProb = Math.log(this.hmmParas.unkZXF(null,tagIndex));//边界上的观察态如何给定:null, tagIndex
            }
            double val = Math.log(this.hmmParas.getProbPi(tagIndex)) + launchProb;
            //为k次最优赋予相同的初始发射概率
            for (int rank = 0; rank < topK; ++rank) {
                midProb[0][rank][tagIndex] = val;
            }
        }

        //句子在时序上遍历
        for (int wordIndex = 1; wordIndex < lenOfSentence; ++wordIndex) {

            //将要转移的下一个隐藏状态
            for (int currTag = 0; currTag < sizeOfTags; ++currTag) {

                //记录下一个隐藏状态固定的情况下，所有k*sizeOfTags个概率
                double[][] tempArr = new double[topK][sizeOfTags];

                for (int rank = 0; rank < topK; ++rank) {
                    //转移前的隐藏状态
                    for (int preTag = 0; preTag < sizeOfTags; ++preTag) {
                        tempArr[rank][preTag] = midProb[wordIndex - 1][rank][preTag] + Math.log(this.hmmParas.getProbA(true, preTag, currTag));
                    }
                }

                //在下一个状态固定的情况下，找到当前ranks*sizeOfTags中最优的ranks个概率
                double[] repeatedProbs = new double[topK];
                for (int countK = 0; countK < topK; ++countK) {

                    double maxProb = Math.log(0);
                    int max_i = -1, max_j = -1;

                    for (int rank2 = 0; rank2 < topK; ++rank2) {
                        for (int tagIndex = 0; tagIndex < sizeOfTags; ++tagIndex) {
                            /*
                            在序列的t_0节点，各个rank下同一个currTag的概率是一样的
                            此后t_1到t_n-1上，存在不同rank下相同状态转移关系[preTag-->currTag]上概率一样的情况，而且这种一样的概率会沿着序列进行传播，从而导致k个最优序列结果是一样的
                            所以，在确定的[preTag-->currTag]下，在大小为[topK*sizeOfTags]的tempArr上，每找到一个最优概率，都要判断这个概率值是否已经被找到过
                            问题：是否会发生二阶HMM的溢出问题？
                             */
                            if (maxProb <= tempArr[rank2][tagIndex] && !this.isRepeated(tempArr[rank2][tagIndex], repeatedProbs)) {
                                maxProb = tempArr[rank2][tagIndex];
                                max_i = rank2;
                                max_j = tagIndex;
                            }

                        }

                    }
                    //回溯用中间数组含义：记录下当前隐藏状态currTag下第countK优的概率对应的上一个k和隐藏状态
                    this.backTrackTool[wordIndex - 1][countK][currTag] = new int[]{max_i, max_j};

                    //状态发射时的未登录词处理
                    double launchProb = 0;
                    if (this.hmmParas.getDictionary().getWordId(words[wordIndex]) != null) {
                        launchProb = Math.log(this.hmmParas.getProbB(false, currTag, this.hmmParas.getDictionary().getWordId(words[wordIndex])));
                    } else {
                        launchProb = Math.log(this.hmmParas.unkZXF(words[wordIndex-1],currTag));//words[wordIndex-1], currTag
                    }
                    midProb[wordIndex][countK][currTag] = maxProb + launchProb;

                    //排除已找到的最大概率
                    repeatedProbs[countK] = tempArr[max_i][max_j];
//                    tempArr[max_i][max_j]=Math.log(0);
                }
            }
        }

        //只需要最后一层每个隐藏状态的k个最优概率
        this.lastLayerProbs = midProb[lenOfSentence - 1];
    }

    /**
     * 判断概率probability，是否存在于概率集合repeatedProbs中
     */
    private boolean isRepeated(double probability, double[] repeatedProbs) {
        for (double p : repeatedProbs) {
            if (probability == p) {
                return true;
            }
        }
        return false;
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
        int[] maxRow = new int[]{rank, lastTagIndexs[0]};

        for (int col = wordLen - 2; col >= 0; --col) {
            maxRow = this.backTrackTool[col][maxRow[0]][maxRow[1]];
            //只取固定的rank下的一个标注序列
            tagIds[col] = maxRow[1];
        }
        return tagIds;
    }
}
