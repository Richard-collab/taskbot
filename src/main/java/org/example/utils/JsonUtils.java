package org.example.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.example.chat.utils.MsgUtils;

import java.io.IOException;

public class JsonUtils {

//    private static final Gson gson = new Gson();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private static final Gson prettyGson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static JsonElement parseJsonFile(String pathInput) throws IOException {
        String json = FileUtils.readText(pathInput);
        return JsonParser.parseString(json);
    }
    public static JsonElement parseJson(String json) {
        return JsonParser.parseString(json);
    }

    public static <T> T fromJsonFile(String pathInput, Class<T> classOfT) throws IOException {
        String json = FileUtils.readText(pathInput);
        return fromJson(json, classOfT);
    }

    public static <T> T fromJsonFile(String pathInput, TypeToken<T> typeOfT) throws IOException {
        String json = FileUtils.readText(pathInput);
        return fromJson(json, typeOfT);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, TypeToken<T> typeOfT) {
        return gson.fromJson(json, typeOfT);
    }

    public static <T> T fromJsonAndFailOnUnknownProperties(String json, TypeReference<T> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            MsgUtils.sendQiweiWarning(String.valueOf(e));
            return null;
        }
    }

    public static <T> T fromJsonAndFailOnUnknownProperties(String json, Class<T> classOfT) {
        try {
            return OBJECT_MAPPER.readValue(json, classOfT);
        } catch (Exception e) {
            MsgUtils.sendQiweiWarning(String.valueOf(e));
            return null;
        }
    }

    public static String toJson(Object object, boolean prettyFormat) {
        if (prettyFormat) {
            return prettyGson.toJson(object);
        } else {
            return gson.toJson(object);
        }
    }

    public static void toJsonFile(Object object, boolean prettyFormat, String pathOutput) {
        String json;
        if (prettyFormat) {
            json = prettyGson.toJson(object);
        } else {
            json = gson.toJson(object);
        }
        FileUtils.write(json, pathOutput, false);
    }
}
