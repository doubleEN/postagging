package com.rui.ngram;

/**
 *
 */
public abstract class AbstractWordTag {

    protected  String wordTag;
    protected  String word;
    protected  String tag;


    public abstract void separateWordTag();


    public String getWordTag() {
        return wordTag;
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }

}
