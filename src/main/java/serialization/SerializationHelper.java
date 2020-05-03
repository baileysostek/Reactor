package serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;

public class SerializationHelper {
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
}
