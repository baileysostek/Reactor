package serialization;

import com.google.gson.*;

import java.util.Collection;
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
            out.add(key, ((Serializable)map.get(key)).serialize(null));
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

        //If this is a collection
        if(object instanceof Collection) {
            //Convert to collection
            Collection data = ((Collection) object);
            int size = data.size();

            //Store members in json array.
            JsonArray array = new JsonArray(size);
            for(Object obj : data){
                array.add(addClass(obj));
            }

            //Return array
            JsonObject helperObject = new JsonObject();
            helperObject.add("value", array);
            return helperObject;

        }else{
            //Not a collection
            JsonObject helperObject = new JsonObject();
            helperObject.addProperty("class", object.getClass().getName());
            if(object.getClass().isEnum()){
                helperObject.addProperty("value", object.toString());
            }else{
                try {
                    helperObject.add("value", gson.toJsonTree(object));
                }catch (IllegalArgumentException argument) {
                    System.out.println("[Error] could not serialize member of object:" + object);
                    argument.printStackTrace();
                    return helperObject;
                }
            }
            return helperObject;
        }
    }

    public static JsonObject differ(JsonObject source, JsonObject other){
        JsonObject out = new JsonObject();

        //Loop through source
        for(String key : source.keySet()){
            if(other.has(key)){
                //Check if differ
                differHelper(source.get(key), other.get(key), key, out);
            }
        }

        for(String key : other.keySet()){
            if(source.has(key)){
                //Check if differ
                differHelper(source.get(key), other.get(key), key, out);
            }else{
                out.add(key, other.get(key));
            }
        }

        return out;
    }

    private static void differHelper(JsonElement source, JsonElement other, String key, JsonObject out){
        if(source.isJsonObject() && other.isJsonObject()){
            JsonObject objectCompare =  differ(source.getAsJsonObject(), other.getAsJsonObject());
            if(objectCompare.size() > 0){
                out.add(key, objectCompare);
            }
        }else if(source.isJsonArray() && other.isJsonArray()){
            JsonArray sourceArray = source.getAsJsonArray();
            JsonArray otherArray = other.getAsJsonArray();
            if(sourceArray.size() != otherArray.size()){
                out.add(key, other);
            }else{
                int maxSize = Math.max(sourceArray.size(), otherArray.size());
                JsonObject arrayCompare = new JsonObject();
                for(int i = 0; i < maxSize; i++){
                    if(i < sourceArray.size()) {
                        JsonElement sourceArrayElement = (sourceArray.size()) > i ? sourceArray.get(i) : new JsonObject();
                        JsonElement otherArrayElement = (otherArray.size()) > i ? otherArray.get(i) : new JsonObject();
                        differHelper(sourceArrayElement, otherArrayElement, i + "", arrayCompare);
                    }else{
                        arrayCompare.add(i + "", otherArray.get(i));
                    }
                }

                if(arrayCompare.size() > 0) {
                    out.add(key, arrayCompare);
                }
            }
        }else{
            if(!(source.equals(other))) {
                if(source.getAsJsonPrimitive().isNumber() && other.getAsJsonPrimitive().isNumber()){
                    float sourceNumber = source.getAsFloat();
                    float otherNumber  = other.getAsFloat();

                    out.addProperty(key, sourceNumber - otherNumber);

                }else {
                    out.add(key, other);
                }
            }
        }
    }

    public static Object toClass(JsonObject object) {
        // Gotta have a value
        if(object.has("value")){
            //Store Value
            JsonElement value = object.get("value");

            //Check if is array
            if(value.isJsonArray()){
                JsonArray arrayData = value.getAsJsonArray();
                LinkedList list = new LinkedList();
                for(int i = 0; i < arrayData.size(); i++){
                    JsonObject data = arrayData.get(i).getAsJsonObject();
                    if(data.has("class")) {
                        list.addLast(toClass(data));
                    }
                }

                return list;
            }

            //If direct single object, determine class.
            if(object.has("class")) {
                Class<?> classType = null;
                try {
                    classType = Class.forName(object.get("class").getAsString());
                    //If is Enum
                    if(classType.isEnum()){
                        Class<Enum> enumClass  = ((Class<Enum>)classType);
                        //deserializing Enum
                        return toEnum(enumClass, object.get("value").getAsString());
                    }

                    //Not enum
                    return SerializationHelper.getGson().fromJson(value, classType);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    //This can take ANY enum and iterate through each element.
    private static <T extends Enum<T>> T toEnum(Class<T> cls, String name) {
        T value = T.valueOf(cls, name);
        return value;
    }

    public static JsonObject removeAllInstancesOf(String keyName, JsonObject object){

        for(String key : object.keySet()){
            if(key.equals(keyName)){
                object.remove(key);
            }else{
                if(object.get(key).isJsonObject()){
                    object.add(key, removeAllInstancesOf(keyName, object.get(key).getAsJsonObject()));
                }
                if(object.get(key).isJsonArray()){
                    JsonArray array = object.get(key).getAsJsonArray();
                    for(int i = 0; i < array.size(); i++){
                        if(array.get(i).isJsonObject()){
                            array.set(i, removeAllInstancesOf(keyName, array.get(i).getAsJsonObject()));
                        }
                    }
                }
            }
        }

        return object;
    }
}
