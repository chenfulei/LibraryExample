package com.library.callback;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.library.constants.FLConstants;
import com.library.utils.Debug;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        post(object, method, new Class[0]);
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
        postAsync(object, method, new Class[0]);
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


/****************************文件操作******************************/
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

    /**
     * 数据写进文件
     * @param file
     * @param data
     */
    public static void write(File file , byte[] data){
        try {
            if (!file.exists()){
                try{
                    file.createNewFile();
                }catch (Exception e){
                    e.printStackTrace();
                    Debug.Log("file create fail");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 保存数据到文件
     * @param file
     */
    public static void store(File file , byte[] data){
        try {

            if (file != null){
                AjaxUtility.write(file, data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 可以提供按照规定时间执行
     */
    private static ScheduledExecutorService storeExe;
    private static ScheduledExecutorService getFileStoreExecutor(){
        if (storeExe == null){
            storeExe = Executors.newSingleThreadScheduledExecutor();
        }

        return storeExe;
    }

    /**
     * 删除缓存文件(同步)
     * @param context
     * @param triggerSize
     * @param targetSize
     */
    public static void cleanCacheAsync(Context context , long triggerSize , long targetSize){
        try {
            File cacheDir = getCacheDir(context);

            //反射删除缓存
            Common common = new Common().method(Common.CLEAN_CACHE , cacheDir , triggerSize , targetSize);
            ScheduledExecutorService executorService = getFileStoreExecutor();
            executorService.schedule(common , 0 , TimeUnit.MILLISECONDS); //规定时间去执行
        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }
    }

    /**
     * 删除缓存文件
     * @param cacheDir 目标缓存文件根目录
     * @param triggerSize // 最小值
     * @param targetSize // 最大值
     */
    public static void cleanCache(File cacheDir , long triggerSize , long targetSize){
        try {
            File[] files = cacheDir.listFiles();
            if (files == null) return;
            Arrays.sort(files , new Common());// 排序 (按照文件修改的时间排顺序)

            if (cleanNeeded(files , triggerSize)){ //删除缓存文件
                cleanCache(files , targetSize);
            }

            File temp = getTempDir(); // 删除临时文件
            if (temp != null && temp.exists()){
                cleanCache(temp.listFiles() , 0);
            }
        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }
    }


    /**
     * 创建零时文件
     * @return
     */
    public static File getTempDir(){
        File ext = Environment.getExternalStorageDirectory();
        File tempDir = new File(ext , "library/temp");
        tempDir.mkdirs();

        if (!tempDir.exists() || !tempDir.canWrite()){ // 如果不能创建或者不能读写的话表示创建失败
            return  null;
        }

        return tempDir;
    }

    /***
     * 判断是否必须删除的文件
     * @param files
     * @param triggerSize
     */
    private static boolean cleanNeeded(File[] files , long triggerSize){
        long total = 0;
        for (File file : files){
            total += file.length();

            if (total > triggerSize){
                return true;
            }
        }

        return false;
    }

    /**
     * 清除缓存(一旦文件大小总值超过max 就会删除掉)
     * @param files
     * @param maxSize // 设置最大值
     */
    private static void cleanCache(File[] files , long maxSize){
        int deletes = 0;
        long total = 0;

        for (int i= 0; i < files.length ; i ++){
            File f = files[i];

            if (f.isFile()){
                //文件大小相加
                total += f.length();

                if (total >= maxSize){ // 总值超过max时会删除之后的文件
                    deletes ++;
                    f.delete();
                }
            }
        }

        Debug.Log("cleanCache deletes file : "+ deletes);
    }

    private static File cacheDir; // 缓存文件根目录
    private static File pcacheDir;
    /**
     * 获取缓存文件根目录
     * @param context
     * @param policy // 缓存指针
     * @return
     */
    public static File getCacheDir(Context context , int policy){
        if (policy == FLConstants.CACHE_PERSISTENT){
            if (pcacheDir != null) return pcacheDir;

            File file = getCacheDir(context);
            pcacheDir = new File(file , "persistent");
            pcacheDir.mkdirs();

            return pcacheDir;
        }else {
            return  getCacheDir(context);
        }
    }
    public static File getCacheDir(Context context){
        if (cacheDir == null){
            cacheDir = new File(context.getCacheDir() , "library"); // 默认的缓存文件根目录
            cacheDir.mkdirs();
        }

        return cacheDir;
    }

    /**
     * 设置缓存文件根目录
     * @param dir
     */
    public static void setCacheDir(File dir){
        cacheDir = dir;
        if (cacheDir != null){
            cacheDir.mkdirs();
        }
    }

    private static final int IO_BUFFER_SIZE = 1024 *4; // 最大速度
    private static final boolean TEST_IO_EXCEPTION = false ;//是否捕捉异常

    /**
     *将输入转换为输出 并设置进度条
     * @param inputStream
     * @param outputStream
     * @throws IOException
     */
    public static void copy(InputStream inputStream , OutputStream outputStream) throws IOException{
        copy(inputStream , outputStream , 0 , null);
    }

    /**
     * 将输入转换为输出 并设置进度条
     * @param inputStream
     * @param outputStream
     * @param max // 该文件的总size
     * @param progress // 进度条
     * @throws IOException
     */
    public static void copy(InputStream inputStream , OutputStream outputStream , int max , Progress progress) throws IOException{
        if (progress !=null){
            progress.reset(); //重置
            progress.setBytes(max); // 设置最大值
        }

        byte[] b = new  byte[IO_BUFFER_SIZE];
        int read = 0;
        int count = 0;

        while ((read = inputStream.read()) != -1){
            outputStream.write(b , 0 , count);
            count ++;

           if (progress != null){
               progress.increment(read); // 显示进度
           }
        }

        //关闭进度条
        if (progress != null ){
            progress.done();
        }
    }


    /**
     * 将inputStream 转换为bytes
     * @param inputStream
     * @return
     */
    public static byte[] toBytes(InputStream inputStream){
        byte[] result = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            //将输入转换为输出
            copy(inputStream , byteArrayOutputStream);
            result = byteArrayOutputStream.toByteArray();

        }catch (Exception e){
            e.printStackTrace();
            Debug.Log(e);
        }

        close(inputStream);

        return result;
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
