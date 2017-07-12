package com.rui.dictionary;

import com.rui.ngram.AbstractWordTag;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.rui.utils.IOUtils.readCorpusFromFile;

/**
 * Handle the corpus to get two categories of dictionaries.
 * Id begins with "0".
 */
public class DictFactory extends AbstractDictionary {

    public DictFactory() {
    }

    @Override
    public void addIndex(AbstractWordTag[] wts) {
        for (AbstractWordTag wt : wts) {
            String word = wt.getWord();
            String tag = wt.getTag();
            if (!this.tagId.containsKey(tag)) {
                this.tagId.put(tag, this.tagId.size());//'put()' return the old 'value'.begining of zero.
                this.tagDict.put(this.tagDict.size(), tag);
            }
            if (!this.wordId.containsKey(word)) {
                this.wordId.put(word, this.wordId.size());
                this.wordDict.put(this.wordDict.size(), word);
            }

        }

    }
}