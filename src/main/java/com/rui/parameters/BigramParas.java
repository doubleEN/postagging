package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.ngram.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import java.util.Arrays;

/**
 * 统计并计算[一阶HMM]的参数
 */
public class BigramParas extends AbstractParas {

//    public static void main(String[] args) {
//        BigramParas paras = new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt", 46, 55320);
//
//        int[][] as = paras.getA();
//        int[][] bs = paras.getB();
//        int[] pi = paras.getPI();
//
//        double[][] pa = paras.getPSA();
//        double[][] pb = paras.getPB();
//
//
//        System.out.println("平滑后概率:");
//        for (double[] a : pa) {
//            System.out.println(Arrays.toString(a));
//        }
////        System.out.println("B:");
////        for (int[] b : bs) {
////            System.out.println(Arrays.toString(b));
////        }
//
//        System.out.println("PI:" + Arrays.toString(pi));
//
//        System.out.println(as.length + ":" + paras.getSizeOfTags());
//        System.out.println(bs.length + ":" + bs[0].length);
//        System.out.println(paras.getPI().length);
//
//        System.out.println(as.length == paras.getHoldOut().length);
//        System.out.println(pa.length == pb.length);
//
//        System.out.println(as.length + ":" + pa.length);
//
//        //PI:[184764, 236810, 74829, 34473, 173047, 20680, 41370, 24244]
//
////        BigramParas paras = new BigramParas("/home/mjx/桌面/PoS/test/testCount.txt");
////
////        System.out.println("probA:");
////        for (double[] p : paras.getPA()) {
////            System.out.println(Arrays.toString(p));
////        }
////        System.out.println("probPi:");
////        System.out.println(Arrays.toString(paras.getPpi()));
////
////        System.out.println("smoothA:");
////        for (double[] p : paras.getPSA()) {
////            System.out.println(Arrays.toString(p));
////        }
////
////        System.out.println("probB:");
////        for (double[] p : paras.getPB()) {
////            System.out.println(Arrays.toString(p));
////        }
//
//    }

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

    public BigramParas(){
        this.dictionary = new DictFactory();
        this.holdOut=new int[1][1];
        this.numMatA = new int[1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
    }

    //在构造器中初始加载这个语料库，并计算初始概率和平滑后的概率
    public BigramParas(String corpusPath) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[1][1];
        this.holdOut = new int[1][1];
        this.numMatB = new int[1][1];
        this.numPi = new int[1];
        this.initParas(corpusPath);
    }

    public BigramParas(String corpusPath, int tagNum, int wordNum) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[tagNum][tagNum];//the size of tag set is 44.
        this.holdOut = new int[tagNum][tagNum];
        this.numMatB = new int[tagNum][wordNum];//the size of word set is 55310.
        this.numPi = new int[tagNum];
        this.initParas(corpusPath);
    }

    @Override
    protected WordTagStream openStream() {
        return new PeopleDailyWordTagStream();
    }

    /*
        对参数计数
     */
    @Override
    protected void countMatA(String[] tags) {
        if (this.getSizeOfTags() > this.numMatA[0].length) {
            this.reBuildA();
        }
        for (int i = 1; i < tags.length; i++) {
            this.numMatA[this.getTagId(tags[i - 1])][this.getTagId(tags[i])]++;
        }
    }

    @Override
    protected void countMatB(String[] words, String[] tags) {
        if (words.length != tags.length) {
            System.err.println("词组，标注长度不匹配。");
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
        int[][] newA = new int[this.getSizeOfTags()][this.getSizeOfTags()];
        for (int i = 0; i < this.numMatA[0].length; ++i) {
            for (int j = 0; j < this.numMatA[0].length; ++j) {
                newA[i][j] = this.numMatA[i][j];
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

    /*
        留存数据处理
     */
    @Override
    protected void addHoldOut(WordTag[] wts) {
        if (this.getSizeOfTags() > this.holdOut.length) {
            this.expandHoldOut();
        }
        for (int i = 1; i < wts.length; i++) {
            this.holdOut[this.getTagId(wts[i - 1].getTag())][this.getTagId(wts[i].getTag())]++;
        }
    }

    @Override
    protected void expandHoldOut() {
        int[][] holeOut = new int[this.getSizeOfTags()][this.getSizeOfTags()];

        for (int i = 0; i < this.holdOut.length; ++i) {
            for (int j = 0; j < this.holdOut[0].length; ++j) {
                holeOut[i][j] = this.holdOut[i][j];
            }
        }
        this.holdOut = holeOut;
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

    /*
        计算概率参数
        注：概率矩阵的大小与映射词典的对应长度是一致的，小雨或等于计数矩阵的大小。
    */
    @Override
    protected void calcProbA() {

        int len = this.getSizeOfTags();

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

        int len = this.getSizeOfTags();

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
            System.err.println("留存数据不存在,不能平滑概率。");
            return;
        }

        int i1=0,i2=0;
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

                //稀疏语料中，t_2的出现概率大多数情况下要比t_2的条件概率大，对应的t_2的联合频数t_1_2要小；
                // 少数情况下，t_2的条件概率比t_2的出现概率大，这时对应的t_2的联合频数t_1_2要大；
                //所以，虽然expression1大的情况多一些，但因为累加的联合频数偏小，所以最后对应系数并不会格外大
                if (expression1 >= expression2) {
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
        System.out.println("系数：" + lambd1 + ":" + lambd2);
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
    public double getProbA(int preTag, int nextTag) {
        return this.probMatA[preTag][nextTag];
    }

    @Override
    public double getProbSmoothA(int preTag, int nextTag) {
        return this.smoothingMatA[preTag][nextTag];
    }


//直接访问数据结构的方法
//    public int[][] getA() {
//        return this.numMatA;
//    }
//
//    public int[][] getB() {
//        return this.numMatB;
//    }
//
//    public int[] getPI() {
//        return this.numPi;
//    }
//
//    public double[][] getPA() {
//        return this.probMatA;
//    }
//
//    public double[][] getPSA() {
//        return this.smoothingMatA;
//    }
//
//    public double[][] getPB() {
//        return this.probMatB;
//    }
//
//    public double[] getPpi() {
//        return this.probPi;
//    }
//
//    public int[][] getHoldOut() {
//        return holdOut;
//    }
}

