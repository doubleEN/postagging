package com.rui.app;

import com.rui.parameter.AbstractParas;
import com.rui.parameter.BigramParas;
import com.rui.stream.PeopleDailyWordTagStream;
import static com.rui.util.GlobalParas.logger;

import java.io.IOException;

/**
 * 建模复杂度测试。
 */
public class Performance {
    public static void main(String[] args) throws IOException,ClassNotFoundException{
        double firstTime=System.currentTimeMillis();
        AbstractParas paras=new BigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt","utf-8"));

        System.out.println();
        double begin1=System.currentTimeMillis();
        for (int i=0;i<100;++i) {
            AbstractParas paras2=new BigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt","utf-8"));
        }
        double end1=System.currentTimeMillis();
        logger.info("2-gram建模时间："+(end1-begin1)/1e5);//0.97755

        double begin2=System.currentTimeMillis();
        for (int i=0;i<100;++i) {
            AbstractParas paras2=new BigramParas(new PeopleDailyWordTagStream("/home/mjx/桌面/PoS/corpus/199801_format.txt","utf-8"));
        }
        double end2=System.currentTimeMillis();
        logger.info("3-gram建模时间："+(end2-begin2)/1e5);//0.88087
    }
}