package org.sqlrecorder.exception;

public class SQLRecorderException extends RuntimeException{
    public SQLRecorderException(String message){
        super(message);
    }

    public SQLRecorderException(String msg, Throwable t){
        super(msg,t);
    }
}
