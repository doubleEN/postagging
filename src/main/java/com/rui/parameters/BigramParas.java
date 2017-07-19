package com.rui.parameters;

import com.rui.dictionary.DictFactory;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

/**
 *
 */
public class BigramParas extends AbstractParas {


    public static void main(String[] args) {
//        BigramParas paras = new BigramParas();
//
//        WordTag[] wordTags = new WordTag[]{
//                new WordTag("我", "n"),
//                new WordTag("爱", "v"),
//                new WordTag("nlp", "fn")
//        };
//        System.out.println(Arrays.toString(wordTags));
//        paras.addCorpus("/home/mjx/桌面/PoS/corpus/199801_format.txt");
//
//        int[][] as = paras.getA();
//        int[][] bs = paras.getB();
//        int[] pi = paras.getPI();
//
//        System.out.println("A:");
//        for (int[] a : as) {
//            System.out.println(Arrays.toString(a));
//        }
//        System.out.println("B:");
//        for (int[] b : bs) {
//            System.out.println(Arrays.toString(b));
//        }
//
//        System.out.println("PI:" + Arrays.toString(pi));
//
//        System.out.println(as.length + ":" + as[0].length);
//        System.out.println(bs.length + ":" + bs[0].length);
//        System.out.println(paras.getPI().length);
//        //PI:[184764, 236810, 74829, 34473, 173047, 20680, 41370, 24244]

//        BigramParas paras = new BigramParas();
//        paras.addCorpus("/home/mjx/桌面/PoS/test/testCount.txt");
//        paras.calcProbs(true);
//
//        System.out.println("probA:");
//        for (double[] p : paras.getPA()) {
//            System.out.println(Arrays.toString(p));
//        }
//        System.out.println("probPi:");
//        System.out.println(Arrays.toString(paras.getPpi()));
//
//        System.out.println("smoothA:");
//        for (double[] p : paras.getPSA()) {
//            System.out.println(Arrays.toString(p));
//        }
//
//        System.out.println("probB:");
//        for (double[] p : paras.getPB()) {
//            System.out.println(Arrays.toString(p));
//        }

    }

    /*
        计数参数
     */
    protected int[][] numMatA;

    protected int[][] numMatB;

    protected int[] numPi;

    /*
        概率参数
     */
    protected double[][] probMatA;

    protected double[][] smoothingMatA;

    protected double[][] probMatB;

    protected double[] probPi;

    public BigramParas() {
        this.dictionary = new DictFactory();
        this.numMatA = new int[1][1];//the size of tag set is 44.
        this.numMatB = new int[1][1];//the size of word set is 55310.
        this.numPi = new int[1];
    }

    public BigramParas(int tagNum, int wordNum) {
        this.dictionary = new DictFactory();
        this.numMatA = new int[tagNum][tagNum];//the size of tag set is 44.
        this.numMatB = new int[tagNum][wordNum];//the size of word set is 55310.
        this.numPi = new int[tagNum];
    }

    @Override
    protected WordTagStream openStream(String corpusPath) {
        return new PeopleDailyWordTagStream(corpusPath);
    }

    //para A
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
            System.err.println("词组，标注长度不匹配。");
        }
        if (this.dictionary.getSizeOfTags() > this.numMatB.length || this.dictionary.getSizeOfWords() > this.numMatB[0].length) {
            this.reBuildB();
        }

        for (int i = 0; i < words.length; i++) {
            this.numMatB[this.dictionary.getTagId(tags[i])][this.dictionary.getWordId(words[i])]++;
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

    //扩展参数数组
    protected void reBuildA() {
        int[][] newA = new int[this.dictionary.getSizeOfTags()][this.dictionary.getSizeOfTags()];
        for (int i = 0; i < this.numMatA[0].length; ++i) {
            for (int j = 0; j < this.numMatA[0].length; ++j) {
                newA[i][j] = this.numMatA[i][j];
            }
        }
        this.numMatA = newA;
    }

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

    protected void reBuildPi() {
        int[] pi = new int[this.dictionary.getSizeOfTags()];
        for (int i = 0; i < this.numPi.length; ++i) {
            pi[i] = this.numPi[i];

        }
        this.numPi = pi;
    }

    /*
        计算概率
     */
    @Override
    protected void calcProbA() {

        int len = this.numMatA.length;

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

        int rowSize = this.numMatB.length;
        int colSize = this.numMatB[0].length;

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

        int vectorSize = this.numPi.length;

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
        int len = this.probMatA.length;

        this.smoothingMatA = new double[len][len];

        double lambd_count1 = 0.0;
        double lambd_count2 = 0.0;

        double sumOfTag = 0.0;
        for (int num : numPi) {
            sumOfTag += num;
        }

//        System.out.println(sumOfTag);
        if (sumOfTag == 0) {
            System.err.println("隐藏状态数为0.");
        }
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                //?????是否是联合频数？？？？？
                int t_1_2 = this.numMatA[t_1][t_2] + numMatA[t_2][t_1];

                double expression1 = (numPi[t_2] - 1) / (sumOfTag - 1);
                double expression2 = 0.0;

                if (numPi[t_1] - 1 != 0) {
                    expression2 = (t_1_2 - 1) / (numPi[t_1] - 1);
                }
                //这里等号对结果的影响
                if (expression1 >= expression2) {
                    lambd_count1 += t_1_2;
                } else {
                    lambd_count2 += t_1_2;
                }
            }
        }
        double lambd1 = lambd_count1 / (lambd_count1 + lambd_count2);
        double lambd2 = lambd_count2 / (lambd_count1 + lambd_count2);
//        System.out.println("系数：" + lambd1 + ":" + lambd2);
        //系数：0.9968451753824765:0.003154824617523456
        //系数：0.956853536835926:0.043146463164073966
        for (int t_1 = 0; t_1 < len; ++t_1) {
            for (int t_2 = 0; t_2 < len; ++t_2) {
                this.smoothingMatA[t_1][t_2] = lambd1 * this.probPi[t_2] + lambd2 * this.probMatA[t_1][t_2];
            }
        }
    }

    @Override
    public double getPi(int indexOfTag) {
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

    public int[][] getA() {
        return this.numMatA;
    }

    public int[][] getB() {
        return this.numMatB;
    }

    public int[] getPI() {
        return this.numPi;
    }

    public double[][] getPA() {
        return this.probMatA;
    }

    public double[][] getPSA() {
        return this.smoothingMatA;
    }


    public double[][] getPB() {
        return this.probMatB;
    }

    public double[] getPpi() {
        return this.probPi;
    }


    @Override
    public String getTagOnId(int tagId) {
        return this.dictionary.getTag(tagId);
    }

    @Override
    public int getWordId(String word) {
        return this.dictionary.getWordId(word);
    }
}

