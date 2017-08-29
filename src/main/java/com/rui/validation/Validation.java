package com.rui.validation;

import com.rui.dictionary.DictFactory;
import com.rui.evaluation.Estimator;
import com.rui.evaluation.Precise;
import com.rui.evaluation.PreciseIV;
import com.rui.evaluation.PreciseOOV;
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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 一次验证评估，按比例划分语料
 */
public class Validation implements ModelScore{

    /**
     * 标明使用的n-gram
     */
    private NGram nGram;

    /**
     * 生成的标注器
     */
    private Tagger tagger;

    /**
     * 读入特定形式的语料
     */
    private WordTagStream stream;

    /**
     * 验证语料
     */
    private Set<WordTag[]> validatonSet;

    /**
     * 每一折交叉验证中，生成的验证语料
     */
    private String[] unknownSentences;

    /**
     * 验证语料的正确标注
     */
    private String[][] expectedTags;

    /**
     * 评估器
     */
    private Estimator estimator;

    /**
     * 折数
     */
    private double ratio;

    /**
     * 词典
     */
    private DictFactory dict;

    /**
     * 验证评分
     */
    private double score;

    public Validation(WordTagStream wordTagStream, double ratio, NGram nGram, Estimator estimator) {
        this.stream = wordTagStream;
        this.ratio = ratio;
        this.nGram = nGram;
        this.estimator = estimator;
    }

    @Override
    public void toScore() {
        this.tagger = this.getTagger();
        this.score=this.estimate();
    }

    /**
     * 通过验证集获得隐藏状态标注器
     */
    private Tagger getTagger() {
        WordTag[] wts = null;
        int num = 0;
        this.validatonSet = new HashSet<>();

        Tagger tagger = null;
        Random random = new Random(11);
        double border = 1000 * this.ratio;

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
            if (random.nextInt(1000) < border) {
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

        this.dict = paras.getDictionary();
        return tagger;
    }

    /**
     * 指定验证集，进行一次交叉验证，并返回评估值
     * @return 验证评分
     */
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

    /**
     * 分割验证集观察状态和隐藏状态
     */
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

    @Override
    public double getScore() {
        return score;
    }
}
