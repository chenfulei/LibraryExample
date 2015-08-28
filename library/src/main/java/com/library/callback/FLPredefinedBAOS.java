package com.library.callback;

import java.io.ByteArrayOutputStream;

/**
 * Return the buffered array as is if the predefined size matches exactly the result byte array length.
 * Reduce memory allocation by half by avoiding array expand and copy.
 *
 * Created by chen_fulei on 2015/8/1.
 */
public class FLPredefinedBAOS extends ByteArrayOutputStream{

    public FLPredefinedBAOS(int size){
        super(size);
    }

    @Override
    public byte[] toByteArray(){

        if(count == buf.length){
            return buf;
        }

        return super.toByteArray();

    }

}
