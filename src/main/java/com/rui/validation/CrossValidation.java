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
public class CrossValidation implements ModelScore {
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
     * 每一折交叉验证中，生成的验证语料
     */
    private String unknownSentence;

    /**
     * 验证语料的正确标注
     */
    private String[] expectedTags;

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

    /**
     * @param wordTagStream 包含特点语料路径的语料读取流
     * @param fold 交叉验证折数
     * @param nGram 语法参数
     * @param estimator 评估方式
     */
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
            this.stream.openReadStream(this.stream.getCorpusPath());
            this.scores[i] = this.estimate(i);
            this.stream.openReadStream(this.stream.getCorpusPath());
            this.estimator.reset();
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
            if (num % this.fold != taggerNO) {
                //语料不能直接放入内存
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
     *
     * @return 一折验证的评分
     */
    private double estimate(int taggerNo) {
        WordTag[] wts = null;
        int num = 0;
        String[] predictTags=null;

        while ((wts = this.stream.readSentence()) != null) {
            if (num % this.fold == taggerNo) {
                //验证语料不能直接放入内存
                this.getTagOfValidation(wts);
                WordTag[] predict = this.tagger.tag(this.unknownSentence);
                predictTags = new String[predict.length];
                for (int j = 0; j < predict.length; ++j) {
                    predictTags[j] = predict[j].getTag();
                }
                this.estimator.eval(this.dict,this.unknownSentence,predictTags,this.expectedTags);
            }
            ++num;
        }
        return estimator.getResult();
    }

    /**
     * 分割验证集观察状态和隐藏状态
     * @param wts 带标注的句子
     */
    private void getTagOfValidation(WordTag[] wts) {
        String sentence = "";
        this.expectedTags = new String[wts.length];
        for (int j = 0; j < wts.length; ++j) {
            this.expectedTags[j] = wts[j].getTag();
            sentence = sentence + wts[j].getWord() + " ";
        }
        this.unknownSentence = sentence.trim();
    }

    /**
     * @return 返回此次验证评分
     */
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