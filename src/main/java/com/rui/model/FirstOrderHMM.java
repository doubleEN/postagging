package com.rui.model;

import com.rui.wordtag.WordTag;
import com.rui.parameter.AbstractParas;

import static com.rui.util.GlobalParas.logger;
import java.util.Arrays;

/**
 * 一阶HMM实现。
 */
public class FirstOrderHMM extends HMM {

    public static void main(String[] args) {
    }

    /**
     * 记录k次viterbi解码中计算得到的句子概率
     */
    private double[][] rankProbs;

    /**
     * 解码时的辅助数组
     */
    private int[][][] indexs;

    public FirstOrderHMM(AbstractParas hmmParas) {
        this.hmmParas = hmmParas;
    }

    @Override
    public int[][] decode(String sentence, int k) {
        String[] words = sentence.trim().split("\\s+");
        int wordLen = words.length;
        int tagSize = this.hmmParas.getDictionary().getSizeOfTags();
        //tagIds记录k个最优的标注序列
        int[][] tagIds = new int[k][wordLen];
        //rankProbs记录每次句子概率计算时，每个标注下的最大概率
        this.rankProbs = new double[k][tagSize];
        //indexs记录每次句子概率计算时，每个标注下的最大概率对应的整个标注
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
                //k次计算句子的概率的步骤都一样，不一样的是每次转移概率取的第k大的概率
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

        //取出最后一列的最大概率并返回，这个概率要放入全部概率中进行排序
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
            logger.severe("回溯参数不合法。");
            System.exit(1);
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
}
