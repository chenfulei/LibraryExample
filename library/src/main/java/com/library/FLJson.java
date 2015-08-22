package com.library;

import android.text.TextUtils;

import com.library.utils.Debug;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  使用说明: 所有属性和内部类的名称跟api上是一致的
 *
 *  解析json
 *
 * Created by chen_fulei on 2015/8/21.
 */
public class FLJson {

    /**
     *  根据对象解析json (用对象反射解析)
     * @param json
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T get(JSONObject json , Class<T> t){
        String set = "set"; //set 方法
        T _t = null;

        try{
            //初始化对象
            if (t.isMemberClass()) {
                _t = t.newInstance();

            } else {
                _t = t.newInstance();
            }
        }catch (Exception e){
            Debug.LogE(t.getSimpleName() + "must 'static' used to modify");
            e.printStackTrace();
            return null;
        }

        //获取所有类（包含内部类）
        Class<?>[] t_class = t.getClasses();
        //获取类的变量
        Field[] fields = t.getDeclaredFields();
        //获得JSONObject 的名称 //也是多少个内部类
        JSONArray arrays = json.names();

        //根据 class T 属性进行解析jsonObject
        for (Field field : fields){
            for (int i = 0 ; i < arrays.length(); i++){
                //把类属性首字母大写，setXxx();
                String fn = field.getName();
                String methodName = fn.substring(0, 1).toUpperCase() + fn.substring(1);
                Debug.Log("json" , methodName);

                //根据不同对象类型解析
                try {

                    //类属性赋值(json的value)
                    if (field.getName().equals(arrays.getString(i))){

                        //字符串类型
                        if (String.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                //设置 class T 的setXxx()方法赋值
                                try {
                                    Method method = t.getMethod(set+methodName , String.class);
                                    method.invoke(_t , json.getString(arrays.getString(i)));

                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        // int 类型
                        if (int.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {
                                    Method method = t.getMethod(set+methodName , int.class);
                                    method.invoke(_t , json.getInt(arrays.getString(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        //Integer 类型
                        if (Integer.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {
                                    Method method = t.getMethod(set+ methodName , Integer.class);
                                    method.invoke(_t , json.getInt(arrays.getString(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        //Long 类型
                        if (Long.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                               try {
                                   Method method = t.getMethod(set + methodName , Long.class);
                                   method.invoke(_t , json.getLong(arrays.getString(i)));
                               }catch (Exception e){
                                   e.printStackTrace();
                               }
                            }
                        }

                        if (long.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {
                                    Method method = t.getMethod(set+methodName , long.class);
                                    method.invoke(_t , json.getLong(arrays.getString(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        //Boolean 类型
                        if (Boolean.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {
                                    Method method = t.getMethod(set + methodName , Boolean.class);
                                    method.invoke(_t , json.getBoolean(arrays.getString(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (boolean.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {
                                    Method method = t.getMethod(set+methodName , boolean.class);
                                    method.invoke(_t , json.getBoolean(arrays.getString(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        //ArrayList 类型
                        if (ArrayList.class.equals(field.getType())){
                            if (json.has(arrays.getString(i))){
                                try {

                                    Method method = t.getMethod(set + methodName , ArrayList.class);
                                    JSONArray array = json.getJSONArray(arrays.getString(i));
                                    List<Object> list = null;

                                    //is JSONArray
                                    for (Class<?> _class : t_class){
                                        if (methodName.equals(_class.getSimpleName())){
                                            list = new ArrayList<Object>();
                                            for (int num = 0 ;num < array.length() ; num++){
                                                Object arrayClass = get(array.getJSONObject(num) , _class);
                                                list.add(arrayClass);
                                            }
                                        }
                                    }

                                    method.invoke(_t , list);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }

                        /**
                         * 解析 T 内部类 JSONArray 中的JSONObject
                         */
                        for (Class<?> _class : t_class){
                             if (_class.equals(field.getType())){
                                 // 判断是否有该Key的JSONObject
                                 if (json.has(arrays.getString(i))){
                                     //反射
                                     try {
                                         Method method = t.getMethod(set+methodName , _class);
                                         Debug.Log("内部类:"+arrays.getString(i));
                                         //赋值属性
                                         Object arrayClass = get(json.getJSONObject(arrays.getString(i)) , _class);

                                         method.invoke(_t , arrayClass);
                                     }catch (Exception e){
                                         e.printStackTrace();
                                     }
                                 }
                             }
                        }

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        if (_t == null ){
            Debug.LogE("没有可执行解析类属性和其方法");
            Debug.LogE("parser json fails ");
        }

        return _t;
    }

    /**
     * 解析数组
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static ArrayList<Map<String, String>> getArrayListMap(
            JSONObject jsonObject, String key,
            ArrayList<Map<String, String>> defaultValue) {

        if (null == jsonObject || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        if (jsonObject.has(key)) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                if (null != jsonArray) {
                    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Map<String, String> map = new HashMap<String, String>();
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        for (int j = 0; j < jObject.names().length(); j++) {
                            map.put(jObject.names().getString(j), jObject
                                    .getString(jObject.names().getString(j)));
                        }
                        list.add(map);
                    }
                    return list;
                } else {
                    return defaultValue;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 解析数组
     * @param strJson
     * @param key
     * @param defaultValue
     * @return
     */
    public static ArrayList<Map<String, String>> getArrayListMap(
            String strJson, String key,
            ArrayList<Map<String, String>> defaultValue) {

        if (TextUtils.isEmpty(strJson) || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(strJson);
        } catch (JSONException e1) {
            try {
                jsonObject = new JSONObject("{" + key + ":" + strJson + "}");
            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        }
        if (jsonObject.has(key)) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(key);
                if (null != jsonArray) {
                    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Map<String, String> map = new HashMap<>();
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        for (int j = 0; j < jObject.names().length(); j++) {
                            map.put(jObject.names().getString(j), jObject
                                    .getString(jObject.names().getString(j)));
                        }
                        list.add(map);
                    }
                    return list;
                } else {
                    return defaultValue;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 解析单个值
     *
     * @param jsonObject
     * @param key
     * @param defaultVaule
     * @return
     */
    public static String getString(JSONObject jsonObject, String key,
                                   String defaultVaule) {
        if (null == jsonObject || TextUtils.isEmpty(key)) {
            return defaultVaule;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return defaultVaule;
            }
        } else {
            return defaultVaule;
        }
    }

    /**
     * 获取JSONObject
     *
     * @param jsonObject
     * @param key
     * @return
     */
    public static JSONObject getJSONObject(JSONObject jsonObject, String key) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return null;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getJSONObject(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取Long类型
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static Long getLong(JSONObject jsonObject, String key,
                               Long defaultValue) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getLong(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取Integer 类型
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static Integer getInteger(JSONObject jsonObject, String key,
                                     Integer defaultValue) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取Boolean 类型
     *
     * @param jsonObject
     * @param key
     * @param defaultValue
     * @return
     */
    public static Boolean getBoolean(JSONObject jsonObject, String key,
                                     Boolean defaultValue) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return defaultValue;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getBoolean(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 返回JSONArray
     *
     * @param jsonObject
     * @param key
     * @return
     */
    public static JSONArray getJSONArray(JSONObject jsonObject, String key) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            return null;
        }
        if (jsonObject.has(key)) {
            try {
                return jsonObject.getJSONArray(key);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

}
