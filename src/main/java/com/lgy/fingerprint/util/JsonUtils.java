package com.lgy.fingerprint.util;


import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * @description json解析
 */
public class JsonUtils {

    private static Gson gson = new Gson();

    private JsonUtils() {
    }

    /**
     * @description 转化后的JSON串
     */
    public static String toJson(Object src) {
        if (null == src) {
            return gson.toJson(JsonNull.INSTANCE);
        }
        try {
            return gson.toJson(src);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @description 用来将JSON串转为对象，此方法可用来转带泛型的集合
     */
    public static Object fromJson(String json, Type typeOfT) {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


}
