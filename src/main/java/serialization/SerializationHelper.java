package serialization;

import com.google.gson.*;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class SerializationHelper {

    //Our one Gson instance
    private static Gson gson = new Gson();

    //When you dont care about the order that the keys are added to the object, use this
    public static JsonObject serializeHashMap(HashMap map){
        JsonObject out = new JsonObject();

        for(Object field : map.keySet().toArray()){
            if(!(map.get(field) instanceof Serializable)){
                System.out.println("Error: Cannot serialize this hashMap.");
                return null;
            }
            out.add(field.toString(), ((Serializable)map.get(field)).serialize());
        }

        return out;
    }

    //If the order of the keys are important, give us a key mapping and we will build the object in that format.
    public static JsonObject serializeHashMap(HashMap map, LinkedList<String> indices){
        JsonObject out = new JsonObject();

        for(String key : indices){
            if(!(map.get(key) instanceof Serializable)){
                System.out.println("Error: Cannot serialize this hashMap.");
                return null;
            }
            out.add(key, ((Serializable)map.get(key)).serialize());
        }

        return out;
    }

    public static JsonElement serializeArray(int[] indicies) {
        JsonArray out = new JsonArray();
        for(int i = 0; i < indicies.length; i++) {
            out.add(indicies[i]);
        }
        return out;
    }

    //This is our getter to access our Gson instance
    public static Gson getGson(){
        return gson;
    }

    //This is essentially a left join
    //Merge two objects together
    public static JsonObject merge(JsonObject base, JsonObject addition){

        if(addition == null){
            return base;
        }

        for(String key : addition.keySet()){
            if(base.has(key)){
                JsonElement attribute = base.get(key);
                JsonElement other     = addition.get(key);

                //If null set to value of other.
                if (attribute.isJsonNull()){
                    attribute = other;
                }

                //If primitive, add other value
                loop:{
                    if (attribute.isJsonPrimitive()) {
                        if (attribute.getAsJsonPrimitive().isBoolean()) {
                            //Set to and
                            attribute = new JsonPrimitive(attribute.getAsBoolean() && other.getAsBoolean());
                        }
                        if (attribute.getAsJsonPrimitive().isString()) {
                            attribute = new JsonPrimitive(attribute.getAsString().equals(other.getAsString()) ? attribute.getAsString() : other.getAsString());
                        }
                        if (attribute.getAsJsonPrimitive().isNumber()) {
                            attribute = new JsonPrimitive(attribute.getAsNumber().floatValue() + other.getAsNumber().floatValue());
                        }
                        break loop;
                    }

                    if (attribute.isJsonArray() && other.isJsonArray()) {
                        JsonArray newArray = new JsonArray();
                        for (Iterator<JsonElement> it = attribute.getAsJsonArray().iterator(); it.hasNext(); ) {
                            newArray.add(it.next());
                        }
                        for (Iterator<JsonElement> it = other.getAsJsonArray().iterator(); it.hasNext(); ) {
                            newArray.add(it.next());
                        }
                        attribute = newArray;
                        break loop;
                    }

                    if (attribute.isJsonObject() && other.isJsonObject()) {
                        attribute = SerializationHelper.merge(attribute.getAsJsonObject(), other.getAsJsonObject());
                        break loop;
                    }
                }

                //Set the attribute.
                base.add(key, attribute);
            }else{
                //This key was not found so append it
                base.add(key, addition.get(key));
            }
        }

        return base;
    }

    public static JsonObject addClass(Object object) {
        JsonObject helperObject = new JsonObject();
        helperObject.addProperty("class", object.getClass().getName());
        helperObject.add("value", gson.toJsonTree(object));
        return helperObject;
    }

    public static Object toClass(JsonObject object) {
        // Gotta have class and value
        if(object.has("class") && object.has("value")){
            Class<?> classType = null;
            try {
                classType = Class.forName(object.get("class").getAsString());
                return SerializationHelper.getGson().fromJson(object.get("value"), classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
