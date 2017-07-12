package com.rui.statistic;

import com.rui.dictionary.AbstractDictionary;
import com.rui.ngram.AbstractWordTag;
import com.rui.ngram.WordTag;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.stream.WordTagStream;

import java.util.Arrays;

/**
 *
 */
public abstract class AbstractParasStats {

    protected int[][][] numMatA;

    protected int[][] numMatB;

    protected int[] numPi;

    protected AbstractDictionary dictionary;

    public void countParas(String corpusPath) {
        WordTagStream wordTagStream = this.openStream(corpusPath);
        AbstractWordTag[] wts;
        while ((wts = wordTagStream.readLine()) != null) {
            this.countParas(wts);
        }
        wordTagStream.close();
    }

    public void countParas(AbstractWordTag[] wts) {
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

    protected abstract WordTagStream openStream(String corpusPath);

    protected abstract void countMatA(String[] tags);

    protected abstract void countMatB(String[] words, String[] tags);

    protected abstract void countPi(String[] tags);

    //Don't use copyOf or arrayCopy.
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

    public AbstractDictionary getDictionary() {
        return dictionary;
    }
}
