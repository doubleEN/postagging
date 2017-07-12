package com.rui.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.rui.utils.IOUtils.readCorpusFromFile;

/**
 * Handle the corpus to get two categories of dictionaries.
 * Id begins with "0".
 */
public class PeopleDailyNewsStateNum extends AbstractStateNum {

    public static void main(String[] args) {
        AbstractStateNum dictFactory = new PeopleDailyNewsStateNum("/home/mjx/桌面/PoS/199801/199801_format.txt");
        dictFactory.createIndex();
        Map m1 = dictFactory.getWordDict();
        Map m2 = dictFactory.getWordId();


        System.out.println(m1.size()+":"+m2.size());
    }


    public PeopleDailyNewsStateNum(String corpusPath) {
        this.corpus = readCorpusFromFile(corpusPath);
        this.createIndex();
    }

    public PeopleDailyNewsStateNum(Map corpus) {
        this.corpus = corpus;
        this.createIndex();
    }

    @Override
    public void createIndex() {
        //All of id begin with index of 1.
        Integer tagId = 0;
        Integer wordId = 0;
        String line;

        Set<Map.Entry<Integer, String>> entrys = this.corpus.entrySet();
        Iterator<Map.Entry<Integer, String>> iterEntrys = entrys.iterator();

        while (iterEntrys.hasNext()) {
            line = iterEntrys.next().getValue().trim();
            String[] groups = line.split("\\s+");
            for (String group : groups) {
                int point = group.indexOf("/");
                String word = group.substring(0, point).trim();
                String tag = group.substring(point + 1, group.length()).trim();
//                    System.out.println("word:" + word +"\t"+ "tag:" + tag);
                if (!this.tagId.containsKey(tag)) {
                    this.tagId.put(tag, tagId);//'put()' return the old 'value'.
                    this.tagDict.put(tagId, tag);
                    ++tagId;
                }
                if (!this.wordId.containsKey(word)) {
                    this.wordId.put(word, wordId);
                    this.wordDict.put(wordId, word);
                    ++wordId;
                }
            }

        }

    }

}
