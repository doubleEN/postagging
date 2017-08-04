package com.rui.evaluation;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
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
//        CrossValidation cv = new CrossValidation("/home/mjx/桌面/PoS/test/testSet2.txt", 3, NGram.BiGram, new PeopleDailyWordTagStream());
//        cv.evalK();
//        Set<WordTag[]>[] set = cv.getTrainTest();
//        int i = 0;
//
//        int fold=1;
//        for (Set<WordTag[]> wts : set) {
//            System.out.println("fold:"+fold);
//            for (WordTag[] wt:wts){
//                System.out.println(Arrays.toString(wt));
//            }
//            fold++;
//            System.out.println("size:"+wts.size());
//        }
    }

    private NGram nGram;

    private HMM hmm;

    private Tagger tagger;

    private WordTagStream stream;

    private Set<WordTag[]>[] trainTest;

    public enum NGram {
        BiGram, TriGram;
    }

    public CrossValidation(String corpusPath, int fold, NGram nGram, WordTagStream stream) {
        this.trainTest = new HashSet[fold];
        this.nGram=nGram;
        this.stream = stream;
        this.stream.openReadStream(corpusPath);
    }

    public double[] evalK() {
        double[] scores = new double[this.trainTest.length];
        List<WordTag[]> sentences = this.readCorpus();
        this.trainTestSplit(sentences);
        return scores;
    }

    private void trainTestSplit(List<WordTag[]> sentences) {
        Random rand = new Random(11);
        //每一折的大小
        int range = sentences.size() / this.trainTest.length+1;
        for (int k = 0; k < this.trainTest.length; ++k) {
            this.trainTest[k] = new HashSet<WordTag[]>();
            for (int num = 0; num < range&&sentences.size()!=0; ++num) {
                WordTag[] wts = sentences.get(0);
                this.trainTest[k].add(wts);
                //list适合做随机划分多个子集
                sentences.remove(0);
                Collections.shuffle(sentences, rand);
            }
        }

    }

    private List<WordTag[]> readCorpus() {
        List<WordTag[]> sentences = new ArrayList<WordTag[]>();
        WordTag[] wt = null;
        while ((wt = this.stream.readSentence()) != null) {
            sentences.add(wt);//按序添加
        }
        return sentences;
    }

    public double eval(int validatonNo){
        if (validatonNo>this.trainTest.length+1||validatonNo<1){
            System.err.println("验证集标号不合法。");
            return -1;
        }

        if (this.nGram==NGram.BiGram){
            AbstractParas paras=new BigramParas();
            for (int k=0;k<this.trainTest.length;k++){

            }
        }

        return 0;
    }

    private Tagger builTagger(){
        return new Tagger("");
    }


    public HMM getHmm() {
        return hmm;
    }

    public Tagger getTagger() {
        return tagger;
    }

    public WordTagStream getStream() {
        return stream;
    }

    public Set<WordTag[]>[] getTrainTest() {
        return trainTest;
    }
}

