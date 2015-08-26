package com.library.http;

import android.os.Handler;

import java.util.concurrent.Executor;

/**
 * Http响应的分发器，这里用于把异步线程中的响应分发到UI线程中执行
 *
 * Created by chen_fulei on 2015/8/26.
 */
public class FLDeliveryExecutor implements FLDelivery {

    private final Executor mResponsePoster;

    public FLDeliveryExecutor(final Handler handler) {
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public FLDeliveryExecutor(Executor executor) {
        mResponsePoster = executor;
    }

    @Override
    public void postResponse(FLRequest<?> request, FLResponse<?> response) {
        postResponse(request, response, null);
    }

    /**
     * 当有中介响应的时候，会被调用，首先返回中介响应，并执行runnable(实际就是再去请求网络)<br>
     * Note:所谓中介响应：当本地有一个未过期缓存的时候会优先返回一个缓存，但如果这个缓存又是需要刷新的时候，会再次去请求网络，
     * 那么之前返回的那个有效但需要刷新的就是中介响应
     */
    @Override
    public void postResponse(FLRequest<?> request, FLResponse<?> response,
                             Runnable runnable) {
        request.markDelivered();
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response,
                runnable));
    }

    @Override
    public void postError(FLRequest<?> request, FLHttpException error) {
        FLResponse<?> response = FLResponse.error(error);
        mResponsePoster.execute(new ResponseDeliveryRunnable(request, response,
                null));
    }

    /**
     * 一个Runnable，将网络请求响应分发到UI线程中
     */
    @SuppressWarnings("rawtypes")
    private class ResponseDeliveryRunnable implements Runnable {
        private final FLRequest mRequest;
        private final FLResponse mResponse;
        private final Runnable mRunnable;

        public ResponseDeliveryRunnable(FLRequest request, FLResponse response,
                                        Runnable runnable) {
            mRequest = request;
            mResponse = response;
            mRunnable = runnable;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            if (mRequest.isCanceled()) {
                mRequest.finish("request已经取消，在分发时finish");
                return;
            }

            if (mResponse.isSuccess()) {
                mRequest.deliverResponse(mResponse.headers, mResponse.result);
            } else {
                mRequest.deliverError(mResponse.error);
            }
            mRequest.requestFinish();
            mRequest.finish("done");
            if (mRunnable != null) { // 执行参数runnable
                mRunnable.run();
            }
        }
    }

    @Override
    public void postDownloadProgress(FLRequest<?> request, long fileSize,
                                     long downloadedSize) {
        request.mCallback.onLoading(fileSize, downloadedSize);
    }

    @Override
    public void postCancel(FLRequest<?> request) {

    }
}