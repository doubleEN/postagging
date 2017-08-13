package com.rui.validation;

import com.rui.dictionary.DictFactory;
import com.rui.evaluation.*;
import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.*;

/**
 * 交叉验证
 */
public class CrossValidation {

    public static void main(String[] args) {
        CrossValidation crossValidation = new CrossValidation(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), 10, NGram.BiGram, new PreciseAll());
//        BiGram:[0.9080436628471887, 0.906321309154582, 0.9070137686103675, 0.907351759320731, 0.9066084156445602, 0.904506189331196, 0.908200845974329, 0.9046056152117299, 0.9089800221298692, 0.9058008673524246]
//        [0.44858651755203477, 0.40913921360255046, 0.4302038295243978, 0.3988995873452545, 0.42012414243711205, 0.38405545927209706, 0.43992120814182534, 0.3771602846492714, 0.4189047785141263, 0.3908716540837337]
//        all:0.9080436628471887IV:0.9213655197261754OOV:0.44858651755203477
//        System.out.println(Arrays.toString(crossValidation.scoreK()));
        System.out.println(crossValidation.score());

    }

    //标明使用的n-gram
    private NGram nGram;

    //生成的标注器
    private Tagger tagger;

    //读入特定形式的语料
    private WordTagStream stream;

    //验证语料
    private Set<WordTag[]> validatonSet;

    //每一折交叉验证中，生成的验证语料
    private String[] unknownSentences;

    //验证语料的正确标注
    private String[][] expectedTags;

    //评估器
    private Estimator estimator;

    //折数
    private int fold;

    //词典
    private DictFactory dict;

    //代表n-gram的常量
    public enum NGram {
        BiGram, TriGram;
    }

    public CrossValidation(WordTagStream wordTagStream, int fold, NGram nGram, Estimator estimator) {
        this.stream = wordTagStream;
        this.fold = fold;
        this.nGram = nGram;
        this.estimator = estimator;
    }

    public double score() {
        this.tagger = this.getTagger(0);
        return this.estimate();
    }

    public double[] scoreK() {
        double[] scores = new double[this.fold];

        for (int i = 0; i < this.fold; ++i) {
            this.tagger = this.getTagger(i);
            scores[i] = this.estimate();
        }
        return scores;
    }

    private Tagger getTagger(int taggerNO) {
        WordTag[] wts = null;
        int num = 0;
        this.validatonSet = new HashSet<>();

        Tagger tagger = null;
        Random random = new Random(11);

        AbstractParas paras = null;
        HMM hmm = null;
        if (this.nGram == NGram.BiGram) {
            paras = new BigramParas();
            hmm = new FirstOrderHMM(paras);
        } else if (this.nGram == NGram.TriGram) {
            paras = new TrigramParas();
            hmm = new SecondOrderHMM(paras);
        }

        while ((wts = this.stream.readSentence()) != null) {
            if (num % this.fold == taggerNO) {
                this.validatonSet.add(wts);
            } else {
                int randNum = random.nextInt(4);
                if (randNum == 1) {
                    paras.addHoldOut(wts);
                } else {
                    paras.addCorpus(wts);
                }
            }
            ++num;
        }
        paras.calcProbs();
        tagger = new Tagger(hmm);

        this.stream.openReadStream(this.stream.getCorpusPath());

        this.dict = paras.getDictionary();
        return tagger;
    }

    //指定验证集，进行一次交叉验证，并返回评估值
    private double estimate() {
        int sizeOfSentences = this.validatonSet.size();
        this.unknownSentences = new String[sizeOfSentences];
        this.expectedTags = new String[sizeOfSentences][];

        this.getTagOfValidation();
        String[][] predictTags = new String[sizeOfSentences][];
        for (int i = 0; i < sizeOfSentences; ++i) {
            WordTag[] wts = this.tagger.tag(this.unknownSentences[i]);
            predictTags[i] = new String[wts.length];
            for (int j = 0; j < wts.length; ++j) {
                predictTags[i][j] = wts[j].getTag();
            }
        }
        return this.estimator.eval(this.dict, this.unknownSentences, predictTags, this.expectedTags);
    }

    private void getTagOfValidation() {
        int i = 0;
        for (WordTag[] wts : this.validatonSet) {
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