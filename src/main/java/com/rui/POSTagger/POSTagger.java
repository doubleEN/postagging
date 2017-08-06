package com.rui.POSTagger;

import com.rui.model.FirstOrderHMM;
import com.rui.model.HMM;
import com.rui.model.SecondOrderHMM;
import com.rui.parameters.AbstractParas;
import com.rui.parameters.BigramParas;
import com.rui.parameters.TrigramParas;
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

//        AbstractParas paras=new BigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
//        HMM hmm=new FirstOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/桌面/BiGram.bin");
//
//        AbstractParas paras2=new TrigramParas("/home/mjx/桌面/PoS/corpus/199801_format.txt",44,50000);
//        HMM hmm2=new SecondOrderHMM(paras);
//        hmm.writeHMM("/home/mjx/桌面/TriGram.bin");


        String[] sentences={
                "谢谢  ！  （  新华社  北京  １２  ３１日  电  ）",
                "在  十五大  精神  指引  下  胜利  前进  —— 元旦  献辞",
                "在  这  辞旧迎新  的  美好  时刻  ，  我  祝  大家 新年  快乐  ，  家庭  幸福  ！",
                "北京  举行  新年  音乐会"
        };

        for (String sentence:sentences){
            WordTag[]wts=POSTagger.tag2Gram(sentence);
            System.out.println(Arrays.toString(wts));
        }
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
