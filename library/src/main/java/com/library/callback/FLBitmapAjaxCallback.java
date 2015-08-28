package com.library.callback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.library.constants.FLConstants;
import com.library.utils.Debug;

import org.apache.http.HttpHost;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 图片管理核心(下载显示图片 包括缓存)
 *
 * Created by chen_fulei on 2015/8/1.
 */
public class FLBitmapAjaxCallback extends FLAbstractAjaxCallback<Bitmap, FLBitmapAjaxCallback> {

    private static int SMALL_MAX = 20;
    private static int BIG_MAX = 20;
    private static int SMALL_PIXELS = 50 * 50; //最小分辨率
    private static int BIG_PIXELS = 400 * 400; //最大分辨率
    private static int BIG_TPIXELS = 1000000;

    private static boolean DELAY_WRITE = false;

    private static Map<String, Bitmap> smallCache;//存储小图
    private static Map<String, Bitmap> bigCache;//存储大图
    private static Map<String, Bitmap> invalidCache;

    private static HashMap<String, WeakHashMap<ImageView, FLBitmapAjaxCallback>> queueMap = new HashMap<String, WeakHashMap<ImageView, FLBitmapAjaxCallback>>();

    private WeakReference<ImageView> v;// 软引用图片
    private int targetWidth;
    private int fallback;
    private File imageFile;
    private Bitmap bm;
    private int animation;
    private Bitmap preset;
    private float ratio;
    private int round;
    private boolean targetDim = true;
    private float anchor = FLConstants.ANCHOR_DYNAMIC;
    private boolean invalid;
    private boolean rotate;


    /**
     * 初始化
     * Instantiates a new bitmap ajax callback.
     */
    public FLBitmapAjaxCallback(){
        type(Bitmap.class).memCache(true).fileCache(true).url("");
    }



    /**
     * Set the target Image view.
     *设置一个imageView
     * @param view the view
     * @return self
     */
    public FLBitmapAjaxCallback imageView(ImageView view){
        v = new WeakReference<ImageView>(view);
        return this;
    }

    /**
     * 设置图片宽度
     * Set the target width for downsampling.
     *
     * @param targetWidth the target width
     * @return self
     */
    public FLBitmapAjaxCallback targetWidth(int targetWidth){
        this.targetWidth = targetWidth;
        return this;
    }


    /**
     * 设置图片缓存文件
     * Set the image source file.
     *
     * @param imageFile the image file
     * @return self
     */
    public FLBitmapAjaxCallback file(File imageFile){
        this.imageFile = imageFile;
        return this;
    }

    /**
     * 调整
     * Set the preset bitmap. This bitmap will be shown immediately until the ajax callback returns the final image from the url.
     *
     * @param preset the preset
     * @return self
     */
    public FLBitmapAjaxCallback preset(Bitmap preset){

        this.preset = preset;
        return this;
    }

    /**
     * 设置一个Bitmap
     * Set the bitmap. This bitmap will be shown immediately with aspect ratio.
     *
     * @param bm
     * @return self
     */
    public FLBitmapAjaxCallback bitmap(Bitmap bm){
        this.bm = bm;
        return this;
    }

    /**
     * 请求失败后返回的图片id（预留图片）
     * Set the fallback image in resource id.
     *
     * @param resId the res id
     * @return self
     */
    public FLBitmapAjaxCallback fallback(int resId){
        this.fallback = resId;
        return this;
    }

    /**
     * \设置动画
     * Set the animation resource id, or AQuery.FADE_IN.
     *
     * @param animation the animation
     * @return self
     */
    public FLBitmapAjaxCallback animation(int animation){
        this.animation = animation;
        return this;
    }

    /**
     * 设置图片旋转
     * Set the image aspect ratio (height / width).
     *
     * @param ratio the ratio
     * @return self
     */
    public FLBitmapAjaxCallback ratio(float ratio){
        this.ratio = ratio;
        return this;
    }

    /**
     * 设置是否可以旋转
     * Set auto rotate to respect image Exif orientation.
     *
     * @param rotate rotate
     * @return self
     */
    public FLBitmapAjaxCallback rotate(boolean rotate){
        this.rotate = rotate;
        return this;
    }


    /**
     * Set the image aspect ratio anchor.
     *
     * Default value is ANCHOR_DYNAMIC.
     *
     * @param anchor the anchor
     * @return self
     */

    public FLBitmapAjaxCallback anchor(float anchor){
        this.anchor = anchor;

        return this;
    }

    /**
     * 圆形
     *
     * Set the round corner radius.
     * @param radius
     * @return
     */
    public FLBitmapAjaxCallback round(int radius){
        this.round = radius;
        return this;
    }


    private static Bitmap decode(String path, byte[] data, BitmapFactory.Options options, boolean rotate){

        Bitmap result = null;


        if(path != null){

            result = decodeFile(path, options, rotate);

        }else if(data != null){

            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        }

        if(result == null && options != null && !options.inJustDecodeBounds){
            Debug.Log("decode image failed", path);
        }

        return result;
    }

    private static boolean isInputSharable(){
        return Build.VERSION.SDK_INT < 19;

    }

    private static Bitmap decodeFile(String path, BitmapFactory.Options options, boolean rotate){

        Bitmap result = null;

        if(options == null){
            options = new BitmapFactory.Options();
        }

        options.inInputShareable = isInputSharable();
        options.inPurgeable = true;

        FileInputStream fis = null;

        try{

            fis = new FileInputStream(path);
            FileDescriptor fd = fis.getFD();
            result = BitmapFactory.decodeFileDescriptor(fd, null, options);

            if(result != null && rotate){
                result = rotate(path, result);
            }


        }catch(IOException e){
            e.printStackTrace();
        }finally{
            FLAjaxUtility.close(fis);
        }

        return result;

    }

    private static Bitmap rotate(String path, Bitmap bm){

        if(bm == null) return null;

        Bitmap result = bm;

        int ori = ExifInterface.ORIENTATION_NORMAL;

        try{
            ExifInterface ei = new ExifInterface(path);
            ori = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }catch(Exception e){
            //simply fallback to normal orientation
            e.printStackTrace();
        }

        if(ori > 0){

            Matrix matrix = getRotateMatrix(ori);
            result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            Debug.Log("before", bm.getWidth() + ":" + bm.getHeight());
            Debug.Log("after", result.getWidth() + ":" + result.getHeight());

            if(bm != result){
                bm.recycle();
            }
        }


        return result;
    }

    private static Matrix getRotateMatrix(int ori){

        Matrix matrix = new Matrix();
        switch (ori) {
            case 2:
                matrix.setScale(-1, 1);
                break;
            case 3:
                matrix.setRotate(180);
                break;
            case 4:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case 5:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 6:
                matrix.setRotate(90);
                break;
            case 7:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case 8:
                matrix.setRotate(-90);
                break;

        }

        return matrix;

    }

    public static Bitmap getResizedImage(String path, byte[] data, int target, boolean width, int round){
        return getResizedImage(path, data, target, width, round, false);
    }

    /**
     * Utility method for downsampling images.
     *
     * @param path the file path
     * @param data if file path is null, provide the image data directly
     * @param target the target dimension
     * @param width use width as target, otherwise use the higher value of height or width
     * @param round corner radius
     * @param rotate auto rotate with exif data
     * @return the resized image
     */
    public static Bitmap getResizedImage(String path, byte[] data, int target, boolean width, int round, boolean rotate){

        if(path == null && data == null) return null;

        BitmapFactory.Options options = null;

        if(target > 0){

            BitmapFactory.Options info = new BitmapFactory.Options();
            info.inJustDecodeBounds = true;

            decode(path, data, info, rotate);

            int dim = info.outWidth;
            if(!width) dim = Math.max(dim, info.outHeight);
            int ssize = sampleSize(dim, target);

            options = new BitmapFactory.Options();
            options.inSampleSize = ssize;

        }

        Bitmap bm = null;
        try{
            bm = decode(path, data, options, rotate);
        }catch(OutOfMemoryError e){
            clearCache();
            e.printStackTrace();
        }

        if(round > 0){
            bm = getRoundedCornerBitmap(bm, round);
        }

        return bm;

    }


    private static int sampleSize(int width, int target){

        int result = 1;

        for(int i = 0; i < 10; i++){

            if(width < target * 2){
                break;
            }

            width = width / 2;
            result = result * 2;

        }

        return result;
    }

    private Bitmap bmGet(String path, byte[] data){
        return getResizedImage(path, data, targetWidth, targetDim, round, rotate);

    }

    @Override
    protected File accessFile(File cacheDir, String url){

        if(imageFile != null && imageFile.exists()){
            return imageFile;
        }

        return super.accessFile(cacheDir, url);
    }


    @Override
    protected Bitmap fileGet(String url, File file, FLAjaxStatus status) {
        return bmGet(file.getAbsolutePath(), null);
    }



    @Override
    public Bitmap transform(String url, byte[] data, FLAjaxStatus status) {

        String path = null;

        File file = status.getFile();
        if(file != null){
            path = file.getAbsolutePath();
        }

        Bitmap bm = bmGet(path, data);

        if(bm == null){

            if(fallback > 0){
                bm = getFallback();
            }else if(fallback == FLConstants.GONE || fallback == FLConstants.INVISIBLE){
                bm = dummy;
            }else if(fallback == FLConstants.PRESET){
                bm = preset;
            }

            if(status.getCode() != 200){
                invalid = true;
            }


            //invalidating the file if it's not an image, could be caused by proxy returning 200 with html data
            if(status.getSource() == FLAjaxStatus.NETWORK && file != null){
                Debug.Log("invalid bm from net");
                file.delete();
            }

        }




        return bm;
    }




    private Bitmap getFallback(){

        Bitmap bm = null;

        View view = v.get();
        if(view != null){

            String key = Integer.toString(fallback);
            bm = memGet(key);

            if(bm == null){
                bm = BitmapFactory.decodeResource(view.getResources(), fallback);

                if(bm != null){
                    memPut(key, bm);
                }
            }
        }

        return bm;
    }


    public static Bitmap getMemoryCached(Context context, int resId){

        String key = Integer.toString(resId);
        Bitmap bm = memGet(key, 0, 0);

        if(bm == null){
            bm = BitmapFactory.decodeResource(context.getResources(), resId);

            if(bm != null){
                memPut(key, 0, 0, bm, false);
            }
        }

        return bm;
    }

    private static Bitmap empty = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
    public static Bitmap getEmptyBitmap(){
        return empty;
    }

    private static Bitmap dummy = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);



    @Override
    public final void callback(String url, Bitmap bm, FLAjaxStatus status) {

        ImageView firstView = v.get();
        WeakHashMap<ImageView, FLBitmapAjaxCallback> ivs = queueMap.remove(url);

        //check if view queue already contains first view
        if(ivs == null || !ivs.containsKey(firstView)){
            checkCb(this, url, firstView, bm, status);
        }

        if(ivs != null){

            Set<ImageView> set = ivs.keySet();

            for(ImageView view: set){
                FLBitmapAjaxCallback cb = ivs.get(view);
                cb.status = status;
                checkCb(cb, url, view, bm, status);
            }

        }

    }

    @Override
    protected void skip(String url, Bitmap bm, FLAjaxStatus status){
        queueMap.remove(url);
    }


    private void checkCb(FLBitmapAjaxCallback cb, String url, ImageView v, Bitmap bm, FLAjaxStatus status){

        if(v == null || cb == null) return;

        if(url.equals(v.getTag(FLConstants.TAG_URL))){

            if(v instanceof ImageView){
                cb.callback(url, (ImageView) v, bm, status);
            }else{
                cb.setBitmap(url, v, bm, false);
            }

        }

        cb.showProgress(false);
    }

    protected void callback(String url, ImageView iv, Bitmap bm, FLAjaxStatus status){
        setBitmap(url, iv, bm, false);
    }


    /**
     * Sets the icon cache size in count. Icons are images less than 50x50 pixels.
     *
     * @param limit the new icon cache limit
     */
    public static void setIconCacheLimit(int limit){
        SMALL_MAX = limit;
        clearCache();
    }

    /**
     * Sets the cache limit in count.
     *
     * @param limit the new cache limit
     */
    public static void setCacheLimit(int limit){
        BIG_MAX = limit;
        clearCache();
    }

    /**
     * Sets the file cache write policy. If set to true, images load from network will be served quicker before caching to disk,
     * this however increase the chance of out of memory due to memory allocation.
     *
     * Default is false.
     */
    public static void setDelayWrite(boolean delay){
        DELAY_WRITE = delay;
    }



    /**
     * Sets the pixel limit per image. Image larger than limit will not be memcached.
     *
     * @param pixels the new pixel limit
     */
    public static void setPixelLimit(int pixels){
        BIG_PIXELS = pixels;
        clearCache();
    }

    /**
     * Sets the pixel criteria for small images. Small images are cached in a separate cache.
     *
     * Default is 50x50 (2500 pixels)
     *
     * @param pixels the small image pixel criteria
     */
    public static void setSmallPixel(int pixels){
        SMALL_PIXELS = pixels;
        clearCache();
    }

    /**
     * Sets the max pixel limit for the entire memcache. LRU images will be expunged if max pixels limit is reached.
     *
     * @param pixels the new max pixel limit
     */
    public static void setMaxPixelLimit(int pixels){
        BIG_TPIXELS = pixels;
        clearCache();
    }

    /**
     * Clear the bitmap memcache.
     */
    public static void clearCache(){
        bigCache = null;
        smallCache = null;
        invalidCache = null;
    }

    protected static void clearTasks(){
        queueMap.clear();
    }

    private static Map<String, Bitmap> getBCache(){
        if(bigCache == null){
            bigCache = Collections.synchronizedMap(new FLBitmapCache(BIG_MAX, BIG_PIXELS, BIG_TPIXELS));
        }
        return bigCache;
    }


    private static Map<String, Bitmap> getSCache(){
        if(smallCache == null){
            smallCache = Collections.synchronizedMap(new FLBitmapCache(SMALL_MAX, SMALL_PIXELS, 250000));
        }
        return smallCache;
    }

    private static Map<String, Bitmap> getICache(){
        if(invalidCache == null){
            invalidCache = Collections.synchronizedMap(new FLBitmapCache(100, BIG_PIXELS, 250000));
        }
        return invalidCache;
    }

    @Override
    protected Bitmap memGet(String url){
        if(bm != null) return bm;
        if(!memCache) return null;
        return memGet(url, targetWidth, round);
    }

    /**
     * Check if the bitmap is memory cached.
     *
     * @param url the url
     * @return if the url is memcached
     */
    public static boolean isMemoryCached(String url){
        return getBCache().containsKey(url) || getSCache().containsKey(url) || getICache().containsKey(url);
    }

    /**
     * Gets the memory cached bitmap.
     *
     * @param url the url
     * @param targetWidth the target width, 0 for non downsampling
     * @return the memory cached bitmap
     */
    public static Bitmap getMemoryCached(String url, int targetWidth){
        return memGet(url, targetWidth, 0);
    }

    private static Bitmap memGet(String url, int targetWidth, int round){

        url = getKey(url, targetWidth, round);

        Map<String, Bitmap> cache = getBCache();
        Bitmap result = cache.get(url);

        if(result == null){
            cache = getSCache();
            result = cache.get(url);
        }

        if(result == null){
            cache = getICache();
            result = cache.get(url);

            if(result != null){

                if(getLastStatus() == 200){
                    invalidCache = null;
                    result = null;
                }

            }
        }

        return result;
    }

    private static String getKey(String url, int targetWidth, int round){

        if(targetWidth > 0){
            url += "#" + targetWidth;
        }

        if(round > 0){
            url += "#" + round;
        }

        return url;
    }

    private static void memPut(String url, int targetWidth, int round, Bitmap bm, boolean invalid){

        if(bm == null) return;

        int pixels = bm.getWidth() * bm.getHeight();

        Map<String, Bitmap> cache = null;

        if(invalid){
            cache = getICache();
        }else if(pixels <= SMALL_PIXELS){
            cache = getSCache();
        }else{
            cache = getBCache();
        }

        if(targetWidth > 0 || round > 0){

            String key = getKey(url, targetWidth, round);
            cache.put(key, bm);

            //to indicate that the variant of that url is cached by puting and empty value
            if(!cache.containsKey(url)){
                cache.put(url, null);
            }

        }else{
            cache.put(url, bm);
        }



    }


    @Override
    protected void memPut(String url, Bitmap bm){
        memPut(url, targetWidth, round, bm, invalid);
    }


    private static Bitmap filter(View iv, Bitmap bm, int fallback){
        //ignore 1x1 pixels
        if(bm != null && bm.getWidth() == 1 && bm.getHeight() == 1 && bm != empty){
            bm = null;
        }

        if(bm != null){
            iv.setVisibility(View.VISIBLE);
        }else if(fallback == FLConstants.GONE){
            iv.setVisibility(View.GONE);
        }else if(fallback == FLConstants.INVISIBLE){
            iv.setVisibility(View.INVISIBLE);
        }

        return bm;
    }

    private void presetBitmap(String url, ImageView v){


        if(!url.equals(v.getTag(FLConstants.TAG_URL)) || preset != null){

            v.setTag(FLConstants.TAG_URL, url);

            if(preset != null && !cacheAvailable(v.getContext())){
                setBitmap(url, v, preset, true);
            }else{

                setBitmap(url, v, null, true);
            }
        }

    }


    private static final int FADE_DUR = 300;

    private void setBitmap(String url, ImageView iv, Bitmap bm, boolean isPreset){

        if(bm == null){
            iv.setImageDrawable(null);
            return;
        }

        if(isPreset){
            iv.setImageDrawable(makeDrawable(iv, bm, ratio, anchor));
            return;
        }

        if(status != null){
            setBmAnimate(iv, bm, preset, fallback, animation, ratio, anchor, status.getSource());
        }

    }

    private static Drawable makeDrawable(ImageView iv, Bitmap bm, float ratio, float anchor){

        BitmapDrawable bd = null;

        if(ratio > 0){
            bd = new FLRatioDrawable(iv.getResources(), bm, iv, ratio, anchor);
        }else{
            bd = new BitmapDrawable(iv.getResources(), bm);
        }

        return bd;

    }

    private static void setBmAnimate(ImageView iv, Bitmap bm, Bitmap preset, int fallback, int animation, float ratio, float anchor, int source){

        bm = filter(iv, bm, fallback);
        if(bm == null){
            iv.setImageBitmap(null);
            return;
        }

        Drawable d = makeDrawable(iv, bm, ratio, anchor);
        Animation anim = null;

        if(fadeIn(animation, source)){
            if(preset == null){
                anim = new AlphaAnimation(0, 1);
                anim.setInterpolator(new DecelerateInterpolator());
                anim.setDuration(FADE_DUR);
            }else{

                Drawable pd = makeDrawable(iv, preset, ratio, anchor);
                Drawable[] ds = new Drawable[]{pd, d};
                TransitionDrawable td = new TransitionDrawable(ds);
                td.setCrossFadeEnabled(true);
                td.startTransition(FADE_DUR);
                d = td;
            }
        }else if(animation > 0){
            anim = AnimationUtils.loadAnimation(iv.getContext(), animation);
        }

        iv.setImageDrawable(d);

        if(anim != null){
            anim.setStartTime(AnimationUtils.currentAnimationTimeMillis());
            iv.startAnimation(anim);
        }else{
            iv.setAnimation(null);
        }
    }

    private static boolean fadeIn(int animation, int source){

        switch(animation){
            case FLConstants.FADE_IN:
                return true;
            case FLConstants.FADE_IN_FILE:
                if(source == FLAjaxStatus.FILE) return true;
            case FLConstants.FADE_IN_NETWORK:
                if(source == FLAjaxStatus.NETWORK) return true;
            default:
                return false;
        }

    }

    /**
     * AQuery internal use only. Please uses AQuery image() methods instead.
     *
     * Optimize memory usage if mem hit and there's no custom callback.
     *
     *
     */

    public static void async(Activity act, Context context, ImageView iv, String url, Object progress, FLAccountHandle ah, FLImageOptions options, HttpHost proxy, String networkUrl){

        async(act, context, iv, url, options.memCache, options.fileCache, options.targetWidth, options.fallback, options.preset, options.animation, options.ratio, options.anchor, progress, ah, options.policy, options.round, proxy, networkUrl);

    }


    /**
     * AQuery internal use only. Please uses AQuery image() methods instead.
     *
     * Optimize memory usage if mem hit and there's no custom callback.
     *
     *
     */

    public static void async(Activity act, Context context, ImageView iv, String url, boolean memCache, boolean fileCache, int targetWidth, int fallbackId, Bitmap preset, int animation, float ratio, float anchor, Object progress, FLAccountHandle ah, int policy, int round, HttpHost proxy, String networkUrl){

        Bitmap bm = null;

        if(memCache){
            bm = memGet(url, targetWidth, round);
        }

        if(bm != null){
            iv.setTag(FLConstants.TAG_URL, url);
            FLCommon.showProgress(progress, url, false);
            setBmAnimate(iv, bm, preset, fallbackId, animation, ratio, anchor, FLAjaxStatus.MEMORY);
        }else{
            FLBitmapAjaxCallback cb = new FLBitmapAjaxCallback();
            cb.url(url).imageView(iv).memCache(memCache).fileCache(fileCache).targetWidth(targetWidth).fallback(fallbackId).preset(preset).animation(animation).ratio(ratio).anchor(anchor).progress(progress).auth(ah).policy(policy).round(round).networkUrl(networkUrl);
            if(proxy != null){
                cb.proxy(proxy.getHostName(), proxy.getPort());
            }
            if(act != null){
                cb.async(act);
            }else{
                cb.async(context);
            }
        }

    }


    @Override
    public void async(Context context){


        String url = getUrl();

        ImageView v = this.v.get();

        if(url == null){
            showProgress(false);
            setBitmap(url, v, null, false);
            return;
        }

        Bitmap bm = memGet(url);
        if(bm != null){
            v.setTag(FLConstants.TAG_URL, url);
            status = new FLAjaxStatus().source(FLAjaxStatus.MEMORY).done();
            callback(url, bm, status);
            return;
        }


        presetBitmap(url, v);

        if(!queueMap.containsKey(url)){
            addQueue(url, v);
            super.async(v.getContext());
        }else{
            showProgress(true);
            addQueue(url, v);
        }


    }

    @Override
    protected boolean isStreamingContent(){
        return !DELAY_WRITE;
    }

    private void addQueue(String url, ImageView iv){


        WeakHashMap<ImageView, FLBitmapAjaxCallback> ivs = queueMap.get(url);

        if(ivs == null){

            if(queueMap.containsKey(url)){
                //already a image view fetching
                ivs = new WeakHashMap<ImageView, FLBitmapAjaxCallback>();
                ivs.put(iv, this);
                queueMap.put(url, ivs);
            }else{
                //register a view by putting a url with no value
                queueMap.put(url, null);
            }

        }else{
            //add to list of image views
            ivs.put(iv, this);

        }

    }

    private static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}