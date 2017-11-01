package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.WordTagStream;

import java.io.IOException;
import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 二元语法参数训练。
 */
public class BigramParas extends AbstractParas {

    /**
     * 状态转移计数矩阵
     * 每一行为同一个隐藏转移状态下，转移到可能的下一个隐藏状态的计数，即[t_i]-->[t_i+1]
     */
    private int[][] numMatA;

    /**
     * 留存状态转移计数矩阵
     * 每一行为同一个隐藏转移状态下，转移到可能的下一个隐藏状态的计数，即[t_i]-->[t_i+1]
     */
    private int[][] holdOut;

    /**
     * 概率参数
     * 每一行为同一个隐藏转移状态下，转移到可能的下一个隐藏状态的概率，即 p([t_i]-->[t_i+1])
     */
    private double[][] probMatA;

    /**
     * 状态转移平滑概率矩阵
     * 对probMatA的平滑
     */
    private double[][] smoothingMatA;

    public BigramParas() {
        this.dictionary = new DictFactory();
        this.holdOut = new int[1][1];
        this.numMatA = new int[1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
    }


    public BigramParas(DictFactory dict) {
        this.dictionary = dict;
        this.numMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.holdOut = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.numMatB = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfWords()];
        this.numPi = new int[this.dictionary.getSizeOfTags()];
    }

    /**
     * @param stream 指明特点语料路径的语料读取流
     */
    public BigramParas(WordTagStream stream,int holdOutRatio) throws IOException {
        this.dictionary = DictFactory.generateDict(stream);
        stream.openReadStream();
        this.numMatA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.holdOut = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        this.numMatB = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfWords()];
        this.numPi = new int[this.dictionary.getSizeOfTags()];
        this.initParas(stream,holdOutRatio);
    }

    @Override
    protected void countMatA(String[] tags) {
        for (int i = 1; i < tags.length; i++) {
            this.numMatA[this.dictionary.getTagId(tags[i - 1])][this.dictionary.getTagId(tags[i])]++;
        }
    }

    //+1平滑会引入偏差
    @Override
    protected void smoothMatB() {
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                ++this.numMatB[i][j];
            }
        }
    }

    /**
     * 留存数据处理
     */
    @Override
    public void addHoldOut(WordTag[] wts) {
        this.smoothFlag=true;
        this.dictionary.addIndex(wts);
        for (int i = 1; i < wts.length; i++) {
            this.holdOut[this.dictionary.getTagId(wts[i - 1].getTag())][this.dictionary.getTagId(wts[i].getTag())]++;
        }
    }

    /*
        计算概率参数
        注：概率矩阵的大小与映射词典的对应长度是一致的，小于或等于计数矩阵的大小。
    */
    @Override
    protected void calcProbA() {
        int len = this.dictionary.getSizeOfTags();

        this.probMatA = new double[len][len];

        for (int row = 0; row < len; ++row) {

            double sumPerRow = 0;
            for (int col = 0; col < len; ++col) {
                sumPerRow += this.numMatA[row][col];
            }

            for (int col = 0; col < len; ++col) {
                if (sumPerRow != 0) {
                    this.probMatA[row][col] = (this.numMatA[row][col]) / (sumPerRow);
                } else {
                    this.probMatA[row][col] = 0.0;
                }
            }
        }
    }

    @Override
    protected void smoothMatA() {

        int len = this.dictionary.getSizeOfTags();

        this.smoothingMatA = new double[len][len];

        double lambd_count1 = 0.0;
        double lambd_count2 = 0.0;

        double sumOfTag = 0.0;
        double[] vector = new double[len];
        double sumOfRow = 0.0;

        for (int row = 0; row < len; ++row) {
            for (int num : this.holdOut[row]) {
                sumOfRow += num;
                sumOfTag += num;
            }
            vector[row] = sumOfRow;
            sumOfRow = 0;
        }

        if (sumOfTag == 0) {
            logger.severe("留存数据不存在,不能平滑概率。");
            return;
        }

        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                int t_1_2 = this.holdOut[t_1][t_2];

                double expression1 = (vector[t_2] - 1) / (sumOfTag - 1);
                double expression2 = 0.0;

                if (vector[t_1] - 1 != 0) {
                    expression2 = (t_1_2 - 1) / (vector[t_1] - 1);
                }

                if (expression1 > expression2) {
                    lambd_count1 += t_1_2;
                } else {
                    lambd_count2 += t_1_2;
                }
            }
        }

        double lambd1 = lambd_count1 / (lambd_count1 + lambd_count2);
        double lambd2 = lambd_count2 / (lambd_count1 + lambd_count2);
        logger.info("系数：" + lambd1 + "-" + lambd2);
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                this.smoothingMatA[t_1][t_2] = lambd1 * this.probPi[t_2] + lambd2 * this.probMatA[t_1][t_2];
            }
        }
    }

    /**
     * 参数访问接口的实现
     */
    @Override
    public double getProbPi(int indexOfTag) {
        return this.probPi[indexOfTag];
    }

    @Override
    public double getProbB(boolean isSmooth, int indexOfTag, int indexOfWord) {
        if (isSmooth) {
            return this.probMatB[indexOfTag][indexOfWord];
        } else {
            return this.probMatB[indexOfTag][indexOfWord];
        }
    }

    @Override
    public double getProbA(boolean isSmooth, int... tagIndex){
        if (tagIndex.length != 2) {
            logger.severe("获取转移概率参数不合法。");
            System.exit(1);
        }
        if (isSmooth) {
            if (this.smoothFlag) {
                return this.smoothingMatA[tagIndex[0]][tagIndex[1]];
            } else {
//                logger.severe("未构造留存信息,返回未平滑概率替代。");
                return this.probMatA[tagIndex[0]][tagIndex[1]];
            }
        } else {
            return this.probMatA[tagIndex[0]][tagIndex[1]];
        }
    }

    @Override
    public double unkInitProb(int currTag) {
        return this.probPi[currTag];
    }

    @Override
    public double unkLaplace() {
        return 1.0;
    }

    @Override
    public double unkZXF(String preWord, int currTag) {
        if (preWord == null) {
            return 1.0;
        }
        //前一个词的频数
        double word_i = 0.0;
        if (this.dictionary.getWordId(preWord) == null) {
            word_i = this.dictionary.getSizeOfTags();
        } else {
            for (int tag = 0; tag < this.dictionary.getSizeOfTags(); ++tag) {
                word_i += this.numMatB[tag][this.dictionary.getWordId(preWord)];
            }
        }
        double sum = 0.0;
        for (int tag = 0; tag < this.dictionary.getSizeOfTags(); ++tag) {
            double part = 0;
            if (this.dictionary.getWordId(preWord) == null) {
                part = (1 / (double) word_i) * (this.numMatA[tag][currTag] / (double) this.numPi[tag]);
            } else {
                part = (this.numMatB[tag][this.dictionary.getWordId(preWord)] / (double) word_i) * this.probMatA[tag][currTag];
            }
            sum += part;
        }
        return sum / this.numPi[currTag];
    }
}