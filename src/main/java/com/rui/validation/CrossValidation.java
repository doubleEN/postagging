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

import java.util.*;

/**
 * 交叉验证
 */
public class CrossValidation implements ModelScore{

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
    private int fold;

    /**
     * 映射词典
     */
    private DictFactory dict;

    /**
     * 每折验证的评分
     */
    private double[] scores;

    public CrossValidation(WordTagStream wordTagStream, int fold, NGram nGram, Estimator estimator) {
        this.stream = wordTagStream;
        this.fold = fold;
        this.nGram = nGram;
        this.estimator = estimator;
    }

    @Override
    public void toScore() {
        this.scores = new double[this.fold];
        for (int i = 0; i < this.fold; ++i) {
            this.tagger = this.getTagger(i);
            this.scores[i] = this.estimate();
        }
    }

    /**
     * 获得指定训练集上训练的隐藏状态标注器
     * @param taggerNO 代表训练集的编号
     * @return 隐藏状态标注器
     */
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

    /**
     * 指定验证集，进行一次交叉验证，并返回评估值
     * @return 一折验证的评分
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
        double sum = 0;
        for (double score : scores) {
            sum += score;
        }
        return sum / scores.length;
    }

    /**
     * 返回每折评分结果
     */
    public double[] getScores() {
        return this.scores;
    }
}