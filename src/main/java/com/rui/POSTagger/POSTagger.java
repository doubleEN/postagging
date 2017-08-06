package com.rui.POSTagger;

import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.tagger.Tagger;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

/**
 * 汉语词性标注工具类
 */
public class POSTagger {

    public static void main(String[] args) {

        System.out.println(Arrays.toString(POSTagger.tag2Gram("学好 自然 语言 处理 ， 实现 台湾 统一  。")));
        System.out.println(Arrays.toString(POSTagger.tag3Gram("学好 自然 语言 处理 ， 实现 台湾 统一  。")));

    }

    //2-gram标注
    public static WordTag[] tag2Gram(String sentence) {

        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("target/classes/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String BiGram = (String) pro.get("BiGram");
            tagger = new Tagger(BiGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }

    //3-gram标注
    public static WordTag[] tag3Gram(String sentence) {
        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("target/classes/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String TriGram = (String) pro.get("TriGram");
            tagger = new Tagger(TriGram);

            propertiesPath.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tagger.tag(sentence);
    }
}
