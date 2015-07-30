package com.library.callback;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.library.utils.Debug;

import java.io.Closeable;
import java.lang.reflect.Method;

import static com.library.callback.AjaxUtility.postAsync;

/**
 * ajax 请求的所有公共方法
 * <p/>
 * Created by chen_fulei on 2015/7/28.
 */
public class AjaxUtility {


    /**
     * 对象反射
     * @param object 对象
     * @param callback 对象方法
     * @param fallback 是否返回
     * @param cls 返回的类型【JSON.class , String.class ....】
     * @param cls2
     * @param params 返回的参数
     * @return
     * @throws Exception
     */
    public static Object invokeMethod(Object object , String callback , boolean fallback , Class<?>[] cls , Class<?> cls2 , Object... params){
        if (object == null || callback == null) return null;

        Method method = null;

        try {
            if (cls == null) cls = new Class[0];

            method = object.getClass().getMethod(callback , cls);
            return method.invoke(object , params);
        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }

        try {

            if (fallback){

                if (cls2 == null){
                    method = object.getClass().getMethod(callback);
                    return method.invoke(object);
                }else{
                    method = object.getClass().getMethod(callback , cls2);
                    return method.invoke(object , params);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }

        return null;
    }
    public static Object invokeMethod(Object object , String callback , boolean fallback , Class<?>[] cls , Object... params){
        return invokeMethod(object , callback , fallback , cls , null , params);
    }


    //创建异步线程（不在主线程）
    private static Handler handler;
    public static Handler getHandler(){
        if (handler == null){
            handler = new Handler(Looper.getMainLooper());
        }

        return  handler;
    }

    /**
     * 执行线程请求
     * @param runnable
     */
    public static void post(Runnable runnable ){
        getHandler().post(runnable);
    }

    /**
     * 执行异步反射
     * @param object
     * @param method
     * @param cls
     * @param params
     */
    public static void post(final Object object , final String method , final Class<?>[] cls , final Object... params){
        post(new Runnable() {

            @Override
            public void run() {
                //反射
                AjaxUtility.invokeMethod(object, method, false, cls, params);
            }
        });
    }
    public static void post(Object object , String method){
        post(object , method , new Class[0]);
    }

    /**
     * 同步执行
     * @param runnable
     */
    public static void postAsync(final Runnable runnable){
        AsyncTask<Void , Void , String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    runnable.run();

                }catch (Exception e){
                    e.printStackTrace();
                    Debug.Log(e);
                }

                return null;
            }
        };

        task.execute();
    }

    /**
     * 执行反射
     * @param object
     * @param method
     * @param cls
     * @param params
     */
    public static void postAsync(final Object object , final String method , final Class<?>[] cls , final Object... params){
        postAsync(new Runnable() {
            @Override
            public void run() {
                AjaxUtility.invokeMethod(object, method, false, cls, params);
            }
        });
    }
    public static void postAsync(Object object , String method){
        postAsync(object , method , new Class[0]);
    }


    /**
     * 判断是否为主线程
     * @return
     */
    public static boolean isUIThread(){
        long uiId = Looper.getMainLooper().getThread().getId();
        long cId = Thread.currentThread().getId();

        return uiId == cId;
    }

    /**
     * 注销线程
     * @param runnable
     */
    public static void removePost(Runnable runnable){
        getHandler().removeCallbacks(runnable);
    }

    /**
     * 在规定时间内注销线程
     * @param runnable
     * @param delay
     */
    public static void postDelayed(Runnable runnable , long delay){
        getHandler().removeCallbacks(runnable , delay);
    }


    /**
     * 关闭
     *
     * @param closeable
     */
    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            Debug.Log(e);
        }
    }

    /**********************生产 Base64 characters  start**************************/
    private static final char[] map1 = new char[64];
    static {
        int i=0;
        for (char c='A'; c<='Z'; c++) map1[i++] = c;
        for (char c='a'; c<='z'; c++) map1[i++] = c;
        for (char c='0'; c<='9'; c++) map1[i++] = c;
        map1[i++] = '+'; map1[i++] = '/'; }

    // Mapping table from Base64 characters to 6-bit nibbles.
    private static final byte[] map2 = new byte[128];
    static {
        for (int i=0; i<map2.length; i++) map2[i] = -1;
        for (int i=0; i<64; i++) map2[map1[i]] = (byte)i; }

    //Source: http://www.source-code.biz/base64coder/java/Base64Coder.java.txt
    public static char[] encode64(byte[] in, int iOff, int iLen) {

        int oDataLen = (iLen*4+2)/3;       // output length without padding
        int oLen = ((iLen+2)/3)*4;         // output length including padding
        char[] out = new char[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;
        while (ip < iEnd) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
            int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 &   3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '='; op++;
            out[op] = op < oDataLen ? map1[o3] : '='; op++; }
        return out;
    }
/**********************生产 Base64 characters  end**************************/

}
