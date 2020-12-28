package entity.component.events;

import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Event;
import graphics.sprite.Sprite;
import sound.SoundEngine;
import util.Callback;


public class EventSetSprite extends Event {

    private Sprite sprite;


    public EventSetSprite() {}

    public EventSetSprite(Sprite sprite) {
        this.sprite = sprite;
        initalize();
    }

    private void initalize(){
        this.setCallback(new Callback() {
            @Override
            public Object callback(Object... objects) {
                Entity e = getParent();
                if(e != null){
                    e.setTexture(sprite);
                }else{
                    System.out.println("Cannot set texture, entity is null.");
                }
                return null;
            }
        });
    }

    public EventSetSprite(JsonObject data) {
        this.deserialize(data);
    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.add("sprite", this.sprite.serialize());
        return out;
    }

    @Override
    public Event deserialize(JsonObject data) {
        if(data.has("sprite")) {
            this.sprite = new Sprite(data.get("sprite").getAsJsonObject());
        }
        initalize();
        return this;
    }
}
