package entity.component;

import com.google.gson.JsonObject;
import entity.Entity;
import serialization.Serializable;
import util.Callback;

public abstract class Event implements Serializable<Event> {

    private Callback callback;
    private Entity parent;

    public Event(){

    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }

    public Entity getParent(){
        return this.parent;
    }

    public Event setCallback(Callback callback){
        this.callback = callback;
        return this;
    }

    public void invoke(Object ... objects){
        if(callback != null){
            callback.callback(objects);
        }
    }
}
