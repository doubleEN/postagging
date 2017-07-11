package com.rui.ngram;

/**
 *
 */
public class WordTag extends AbstractWordTag{

    @Override
    public void separateWordTag() {

        String[] wordAndTag = wordTag.split("/");
        this.word = wordAndTag[0];
        this.tag = wordAndTag[1];

    }

    public WordTag(String wordTag) {
        this.wordTag = wordTag;
        this.separateWordTag();
    }

    @Override
    public String toString() {
        return this.wordTag;
    }
}
