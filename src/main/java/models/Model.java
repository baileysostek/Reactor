package models;

import com.google.gson.JsonObject;
import graphics.renderer.Handshake;
import org.joml.Vector3f;
import serialization.Serializable;

public class Model implements Serializable<Model> {

    //Metadata
    private String name;
    private int numIndicies = 0;
    private int id;

    private AABB aabb = new AABB();

    //VAO
    private Handshake handshake;

    //Just used to create a new pointer so deserilize can be called
    public Model(int id){
        this.id = id;
    }

    public Model(int id, Handshake handshake, int numIndicies, Vector3f[] AABB){
//        this.name      = name;
        this.id = id;
        this.handshake = handshake;
        this.numIndicies = numIndicies;
        aabb = new AABB(AABB[0], AABB[1]);
    }

    public Handshake getHandshake(){
        return this.handshake;
    }

    public int getID(){
        return this.id;
    }

    public int getNumIndicies() {
        return this.numIndicies;
    }

    public Vector3f[] getAABB(){
        return new Vector3f[]{new Vector3f(this.aabb.getMIN()), new Vector3f(this.aabb.getMAX())};
    }

    @Override
    public JsonObject serialize() {
        JsonObject saveData = new JsonObject();
        saveData.add("handshake", handshake.serialize());
        saveData.addProperty("indices", this.numIndicies);
        saveData.add("aabb", aabb.serialize());
        return saveData;
    }

    @Override
    public Model deserialize(JsonObject data) {
        this.numIndicies = data.get("indices").getAsInt();
        this.handshake = new Handshake().deserialize(data.get("handshake").getAsJsonObject());
        if(data.has("aabb")) {
            this.aabb = new AABB().deserialize(data.get("aabb").getAsJsonObject());
        }
        return this;
    }

}
