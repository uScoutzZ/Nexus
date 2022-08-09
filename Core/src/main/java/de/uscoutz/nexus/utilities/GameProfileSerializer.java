package de.uscoutz.nexus.utilities;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class GameProfileSerializer {
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).registerTypeAdapter(GameProfile.class, new GameProfileJsonSerializer()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();

    public static String toString(GameProfile profile) {
        return gson.toJson(profile);
    }

    public static GameProfile fromString(String string) {
        return gson.fromJson(string, GameProfile.class);
    }

    private static class GameProfileJsonSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        public GameProfile deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = (JsonObject) json;
            UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
            String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
            GameProfile profile = new GameProfile(id, name);
            if (object.has("properties")) {
                for (Map.Entry<String, Property> prop : ((PropertyMap) context.deserialize(object.get("properties"), PropertyMap.class)).entries()) {
                    profile.getProperties().put(prop.getKey(), prop.getValue());
                }
            }
            return profile;
        }


        public JsonElement serialize(GameProfile profile, Type type, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            if(profile.getId() != null)

                result.add("id", context.serialize(profile.getId()));

            if(profile.getName() != null)

                result.addProperty("name", profile.getName());

            if(!profile.getProperties().isEmpty())

                result.add("properties", context.serialize(profile.getProperties()));

            return result;
        }
    }
}
