package com.rui.model;

import com.rui.parameters.AbstractParas;
import com.rui.parameters.TrigramParas;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.util.Arrays;

/**
 * 未支持句子词数小于３的标注。
 */
public class SecondOrderHMM extends HMM {

    public static void main(String[] args) {
        AbstractParas paras = new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 44, 55310);
        HMM hmm = new SecondOrderHMM(paras);
        Tagger tagger = new Tagger(hmm);
        //[学好/v, 自然/a] [中共中央/nt, 总书记/n, 、/w, 国家/n, 主席/n, 江/nr, 泽民/nr]
        WordTag[] wts = tagger.tag("前面 是 一 片 湖水 ， 湖边 是 一丛丛 冬眠 的 丁香 ， 有 几 只 小鸟 偶 从 这里 飞 起 ， 又 平缓 地 在 稍 远 的 地方 落 下 。 就 在 这 湖 的 旁边 ， 有 一 道 篱笆 ， 见 里面 有 灯火 又 闻 人声 ， 我 推开 柴门 走 了 进去 。 花畦 呈 东西 走向 ， 很 整齐 地 排列 着 。 走过 一 段 不 长 的 小径 ， 绕 过 一 道 围墙 ， 满园 高低 有序 的 菊花 映 入 我 的 眼帘 。");
        System.out.println(Arrays.toString(wts));
    }

//    //记录k次viterbi解码中计算得到的句子概率
//    private double[][][] rankProbs;

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

        if (wordLen == 1) {
            System.err.println(" 独词成句");
            return this.decodeOneWord(words[0], k);
        }

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
            double currProb = Math.log(0);

            //找到当前概率最大的索引
            for (int everyRank = 0; everyRank < k; ++everyRank) {
                for (int i = 0; i < tagSize; ++i) {
                    for (int j = 0; j < tagSize; ++j) {

                        if (this.rankProbs[everyRank][i][j] >= currProb) {
                            currProb = this.rankProbs[everyRank][i][j];
                            index_rank = everyRank;
                            index_i = i;
                            index_j = j;
                        }
                    }
                }
            }

            this.rankProbs[index_rank][index_i][index_j] = Math.log(0);
            //第rank大的概率为第index_rank次句子概率计算中的索引[index_i,index_j]
            tagIds[rank - 1] = this.backTrack(index_rank, index_i, index_j);
        }
        return tagIds;
    }

    //独词成句处理
    public int[][] decodeOneWord(String word, int k) {
        int[][] tags = new int[k][1];

        int tagSize = this.hmmParas.getSizeOfTags();
        int tagId = -1;
        if (this.hmmParas.getWordId(word) == null) {
            double maxProb = -1;
            int maxIndex = -1;
            for (int id = 0; id < tagSize; ++id) {
                if (this.hmmParas.getProbPi(id) > maxProb) {
                    maxIndex = id;
                    maxProb = this.hmmParas.getProbPi(id);
                }

            }
            tagId = maxIndex;
        } else {
            int wordId = this.hmmParas.getWordId(word);
            double maxProb = -1;
            int maxIndex = -1;
            for (int i = 0; i < tagSize; ++i) {
                double probs = -1;
                probs = this.hmmParas.getProbPi(i) * this.hmmParas.getProbB(i, wordId);
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
                double launchProb = 0;
                if (this.hmmParas.getWordId(words[0]) != null) {
                    launchProb = Math.log(this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[0])));
                }
                sentenceProb[tag_j][tag_k][0] = Math.log(this.hmmParas.getProbPi(tag_k)) + launchProb;
            }
        }
        //处理t_2上的状态
        for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
                double launchProb = 0;
                if (this.hmmParas.getWordId(words[1]) != null) {
                    launchProb = Math.log(this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[1])));
                }
                sentenceProb[tag_j][tag_k][1] = sentenceProb[0][tag_j][0] +Math.log(this.hmmParas.getProbSmoothA(tag_j, tag_k))+ launchProb;
            }
        }

        //处理t_2以后的状态
        //O(wordLen*tagSize*tagSize*tagSize)
        for (int wordIndex = 2; wordIndex < wordLen; ++wordIndex) {
            //probs记录了第三个状态固定的情况下，前两个状态不同取值下的概率
            double[] probs = new double[tagSize];
            double[] midProbs = new double[tagSize];

            //排列无重复
            //三维数组先固定二维
            //一个三元：tag_i,tag_j,tag_k
            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
                //再固定三维
                for (int tag_k = 0; tag_k < tagSize; ++tag_k) {

                    for (int tag_i = 0; tag_i < tagSize; ++tag_i) {
                        probs[tag_i] = sentenceProb[tag_i][tag_j][wordIndex - 1] +Math.log(this.hmmParas.getProbSmoothA(tag_i, tag_j, tag_k));
                    }
                    midProbs = Arrays.copyOf(probs, tagSize);
                    Arrays.sort(midProbs);
                    int i = -1;
                    for (int row = 0; row < tagSize; ++row) {
                        if (probs[row] == midProbs[tagSize - ranking]) {
                            i = row;
                            break;
                        }
                    }
                    double launchProb = 0;
                    if (this.hmmParas.getWordId(words[wordIndex]) != null) {
                        launchProb = Math.log(this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[wordIndex])));
                    }
                    sentenceProb[tag_j][tag_k][wordIndex] = midProbs[tagSize - ranking] + launchProb;
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
    protected int[] backTrack(int ranking, int... lastIndexs) {
        if (lastIndexs.length != 2) {
            System.err.println("回溯参数不合法。");
            return null;
        }
        int wordLen = this.indexs[ranking][0][0].length;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 2] = lastIndexs[0];
        tagIds[wordLen - 1] = lastIndexs[1];
        int max_j = lastIndexs[0];
        int max_k = lastIndexs[1];
        for (int col = wordLen - 3; col >= 0; --col) {
            int max_i = this.indexs[ranking][max_j][max_k][col + 1];
            max_k = max_j;
            max_j = max_i;
            tagIds[col] = max_i;
        }
        return tagIds;
    }


//    @Override
//    public int[][] decode(String sentence, int k) {
//        String[] words = sentence.trim().split("\\s+");
//        int wordLen = words.length;
//        int tagSize = this.hmmParas.getSizeOfTags();
//
//        if (wordLen == 1) {
//            System.err.println(" 独词成句");
//            return this.decodeOneWord(words[0], k);
//        }
//
//        //存储最可能的标注集
//        int[][] tagIds = new int[k][wordLen];
//
//        this.rankProbs = new double[k][tagSize][tagSize];
//        //解码用到中间数组
//        this.indexs = new int[k][tagSize][tagSize][wordLen];
//
//        //计算需要的句子概率
//        for (int rank = 1; rank <= k; ++rank) {
//            this.forward(sentence, rank);
//        }
//
//        WordTag[][] wts = new WordTag[k][wordLen];
//        //找到k个概率最大的句子并解码
//        for (int rank = 1; rank <= k; ++rank) {
//            int index_rank = -1;
//            int index_i = -1;
//            int index_j = -1;
//            double currProb = -0.1;
//
//            //找到当前概率最大的索引
//            for (int everyRank = 0; everyRank < k; ++everyRank) {
//                for (int i = 0; i < tagSize; ++i) {
//                    for (int j = 0; j < tagSize; ++j) {
//
//                        if (this.rankProbs[everyRank][i][j] > currProb) {
//                            currProb = this.rankProbs[everyRank][i][j];
//                            index_rank = everyRank;
//                            index_i = i;
//                            index_j = j;
//                        }
//                    }
//                }
//            }
//
//            this.rankProbs[index_rank][index_i][index_j] = -1;
//
//            //第rank大的概率为第index_rank次句子概率计算中的索引[index_i,index_j]
//            tagIds[rank - 1] = this.backTrack(index_rank, index_i, index_j);
//        }
//        return tagIds;
//    }
//
//    //独词成句处理
//    public int[][] decodeOneWord(String word, int k) {
//        int[][] tags = new int[k][1];
//
//        int tagSize = this.hmmParas.getSizeOfTags();
//        int tagId = -1;
//        if (this.hmmParas.getWordId(word) == null) {
//            double maxProb = -1;
//            int maxIndex = -1;
//            for (int id = 0; id < tagSize; ++id) {
//                if (this.hmmParas.getProbPi(id) > maxProb) {
//                    maxIndex = id;
//                    maxProb = this.hmmParas.getProbPi(id);
//                }
//
//            }
//            tagId = maxIndex;
//        } else {
//            int wordId = this.hmmParas.getWordId(word);
//            double maxProb = -1;
//            int maxIndex = -1;
//            for (int i = 0; i < tagSize; ++i) {
//                double probs = -1;
//                probs = this.hmmParas.getProbPi(i) * this.hmmParas.getProbB(i, wordId);
//                if (maxProb < probs) {
//                    maxIndex = i;
//                    maxProb = probs;
//                }
//            }
//            //可以获得不同的初始概率
//            tagId = maxIndex;
//        }
//
//        for (int i = 0; i < k; ++i) {
//            tags[i][0] = tagId;
//        }
//        return tags;
//    }
//
//
//    @Override
//    protected int[] backTrack(int ranking, int... lastIndexs) {
//        if (lastIndexs.length != 2) {
//            System.err.println("回溯参数不合法。");
//            return null;
//        }
//        int wordLen = this.indexs[ranking][0][0].length;
//        int[] tagIds = new int[wordLen];
//        tagIds[wordLen - 2] = lastIndexs[0];
//        tagIds[wordLen - 1] = lastIndexs[1];
//        int max_j = lastIndexs[0];
//        int max_k = lastIndexs[1];
//        for (int col = wordLen - 3; col >= 0; --col) {
//            int max_i = this.indexs[ranking][max_j][max_k][col + 1];
//            max_k = max_j;
//            max_j = max_i;
//            tagIds[col] = max_i;
//        }
//        return tagIds;
//    }
//    @Override
//    protected void forward(String sentence, int ranking) {
//        String[] words = sentence.split("\\s+");
//        int tagSize = this.hmmParas.getSizeOfTags();
//        int wordLen = words.length;
//        //用以记录中间最大概率的数组
//        double[][][] sentenceProb = new double[tagSize][tagSize][wordLen];
//        //回溯解码用的中间数组:索引包含了j与k，存储的值是i
//        int[][][] indexs = new int[tagSize][tagSize][wordLen];
//
//        //处理t_1上的状态
//        for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
//            //第三个状态tag_k固定的情况下，不同的tag_j组合是一样的
//            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
//                double launchProb = 0;
//                if (this.hmmParas.getWordId(words[0]) != null) {
//                    launchProb = this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[0]));
//                } else {
//                    launchProb = 1;
//                }
//                sentenceProb[tag_j][tag_k][0] = this.hmmParas.getProbPi(tag_k) * launchProb;
//            }
//        }
//        //处理t_2上的状态
//        for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
//            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
//                double launchProb = 0;
//                if (this.hmmParas.getWordId(words[1]) != null) {
//                    launchProb = this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[1]));
//                } else {
//                    launchProb = 1;
//                }
//                sentenceProb[tag_j][tag_k][1] = sentenceProb[0][tag_j][0] * this.hmmParas.getProbSmoothA(tag_j, tag_k) * launchProb;
//            }
//        }
//
//        //处理t_2以后的状态
//        //O(wordLen*tagSize*tagSize*tagSize)
//        for (int wordIndex = 2; wordIndex < wordLen; ++wordIndex) {
//            //probs记录了第三个状态固定的情况下，前两个状态不同取值下的概率
//            double[] probs = new double[tagSize];
//            double[] midProbs = new double[tagSize];
//
//            //排列无重复
//            //三维数组先固定二维
//            //一个三元：tag_i,tag_j,tag_k
//            for (int tag_j = 0; tag_j < tagSize; ++tag_j) {
//                //再固定三维
//                for (int tag_k = 0; tag_k < tagSize; ++tag_k) {
//
//                    for (int tag_i = 0; tag_i < tagSize; ++tag_i) {
//                        probs[tag_i] = sentenceProb[tag_i][tag_j][wordIndex - 1] * this.hmmParas.getProbSmoothA(tag_i, tag_j, tag_k);
//                    }
//                    midProbs = Arrays.copyOf(probs, tagSize);
//                    Arrays.sort(midProbs);
//                    int i = -1;
//                    for (int row = 0; row < tagSize; ++row) {
//                        if (probs[row] == midProbs[tagSize - ranking]) {
//                            i = row;
//                            break;
//                        }
//                    }
//                    double launchProb = 0;
//                    if (this.hmmParas.getWordId(words[wordIndex]) != null) {
//                        launchProb = this.hmmParas.getProbB(tag_k, this.hmmParas.getWordId(words[wordIndex]));
//                    } else {
//                        launchProb = 1;
//                    }
//                    sentenceProb[tag_j][tag_k][wordIndex] = midProbs[tagSize - ranking] * launchProb;
//                    indexs[tag_j][tag_k][wordIndex - 1] = i;
//                }
//            }
//        }
//
//        //取出最后一列的概率并返回，最后一列由两个隐藏态组成
//        double[][] maxProbs = new double[tagSize][tagSize];
//        for (int i = 0; i < tagSize; ++i) {
//            for (int j = 0; j < tagSize; ++j) {
//                maxProbs[i][j] = sentenceProb[i][j][wordLen - 1];
//            }
//        }
//        this.indexs[ranking - 1] = indexs;
//        this.rankProbs[ranking - 1] = maxProbs;
//    }

}