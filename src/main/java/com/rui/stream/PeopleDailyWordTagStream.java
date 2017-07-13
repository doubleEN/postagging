package com.rui.stream;

import com.rui.ngram.IWordTagGenerator;
import com.rui.ngram.PeopleDailyNewsWordTagGenerator;
import com.rui.ngram.WordTag;

import java.io.BufferedReader;

/**
 *
 */
public class PeopleDailyWordTagStream extends WordTagStream {

    protected BufferedReader br;


    public PeopleDailyWordTagStream(String corpusPath) {
        this.openReadStream(corpusPath);
    }

    /**
     * 以空白符分割[wordtag]的句子做分割处理
     *
     * @param sentence 一行独立的句子。
     * @return
     */
    @Override
    public WordTag[] segSentence(String sentence) {
        String[] wordTags = sentence.split("\\s+");
        WordTag[] wt = new WordTag[wordTags.length];
        IWordTagGenerator generator=new PeopleDailyNewsWordTagGenerator();
        for (int i = 0; i < wordTags.length; i++) {
            wt[i] = generator.separateWordTag(wordTags[i]);
        }
        return wt;
    }
}
