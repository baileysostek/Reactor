package serialization;

import com.google.gson.JsonObject;

public interface Serializable <T>{
    public default JsonObject serialize(){
      return serialize(null);
    };
    public JsonObject serialize(JsonObject meta);
    public T deserialize(JsonObject data);
}
