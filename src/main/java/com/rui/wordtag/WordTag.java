package com.rui.wordtag;

/**
 * 存储word及其tag。
 */
public class WordTag {

    private String word;
    private String tag;

    public WordTag(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    @Override
    public String toString() {
        return this.word + "/" + this.tag;
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }
}
