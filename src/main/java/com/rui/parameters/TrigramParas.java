package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.wordtag.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

/**
 * 三元语法参数训练。
 */
public class TrigramParas extends AbstractParas {

//    public static void main(String[] args) {
//        TrigramParas paras = new TrigramParas("/home/mjx/桌面/PoS/test/testCount.txt");
//        int[][][] numA = paras.getNumMatA();
//        int[][] numB = paras.getNumMatB();
//        int[] numPi = paras.getNumPi();
//        double[][][] probA = paras.getProbMatA();
//        double[][] probB = paras.getProbMatB();
//        double[] probPi = paras.getProbPi();
//
//        System.out.println("numA:");
//        for (int[][] as : numA) {
//            for (int[] a : as) {
//                System.out.println(Arrays.toString(a));
//            }
//            System.out.println();
//        }
//        System.out.println("numB:");
//        for (int[] b : numB) {
//            System.out.println(Arrays.toString(b));
//        }
//        System.out.println("pi:" + Arrays.toString(numPi));
//
//        System.out.println("probA:");
//        for (double[][] as : probA) {
//            System.out.println();
//            for (double[] a : as) {
//                System.out.println(Arrays.toString(a));
//            }
//        }
//        System.out.println("probB:");
//        for (double[] b : probB) {
//            System.out.println(Arrays.toString(b));
//        }
//        System.out.println("probPi:" + Arrays.toString(probPi));
//
//    }

    /*
        计数参数
     */
    private int[][][] numMatA;

//    private int[][] numHeadA;

    private int[][] numMatB;

    private int[] numPi;

    private int[][][] holdOut;

    /*
        概率参数
     */
    private double[][][] probMatA;

//    private double[][] probHeadA;

    private double[][][] smoothingMatA;

    private double[][] probMatB;

    private double[] probPi;

    public TrigramParas(String corpusPath) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[1][1][1];
        this.holdOut = new int[1][1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
        this.initParas(corpusPath);
    }

    public TrigramParas(String corpusPath, int tagNum, int wordNum) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[tagNum][tagNum][tagNum];
        this.holdOut = new int[tagNum][tagNum][tagNum];
        this.numMatB = new int[tagNum][wordNum];
        this.numPi = new int[tagNum];
        this.initParas(corpusPath);
    }


    @Override
    protected WordTagStream chooseStream() {
        return new PeopleDailyWordTagStream();
    }

    @Override
    protected void countMatA(String[] tags) {
        if (tags.length < 3) {
            System.err.println("标注长度小于3，不能用于统计转移频数。");
            return;
        }
        if (this.getSizeOfTags() > this.numMatA[0].length) {
            this.reBuildA();
        }
        for (int i = 2; i < tags.length; i++) {
            this.numMatA[this.getTagId(tags[i - 2])][this.getTagId(tags[i - 1])][this.getTagId(tags[i])]++;
        }
    }

    @Override
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            System.err.println("词组，标注长度不匹配。");
            return;
        }
        if (this.getSizeOfTags() > this.numMatB.length || this.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }

        for (int i = 0; i < words.length; i++) {
            this.numMatB[this.getTagId(tags[i])][this.getWordId(words[i])]++;
        }
    }

    @Override
    protected void countPi(String[] tags) {
        if (this.getSizeOfTags() > this.numPi.length) {
            this.reBuildPi();
        }
        for (String tag : tags) {
            this.numPi[this.getTagId(tag)]++;
        }
    }

    @Override
    protected void reBuildA() {
        int len = this.getSizeOfTags();
        int[][][] newA = new int[len][len][len];
        for (int i = 0; i < this.numMatA.length; ++i) {
            for (int j = 0; j < this.numMatA.length; ++j) {
                for (int k = 0; k < this.numMatA.length; ++k) {

                    newA[i][j][k] = this.numMatA[i][j][k];
                }
            }
        }
        this.numMatA = newA;

    }

    @Override
    protected void reBuildB() {
        int row = this.getSizeOfTags() > this.numMatB.length ? this.getSizeOfTags() : this.numMatB.length;
        int col = this.getSizeOfWords() > this.numMatB[0].length ? this.getSizeOfWords() : this.numMatB[0].length;
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
        int[] pi = new int[this.getSizeOfTags()];
        for (int i = 0; i < this.numPi.length; ++i) {
            pi[i] = this.numPi[i];

        }
        this.numPi = pi;
    }

    @Override
    protected void addHoldOut(WordTag[] wts) {
        if (wts.length < 3) {
            System.err.println("句子长度不够，不能添加留存频数。");
            return;
        }

        if (this.getSizeOfTags() > this.holdOut.length) {
            this.expandHoldOut();
        }
        for (int i = 2; i < wts.length; i++) {
            this.holdOut[this.getTagId(wts[i - 2].getTag())][this.getTagId(wts[i - 1].getTag())][this.getTagId(wts[i].getTag())]++;
        }
    }

    @Override
    protected void expandHoldOut() {
        int len = this.getSizeOfTags();
        int[][][] holdOut = new int[len][len][len];

        for (int i = 0; i < this.holdOut.length; ++i) {
            for (int j = 0; j < this.holdOut[0].length; ++j) {
                for (int k = 0; k < this.holdOut.length; ++k) {
                    holdOut[i][j] = this.holdOut[i][j];
                }
            }
        }
        this.holdOut = holdOut;
    }

    @Override
    protected void ensureLenOfTag() {
        int tagSize = this.getSizeOfTags();
        if (tagSize > this.numMatA.length) {
            this.reBuildA();
        }
        if (tagSize > this.holdOut.length) {
            this.expandHoldOut();
        }
    }

    @Override
    protected void calcProbA() {

        int len = this.getSizeOfTags();

        this.probMatA = new double[len][len][len];

        //p(t_3|t_2,t_1)=num(t_3,t_2,t_1)/num(t_2,t_1)
        for (int t_1 = 0; t_1 < len; ++t_1) {

            for (int t_2 = 0; t_2 < len; ++t_2) {
                double sumPerRow = 0;

                for (int col = 0; col < len; ++col) {
                    sumPerRow += this.numMatA[t_1][t_2][col];
                }

                for (int t_3 = 0; t_3 < len; ++t_3) {
                    if (sumPerRow != 0) {
                        this.probMatA[t_1][t_2][t_3] = (this.numMatA[t_1][t_2][t_3]) / (sumPerRow);
                    } else {
                        //处理分母为0的情况
                        this.probMatA[t_1][t_2][t_3] = 0.0;
                    }
                }
            }


        }
    }

    @Override
    protected void calcProbB() {

        int rowSize = this.getSizeOfTags();
        int colSize = this.getSizeOfWords();

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

        int vectorSize = this.getSizeOfTags();

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

    }

    /*
        获得指定概率
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
        if (tagIndex.length!=3){
            System.err.println("获取转移概率的参数不合法。");
        }
        return this.probMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
    }

    @Override
    public double getProbSmoothA(int... tagIndex) {
        if (tagIndex.length!=3){
            System.err.println("获取转移概率的参数不合法。");
        }
        return this.smoothingMatA[tagIndex[0]][tagIndex[1]][tagIndex[2]];
    }


//    public int[][][] getNumMatA() {
//        return numMatA;
//    }
//
//    public int[][] getNumMatB() {
//        return numMatB;
//    }
//
//    public int[] getNumPi() {
//        return numPi;
//    }
//
//    public double[][][] getProbMatA() {
//        return probMatA;
//    }
//
//    public double[][] getProbMatB() {
//        return probMatB;
//    }
//
//    public double[] getProbPi() {
//        return probPi;
//    }
}
