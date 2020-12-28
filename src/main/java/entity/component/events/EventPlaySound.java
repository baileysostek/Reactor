package entity.component.events;

import com.google.gson.JsonObject;
import entity.component.Event;
import sound.SoundEngine;
import util.Callback;


public class EventPlaySound extends Event {

    int soundID = -1;
    private String sourceName;

    //Default constructor
    public EventPlaySound(){}

    public EventPlaySound(String soundName) {
        this.sourceName = soundName;
        intialize();
    }

    public void intialize(){
        soundID = SoundEngine.getInstance().loadSound(this.sourceName);
        int sourcePointer = SoundEngine.getInstance().createSoundSource();
        SoundEngine.getInstance().loadSoundIntoSource(sourcePointer, soundID);

        this.setCallback(new Callback() {
            @Override
            public Object callback(Object... objects) {
                System.out.println("Trying to play sound.");
                SoundEngine.getInstance().playSound(soundID);
                return null;
            }
        });
    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.addProperty("sourceName", sourceName);
        return out;
    }

    @Override
    public Event deserialize(JsonObject data) {
        if(data.has("sourceName")){
            this.sourceName = data.get("sourceName").getAsString();
            intialize();
        }
        return this;
    }
}
