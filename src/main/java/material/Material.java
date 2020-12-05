package material;

import com.google.gson.JsonObject;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import serialization.Serializable;

public class Material implements Serializable<Material> {
    //Properties of Material
    private String name;

    //Texture ID representing the screenshot of this material rendered in world.
    private int textureID;

    //Different Texture Channels
    private int albedoID;
    private int metallicID;
    private int ambientOcclusionID;
    private int normalID;
    private int roughnessID;

    public Material(){
        initialize();
    }

    public Material(Sprite sprite){
        initialize();
        albedoID = sprite.getTextureID();
    }

    public Material(Material clone){
        initialize();
        Material copy = clone.newInstance();
        this.albedoID = copy.albedoID;
        this.metallicID = copy.metallicID;
        this.ambientOcclusionID = copy.ambientOcclusionID;
        this.normalID = copy.normalID;
        this.roughnessID = copy.roughnessID;
    }

    private void initialize(){
        albedoID = SpriteBinder.getInstance().getFileNotFoundID();
        metallicID = SpriteBinder.getInstance().getDefaultMetallicMap();
        ambientOcclusionID = SpriteBinder.getInstance().getDefaultAmbientOcclusionMap();
        normalID = SpriteBinder.getInstance().getDefaultNormalMap();
        roughnessID = SpriteBinder.getInstance().getDefaultRoughnessMap();

        textureID = MaterialManager.getInstance().generatePreview(this);

        System.out.println("Material texture:" + textureID  );
    }

    public Material newInstance(){
        //Template for new material instance.
        Material material = new Material();

        //Register the sprite copies
        material.albedoID = SpriteBinder.getInstance().copySprite(albedoID).getTextureID();
        material.metallicID = SpriteBinder.getInstance().copySprite(metallicID).getTextureID();
        material.ambientOcclusionID = SpriteBinder.getInstance().copySprite(ambientOcclusionID).getTextureID();
        material.normalID = SpriteBinder.getInstance().copySprite(normalID).getTextureID();
        material.roughnessID = SpriteBinder.getInstance().copySprite(roughnessID).getTextureID();

        //Return the copy
        return material;
    }

    public void refreshTexture(){
        //Logic to take a new snapshot.
    }

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        out.addProperty("name", name);
        return null;
    }

    @Override
    public Material deserialize(JsonObject data) {

        return this;
    }

    public int getAlbedoID() {
        return albedoID;
    }

    public int getMetallicID() {
        return metallicID;
    }

    public int getAmbientOcclusionID() {
        return ambientOcclusionID;
    }

    public int getNormalID() {
        return normalID;
    }

    public int getRoughnessID() {
        return roughnessID;
    }
}
