package com.library.http;

import android.os.Looper;

import com.library.FLHttp;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chen_fulei on 2015/8/26.
 */
public class FLDownloadTaskQueue {
    private final int mParallelTaskCount; // 最大同时下载量
    private final List<FLDownloadController> mTaskQueue; // 以链表的形式储存下载控制器
    private FLHttp mRequestQueue; // 关联一个请求队列，目的是在恢复下载的时候可以再次将下载请求加入到请求队列中

    public FLDownloadTaskQueue(int parallelTaskCount) {
        if (parallelTaskCount >= FLHttpConfig.NETWORK_POOL_SIZE) {
            parallelTaskCount = FLHttpConfig.NETWORK_POOL_SIZE - 1;
        }
        mParallelTaskCount = parallelTaskCount;
        mTaskQueue = new LinkedList<FLDownloadController>();
    }

    public List<FLDownloadController> getTaskQueue() {
        return mTaskQueue;
    }

    /**
     * 清空全部下载任务
     */
    public void clearAll() {
        synchronized (mTaskQueue) {
            while (mTaskQueue.size() > 0) {
                mTaskQueue.get(0).removeTask();
            }
        }
    }
    public void add(FLFileRequest request){
        add(request,true);
    }
    /**
     * 添加一个下载请求,如果这个请求已经存在，则尝试唤醒这个请求
     *
     * @param request
     */
    public void add(FLFileRequest request,boolean onMainThread) {
        if(onMainThread){
            throwIfNotOnMainThread();
        }
        FLDownloadController requestTask = requestExist(request);
        if (requestTask != null) {
            requestTask.removeTask();
        }
        synchronized (mTaskQueue) {
            mTaskQueue.add(new FLDownloadController(this, request));
        }
        wake();
    }

    /**
     * 移除一个下载任务
     *
     * @param url
     */
    public void remove(String url) {
        for (FLDownloadController controller : mTaskQueue) {
            if (controller.equalsUrl(url)) {
                synchronized (mTaskQueue) {
                    mTaskQueue.remove(controller);
                    wake();
                    return;
                }
            }
        }
    }

    /**
     *
     * @param storeFilePath
     * @param url
     * @return
     */
    public FLDownloadController get(String storeFilePath, String url) {
        synchronized (mTaskQueue) {
            for (FLDownloadController controller : mTaskQueue) {
                if (controller.equalsRequest(storeFilePath, url))
                    return controller;
            }
        }
        return null;
    }

    public void setRequestQueue(FLHttp requestQueue) {
        this.mRequestQueue = requestQueue;
    }

    /* package */FLHttp getRequestQueue() {
        return mRequestQueue;
    }

    /* package */void wake() {
        synchronized (mTaskQueue) {
            int parallelTaskCount = 0; // 同时下载的数量

            for (FLDownloadController controller : mTaskQueue) {
                if (controller.isDownloading()) {
                    parallelTaskCount++;
                }
            }

            // 判断同时下载数量是否超过最大值
            for (FLDownloadController controller : mTaskQueue) {
                if (parallelTaskCount < mParallelTaskCount) {
                    if (controller.doLoadOnWait()) {
                        parallelTaskCount++;
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 必须在主线程执行
     */
    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException(
                    "FileDownloader must be invoked from the main thread.");
        }
    }

    /**
     * 如果这个请求本身就存在，则直接返回这个请求
     *
     * @param request
     * @return
     */
    private FLDownloadController requestExist(FLFileRequest request) {
        for (FLDownloadController task : mTaskQueue) {
            FLFileRequest req = task.getRequest();
            if (request.getUrl().equals(req.getUrl())
                    && request.getStoreFile().getAbsolutePath()
                    .equals(req.getStoreFile().getAbsolutePath())) {
                return task;
            }
        }
        return null;
    }

}
