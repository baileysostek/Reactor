package entity.component.events;

import com.google.gson.JsonObject;
import entity.component.Event;
import org.joml.Vector3f;
import util.Callback;


public class EventWarp extends Event {

    private String   desination;
    private Vector3f position;

    public EventWarp() {}

    public EventWarp(String destination) {
        this.desination = desination;
        initalize();
    }

    private void initalize(){
        this.setCallback(new Callback() {
            @Override
            public Object callback(Object... objects) {

                return null;
            }
        });
    }

    public EventWarp(JsonObject data) {
        this.deserialize(data);
    }

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        out.addProperty("destination", desination);
        return out;
    }

    @Override
    public Event deserialize(JsonObject data) {
        if(data.has("destination")) {
            this.desination = data.get("destination").getAsString();
        }
        initalize();
        return this;
    }
}
