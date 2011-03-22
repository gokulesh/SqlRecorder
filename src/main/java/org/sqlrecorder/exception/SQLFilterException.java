package org.sqlrecorder.exception;

public class SQLFilterException extends SQLRecorderException {
    public SQLFilterException(String message) {
        super(message);
    }

    public SQLFilterException(String msg, Throwable t) {
        super(msg, t);
    }
}
