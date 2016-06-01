package com.vipheyue.livegame.utils;

import com.google.gson.Gson;

/**
 * Created by heyue on 2015/11/22.
 */
public class GsonUtils {
    public static final String toJson(Object object) {
        Gson gson=new Gson();
        return gson.toJson(object);
    }
    public static final <V> V fromJson(String json, Class<V> type) {
        Gson gson=new Gson();
        return gson.fromJson(json, type);
    }
}
