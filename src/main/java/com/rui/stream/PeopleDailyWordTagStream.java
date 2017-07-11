package com.rui.stream;

import com.rui.ngram.AbstractWordTag;
import com.rui.ngram.WordTag;

import java.io.BufferedReader;

/**
 *
 */
public class PeopleDailyWordTagStream extends WordTagStream {

    protected BufferedReader br;


    public PeopleDailyWordTagStream(String corpusPath) {
        this.corpusPath=corpusPath;
        this.openReadStream();
    }

    @Override
    public AbstractWordTag[] segSentence(String sentence) {

        String[] wordTags = sentence.split("\\s+");
        AbstractWordTag[] wt = new WordTag[wordTags.length];
        for (int i = 0; i < wordTags.length; i++) {
            AbstractWordTag part = new WordTag(wordTags[i].trim());
            wt[i] = part;
        }
        return wt;
    }
}
