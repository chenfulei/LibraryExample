package com.library;

import com.library.http.FLHttpConfig;
import com.library.http.FLRequest;

import java.util.HashMap;
import java.util.HashSet;
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
 * 整个HCHttp工作流程：采用责任链设计模式，由三部分组成，类似设计可以类比Handle...Looper...MessageQueue<br>
 *
 * 1、HCHttp负责不停向NetworkQueue(或CacheQueue实际还是NetworkQueue， 具体逻辑请查看
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
//    private final NetworkDispatcher[] mTaskThreads;
    // 缓存队列调度器
//    private CacheDispatcher mCacheDispatcher;
    // 配置器
    private FLHttpConfig mConfig;


    /**
     * 向请求队列加入一个请求<br>
     * Note:此处工作模式是这样的：HCHttp可以看做是一个队列类，而本方法不断的向这个队列添加request；另一方面，
     * TaskThread不停的从这个队列中取request并执行。类似的设计可以参考Handle...Looper...MessageQueue的关系
     */
    public <T> FLRequest<T> add(FLRequest<T> request) {

        return request;
    }

    /**
     * 将一个请求标记为已完成
     */
    public void finish(FLRequest<?> request) {

    }

    public FLHttpConfig getConfig() {
        return mConfig;
    }



}
