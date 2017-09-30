package com.rui.app;

import com.rui.model.HMM;
import com.rui.model.HMM1st;
import com.rui.model.HMM2nd;
import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.parameter.TrigramParas;
import com.rui.stream.PeopleDailyWordTagStream;

import java.io.IOException;

import static com.rui.util.GlobalParas.logger;

/**
 * 模型训练主函数
 */
public class HMMTraining {
    /**
     * 从指定语料库
     * @param args [0]:人民日报语料库路径
     *             [1]:n-gram参数，2或者3
     *             [2]:模型输出路径
     *             [3]:语料字符编码方式
     */
    public static void main(String[] args) throws ClassNotFoundException,IOException{
        if (args.length != 4) {
            logger.severe("参数数目不合法，数目为"+args.length+",应为4。");
            System.exit(1);
        }
        //路劲也判断
        AbstractParas paras = null;
        HMM hmm = null;
        if (args[1].equals("2")) {
            paras = new BigramParas(new PeopleDailyWordTagStream(args[0],args[3]));
            hmm = new HMM1st(paras);
        } else if (args[1].equals("3")) {
            paras = new TrigramParas(new PeopleDailyWordTagStream(args[0],args[3]));
            hmm = new HMM2nd(paras);
        } else {
            logger.severe("n-gram参数形式不合法：参数值应为 2 或 3 。");
            System.exit(1);
        }

        try {
            hmm.writeHMM(args[2]);
        }catch (IOException e){
            logger.severe("模型输出路劲:"+args[2]+" 不存在");
        }
    }
}
