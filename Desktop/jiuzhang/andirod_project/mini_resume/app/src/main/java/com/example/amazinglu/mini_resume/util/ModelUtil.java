package com.example.amazinglu.mini_resume.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ModelUtil {

    /**
     * need to tell Gson how to serialize Uri
     * */
    private static Gson gsonForSerialization = new GsonBuilder().registerTypeAdapter(Uri.class,
            new UriSerializer()).create();
    private static Gson gsonForDeserialization = new GsonBuilder().registerTypeAdapter(Uri.class,
            new UriDeserializer()).create();

    private static final String PREF_MODEL_KEY = "models";

    public static void save(Context context, String key, Object object) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_MODEL_KEY, Context.MODE_PRIVATE);
        String jsonString = gsonForSerialization.toJson(object);
        sp.edit().putString(key, jsonString).apply();
    }

    public static <T> T read(Context context, String key, TypeToken<T> typeToken) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_MODEL_KEY, Context.MODE_PRIVATE);
        return gsonForDeserialization.fromJson(sp.getString(key, ""), typeToken.getType());
    }

    private static class UriSerializer implements JsonSerializer<Uri> {
        @Override
        public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    private static class UriDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Uri.parse(json.getAsString());
        }
    }
}


