package com.rui.app;

import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.validation.*;

import static com.rui.util.GlobalParas.logger;

/**
 * 模型测试主函数。
 */
public class TaggingTest {

    /**
     * 指定语料库进行模型的验证
     * @param args  [0]:人民日报词性标注语料格式训练数据路径
     *              [1]:人民日报词性标注语料格式测试数据路径
     *              [2]:n-gram参数，2或者3，分别代表二元或三元
     *              [3]:训练语料字符编码方式
     *              [4]:测试语料字符编码方式
     */
    public static void main(String[] args) throws Exception{
        if (args.length != 5) {
            logger.severe("参数数目不合法，数目为" + args.length + ",应为5。");
            System.exit(1);
        }

        NGram nGram = null;

        if (args[2].equals("2")) {
            nGram = NGram.BiGram;
        } else if (args[2].equals("3")) {
            nGram = NGram.TriGram;
        } else {
            logger.severe("n-gram参数形式不合法：参数值应为 2 或 3 。");
            System.exit(1);
        }

        ModelScore modelScore = new ModelTesting(new PeopleDailyWordTagStream(args[0],args[3]),new PeopleDailyWordTagStream(args[1],args[4]), nGram);
        modelScore.toScore();
        logger.info("交加验证评分为：\n "+ modelScore.getScores().toString());
    }
}
