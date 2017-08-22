package com.rui.util;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 预定义的全局参数
 */
public class GlobalParas {

    //PeopleDailyNews语料库标注集大小
    public static int tagSizePDN=44;

    //PeopleDailyNews语料库词集大小
    public static int wordSizePDN=55310;

    //日志
    public static Logger logger = Logger.getLogger("POSTagger");
    static
    {
        logger.setLevel(Level.INFO);
    }
}
