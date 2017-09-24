package com.rui.app;

import com.rui.POSTagger.POSTaggerFactory;
import com.rui.tagger.Tagger;

import java.io.IOException;
import java.util.Arrays;

import static com.rui.util.GlobalParas.logger;

/**
 * 指定序列化模型未标注句子进行标注。
 */
public class POSTagging {
    /**
     * 指定模型路径对若干句子标注
     * @param args  [0]:模型路径
     *              [1...]:未标注句子序列
     */
    public static void main(String[] args) throws ClassNotFoundException,IOException{
        int len=args.length;
        if (len<=1){
            logger.severe("参数数目不合法，数目为" + args.length + ",至少为2。");
            System.exit(1);
        }
        Tagger tagger= POSTaggerFactory.buildTagger(args[0]);
        for (int i=1;i<args.length;++i) {
            logger.info("\n\t未标注句子:["+args[i]+"]\n"+ "\t标注后句子："+Arrays.toString(tagger.tag(args[i])));
            System.out.println();
        }
    }
}
