package material;

import com.google.gson.JsonObject;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import graphics.renderer.Shader;
import graphics.renderer.ShaderManager;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import serialization.Serializable;
import util.Callback;

import java.util.HashMap;

public class Material implements Serializable<Material> {
    //Properties of Material
    private Attribute<String> name;

    //Texture ID representing the screenshot of this material rendered in world.
    private int textureID;

    //Different Texture Channels
    private int albedoID;
    private int metallicID;
    private int ambientOcclusionID;
    private int normalID;
    private int roughnessID;

    //Attributes that this material loads into its shader.
    private HashMap<String, Attribute> attributes = new HashMap<>();

    //String representing the shader that is used to render this material.
    private Attribute<String> shaderName;
    private Shader shader;
    private boolean shaderValid = false;

    protected Material(){
        initialize();
    }

    protected Material(int textureID){
        initialize();
        albedoID = textureID;
        redrawPreview();

    }

    protected Material(Sprite sprite){
        initialize();
        albedoID = sprite.getTextureID();
        redrawPreview();
    }

    protected Material(Material clone){
        initialize();
        Material copy = clone.newInstance();
        this.albedoID = copy.albedoID;
        this.metallicID = copy.metallicID;
        this.ambientOcclusionID = copy.ambientOcclusionID;
        this.normalID = copy.normalID;
        this.roughnessID = copy.roughnessID;
    }

    public Material setName(String name){
        this.name.setData(name);
        return this;
    }

    private void initialize(){
        //Get references for all of our textures.
        albedoID            = SpriteBinder.getInstance().getFileNotFoundID();
        metallicID          = SpriteBinder.getInstance().getDefaultMetallicMap();
        ambientOcclusionID  = SpriteBinder.getInstance().getDefaultAmbientOcclusionMap();
        normalID            = SpriteBinder.getInstance().getDefaultNormalMap();
        roughnessID         = SpriteBinder.getInstance().getDefaultRoughnessMap();

        //Material Name
        name = new Attribute<String>("name", "Material_" + MaterialManager.getInstance().getNextID());

        name.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                MaterialManager.getInstance().updateMapping((String) objects[1], (String) objects[2]);
                return null;
            }
        });


        //Init shader name.
        shaderName = new Attribute<String>("shader", "");

        shaderName.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                System.out.println("Objects:" + objects);
//                shader = ShaderManager.getInstance().hasShader();
                return null;
            }
        });

        shaderName.setData("default");

        //Generate our default texture.
        redrawPreview();

        System.out.println("Material texture:" + textureID);

        //

    }

    private void redrawPreview(){
        //TODO pass in shader, also there can only be one global preview texture the way things are set up now.
        textureID = MaterialManager.getInstance().generatePreview(this);
    }

    public String getName(){
        return this.name.getData();
    }

    public void setShader(String name){
        this.shaderName.setData(name);
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

    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
//        out.addProperty("name", name);
        return null;
    }

    @Override
    public Material deserialize(JsonObject data) {

        return this;
    }

    //Getters for our lovely textureIDs
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

    public int getMaterialPreview() {
        return textureID;
    }

    //Setters
    public void setAlbedoID(int albedoID) {
        this.albedoID = albedoID;
        redrawPreview();
    }

    public void setMetallicID(int metallicID) {
        this.metallicID = metallicID;
        redrawPreview();
    }

    public void setAmbientOcclusionID(int ambientOcclusionID) {
        this.ambientOcclusionID = ambientOcclusionID;
        redrawPreview();
    }

    public void setNormalID(int normalID) {
        this.normalID = normalID;
        redrawPreview();
    }

    public void setRoughnessID(int roughnessID) {
        this.roughnessID = roughnessID;
        redrawPreview();
    }


}
