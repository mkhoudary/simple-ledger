/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mk.projects.simpleledger.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mk.projects.simpleledger.utils.PathSplitter.Pair;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Mohammad
 */
public class GsonUtils {

    public final static Gson INSTANCE = new GsonBuilder().create();

    private final static Type MAP_TYPE_TOKEN = new TypeToken<Map>() {
    }.getType();

    public static <T, S> Map<T, S> getMap(String json) {
        return INSTANCE.fromJson(json, MAP_TYPE_TOKEN);
    }

    public static String getString(JsonObject source, String key, String defaultValue) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getString(nextSource, splittedKey.getSecond(), defaultValue);
        }

        if (!source.has(key) || source.get(key).isJsonNull()) {
            return defaultValue;
        }

        return source.get(key).getAsString();
    }

    public static String getNotBlankString(JsonObject source, String key, String errorMessage) {
        String value = getString(source, key, "");

        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static void addStringIfNotBlank(JsonObject source, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            source.addProperty(key, value);
        }
    }

    public static void addBigDecimalIfNotNull(JsonObject source, String key, BigDecimal value) {
        if (value != null) {
            source.addProperty(key, value);
        }
    }

    public static Boolean getBoolean(JsonObject source, String key, Boolean defaultValue) {
        return getBoolean(source, key, defaultValue, String.format("Error while reading boolean of %s", key));
    }

    public static Boolean getBoolean(JsonObject source, String key, Boolean defaultValue, String onErrorMessage) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getBoolean(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (source == null) {
            return defaultValue;
        }

        if (!source.has(key) || source.get(key).isJsonNull() || StringUtils.isBlank(source.get(key).getAsString())) {
            return defaultValue;
        }

        try {
            return source.get(key).getAsBoolean();
        } catch (Exception e) {
            throw new RuntimeException(onErrorMessage);
        }
    }

    public static BigDecimal getBigDecimal(JsonObject source, String key, BigDecimal defaultValue) {
        return getBigDecimal(source, key, defaultValue, String.format("Error while reading big decimal of %s", key));
    }

    public static BigDecimal getBigDecimal(JsonObject source, String key, BigDecimal defaultValue, String onErrorMessage) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getBigDecimal(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (source == null) {
            return defaultValue;
        }

        if (!source.has(key) || source.get(key).isJsonNull() || StringUtils.isBlank(source.get(key).getAsString())) {
            return defaultValue;
        }

        try {
            return source.get(key).getAsBigDecimal();
        } catch (Exception e) {
            throw new RuntimeException(onErrorMessage);
        }
    }

    public static Double getDouble(JsonObject source, String key, Double defaultValue) {
        return getDouble(source, key, defaultValue, String.format("Error while reading double of %s", key));
    }

    public static Double getDouble(JsonObject source, String key, Double defaultValue, String onErrorMessage) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getDouble(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (source == null) {
            return defaultValue;
        }

        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getDouble(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (!source.has(key) || source.get(key).isJsonNull() || StringUtils.isBlank(source.get(key).getAsString())) {
            return defaultValue;
        }

        try {
            return source.get(key).getAsDouble();
        } catch (Exception e) {
            throw new RuntimeException(onErrorMessage);
        }
    }

    public static Long getLong(JsonObject source, String key, Long defaultValue) {
        return getLong(source, key, defaultValue, String.format("Error while reading long of %s", key));
    }

    public static Long getLong(JsonObject source, String key, Long defaultValue, String onErrorMessage) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getLong(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (source == null) {
            return defaultValue;
        }

        if (!source.has(key) || source.get(key).isJsonNull() || StringUtils.isBlank(source.get(key).getAsString())) {
            return defaultValue;
        }

        try {
            return source.get(key).getAsLong();
        } catch (Exception e) {
            throw new RuntimeException(onErrorMessage);
        }
    }

    public static Integer getInteger(JsonObject source, String key, Integer defaultValue) {
        return getInteger(source, key, defaultValue, String.format("Error while reading integer of %s", key));
    }

    public static Integer getInteger(JsonObject source, String key, Integer defaultValue, String onErrorMessage) {
        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getInteger(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (source == null) {
            return defaultValue;
        }

        if (!source.has(key) || source.get(key).isJsonNull() || StringUtils.isBlank(source.get(key).getAsString())) {
            return defaultValue;
        }

        try {
            return source.get(key).getAsInt();
        } catch (Exception e) {
            throw new RuntimeException(onErrorMessage);
        }
    }

    public static Boolean hasJsonArray(JsonObject source, String key) {
        if (!source.has(key) || source.get(key).isJsonNull() || !source.get(key).isJsonArray()) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public static JsonObject getJsonObject(JsonObject source, String key, JsonObject defaultValue) {
        return getJsonObject(source, key, defaultValue, String.format("Error while reading json object of %s", key));
    }

    public static JsonObject getJsonObject(JsonObject source, String key, JsonObject defaultValue, String onErrorMessage) {
        if (source == null) {
            return defaultValue;
        }

        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            if (nextSource == null) {
                return defaultValue;
            }

            return getJsonObject(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (!source.has(key) || source.get(key).isJsonNull()) {
            return defaultValue;
        }

        if (source.get(key).isJsonObject()) {
            return source.getAsJsonObject(key);
        }

        throw new RuntimeException(onErrorMessage);
    }

    public static JsonArray getJsonArray(JsonObject source, String key, JsonArray defaultValue) {
        return getJsonArray(source, key, defaultValue, String.format("Error while reading json array of %s", key));
    }

    public static JsonArray getJsonArrayIgnore(JsonObject source, String key, JsonArray defaultValue) {
        return getJsonArray(source, key, defaultValue, "");
    }

    public static JsonArray getJsonArray(JsonObject source, String key, JsonArray defaultValue, String onErrorMessage) {
        if (source == null) {
            return defaultValue;
        }

        if (key.contains(".")) {
            Pair<String, String> splittedKey = PathSplitter.splitPath(key);

            JsonObject nextSource = getJsonObject(source, splittedKey.getFirst(), new JsonObject());

            return getJsonArray(nextSource, splittedKey.getSecond(), defaultValue, onErrorMessage);
        }

        if (!source.has(key) || source.get(key).isJsonNull()) {
            return defaultValue;
        }

        if (source.get(key).isJsonArray()) {
            return source.getAsJsonArray(key);
        }

        if (!StringUtils.isBlank(onErrorMessage)) {
            throw new RuntimeException(onErrorMessage);
        }

        return defaultValue;
    }

    public static List<String> jsonStringValues(String json) {
        try {
            JsonObject jsonObject = INSTANCE.fromJson(json, JsonObject.class);

            List<String> values = new ArrayList<>();

            extractJsonValues(jsonObject, values);

            return values;
        } catch (Exception ex) {
            Logger.getLogger(GsonUtils.class.getSimpleName()).log(Level.SEVERE, ex.getMessage(), ex);

            return Collections.EMPTY_LIST;
        }
    }

    public static void extractJsonValues(JsonElement element, List<String> values) {
        if (element instanceof JsonObject) {
            ((JsonObject) element).entrySet().forEach((entry) -> {
                extractJsonValues(entry.getValue(), values);
            });
        } else if (element instanceof JsonArray) {
            for (JsonElement entry : ((JsonArray) element)) {
                extractJsonValues(entry, values);
            }
        } else if (element instanceof JsonPrimitive && !(element instanceof JsonNull) && StringUtils.isNotBlank(element.getAsString())) {
            values.add(element.getAsString());
        }
    }

    public static JsonObjectBuilder jsonObjectBuilder() {
        return new JsonObjectBuilder();
    }

    public static JsonArrayBuilder jsonArrayBuilder() {
        return new JsonArrayBuilder();
    }

    public static class JsonObjectBuilder {

        private final JsonObject toReturn = new JsonObject();
        private String parentPropertyName;
        private JsonObjectBuilder parent;

        public JsonObjectBuilder() {
        }

        private JsonObjectBuilder(String parentPropertyName, JsonObjectBuilder parent) {
            this.parentPropertyName = parentPropertyName;
            this.parent = parent;
        }

        public JsonObjectBuilder objectBuilder(String key) {
            return new JsonObjectBuilder(key, this);
        }

        public JsonArrayBuilder arrayBuilder(String key) {
            return new JsonArrayBuilder(key, this);
        }

        public JsonObjectBuilder prop(String key, String value) {
            toReturn.addProperty(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, Boolean value) {
            toReturn.addProperty(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, Character value) {
            toReturn.addProperty(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, Number value) {
            toReturn.addProperty(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, JsonElement value) {
            toReturn.add(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, JsonObject value) {
            toReturn.add(key, value);

            return this;
        }

        public JsonObjectBuilder prop(String key, JsonArray value) {
            toReturn.add(key, value);

            return this;
        }

        public JsonObject build() {
            return toReturn;
        }

        public JsonObjectBuilder buildChild() {
            parent.prop(parentPropertyName, toReturn);

            return parent;
        }
    }

    public static class JsonArrayBuilder {

        private final JsonArray toReturn = new JsonArray();
        private final String parentPropertyName;
        private final JsonObjectBuilder parent;

        public JsonArrayBuilder() {
            this.parentPropertyName = null;
            this.parent = null;
        }

        public JsonArrayBuilder(String parentPropertyName, JsonObjectBuilder parent) {
            this.parentPropertyName = parentPropertyName;
            this.parent = parent;
        }

        public JsonArrayBuilder prop(String value) {
            toReturn.add(value);

            return this;
        }

        public JsonArrayBuilder prop(Boolean value) {
            toReturn.add(value);

            return this;
        }

        public JsonArrayBuilder prop(Character value) {
            toReturn.add(value);

            return this;
        }

        public JsonArrayBuilder prop(Number value) {
            toReturn.add(value);

            return this;
        }

        public JsonArrayBuilder prop(JsonObject value) {
            toReturn.add(value);

            return this;
        }

        public JsonArray build() {
            return toReturn;
        }

        public JsonObjectBuilder buildChild() {
            parent.prop(parentPropertyName, toReturn);

            return parent;
        }
    }
}
