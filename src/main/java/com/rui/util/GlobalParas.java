package com.rui.util;

import org.omg.CORBA.PUBLIC_MEMBER;

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
