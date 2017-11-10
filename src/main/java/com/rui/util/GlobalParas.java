package com.rui.util;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 预定义的全局参数
 */
public class GlobalParas {

    /**
     * 平滑参数
     */
    public static final String DELETE_INTERPOLATION="DELETE_INTERPOLATION";
    public static final String LAPLACE="LAPLACE";
    public static final String GOOD_TURING="GOOD_TURING";

    /**
     * 未登录词处理
     */
    public static final int UNK_MAXPROB=1;
    public static final int UNK_INITPROB=2;
    public static final int UNK_ZXF=3;

    public static String getUnkHandle(int unkHandle) {

        if (unkHandle == GlobalParas.UNK_MAXPROB) {
            return "UNK_MAXPROB";
        }

        if (unkHandle == GlobalParas.UNK_INITPROB) {
            return "UNK_INITPROB";
        }

        if (unkHandle == GlobalParas.UNK_ZXF) {
            return "UNK_ZXF";
        }

        return "未指明有效的未登录词处理方式";
    }

    /**
     * PeopleDailyNews语料库标注集大小
     */
    public static int tagSizePDN=44;

    /**
     * PeopleDailyNews语料库词集大小
     */
    public static int wordSizePDN=55310;

    /**
     * 全局日志
     */
    public static Logger logger = Logger.getLogger("POSTagger");

    /**
     * 声明日志level
     */
    static
    {
        logger.setLevel(Level.INFO);
    }
}
