package entity.component.events;

import com.google.gson.JsonObject;
import entity.component.Event;
import util.Callback;


public class EventCallback extends Event {

    public EventCallback(Callback callback) {
        this.setCallback(callback);
    }

    @Override
    public JsonObject serialize() {
        return null;
    }

    @Override
    public Event deserialize(JsonObject data) {
        return null;
    }
}
