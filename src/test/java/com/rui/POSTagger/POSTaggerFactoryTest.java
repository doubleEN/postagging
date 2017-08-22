package com.rui.POSTagger;

import com.rui.evaluation.PreciseIV;
import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.tagger.Tagger;
import com.rui.validation.NGram;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 */
public class POSTaggerFactoryTest {

    @Test
    public void main() throws Exception {

        //序列化的模型中得到2-gram模型
        Tagger tagger2GramSerializable=POSTaggerFactory.buildTagger("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/BiGram.bin");
        //预料中获得2-gram模型
        Tagger tagger2GramCorpus=POSTaggerFactory.buildTagger(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), NGram.BiGram);

        //序列化的模型中得到3-gram模型
        Tagger tagger3GramSerializable=POSTaggerFactory.buildTagger("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/TriGram.bin");
        //预料中获得3-gram模型
        Tagger tagger3GramCorpus=POSTaggerFactory.buildTagger(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), NGram.TriGram);

        String unknownSentence="学习 NLP ， 实现 台湾 同意 。 ";
        //2-gram
        assertEquals(Arrays.toString(tagger2GramCorpus.tag(unknownSentence)),Arrays.toString(tagger2GramSerializable.tag(unknownSentence)));
        //3-gram
        assertEquals(Arrays.toString(tagger3GramCorpus.tag(unknownSentence)),Arrays.toString(tagger3GramSerializable.tag(unknownSentence)));

    }

}