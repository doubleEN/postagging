package com.rui.model;

import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 二阶隐马尔科夫。
 */
public class HMM2nd extends HMM {

    /**
     * 回溯中间索引数组
     */
    private int[][][][][] backTrackTool;

    /**
     * 序列上最后一个节点上每个隐藏状态对应的k个最优概率值
     */
    private double[][][] lastLayerProbs;

    public HMM2nd(AbstractParas paras) {
        this.hmmParas = paras;
    }

    @Override
    public int[][] decode(String[] words, int topK) {
        //句长
        int lenOfSentence = words.length;
        //隐藏状态数
        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();

        if (lenOfSentence == 1) {
            logger.info(" 独词成句<" + Arrays.toString(words) + ">");
            return this.decodeOneWord(words[0], topK);
        }

        //回溯工具数组的长度为[句长-2]
        this.backTrackTool = new int[lenOfSentence - 2][topK][sizeOfTags][sizeOfTags][2];
        this.lastLayerProbs = new double[topK][sizeOfTags][sizeOfTags];

        //计算句子概率
        this.forward(words, topK);

        //回溯解码得到topK个最优序列
        int[][] bestKSequence = new int[topK][lenOfSentence];
        for (int rankCount = 0; rankCount < topK; ++rankCount) {
            double maxProb = Math.log(0);
            int max_j = -1, max_k = -1, max_rank = -1;

            for (int rank = 0; rank < topK; ++rank) {
                for (int tag_j = 0; tag_j < sizeOfTags; ++tag_j) {
                    for (int tag_k = 0; tag_k < sizeOfTags; ++tag_k) {
                        if (maxProb <= this.lastLayerProbs[rank][tag_j][tag_k]) {
                            maxProb = this.lastLayerProbs[rank][tag_j][tag_k];
                            max_rank = rank;
                            max_j = tag_j;
                            max_k = tag_k;
                        }
                    }
                }
            }

            bestKSequence[rankCount] = this.backTrack(max_rank, max_j, max_k);
            this.lastLayerProbs[max_rank][max_j][max_k] = Math.log(0);
        }
        return bestKSequence;
    }

    /**
     * 独词成句概率处理
     *
     * @param word 独词成句的词
     * @param k    k个最优标注
     * @return k个最优标注对应的标注id
     */
    public int[][] decodeOneWord(String word, int k) {
        int[][] tags = new int[k][1];

        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();
        int tagId = -1;
        if (this.hmmParas.getDictionary().getWordId(word) == null) {
            double maxProb = -1;
            int maxIndex = -1;
            for (int id = 0; id < sizeOfTags; ++id) {
                if (this.hmmParas.getProbPi(id) > maxProb) {
                    maxIndex = id;
                    maxProb = this.hmmParas.getProbPi(id);
                }

            }
            tagId = maxIndex;
        } else {
            int wordId = this.hmmParas.getDictionary().getWordId(word);
            double maxProb = -1;
            int maxIndex = -1;
            for (int i = 0; i < sizeOfTags; ++i) {
                double probs = -1;
                probs = this.hmmParas.getProbPi(i) * this.hmmParas.getProbB(false,i, wordId);
                if (maxProb < probs) {
                    maxIndex = i;
                    maxProb = probs;
                }
            }
            //可以获得不同的初始概率
            tagId = maxIndex;
        }

        for (int i = 0; i < k; ++i) {
            tags[i][0] = tagId;
        }
        return tags;
    }

    @Override
    protected void forward(String[] words, int topK) {
        int lenOfSentence = words.length;
        int sizeOfTags = this.hmmParas.getDictionary().getSizeOfTags();

        //四维：句子长度，k，标注集大小，标注集大小。
        // 该四维数组用以记录viterbi产生的中间概率，这个概率是发射过后的概率，而不仅仅是转移后的概率
        //序列每一个点上保存的是一个二元组[],而不是一阶解码中的一元
        double[][][][] midProb = new double[lenOfSentence][topK][sizeOfTags][sizeOfTags];

        //处理t_1上的状态
        //当t_1时，三元[i,j,k]中，i和j在句子左边界外，此时，k就是句首，对于同一个k，不同topK下的i,j组合是一样的
        for (int tag_k = 0; tag_k < sizeOfTags; ++tag_k) {
            for (int tag_j = 0; tag_j < sizeOfTags; ++tag_j) {
                double launchProb = 0;
                if (this.hmmParas.getDictionary().getWordId(words[0]) != null) {
                    launchProb = Math.log(this.hmmParas.getProbB(false,tag_k, this.hmmParas.getDictionary().getWordId(words[0])));
                } else {
                    launchProb=Math.log(this.hmmParas.getUnkProb(null,tag_k));
                }

                double val = Math.log(this.hmmParas.getProbPi(tag_k)) + launchProb;

                //t_1时，不同topK，tag_j下得到的发射概率都是一样的
                for (int rank = 0; rank < topK; ++rank) {
                    midProb[0][rank][tag_j][tag_k] = val;
                }
            }
        }

        //处理t_2上的状态
        //在t_1时，不同topK，tag_j下得到的发射概率都是一样的，所以在处理t_2时，是按二元概率转移来计算的
        for (int tag_k = 0; tag_k < sizeOfTags; ++tag_k) {
            for (int tag_j = 0; tag_j < sizeOfTags; ++tag_j) {

                double launchProb = 0;
                if (this.hmmParas.getDictionary().getWordId(words[1]) != null) {
                    launchProb = Math.log(this.hmmParas.getProbB(false, tag_k, this.hmmParas.getDictionary().getWordId(words[1])));
                } else {
                    launchProb=Math.log(this.hmmParas.getUnkProb(words[1],tag_k));
                }

                for (int rank = 0; rank < topK; ++rank) {
                    midProb[1][rank][tag_j][tag_k] = midProb[0][rank][0][tag_j] + Math.log(this.hmmParas.getProbA(true,tag_j, tag_k)) + launchProb;
                }
            }
        }

        //处理t_2以后的状态
        //O(topK*sizeOfTags*sizeOfTags*sizeOfTags*sizeOfTags)
        for (int wordIndex = 2; wordIndex < lenOfSentence; ++wordIndex) {
            //一个三元：tag_i,tag_j,tag_k
            for (int tag_k = 0; tag_k < sizeOfTags; ++tag_k) {


                for (int tag_j = 0; tag_j < sizeOfTags; ++tag_j) {

                    //记录tag_j,tag_k固定的情况下，所有 k*sizeOfTags 个概率
                    double[][] tempArr = new double[topK][sizeOfTags];
                    for (int rank = 0; rank < topK; ++rank) {
                        for (int tag_i = 0; tag_i < sizeOfTags; ++tag_i) {
                            //当tag_j,tag_k固定的情况下，计算并记录转移到tag_k的前topK个概率
                            //概率转移：[i,j]--> k

                            tempArr[rank][tag_i] = midProb[wordIndex - 1][rank][tag_i][tag_j] + Math.log(this.hmmParas.getProbA(true,tag_i, tag_j, tag_k));
                        }
                    }

                    for (int rankCount = 0; rankCount < topK; ++rankCount) {

                        double maxProb = Math.log(0);
                        int max_i = -1, max_rank = -1;

                        for (int rank2 = 0; rank2 < topK; ++rank2) {
                            for (int i = 0; i < sizeOfTags; ++i) {

                                if (maxProb <= tempArr[rank2][i]) {
                                    maxProb = tempArr[rank2][i];
                                    max_rank = rank2;
                                    max_i = i;
                                }
                            }
                        }

                        //回溯用中间数组含义：记录下当前隐藏状态[tag_j][tag_k]下第rankCount优的概率对应的上一个k和隐藏状态
                        this.backTrackTool[wordIndex - 2][rankCount][tag_j][tag_k] = new int[]{max_rank, max_i};

                        //状态发射时的未登录词处理
                        double launchProb = 0;
                        if (this.hmmParas.getDictionary().getWordId(words[wordIndex]) != null) {
                            launchProb = Math.log(this.hmmParas.getProbB(false,tag_k, this.hmmParas.getDictionary().getWordId(words[wordIndex])));
                        } else {
                            launchProb=Math.log(this.hmmParas.getUnkProb(words[wordIndex-1],tag_k));
                        }
                        midProb[wordIndex][rankCount][tag_j][tag_k] = maxProb + launchProb;

                        //重构版本为什么总是在最后一个节点向下溢出
                        for (int i=0;i<topK;++i) {
                            for (int j=0;j<sizeOfTags;++j) {
                                if (tempArr[max_rank][max_i] == tempArr[i][j]) {
                                    //排除已找到的最大概率
                                    tempArr[i][j]=Math.log(0);
                                }
                            }
                        }
                    }
                }
            }
        }
        //只需要最后一层每个隐藏状态的k个最优概率
        this.lastLayerProbs = midProb[lenOfSentence - 1];
    }

    @Override
    protected int[] backTrack(int rank, int... lastTagIndexs) {
        if (lastTagIndexs.length != 2) {
            logger.severe("回溯参数不合法。");
            System.exit(1);
        }
        int lenOfSentence = this.backTrackTool.length + 2;
        int[] tagIds = new int[lenOfSentence];
        tagIds[lenOfSentence - 2] = lastTagIndexs[0];
        tagIds[lenOfSentence - 1] = lastTagIndexs[1];
        int max_j = lastTagIndexs[0];
        int max_k = lastTagIndexs[1];
        int max_rank = rank;
        for (int col = lenOfSentence - 3; col >= 0; --col) {
            int[] max_rank_i = this.backTrackTool[col][max_rank][max_j][max_k];
            tagIds[col] = max_rank_i[1];
            max_k = max_j;
            max_j = max_rank_i[1];
            max_rank = max_rank_i[0];
        }
        return tagIds;
    }
}
