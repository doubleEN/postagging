package com.rui.app;

import com.rui.stream.PeopleDailyWordTagStream;
import com.rui.validation.CrossValidation;
import com.rui.validation.ModelScore;
import com.rui.validation.NGram;

import java.io.IOException;

import static com.rui.util.GlobalParas.logger;

/**
 * 交叉验证主函数
 */
public class HMMValidation {

    /**
     * 指定语料库进行模型的验证
     * @param args  [0]:人民日报语料库路径
     *              [1]:n-gram参数，2或者3，分别代表二元或三元
     *              [2]:验证折数，为大于2数字
     *              [3]:字符编码方式
     *              [4]:留存数据比例
     *              [5]:未登录次处理方式
     */
    public static void main(String[] args) throws Exception{
        if (args.length != 6) {
            logger.severe("参数数目不合法，数目为" + args.length + ",应为6。");
            System.exit(1);
        }
        int cv = -1;
        if ((cv = Integer.parseInt(args[2])) < 2) {
            logger.severe("交叉验证折数至少为2,不能为" + args[2]);
            System.exit(1);
        }

        NGram nGram = null;

        if (args[1].equals("2")) {
            nGram = NGram.BiGram;
        } else if (args[1].equals("3")) {
            nGram = NGram.TriGram;
        } else {
            logger.severe("n-gram参数形式不合法：参数值应为 2 或 3 。");
            System.exit(1);
        }

        ModelScore modelScore = new CrossValidation(new PeopleDailyWordTagStream(args[0],args[3]), cv, nGram,Integer.parseInt(args[4]),Integer.parseInt(args[5]));
        modelScore.toScore();
        logger.info("交加验证评分为： "+ modelScore.getScores().toString());
    }

}
