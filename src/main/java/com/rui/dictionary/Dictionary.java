package com.rui.dictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.rui.utils.IOUtils.*;

/**
 *
 */
public class Dictionary {

    public static void main(String[] args) {
        Map<Integer, String> corpus = readCorpusFromFile("/home/mjx/桌面/PoS/test/testCount.txt");

        Set<Map.Entry<Integer, String>> sentences = corpus.entrySet();
        for (Map.Entry<Integer, String> sentence : sentences) {
            String line = sentence.getValue().trim();
            String[][] mid = Dictionary.separateSentence(line);

        }
    }

    private HashMap<String, Nature> natures = new HashMap<String, Nature>();

    public static String[][] separateSentence(String sentence) {
        String[] parts = sentence.split("\\s+");
        String[] tags = new String[parts.length];
        String[] words = new String[parts.length];
        int index = 0;
        for (String part : parts) {
            String[] couple = part.split("/");
            words[index] = couple[0];
            tags[index] = couple[1];
            ++index;
        }
        return new String[][]{tags, words};
    }


    class Nature<Gram> {

//    private String tag;

        private Map<Gram, Integer> nextTags;

        private Map<Gram, Integer> nextWords;

        private int numOfTag;
        private int numOfword;

        public Nature(Gram tag) {
            this.nextTags = new HashMap<Gram, Integer>();
            this.nextWords = new HashMap<Gram, Integer>();
        }

        public void countNextState(Gram nextTag) {
            this.nextTags.put(nextTag, this.nextTags.get(nextTag) + 1);
            ++this.numOfTag;
        }

        public void countNextWord(Gram nextWord) {
            this.nextWords.put(nextWord, this.nextWords.get(nextWord) + 1);
            ++this.numOfword;
        }
    }
}
