package ru.kanban.api;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;


public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration src, Type t, JsonSerializationContext c) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Duration deserialize(JsonElement json, Type t, JsonDeserializationContext c)
            throws JsonParseException {
        return Duration.parse(json.getAsString());
    }
}