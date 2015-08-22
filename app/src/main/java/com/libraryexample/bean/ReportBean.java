package com.libraryexample.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/21.
 */
public class ReportBean {

    int status;
    String message;
    ArrayList<Data> data = new ArrayList<Data>();

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setData(ArrayList<Data> data) {
        this.data = data;
    }

    public ArrayList<Data> getData() {
        return data;
    }


    public static class Data{
        String str;

        public void setStr(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }
    }
}
