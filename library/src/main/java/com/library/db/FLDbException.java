package com.library.db;

/**
 * db 异常处理
 *
 * Created by chen_fulei on 2015/8/29.
 */
public class FLDbException extends Exception {
    private static final long serialVersionUID = 1L;

    public FLDbException(){
    }

    public FLDbException(String detailMessage){
        super(detailMessage);
    }

    public FLDbException(String detailMessage, Throwable throwable){
        super(detailMessage , throwable);
    }

    public FLDbException(Throwable throwable){
        super(throwable);
    }

}
