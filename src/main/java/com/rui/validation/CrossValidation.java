package com.rui.validation;

import com.rui.evaluation.Estimator;
import com.rui.evaluation.Precies;
import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.parameters.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.util.*;

/**
 * 交叉验证
 */
public class CrossValidation {

    public static void main(String[] args) {
        Estimator estimator = new Precies();
        CrossValidation cv = new CrossValidation("/home/mjx/桌面/PoS/corpus/2.txt", 5, NGram.TriGram, new PeopleDailyWordTagStream(), estimator);
        System.out.println(cv.score());
        //        System.out.println(Arrays.toString(cv.scoreK()));
    }

    //标明使用的n-gram
    private NGram nGram;

    //生成的标注器
    private Tagger tagger;

    //读入特定形式的语料
    private WordTagStream stream;

    //将语料划分成k折
    private Set<WordTag[]>[] trainTest;

    //每一折交叉验证中，生成的验证语料
    private String[] unknownSentences;

    //验证语料的正确标注
    private String[][] expectedTags;

    //评估器
    private Estimator estimator;

    //代表n-gram的常量
    public enum NGram {
        BiGram, TriGram;
    }

    public CrossValidation(String corpusPath, int fold, NGram nGram, WordTagStream stream, Estimator estimator) {
        this.trainTest = new HashSet[fold];
        this.nGram = nGram;
        this.stream = stream;
        this.stream.openReadStream(corpusPath);
        this.estimator = estimator;
    }

    //进行k折交叉验证，返回每次验证的评估
    public double[] scoreK() {
        double[] scores = new double[this.trainTest.length];
        List<WordTag[]> sentences = this.readCorpus();
        this.trainTestSplit(sentences);
        for (int validationNo = 1; validationNo <= this.trainTest.length; ++validationNo) {
            scores[validationNo - 1] = this.estimate(validationNo);
        }
        return scores;
    }

    //进行一次验证，返回评估结果
    public double score() {
        double[] scores = new double[this.trainTest.length];
        List<WordTag[]> sentences = this.readCorpus();
        this.trainTestSplit(sentences);
        return this.estimate(1);
    }

    //划分原始语料为k份
    private void trainTestSplit(List<WordTag[]> sentences) {
        Random rand = new Random(11);
        //每一折的大小
        int range = sentences.size() / this.trainTest.length + 1;
        for (int k = 0; k < this.trainTest.length; ++k) {
            this.trainTest[k] = new HashSet<WordTag[]>();
            for (int num = 0; num < range && sentences.size() != 0; ++num) {
                int no=rand.nextInt(sentences.size());
                WordTag[] wts = sentences.get(no);
                this.trainTest[k].add(wts);
                //list适合做随机划分多个子集
                sentences.remove(no);
            }
        }

    }

    //读取语料到List中
    private List<WordTag[]> readCorpus() {
        List<WordTag[]> sentences = new ArrayList<WordTag[]>();
        WordTag[] wt = null;
        while ((wt = this.stream.readSentence()) != null) {
            sentences.add(wt);//按序添加
        }
        return sentences;
    }

    //指定验证集，进行一次交叉验证，并返回评估值
    private double estimate(int validationNo) {
        if (validationNo > this.trainTest.length || validationNo < 1) {
            System.err.println("验证集标号不合法。");
            return 0;
        }

        int sizeOfSentences = this.trainTest[validationNo - 1].size();
        this.unknownSentences = new String[sizeOfSentences];
        this.expectedTags = new String[sizeOfSentences][];
        this.getTagOfValidation(validationNo);
        this.tagger = this.getTagger(validationNo);

        String[][] predictTags = new String[sizeOfSentences][];
        for (int i = 0; i < sizeOfSentences; ++i) {
            WordTag[] wts = this.tagger.tag(this.unknownSentences[i]);
            predictTags[i] = new String[wts.length];
            for (int j = 0; j < wts.length; ++j) {
                predictTags[i][j] = wts[j].getTag();
            }
        }
        return this.estimator.eval(predictTags, this.expectedTags);
    }

    //获得一次交叉验证生成的标注器
    private Tagger getTagger(int validatonNo) {

        Tagger tagger = null;
        Random random = new Random(11);
        if (this.nGram == NGram.BiGram) {
            AbstractParas paras = new BigramParas();
            for (int k = 0; k < this.trainTest.length; k++) {
                if (k + 1 == validatonNo) {
                    continue;
                }
                for (WordTag[] wts : this.trainTest[k]) {
                    int randNum = random.nextInt(4);
                    if (randNum == 1) {
                        paras.addHoldOut(wts);
                    } else {
                        paras.addCorpus(wts);
                    }
                }
            }
            paras.calcProbs();
            HMM hmm = new FirstOrderHMM(paras);
            tagger = new Tagger(hmm);
        } else if (this.nGram == NGram.TriGram) {
            AbstractParas paras = new TrigramParas();
            for (int k = 0; k < this.trainTest.length; k++) {
                if (k + 1 == validatonNo) {
                    continue;
                }
                for (WordTag[] wts : this.trainTest[k]) {
                    int randNum = random.nextInt(4);
                    if (randNum == 1) {
                        paras.addHoldOut(wts);
                    } else {
                        paras.addCorpus(wts);
                    }
                }
            }
            paras.calcProbs();
            HMM hmm = new SecondOrderHMM(paras);
            tagger = new Tagger(hmm);
        }
        return tagger;
    }

    //分离验证集的句子，得到未标注的句子和期望的标注序列
    private void getTagOfValidation(int validationNo) {
        int i = 0;
        for (WordTag[] wts : this.trainTest[validationNo - 1]) {
            String sentence = "";//字符串拼接时，null的影响
            this.expectedTags[i] = new String[wts.length];
            for (int j = 0; j < wts.length; ++j) {
                this.expectedTags[i][j] = wts[j].getTag();
                sentence = sentence + wts[j].getWord() + " ";
            }
            this.unknownSentences[i] = sentence.trim();
            ++i;
        }
    }
}

