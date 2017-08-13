package com.rui.model;

import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;

import java.io.IOException;
import java.util.Arrays;

/**
 * 一阶HMM实现。
 */
public class FirstOrderHMM extends HMM {

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        AbstractParas paras = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 44, 50000);
        HMM hmm = new FirstOrderHMM(paras);

        Tagger tagger = new Tagger(hmm);
        //[中共中央/nt, 总书记/n, 、/w, 国家/n, 主席/n, 江/nr, 泽民/nr]
//        WordTag[] wts=tagger.tag("中共中央 总书记 、 国家 主席 江 泽民");
        WordTag[] wts=tagger.tag("新华社 伦敦 １月 ９日 电 在 纽约 华尔街 股市 和 亚洲 金融 市场 动荡 的 影响 下 ， 欧洲 三 大 主要 股市 ９日 均 出现 大幅度 下跌 。 伦敦 股市 《 金融 时报 》 １００ 种 股票 平均 价格 指数 当天 下跌 ９８．８ 点 ， 以 ５１３８．３ 点 报收 。 德国 法兰克福 股市 ＤＡＸ ３０ 种 股票 价格 指数 下泻 １１０．２９ 点 ， 收 于 ４２３６．９４ 点 。 法国 巴黎 股市 ＣＡＣ ４０ 种 股票 价格 指数 下跌 ３５．１３ 点 ， 以 ２９１９．８１ 点 收盘 。");
        System.out.println(Arrays.toString(wts));
        hmm.writeHMM("/home/mjx/桌面/hmm.bin");
    }

//    //记录k次viterbi解码中计算得到的句子概率
//    private double[][] rankProbs;

    //记录k次viterbi解码中计算得到的句子概率
    private double[][] rankProbs;

    //解码时的辅助数组
    private int[][][] indexs;

    public FirstOrderHMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    public int[][] decode(String sentence, int k) {
        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getDictionary().getSizeOfTags();
        int[][] tagIds = new int[k][wordLen];

        this.rankProbs = new double[k][tagSize];
        //解码用到中间数组
        this.indexs = new int[k][tagSize][wordLen];

        //计算需要的句子概率
        for (int rank = 1; rank <= k; ++rank) {
            this.forward(sentence, rank);
        }


        WordTag[][] wts = new WordTag[k][wordLen];
        //找到k个概率最大的句子并解码
        for (int rank = 1; rank <= k; ++rank) {
            int index_i = -1;
            int index_j = -1;
            double currProb = Math.log(0);

            //找到当前概率最大的索引
            for (int i = 0; i < k; ++i) {
                for (int j = 0; j < tagSize; ++j) {
                    if (this.rankProbs[i][j] >=currProb) {
                        currProb = this.rankProbs[i][j];
                        index_i = i;
                        index_j = j;
                    }
                }
            }
            this.rankProbs[index_i][index_j] = Math.log(0);
            //index_i为第index_i次前向算法，index_j为index_i次前向算法中的某个概率索引
            tagIds[rank - 1] = this.backTrack(index_i, index_j);
        }
        return tagIds;
    }

    @Override
    public void forward(String sentence, int ranking) {
        String[] words = sentence.split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getDictionary().getSizeOfTags();
        //索引数组差概率数组一位
        double[][] sentencesProb = new double[tagSize][wordLen];
        int[][] indexs = new int[tagSize][wordLen];

        //带入初始状态计算第一个观察态概率，不用记录最大值索引
        for (int i = 0; i < tagSize; ++i) {
            //句首的未登录词处理
            double launchProb = 0;
            if (this.hmmParas.getDictionary().getWordId(words[0]) != null) {
                launchProb = Math.log(this.hmmParas.getProbB(i, this.hmmParas.getDictionary().getWordId(words[0])));
            }
            sentencesProb[i][0] = Math.log(this.hmmParas.getProbPi(i)) + launchProb;
        }

        //外层循环：t(i)-->t(i+1)-->w(i+1)
        for (int wordIndex = 1; wordIndex < wordLen; ++wordIndex) {
            for (int nextTag = 0; nextTag < tagSize; ++nextTag) {
                int index = -1;
                //同一个转移后状态接受的概率存入数组
                double[] probs = new double[tagSize];

                for (int preTag = 0; preTag < tagSize; ++preTag) {
                    probs[preTag] = sentencesProb[preTag][wordIndex - 1] + Math.log(this.hmmParas.getProbSmoothA(preTag, nextTag));
                }
                double[] midProbs = Arrays.copyOf(probs, tagSize);
                Arrays.sort(midProbs);
                for (int i = 0; i < tagSize; ++i) {
                    if (probs[i] == midProbs[tagSize - ranking]) {
                        index = i;
                        break;
                    }
                }
                double launchProb = 0;
                //未登录词处理
                if (this.hmmParas.getDictionary().getWordId(words[wordIndex]) != null) {
                    launchProb = Math.log(this.hmmParas.getProbB(nextTag, this.hmmParas.getDictionary().getWordId(words[wordIndex])));
                }
                sentencesProb[nextTag][wordIndex] = midProbs[tagSize - ranking] + launchProb;
                indexs[nextTag][wordIndex - 1] = index;
            }
        }

        //取出最后一列的最大概率并返回
        double[] maxProbs = new double[tagSize];
        for (int i = 0; i < tagSize; ++i) {
            maxProbs[i] = sentencesProb[i][wordLen - 1];
        }
        this.indexs[ranking - 1] = indexs;
        this.rankProbs[ranking - 1] = maxProbs;
    }

    @Override
    public int[] backTrack(int ranking, int... lastIndexs) {
        if (lastIndexs.length != 1) {
            System.err.println("回溯参数不合法。");
            return null;
        }
        int wordLen = this.indexs[0][0].length;
        int[] tagIds = new int[wordLen];
        tagIds[wordLen - 1] = lastIndexs[0];
        int maxRow = lastIndexs[0];

        for (int col = wordLen - 2; col >= 0; --col) {
            maxRow = this.indexs[ranking][maxRow][col];
            tagIds[col] = maxRow;
        }
        return tagIds;
    }
//
//    @Override
//    public int[][] decode(String sentence, int k) {
//        String[] words = sentence.trim().split("\\s+");
//        int wordLen = words.length;
//        int tagSize = this.hmmParas.getDictionary().getSizeOfTags();
//        int[][] tagIds = new int[k][wordLen];
//
//        this.rankProbs = new double[k][tagSize];
//        //解码用到中间数组
//        this.indexs = new int[k][tagSize][wordLen];
//
//        //计算需要的句子概率
//        for (int rank = 1; rank <= k; ++rank) {
//            this.forward(sentence, rank);
//        }
//
//
//        WordTag[][] wts = new WordTag[k][wordLen];
//        //找到k个概率最大的句子并解码
//        for (int rank = 1; rank <= k; ++rank) {
//            int index_i = -1;
//            int index_j = -1;
//            double currProb = -0.1;
//
//            //找到当前概率最大的索引
//            for (int i = 0; i < k; ++i) {
//                for (int j = 0; j < tagSize; ++j) {
//                    if (this.rankProbs[i][j] > currProb) {
//                        currProb = this.rankProbs[i][j];
//                        index_i = i;
//                        index_j = j;
//                    }
//                }
//            }
//            this.rankProbs[index_i][index_j] = -1;
//            //index_i为第index_i次前向算法，index_j为index_i次前向算法中的某个概率索引
//            tagIds[rank - 1] = this.backTrack(index_i, index_j);
//        }
//        return tagIds;
//    }
//
//    @Override
//    public void forward(String sentence, int ranking) {
//        String[] words = sentence.split("\\s+");
//        int wordLen = words.length;
//        int tagSize = this.hmmParas.getSizeOfTags();
//        //索引数组差概率数组一位
//        double[][] sentencesProb = new double[tagSize][wordLen];
//        int[][] indexs = new int[tagSize][wordLen];
//
//        //带入初始状态计算第一个观察态概率，不用记录最大值索引
//        for (int i = 0; i < tagSize; ++i) {
//            //句首的未登录词处理
//            double launchProb = 0;
//            if (this.hmmParas.getWordId(words[0]) != null) {
//
//                launchProb = this.hmmParas.getProbB(i, this.hmmParas.getWordId(words[0]));
//            } else {
//                launchProb = 1;
//            }
//            sentencesProb[i][0] = this.hmmParas.getProbPi(i) * launchProb;
//        }
//
//        //外层循环：t(i)-->t(i+1)-->w(i+1)
//        for (int wordIndex = 1; wordIndex < wordLen; ++wordIndex) {
//            for (int nextTag = 0; nextTag < tagSize; ++nextTag) {
//                int index = -1;
//                //同一个转移后状态接受的概率存入数组
//                double[] probs = new double[tagSize];
//
//                for (int preTag = 0; preTag < tagSize; ++preTag) {
//                    probs[preTag] = sentencesProb[preTag][wordIndex - 1] * this.hmmParas.getProbSmoothA(preTag, nextTag);
//                }
//                double[] midProbs = Arrays.copyOf(probs, tagSize);
//                Arrays.sort(midProbs);
//
//                for (int i = 0; i < tagSize; ++i) {
//                    if (probs[i] == midProbs[tagSize - ranking]) {
//                        index = i;
//                        break;
//                    }
//                }
//                double launchProb = 0;
//                //未登录词处理
//                if (this.hmmParas.getWordId(words[wordIndex]) != null) {
//                    launchProb = this.hmmParas.getProbB(nextTag, this.hmmParas.getWordId(words[wordIndex]));
//                } else {
//                    launchProb = 1;
//                }
//                sentencesProb[nextTag][wordIndex] = midProbs[tagSize - ranking] * launchProb;
//                indexs[nextTag][wordIndex - 1] = index;
//            }
//        }
//
//        //取出最后一列的最大概率并返回
//        double[] maxProbs = new double[tagSize];
//        for (int i = 0; i < tagSize; ++i) {
//            maxProbs[i] = sentencesProb[i][wordLen - 1];
//        }
//        this.indexs[ranking - 1] = indexs;
//        System.out.println(Arrays.toString(maxProbs));
//        this.rankProbs[ranking - 1] = maxProbs;
//    }
//
//    @Override
//    public int[] backTrack(int ranking, int... lastIndexs) {
//        if (lastIndexs.length != 1) {
//            System.err.println("回溯参数不合法。");
//            return null;
//        }
//        int wordLen = this.indexs[0][0].length;
//        int[] tagIds = new int[wordLen];
//        tagIds[wordLen - 1] = lastIndexs[0];
//        int maxRow = lastIndexs[0];
//
//        for (int col = wordLen - 2; col >= 0; --col) {
//            maxRow = this.indexs[ranking][maxRow][col];
//            tagIds[col] = maxRow;
//        }
//        return tagIds;
//    }
}
