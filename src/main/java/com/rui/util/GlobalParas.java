package com.rui.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 预定义的全局参数
 */
public class GlobalParas {

    //日志
    public static Logger logger = Logger.getLogger("POSTagger");
    static
    {
        logger.setLevel(Level.INFO);
    }
}
