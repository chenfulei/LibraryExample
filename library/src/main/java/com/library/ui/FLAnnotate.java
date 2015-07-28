package com.library.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.library.utils.Debug;

import java.lang.reflect.Field;

/**
 * 以注解方式绑定控件
 * Created by chen_fulei on 2015/7/17.
 */
public class FLAnnotate {

    /**
     * activity 初始化
     *
     * @param activity
     */
    public static void init(Activity activity) {
        init(activity, activity.getWindow().getDecorView());
    }

    /**
     * Fragment 初始化
     *
     * @param fragment
     */
    public static void init(Fragment fragment) {
        init(fragment, fragment.getView());
    }

    /**
     * View 初始化
     *
     * @param view
     */
    public static void init(View view) {
        Context mContext = view.getContext();

        if (mContext instanceof Activity) {
            init((Activity) mContext);
        } else {
            Debug.LogE("view must into Activity");
            throw new RuntimeException("view must into Activity");
        }
    }

    /**
     * 绑定该activity的所有控件
     *
     * @param obj
     * @param v
     */
    private static void init(Object obj, View v) {
        // 通过反射获取到全部属性，反射的字段可能是一个类（静态）字段或实例字段
        Field[] fields = obj.getClass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                // 返回BindView类型的注解内容
                FLBindView bindView = field.getAnnotation(FLBindView.class);
                if (bindView != null) {

                    int viewId = bindView.id();
                    boolean isClick = bindView.click();

                    try {
                        field.setAccessible(true);
                        if (isClick) {
                            v.findViewById(viewId).setOnClickListener((View.OnClickListener) obj);
                        }
                        // 将currentClass的field赋值为sourceView.findViewById(viewId)
                        field.set(obj, v.findViewById(viewId));
                    } catch (Exception e) {
                        Debug.Log(e);
                    }
                }
            }
        }
    }

}
