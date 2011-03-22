package org.sqlrecorder.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    public static Logger loggerForThisClass() {
        // We use the third stack element; first is .getStackTrace(),second is this method
        StackTraceElement myCaller = Thread.currentThread().getStackTrace()[2];
        return LoggerFactory.getLogger(myCaller.getClassName());
    }
}
