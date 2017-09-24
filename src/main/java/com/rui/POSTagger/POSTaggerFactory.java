package com.rui.POSTagger;

import com.rui.evaluation.Estimator;
import com.rui.model.HMM;
import com.rui.model.HMM1st;
import com.rui.model.HMM2nd;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.WordTagStream;
import com.rui.tagger.Tagger;
import com.rui.validation.CrossValidation;
import com.rui.validation.ModelScore;
import com.rui.validation.NGram;
import com.rui.validation.Validation;
import com.rui.wordtag.WordTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static com.rui.util.GlobalParas.logger;

/**
 * 汉语词性标注工具类
 */
public class POSTaggerFactory {

 /*   public static void main(String[] args) throws IOException{
        AbstractParas paras = new BigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), 44, 50000);
        HMM hmm = new FirstOrderHMM(paras);
        hmm.writeHMM("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/BiGram.bin");

        AbstractParas paras2 = new TrigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt"), 44, 50000);
        HMM hmm2 = new SecondOrderHMM(paras);
        hmm.writeHMM("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/TriGram.bin");

        WordTag[]wts1=POSTaggerFactory.tag2Gram("学习 自然 语言 处理 ， 实现 台湾 统一 。");
        System.out.println(Arrays.toString(wts1));
        WordTag[]wts2=POSTaggerFactory.tag3Gram("学习 自然 语言 处理 ， 实现 台湾 统一 。");
        System.out.println(Arrays.toString(wts2));
    }
*/
    /**
     * 一次验证评估
     *
     * @param stream    读取特点语料的特点流
     * @param ratio     验证集比例
     * @param nGram     {@link NGram}常量类型
     * @param estimator 指定度量方式
     * @return 返回评分
     */
    public static double[] scoreOnValidation(WordTagStream stream, double ratio, NGram nGram, Estimator estimator) throws IOException{
        ModelScore crossValidation = new Validation(stream, ratio, nGram, estimator);
        crossValidation.toScore();
        return crossValidation.getScores();
    }
    /**
     * 交叉验证评估
     *
     * @param stream    读取特点语料的特点流
     * @param fold      交叉验证折数
     * @param nGram     {@link NGram}常量类型
     * @param estimator 指定度量方式
     * @return 返回评分
     */
    public static double[] crossValidation(WordTagStream stream, int fold, NGram nGram, Estimator estimator) throws IOException{
        ModelScore crossValidation = new CrossValidation(stream, fold, nGram, estimator);
        crossValidation.toScore();
        return crossValidation.getScores();
    }
    /**
     * 指定语料生成标注器
     *
     * @param stream    读取特点语料的特点流
     * @param nGram     {@link NGram}常量类型
     * @param writePath 模型序列化路径
     * @throws IOException
     */
    public static void writeHMM(WordTagStream stream, NGram nGram, String writePath) throws IOException {

        AbstractParas paras = null;
        HMM hmm = null;
        if (nGram == NGram.BiGram) {
            paras = new BigramParas(stream);
            hmm = new HMM1st(paras);
        } else if (nGram == NGram.TriGram) {
            paras = new TrigramParas(stream);
            hmm = new HMM2nd(paras);
        }

        hmm.writeHMM(writePath);
    }

    /**
     * 指定语料生成标注器
     *
     * @param stream 读取特点语料的特点流
     * @param nGram  {@link NGram}
     * @return 标注器
     */
    public static Tagger buildTagger(WordTagStream stream, NGram nGram) throws IOException{
        Tagger tagger = null;

        AbstractParas paras = null;
        HMM hmm = null;
        if (nGram == NGram.BiGram) {
            paras = new BigramParas(stream);
            hmm = new HMM1st(paras);
            tagger = new Tagger(hmm);
        } else if (nGram == NGram.TriGram) {
            paras = new TrigramParas(stream);
            hmm = new HMM2nd(paras);
            tagger = new Tagger(hmm);
        }

        return tagger;
    }

    /**
     * 从指定模型路径中生成标注器
     *
     * @param HMMPath 反序列化模型的路径
     * @return 标注器
     */
    public static Tagger buildTagger(String HMMPath) throws IOException, ClassNotFoundException {
        return new Tagger(HMMPath);
    }

    /**
     * 2-gram标注
     *
     * @param sentence 未标注句子
     * @return WordTag[]形式的标注结果
     */
    public static WordTag[] tag2Gram(String sentence) throws IOException, ClassNotFoundException {

        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String BiGram = (String) pro.get("BiGram");
            tagger = new Tagger(BiGram);

            propertiesPath.close();

        } catch (IOException e) {
            logger.severe("序列化模型的路径不合法，" + e.getMessage());
            throw e;
        } catch (ClassNotFoundException e) {
            logger.severe("模型对象不存在，" + e.getMessage());
            throw e;
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                logger.severe("序列化模型读取流关闭错误，" + e.getMessage());
                throw e;
            }
        }
        return tagger.tag(sentence);
    }

    /**
     * 3-gram标注
     *
     * @param sentence 未标注句子
     * @return WordTag[]形式的标注结果
     */
    public static WordTag[] tag3Gram(String sentence) throws ClassNotFoundException, IOException {
        Properties pro = new Properties();
        Tagger tagger = null;
        InputStreamReader propertiesPath = null;
        try {
            propertiesPath = new InputStreamReader(new FileInputStream("/home/mjx/IdeaProjects/tags/src/main/java/com/rui/POSTagger/tag.properties"), "UTF-8");
            pro.load(propertiesPath);
            String TriGram = (String) pro.get("TriGram");
            tagger = new Tagger(TriGram);

            propertiesPath.close();

        } catch (IOException e) {
            logger.severe("序列化模型的路径不合法，" + e.getMessage());
            throw e;
        } catch (ClassNotFoundException e) {
            logger.severe("模型对象不存在，" + e.getMessage());
            throw e;
        } finally {
            try {
                propertiesPath.close();
            } catch (IOException e) {
                logger.severe("序列化模型的路径不合法，" + e.getMessage());
                throw e;
            }
        }
        return tagger.tag(sentence);
    }
}
