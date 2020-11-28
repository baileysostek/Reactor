package material;

import com.google.gson.JsonObject;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import serialization.Serializable;

public class Material implements Serializable<Material> {
    private int id;
    private String name;

    //Different Texture Channels
    private int albedoID;
    private int metallicID;
    private int ambientOcclusionID;
    private int normalID;
    private int roughnessID;

    public Material(Sprite sprite){
        initialize();
        albedoID = sprite.getTextureID();
    }

    private void initialize(){
        albedoID = SpriteBinder.getInstance().getFileNotFoundID();
        metallicID = SpriteBinder.getInstance().getDefaultMetallicMap();
        ambientOcclusionID = SpriteBinder.getInstance().getDefaultAmbientOcclusionMap();
        normalID = SpriteBinder.getInstance().getDefaultNormalMap();
        roughnessID = SpriteBinder.getInstance().getDefaultRoughnessMap();
    }

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        out.addProperty("name", name);
        out.addProperty("id", id);
        return null;
    }

    @Override
    public Material deserialize(JsonObject data) {

        return this;
    }
}
