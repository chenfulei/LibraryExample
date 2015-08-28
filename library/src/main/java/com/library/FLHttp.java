package com.library;

import com.library.http.FLCache;
import com.library.http.FLCacheDispatcher;
import com.library.http.FLDownloadController;
import com.library.http.FLDownloadTaskQueue;
import com.library.http.FLFileRequest;
import com.library.http.FLFormRequest;
import com.library.http.FLHttpCallBack;
import com.library.http.FLHttpConfig;
import com.library.http.FLHttpParams;
import com.library.http.FLJsonRequest;
import com.library.http.FLNetworkDispatcher;
import com.library.http.FLRequest;
import com.library.utils.Debug;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

 /**
 *本类工作流程： 每当发起一次Request，会对这个Request标记一个唯一值。<br>
 * 并加入当前请求的Set中(保证唯一;方便控制)。<br>
 * 同时判断是否启用缓存，若启用则加入缓存队列，否则加入执行队列。<br>
 *
 * Note:<br>
 * 整个FLHttp工作流程：采用责任链设计模式，由三部分组成，类似设计可以类比Handle...Looper...MessageQueue<br>
 *
 * 1、FLHttp负责不停向NetworkQueue(或CacheQueue实际还是NetworkQueue， 具体逻辑请查看
 * {CacheDispatcher})添加Request<br>
 * 2、另一边由TaskThread不停从NetworkQueue中取Request并交给Network执行器(逻辑请查看
 * { NetworkDispatcher} )，<br>
 * 3、Network执行器将执行成功的NetworkResponse返回给TaskThead，并通过Request的定制方法
 * {Request#parseNetworkResponse()}封装成Response，最终交给分发器 { Delivery}
 * 分发到主线程并调用HttpCallback相应的方法
 *
 * Created by chen_fulei on 2015/8/25.
 */
public class FLHttp {

    // 请求缓冲区
    private final Map<String, Queue<FLRequest<?>>> mWaitingRequests = new HashMap<String, Queue<FLRequest<?>>>();
    // 请求的序列化生成器
    private final AtomicInteger mSequenceGenerator = new AtomicInteger();
    // 当前正在执行请求的线程集合
    private final Set<FLRequest<?>> mCurrentRequests = new HashSet<FLRequest<?>>();
    // 执行缓存任务的队列.
    private final PriorityBlockingQueue<FLRequest<?>> mCacheQueue = new PriorityBlockingQueue<FLRequest<?>>();
    // 需要执行网络请求的工作队列
    private final PriorityBlockingQueue<FLRequest<?>> mNetworkQueue = new PriorityBlockingQueue<FLRequest<?>>();
    // 请求任务执行池
    private final FLNetworkDispatcher[] mTaskThreads;
    // 缓存队列调度器
    private FLCacheDispatcher mCacheDispatcher;
    // 配置器
    private FLHttpConfig mConfig;


    public FLHttp() {
        this(new FLHttpConfig());
    }

    public FLHttp(FLHttpConfig config) {
        this.mConfig = config;
        mConfig.mController.setRequestQueue(this);
        mTaskThreads = new FLNetworkDispatcher[FLHttpConfig.NETWORK_POOL_SIZE];
        start();
    }

    /**
     * 发起get请求
     *
     * @param url
     *            地址
     * @param callback
     *            请求中的回调方法
     */
    public FLRequest<byte[]> get(String url, FLHttpCallBack callback) {
        return get(url, new FLHttpParams(), callback);
    }

    /**
     * 发起get请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     */
    public FLRequest<byte[]> get(String url, FLHttpParams params,
                                 FLHttpCallBack callback) {
        return get(url, params, true, callback);
    }

    /**
     * 发起get请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     * @param useCache
     *            是否缓存本条请求
     */
    public FLRequest<byte[]> get(String url, FLHttpParams params, boolean useCache,
                                 FLHttpCallBack callback) {
        if (params != null) {
            url += params.getUrlParams();
        }
        FLRequest<byte[]> request = new FLFormRequest(FLRequest.HttpMethod.GET, url, params,
                callback);
        request.setShouldCache(useCache);
        doRequest(request);
        return request;
    }

    /**
     * 发起post请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     */
    public FLRequest<byte[]> post(String url, FLHttpParams params,
                                  FLHttpCallBack callback) {
        return post(url, params, true, callback);
    }

    /**
     * 发起post请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     * @param useCache
     *            是否缓存本条请求
     */
    public FLRequest<byte[]> post(String url, FLHttpParams params,
                                  boolean useCache, FLHttpCallBack callback) {
        FLRequest<byte[]> request = new FLFormRequest(FLRequest.HttpMethod.POST, url, params,
                callback);
        request.setShouldCache(useCache);
        doRequest(request);
        return request;
    }

    /**
     * 使用JSON传参的post请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     */
    public FLRequest<byte[]> jsonPost(String url, FLHttpParams params,
                                      FLHttpCallBack callback) {
        return jsonPost(url, params, true, callback);
    }

    /**
     * 使用JSON传参的post请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     * @param useCache
     *            是否缓存本条请求
     */
    public FLRequest<byte[]> jsonPost(String url, FLHttpParams params,
                                      boolean useCache, FLHttpCallBack callback) {
        FLRequest<byte[]> request = new FLJsonRequest(FLRequest.HttpMethod.POST, url, params,
                callback);
        request.setShouldCache(useCache);
        doRequest(request);
        return request;
    }

    /**
     * 使用JSON传参的get请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     */
    public FLRequest<byte[]> jsonGet(String url, FLHttpParams params,
                                     FLHttpCallBack callback) {
        FLRequest<byte[]> request = new FLJsonRequest(FLRequest.HttpMethod.GET, url, params,
                callback);
        doRequest(request);
        return request;
    }

    /**
     * 使用JSON传参的get请求
     *
     * @param url
     *            地址
     * @param params
     *            参数集
     * @param callback
     *            请求中的回调方法
     * @param useCache
     *            是否缓存本条请求
     */
    public FLRequest<byte[]> jsonGet(String url, FLHttpParams params,
                                     boolean useCache, FLHttpCallBack callback) {
        FLRequest<byte[]> request = new FLJsonRequest(FLRequest.HttpMethod.GET, url, params,
                callback);
        request.setShouldCache(useCache);
        doRequest(request);
        return request;
    }

    public FLDownloadTaskQueue download(String storeFilePath, String url,
                                      FLHttpCallBack callback){
        return download(storeFilePath,url,callback,true);
    }
    /**
     * 下载
     *
     * @param storeFilePath
     *            文件保存路径。注，必须是一个file路径不能是folder
     * @param url
     *            下载地址
     * @param callback
     *            请求中的回调方法
     *  @param onMainThread 是否运行在主线程
     */
    public FLDownloadTaskQueue download(String storeFilePath, String url,
                                      FLHttpCallBack callback,boolean onMainThread) {
        FLFileRequest request = new FLFileRequest(storeFilePath, url, callback);
        mConfig.mController.add(request,onMainThread);
        doRequest(request);
        return mConfig.mController;
    }

    /**
     * 尝试唤醒一个处于暂停态的下载任务(不推荐)
     *
     * @param storeFilePath
     *            文件保存路径。注，必须是一个file路径不能是folder
     * @param url
     *            下载地址
     * @deprecated 会造成莫名其妙的问题，建议直接再次调用download方法
     */
    @Deprecated
    public void resumeTask(String storeFilePath, String url) {
        FLDownloadController controller = mConfig.mController.get(storeFilePath,
                url);
        controller.resume();
    }

    /**
     * 返回下载总控制器
     *
     * @return
     */
    public FLDownloadController getDownloadController(String storeFilePath,
                                                    String url) {
        return mConfig.mController.get(storeFilePath, url);
    }

    public void cancleAll() {
        mConfig.mController.clearAll();
    }

    /**
     * 执行一个自定义请求
     *
     * @param request
     */
    public void doRequest(FLRequest<?> request) {
        request.setConfig(mConfig);
        add(request);
    }

    /**
     * 获取内存缓存数据
     *
     * @param url
     *            哪条url的缓存
     * @return
     */
    public byte[] getCache(String url) {
        FLCache cache = mConfig.mCache;
        cache.initialize();
        FLCache.Entry entry = cache.get(url);
        if (entry != null) {
            return entry.data;
        } else {
            return new byte[0];
        }
    }

    /**
     * 只有你确定cache是一个String时才可以使用这个方法，否则还是应该使用getCache(String);
     *
     * @param url
     * @return
     */
    public String getStringCache(String url) {
        return new String(getCache(url));
    }

    /**
     * 移除一个缓存
     *
     * @param url
     *            哪条url的缓存
     */
    public void removeCache(String url) {
        mConfig.mCache.remove(url);
    }

    /**
     * 清空缓存
     */
    public void cleanCache() {
        mConfig.mCache.clear();
    }

    public FLHttpConfig getConfig() {
        return mConfig;
    }

    public void setConfig(FLHttpConfig config) {
        this.mConfig = config;
    }

    /******************************** core method ****************************************/

    /**
     * 启动队列调度
     */
    private void start() {
        stop();// 首先关闭之前的运行，不管是否存在
        mCacheDispatcher = new FLCacheDispatcher(mCacheQueue, mNetworkQueue,
                mConfig.mCache, mConfig.mDelivery, mConfig);
        mCacheDispatcher.start();
        // 构建线程池
        for (int i = 0; i < mTaskThreads.length; i++) {
            FLNetworkDispatcher tasker = new FLNetworkDispatcher(mNetworkQueue,
                    mConfig.mNetwork, mConfig.mCache, mConfig.mDelivery);
            mTaskThreads[i] = tasker;
            tasker.start();
        }
    }

    /**
     * 停止队列调度
     */
    private void stop() {
        if (mCacheDispatcher != null) {
            mCacheDispatcher.quit();
        }
        for (int i = 0; i < mTaskThreads.length; i++) {
            if (mTaskThreads[i] != null) {
                mTaskThreads[i].quit();
            }
        }
    }

    public void cancel(String url) {
        synchronized (mCurrentRequests) {
            for (FLRequest<?> request : mCurrentRequests) {
                if (url.equals(request.getTag())) {
                    request.cancel();
                }
            }
        }
    }

    /**
     * 取消全部请求
     */
    public void cancelAll() {
        synchronized (mCurrentRequests) {
            for (FLRequest<?> request : mCurrentRequests) {
                request.cancel();
            }
        }
    }

    /**
     * 向请求队列加入一个请求<br>
     * Note:此处工作模式是这样的：FLHttp可以看做是一个队列类，而本方法不断的向这个队列添加request；另一方面，
     * TaskThread不停的从这个队列中取request并执行。类似的设计可以参考Handle...Looper...MessageQueue的关系
     */
    public <T> FLRequest<T> add(FLRequest<T> request) {
        if (request.getCallback() != null) {
            request.getCallback().onPreStart();
        }

        // 标记该请求属于该队列，并将它添加到该组当前的请求。
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }
        // 设置进程优先序列
        request.setSequence(mSequenceGenerator.incrementAndGet());

        // 如果请求不可缓存，跳过缓存队列，直接进入网络。
        if (!request.shouldCache()) {
            mNetworkQueue.add(request);
            return request;
        }

        // 如果已经在mWaitingRequests中有本请求，则替换
        synchronized (mWaitingRequests) {
            String cacheKey = request.getCacheKey();
            if (mWaitingRequests.containsKey(cacheKey)) {
                // There is already a request in flight. Queue up.
                Queue<FLRequest<?>> stagedRequests = mWaitingRequests
                        .get(cacheKey);
                if (stagedRequests == null) {
                    stagedRequests = new LinkedList<>();
                }
                stagedRequests.add(request);
                mWaitingRequests.put(cacheKey, stagedRequests);
                   Debug.Log(
                           "Request for cacheKey=%s is in flight, putting on hold.",
                           cacheKey);
            } else {
                mWaitingRequests.put(cacheKey, null);
                mCacheQueue.add(request);
            }
            return request;
        }
    }

    /**
     * 将一个请求标记为已完成
     */
    public void finish(FLRequest<?> request) {
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }

        if (request.shouldCache()) {
            synchronized (mWaitingRequests) {
                String cacheKey = request.getCacheKey();
                Queue<FLRequest<?>> waitingRequests = mWaitingRequests
                        .remove(cacheKey);
                if (waitingRequests != null) {
                     Debug.Log(
                             "Releasing %d waiting requests for cacheKey=%s.",
                             waitingRequests.size(), cacheKey);
                    mCacheQueue.addAll(waitingRequests);
                }
            }
        }
    }

    public void destroy() {
        cancelAll();
        stop();
    }
}
