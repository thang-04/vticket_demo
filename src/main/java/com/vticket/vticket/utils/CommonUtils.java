package com.vticket.vticket.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtils {
    private static final Logger LOGGER = Logger.getLogger(CommonUtils.class);

    public static Gson gson = new Gson();

    public static boolean isNotEmpty(String string) {
        return string != null && !string.equals("");
    }

    public static String toJson(Map<String, Object> map) {
        Gson gsonBuilder = new GsonBuilder().create();
        String jsonStr = gsonBuilder.toJson(map);
        return jsonStr;

    }

    public static String convertMapToJson(Map<String, String> map) {
        Gson gsonBuilder = new GsonBuilder().create();
        String jsonStr = gsonBuilder.toJson(map);
        return jsonStr;

    }

    public static String convertMapObjectToJson(Map<Object, Object> map) {
        Gson gsonBuilder = new GsonBuilder().create();
        String jsonStr = gsonBuilder.toJson(map);
        return jsonStr;

    }

    public static String listToJsonArray(List<Object> lsObject) {
        Type listType = new TypeToken<List<Object>>() {
        }.getType();
        JsonElement js = gson.toJsonTree(lsObject, listType);
        return js.toString();
    }

    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static String genRandomString(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.trim().isEmpty());
    }

    /**
     * Chuyen doi tuong Date thanh doi tuong String.
     *
     * @param date Doi tuong Date
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static String convertDateToString(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(date);
        }
    }

    /**
     * Chuyen doi tuong Date thanh doi tuong String by pattern.
     *
     * @param date    Doi tuong Date
     * @param pattern
     * @return Xau ngay, co dang dd/MM/yyyy
     */
    public static String convertDateToString(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(date);
        }
    }

    /**
     * Chuyen doi tuong String thanh doi tuong Date.
     *
     * @param date Xau ngay, co dinh dang duoc quy trinh trong file Constants
     * @return Doi tuong Date
     * @throws Exception Exception
     */
    public static Date convertStringToDateTime(String date) throws Exception {
        if (date == null || date.trim().isEmpty()) {
            return null;
        } else {
            String pattern = "dd/MM/yyyy HH:mm:ss";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setLenient(false);
            return dateFormat.parse(date);
        }
    }

    /**
     * Chuyen doi tuong String thanh doi tuong Date.
     *
     * @param date Xau ngay, co dinh dang duoc quy trinh trong file Constants
     * @return Doi tuong Date
     * @throws Exception Exception
     */
    public static Date convertStringToDate(String date) throws Exception {
        if (date == null || date.trim().isEmpty()) {
            return null;
        } else {
            String pattern = "dd/MM/yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setLenient(false);
            return dateFormat.parse(date);
        }
    }

    /**
     * Chuyen doi tuong String thanh doi tuong Date.
     *
     * @param date    Xau ngay, co dinh dang duoc quy trinh trong file Constants
     * @param pattern
     * @return Doi tuong Date
     * @throws Exception Exception
     */
    public static Date convertStringToDate(String date, String pattern) throws Exception {
        if (date == null || date.trim().isEmpty()) {
            return null;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            dateFormat.setLenient(false);
            return dateFormat.parse(date);
        }
    }

    public static <T> List<T> convertJsonToList(Class<T> className, String json) throws IOException {
        Type type = new TypeToken<ArrayList<T>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static Map<String, String> convertJsonStringToMap(String jsonString) throws IOException {
        Map<String, Object> mapGenerate = new ObjectMapper().readValue(jsonString, HashMap.class);
        Map<String, String> mapResult = new HashMap<>();
        for (String key : mapGenerate.keySet()) {
            mapResult.put(key, String.valueOf(mapGenerate.get(key)));
        }
        return mapResult;
    }

    // Partition a list into sublists of a specified size.
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null) {
            throw new NullPointerException(
                    "'list' must not be null");
        }
        if (!(size > 0)) {
            throw new IllegalArgumentException(
                    "'size' must be greater than 0");
        }
        return new Partition<>(list, size);
    }

}
