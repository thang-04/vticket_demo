package com.vticket.vticket.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vticket.vticket.exception.ErrorCode;

public class ResponseJson {

    private static final Gson gson = new Gson();

    public static String of(ErrorCode code) {
        return of(code, code.getMessage(), (JsonObject) null);
    }

    public static String of(ErrorCode code, String desc) {
        return of(code, desc, (JsonObject) null);
    }

    public static String of(ErrorCode code, Object data) {
        JsonElement jsonElement = gson.toJsonTree(data);
        JsonObject jsonObject = jsonElement != null && jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
        return of(code, code.getMessage(), jsonObject);
    }

    public static String of(ErrorCode code, String desc, Object data) {
        JsonElement jsonElement = gson.toJsonTree(data);
        JsonObject jsonObject = jsonElement != null && jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
        return of(code, desc, jsonObject);
    }

    public static String of(ErrorCode code, String desc, JsonObject data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", code.getCode());
        obj.addProperty("codeName", code.name());
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? code.getMessage() : desc);
        if (data != null) {
            obj.add("result", data);
        }
        return obj.toString();
    }

    public static String ofArray(ErrorCode code, String desc, JsonArray data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", code.getCode());
        obj.addProperty("codeName", code.name());
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? code.getMessage() : desc);
        if (data != null) {
            obj.add("result", data);
        }
        return obj.toString();
    }

    public static String success(String desc, Object data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", 1000);
        obj.addProperty("codeName", "SUCCESS");
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? "Success" : desc);
        if (data != null) {
            JsonElement jsonElement = gson.toJsonTree(data);
            if (jsonElement.isJsonObject()) {
                obj.add("result", jsonElement.getAsJsonObject());
            } else {
                obj.add("result", jsonElement);
            }
        }
        return obj.toString();
    }

    public static String success(String desc) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", 1000);
        obj.addProperty("codeName", "SUCCESS");
        obj.addProperty("desc", CommonUtils.isNullOrEmpty(desc) ? "Success" : desc);
        return obj.toString();
    }
}


