package com.rui.parameter;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import static com.rui.util.GlobalParas.logger;

/**
 * 统计并计算[一阶HMM]的参数
 */
public class BigramParas extends AbstractParas {

    /*
        计数参数
     */
    private int[][] numMatA;

    private int[][] numMatB;

    private int[] numPi;

    private int[][] holdOut;

    /*
        概率参数
     */
    private double[][] probMatA;

    private double[][] smoothingMatA;

    private double[][] probMatB;

    private double[] probPi;

    public BigramParas() {
        this.dictionary = new DictFactory();
        this.holdOut = new int[1][1];
        this.numMatA = new int[1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
    }

    //在构造器中初始加载这个语料库，并计算初始概率和平滑后的概率
    public BigramParas(WordTagStream stream) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[1][1];
        this.holdOut = new int[1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
        this.initParas(stream);
    }

    public BigramParas(WordTagStream stream, int tagNum, int wordNum) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[tagNum][tagNum];//the size of tag set is 44.
        this.holdOut = new int[tagNum][tagNum];
        this.numMatB = new int[tagNum][wordNum];//the size of word set is 55310.
        this.numPi = new int[tagNum];
        this.initParas(stream);
    }

    /*
        对参数计数
     */
    @Override
    protected void countMatA(String[] tags) {
        if (this.dictionary.getSizeOfTags() > this.numMatA[0].length) {
            this.reBuildA();
        }
        for (int i = 1; i < tags.length; i++) {
            this.numMatA[this.dictionary.getTagId(tags[i - 1])][this.dictionary.getTagId(tags[i])]++;
        }
    }

    @Override
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            logger.warning("词组，标注长度不匹配。");//Level.info
            return;
        }
        if (this.dictionary.getSizeOfTags() > this.numMatB.length || this.dictionary.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }

        for (int i = 0; i < words.length; i++) {
            this.numMatB[this.dictionary.getTagId(tags[i])][this.dictionary.getWordId(words[i])]++;
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

    @Override
    protected void countPi(String[] tags) {
        if (this.dictionary.getSizeOfTags() > this.numPi.length) {
            this.reBuildPi();
        }
        for (String tag : tags) {
            this.numPi[this.dictionary.getTagId(tag)]++;
        }
    }

    @Override
    protected void reBuildA() {
        int[][] newA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        for (int i = 0; i < this.numMatA[0].length; ++i) {
            for (int j = 0; j < this.numMatA[0].length; ++j) {
                newA[i][j] = this.numMatA[i][j];
            }
        }
        this.numMatA = newA;
    }

    @Override
    protected void reBuildB() {
        int row = this.dictionary.getSizeOfTags() > this.numMatB.length ? this.dictionary.getSizeOfTags() : this.numMatB.length;
        int col = this.dictionary.getSizeOfWords() > this.numMatB[0].length ? this.dictionary.getSizeOfWords() : this.numMatB[0].length;
        int[][] newB = new int[row][col];
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                newB[i][j] = this.numMatB[i][j];
            }
        }
        this.numMatB = newB;
    }

    @Override
    protected void reBuildPi() {
        int[] pi = new int[this.dictionary.getSizeOfTags()];
        for (int i = 0; i < this.numPi.length; ++i) {
            pi[i] = this.numPi[i];

        }
        this.numPi = pi;
    }

    /*
        留存数据处理
     */
    @Override
    public void addHoldOut(WordTag[] wts) {
        this.dictionary.addIndex(wts);
        if (this.dictionary.getSizeOfTags() > this.holdOut.length) {
            this.expandHoldOut();
        }
        for (int i = 1; i < wts.length; i++) {
            this.holdOut[this.dictionary.getTagId(wts[i - 1].getTag())][this.dictionary.getTagId(wts[i].getTag())]++;
        }
    }

    @Override
    protected void expandHoldOut() {
        int[][] holdOut = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];

        for (int i = 0; i < this.holdOut.length; ++i) {
            for (int j = 0; j < this.holdOut[0].length; ++j) {
                holdOut[i][j] = this.holdOut[i][j];
            }
        }
        this.holdOut = holdOut;
    }

    @Override
    protected void ensureLenOfTag() {
        int tagSize = this.dictionary.getSizeOfTags();
        if (tagSize > this.numMatA.length) {
            this.reBuildA();
        }
        if (tagSize > this.holdOut.length) {
            this.expandHoldOut();
        }
        if (tagSize > this.numPi.length) {
            this.reBuildPi();
        }
        if (this.dictionary.getSizeOfTags() > this.numMatB.length || this.dictionary.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
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
                    //处理分母为0的情况
                    this.probMatA[row][col] = 0.0;
                }
            }
        }
    }

    @Override
    protected void calcProbB() {

        int rowSize = this.dictionary.getSizeOfTags();
        int colSize = this.dictionary.getSizeOfWords();

        this.probMatB = new double[rowSize][colSize];

        for (int row = 0; row < rowSize; ++row) {
            double sumPerRow = 0;

            for (int col = 0; col < colSize; ++col) {
                sumPerRow += this.numMatB[row][col];
            }

            for (int col = 0; col < colSize; ++col) {
                if (sumPerRow != 0) {
                    probMatB[row][col] = (this.numMatB[row][col]) / (sumPerRow);
                } else {
                    //处理分母为0的情况
                    probMatB[row][col] = 0.0;
                }
            }
        }

    }

    @Override
    protected void calcProbPi() {

        int vectorSize = this.dictionary.getSizeOfTags();

        this.probPi = new double[vectorSize];

        double sumOfVector = 0.0;
        for (int val : this.numPi) {
            sumOfVector += val;
        }
        for (int index = 0; index < vectorSize; ++index) {
            if (sumOfVector != 0) {
                this.probPi[index] = this.numPi[index] / sumOfVector;
            } else {
                this.probPi[index] = 0.0;
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

//        System.out.println(sumOfTag);
        if (sumOfTag == 0) {
            logger.severe("留存数据不存在,不能平滑概率。");//Level.info
            System.exit(1);
        }

        int i1 = 0, i2 = 0;
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                // 系数：0.1514629948364888:0.8485370051635112
                //系数：0.3683304647160069:0.6316695352839932
                int t_1_2 = this.holdOut[t_1][t_2];

                double expression1 = (vector[t_2] - 1) / (sumOfTag - 1);
                double expression2 = 0.0;

                if (vector[t_1] - 1 != 0) {
                    expression2 = (t_1_2 - 1) / (vector[t_1] - 1);
                }

                if (expression1 > expression2) {
                    lambd_count1 += t_1_2;
                    i1++;
                } else {
                    i2++;
                    lambd_count2 += t_1_2;
                }
            }
        }

        double lambd1 = lambd_count1 / (lambd_count1 + lambd_count2);
        double lambd2 = lambd_count2 / (lambd_count1 + lambd_count2);
//        System.out.println("系数频次：" + lambd_count1 + ":" + lambd_count2);
        logger.info("系数：" + lambd1 + "-" + lambd2);
//        System.out.println("系数的计数频次："+i1 + ":" + i2);

        //系数：0.9968451753824765:0.003154824617523456
        //系数：0.956853536835926:0.043146463164073966
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                this.smoothingMatA[t_1][t_2] = lambd1 * this.probPi[t_2] + lambd2 * this.probMatA[t_1][t_2];
            }
        }
    }

    /*
        参数访问接口的实现
     */
    @Override
    public double getProbPi(int indexOfTag) {
        return this.probPi[indexOfTag];
    }

    @Override
    public double getProbB(int indexOfTag, int indexOfWord) {
        return this.probMatB[indexOfTag][indexOfWord];
    }

    @Override
    public double getProbA(int... tagIndex) {
        if (tagIndex.length != 2) {
            logger.severe("获取转移概率参数不合法。");
            System.exit(1);
        }
        return this.probMatA[tagIndex[0]][tagIndex[1]];
    }

    @Override
    public double getProbSmoothA(int... tagIndex) {
        if (tagIndex.length != 2) {
            logger.severe("获取转移概率参数不合法。");
            System.exit(1);
        }
        return this.smoothingMatA[tagIndex[0]][tagIndex[1]];
    }

}

