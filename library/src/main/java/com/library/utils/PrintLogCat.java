package com.library.utils;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 打印logCat当前应用所有信息并保存在当前应用缓存的logcat.txt文件里
 * <p/>
 * Created by chen_fulei on 2015/7/17.
 */
public class PrintLogCat {
    private static volatile PrintLogCat printLogCat = null;

    private static String PATH_LOGCAT; // 保存的路径
    private int mPid;//当前应用运行进程id
    private String file_path;
    private LogDumper mLogDumper = null;

    /**
     * 构造方法
     */
    public PrintLogCat(Context mContext) {
        init(mContext);

        mPid = android.os.Process.myPid();
    }

    public static PrintLogCat getInstance(Context mContext) {
        if (printLogCat == null) {
            synchronized (PrintLogCat.class) {
                if (printLogCat == null) {
                    printLogCat = new PrintLogCat(mContext);
                }
            }
        }

        return printLogCat;
    }

    /**
     * 文件大小
     *
     * @param fileSize
     * @return
     */
    private static double getSize(long fileSize) {
        return fileSize / (1024 * 1024);

    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String data = format.format(new Date(System.currentTimeMillis()));
        return data;
    }

    /**
     * 获得当前时间
     *
     * @return
     */
    public static String getDateEN() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String data = format.format(new Date(System.currentTimeMillis()));
        return data;
    }

    /**
     * 初始化
     */
    private void init(Context mContext) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中

            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "LogCat";
        } else {// 如果SD卡不存在，就保存到本应用的目录下

            PATH_LOGCAT = mContext.getFilesDir().getAbsolutePath() + File.separator + "LogCat";
        }

        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 开启执行logcat打印保存
     */
    public void start() {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPid), PATH_LOGCAT);
        }

        mLogDumper.start();
    }

    /**
     * 结束打印logcat
     */
    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    public String getFilepath() {
        return file_path;
    }

    /**
     * 异步执行打印
     */
    private class LogDumper extends Thread {
        String cmds = null;
        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        private String mPID;
        private FileOutputStream out = null;
        private File file;

        public LogDumper(String pid, String dir) {
            this.mPID = pid;

            try {
                file = new File(dir, "logCat" + ".txt");
                out = new FileOutputStream(file);

                file_path = dir + "/logCat" + ".txt";
            } catch (Exception e) {
                Debug.Log(e);
            }

            cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
        }

        /**
         * 结束打印logcat
         */
        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);

                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }

                    //大于4m时重新保存
                    if (getSize(file.length()) > 4) {
                        file.delete();
                        file.createNewFile();
                        out.close();
                        out = new FileOutputStream(file);
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((getDateEN() + "  " + line + "\n").getBytes());
                    }
                }

            } catch (Exception e) {
                Debug.Log(e);
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        Debug.Log(e);
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Debug.Log(e);
                    }
                    out = null;
                }
            }
        }
    }

}
