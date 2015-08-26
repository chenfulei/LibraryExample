package com.library.http;

import android.text.TextUtils;

import com.library.utils.Debug;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 文件请求
 *
 * Created by chen_fulei on 2015/8/25.
 */
public class FLFileRequest extends FLRequest<byte[]> {

    private final File mStoreFile;
    private final File mTemporaryFile; // 临时文件

    public FLFileRequest(String storeFilePath, String url, FLHttpCallBack callback) {
        super(HttpMethod.GET, url, callback);
        mStoreFile = new File(storeFilePath);
        File folder = mStoreFile.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        if (!mStoreFile.exists()) {
            try {
                mStoreFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mTemporaryFile = new File(storeFilePath + ".tmp");
        setShouldCache(false);
    }

    public File getStoreFile() {
        return mStoreFile;
    }

    public File getTemporaryFile() {
        return mTemporaryFile;
    }

    @Override
    public FLResponse<byte[]> parseNetworkResponse(FLNetworkResponse response) {
        String errorMessage = null;
        if (!isCanceled()) {
            if (mTemporaryFile.canRead() && mTemporaryFile.length() > 0) {
                if (mTemporaryFile.renameTo(mStoreFile)) {
                    return FLResponse.success(response.data, response.headers,
                            FLHttpHeaderParser.parseCacheHeaders(mConfig,
                                    response));
                } else {
                    errorMessage = "Can't rename the download temporary file!";
                }
            } else {
                errorMessage = "Download temporary file was invalid!";
            }
        }
        if (errorMessage == null) {
            errorMessage = "Request was Canceled!";
        }
        return FLResponse.error(new FLHttpException(errorMessage));
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Range", "bytes=" + mTemporaryFile.length() + "-");
        header.put("Accept-Encoding", "identity");
        return header;
    }

    public byte[] handleResponse(HttpResponse response) throws IOException,
            FLHttpException {
        HttpEntity entity = response.getEntity();
        long fileSize = entity.getContentLength();
        if (fileSize <= 0) {
            Debug.Log("Response doesn't present Content-Length!");
        }

        long downloadedSize = mTemporaryFile.length();
        boolean isSupportRange = FLHttpUtils.isSupportRange(response);
        if (isSupportRange) {
            fileSize += downloadedSize;

            String realRangeValue = FLHttpUtils.getHeader(response,
                    "Content-Range");
            if (!TextUtils.isEmpty(realRangeValue)) {
                String assumeRangeValue = "bytes " + downloadedSize + "-"
                        + (fileSize - 1);
                if (TextUtils.indexOf(realRangeValue, assumeRangeValue) == -1) {
                    throw new IllegalStateException(
                            "The Content-Range Header is invalid Assume["
                                    + assumeRangeValue + "] vs Real["
                                    + realRangeValue + "], "
                                    + "please remove the temporary file ["
                                    + mTemporaryFile + "].");
                }
            }
        }

        if (fileSize > 0 && mStoreFile.length() == fileSize) {
            mStoreFile.renameTo(mTemporaryFile);
            mRequestQueue.getConfig().mDelivery.postDownloadProgress(this,
                    fileSize, fileSize);
            return null;
        }

        RandomAccessFile tmpFileRaf = new RandomAccessFile(mTemporaryFile, "rw");
        if (isSupportRange) {
            tmpFileRaf.seek(downloadedSize);
        } else {
            tmpFileRaf.setLength(0);
            downloadedSize = 0;
        }

        try {
            InputStream in = entity.getContent();
            if (FLHttpUtils.isGzipContent(response)
                    && !(in instanceof GZIPInputStream)) {
                in = new GZIPInputStream(in);
            }
            byte[] buffer = new byte[6 * 1024]; // 6K buffer
            int offset;

            while ((offset = in.read(buffer)) != -1) {
                tmpFileRaf.write(buffer, 0, offset);

                downloadedSize += offset;
                mRequestQueue.getConfig().mDelivery.postDownloadProgress(this,
                        fileSize, downloadedSize);

                if (isCanceled()) {
                    break;
                }
            }
        } finally {
            try {
                if (entity != null)
                    entity.consumeContent();
            } catch (Exception e) {
                Debug.Log("Error occured when calling consumingContent");
            }
            tmpFileRaf.close();
        }
        return null;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    protected void deliverResponse(Map<String, String> headers, byte[] response) {
        if (mCallback != null) {
            mCallback.onSuccess(headers, response);
        }
    }
}
