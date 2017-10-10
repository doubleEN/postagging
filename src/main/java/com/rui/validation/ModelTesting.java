package com.rui.validation;

import com.rui.evaluation.WordPOSMeasure;
import com.rui.model.HMM;
import com.rui.model.HMM1st;
import com.rui.model.HMM2nd;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.io.IOException;
import java.util.HashSet;

public class ModelTesting implements ModelScore {

    public static void main(String[] args) throws Exception {
        ModelScore modelScore = new ModelTesting(new PeopleDailyWordTagStream("/home/jx_m/桌面/PoS/corpus/training", "utf-8"),new PeopleDailyWordTagStream("/home/jx_m/桌面/PoS/corpus/testing", "utf-8"), NGram.TriGram);
        modelScore.toScore();
        System.out.println(modelScore.getScores().toString());
    }

    /**
     * 标明使用的n-gram
     */
    private NGram nGram;

    /**
     * 生成的标注器
     */
    private Tagger tagger;

    /**
     * 读入特定形式的训练语料
     */
    private WordTagStream training;


    /**
     * 读入特定形式的测试语料
     */
    private WordTagStream testing;

    /**
     * 每一折交叉验证中，生成的验证语料
     */
    private String unknownSentence;

    /**
     * 验证语料的正确标注
     */
    private String[] expectedTags;

    /**
     * 词典
     */
    private HashSet<String> dict;

    /**
     * 评估器
     */
    private WordPOSMeasure measure;

    public ModelTesting(WordTagStream training, WordTagStream testing, NGram nGram) {
        this.training = training;
        this.testing = testing;
        this.nGram = nGram;
    }

    @Override
    public void toScore() throws IOException {
        this.getTagger();
        this.estimate();
    }

    /**
     * 通过训练集获得隐藏状态标注器
     */
    private void getTagger() throws IOException {

        Tagger tagger = null;
        AbstractParas paras = null;
        HMM hmm = null;
        if (this.nGram == NGram.BiGram) {
            paras = new BigramParas(this.training);
            hmm = new HMM1st(paras);
        } else if (this.nGram == NGram.TriGram) {
            paras = new TrigramParas(this.training);
            hmm = new HMM2nd(paras);
        }
        this.tagger = new Tagger(hmm);
        this.dict = paras.getDictionary().getWordSet();
        this.measure = new WordPOSMeasure(this.dict);
    }

    /**
     * 在测试集上进行测试
     *
     */
    private void estimate() throws IOException {
        WordTag[] wts = null;

        while ((wts = this.testing.readSentence()) != null) {
            this.getTagOfValidation(wts);
            WordTag[] predict = this.tagger.tag(this.unknownSentence);
            String[] words =new String[predict.length];
            String[] predictTags = new String[predict.length];
            for (int j = 0; j < predict.length; ++j) {
                predictTags[j] = predict[j].getTag();
                words[j]=predict[j].getWord();
            }
            this.measure.updateScores(words,this.expectedTags,predictTags);
        }
    }


    /**
     * 分割验证集观察状态和隐藏状态
     *
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
    public WordPOSMeasure getScores() {
        return this.measure;
    }

}
