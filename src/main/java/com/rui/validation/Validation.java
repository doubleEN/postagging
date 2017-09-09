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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 一次验证评估，按比例划分语料
 */
public class Validation implements ModelScore {

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
    private Estimator[] estimators;

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
    private double[] scores;

    public Validation(WordTagStream wordTagStream, double ratio, NGram nGram, Estimator... estimators) {
        this.stream = wordTagStream;
        this.ratio = ratio;
        this.nGram = nGram;
        this.estimators = estimators;
    }

    @Override
    public void toScore() throws FileNotFoundException,IOException{
        this.scores=new double[estimators.length];
        this.tagger = this.getTagger();
        this.stream.openReadStream();
        for (int i=0;i<estimators.length;++i) {
            this.scores[i] = this.estimate(i);
            this.stream.openReadStream();
        }
    }

    /**
     * 通过验证集获得隐藏状态标注器
     */
    private Tagger getTagger() throws IOException{



        WordTag[] wts = null;
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
            //在1000中取指定比例样本
            if (random.nextInt(1000) >= border) {
                int randNum = random.nextInt(4);
                if (randNum == 1) {
                    paras.addHoldOut(wts);
                } else {
                    paras.addCorpus(wts);
                }
            }
        }
        paras.calcProbs();
        tagger = new Tagger(hmm);

        this.dict = paras.getDictionary();
        return tagger;
    }

    /**
     * 指定验证集，进行一次交叉验证，并返回评估值
     *
     * @return 验证评分
     */
    private double estimate(int estimatorNo) throws IOException{
        WordTag[] wts = null;

        Tagger tagger = null;
        Random random = new Random(11);
        double border = 1000 * this.ratio;
        String[] predictTags = null;

        while ((wts = this.stream.readSentence()) != null) {
            if (random.nextInt(1000) < border) {
                this.getTagOfValidation(wts);
                WordTag[] predict = this.tagger.tag(this.unknownSentence);
                predictTags = new String[predict.length];
                for (int j = 0; j < predict.length; ++j) {
                    predictTags[j] = predict[j].getTag();
                }
                this.estimators[estimatorNo].eval(this.dict, this.unknownSentence, predictTags, this.expectedTags);
            }
        }
        return this.estimators[estimatorNo].getResult();
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

    @Override
    public double[] getScores() {
        return scores;
    }
}
