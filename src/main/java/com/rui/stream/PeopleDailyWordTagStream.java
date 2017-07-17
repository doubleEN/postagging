package com.rui.stream;

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

        for (int i = 0; i < wordTags.length; i++) {

            String[] wordAndTag = wordTags[i].trim().split("/");
            if (wordAndTag.length != 2) {
                System.out.println(wordAndTag.length);
                System.err.println("word-tag 长度不为2");
            }
            wt[i] = new WordTag(wordAndTag[0], wordAndTag[1]);
        }
        return wt;
    }
}
