package serialization;

import com.google.gson.JsonObject;

public interface Serializable <T>{
    public JsonObject serialize();
    public T deserialize(JsonObject data);
}
