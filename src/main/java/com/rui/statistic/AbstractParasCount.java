package com.rui.statistic;

import com.rui.dictionary.DictFactory;
import com.rui.ngram.WordTag;
import com.rui.stream.WordTagStream;

/**
 *
 */
public abstract class AbstractParasCount {

    protected int[][][] numMatA;

    protected int[][] numMatB;

    protected int[] numPi;

    protected DictFactory dictionary;

    //从语料库中累计参数
    public void countParas(String corpusPath) {
        WordTagStream wordTagStream = this.openStream(corpusPath);
        WordTag[] wts;
        while ((wts = wordTagStream.readSentence()) != null) {
            this.countParas(wts);
        }
        wordTagStream.close();
    }

    //从wordTag数组中累计数组
    public void countParas(WordTag[] wts) {
//        System.out.println(Arrays.toString(wts));
        dictionary.addIndex(wts);
        String[] words = new String[wts.length];
        String[] tags = new String[wts.length];
        for (int i = 0; i < wts.length; ++i) {
            words[i] = wts[i].getWord();
            tags[i] = wts[i].getTag();
        }
        this.countMatA(tags);
        this.countMatB(words, tags);
        this.countPi(tags);
    }

    //打开特定的语料库
    protected abstract WordTagStream openStream(String corpusPath);

    //统计参数
    protected abstract void countMatA(String[] tags);

    protected abstract void countMatB(String[] words, String[] tags);

    protected abstract void countPi(String[] tags);

    //扩展参数数组
    protected void reBuildA() {
        int[][][] newA = new int[1][this.dictionary.getTagId().size()][this.dictionary.getTagId().size()];
        int[][] midA = this.numMatA[0];
        for (int i = 0; i < this.numMatA[0].length; ++i) {
            for (int j = 0; j < this.numMatA[0].length; ++j) {
                newA[0][i][j] = midA[i][j];
            }
        }
        this.numMatA = newA;
    }

    protected void reBuildB() {
        int row = this.dictionary.getTagId().size() > this.numMatB.length ? this.dictionary.getTagId().size() : this.numMatB.length;
        int col = this.dictionary.getWordId().size() > this.numMatB[0].length ? this.dictionary.getWordId().size() : this.numMatB[0].length;
        int[][] newB = new int[row][col];
        for (int i = 0; i < this.numMatB.length; ++i) {
            for (int j = 0; j < this.numMatB[0].length; ++j) {
                newB[i][j] = this.numMatB[i][j];
            }
        }
        this.numMatB = newB;
    }

    protected void reBuildPi() {
        int[] pi = new int[this.dictionary.getTagId().size()];
        for (int i = 0; i < this.numPi.length; ++i) {
            pi[i] = this.numPi[i];

        }
        this.numPi = pi;
    }



    public int[][][] getNumMatA() {
        return numMatA;
    }

    public int[][] getNumMatB() {
        return numMatB;
    }

    public int[] getNumPi() {
        return numPi;
    }

    public DictFactory getDictionary() {
        return dictionary;
    }
}
