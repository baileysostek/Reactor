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

//    //Attributes that this material loads into its shader.
//    private HashMap<String, Attribute> attributes = new HashMap<>();

    //String representing the shader that is used to render this material.
    private Attribute<String> shaderName;
    private int shaderID;
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
        redrawPreview();
    }

    public Material setName(String name){
        this.name.setData(name);
        return this;
    }

    private void initialize(){
        //Get the default shader
        shaderID = ShaderManager.getInstance().getDefaultShader();

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
                if(shaderName.getData().equals("default")) {
                    shaderID = ShaderManager.getInstance().getDefaultShader();
                }else{
                    shaderID = ShaderManager.getInstance().loadShader(shaderName.getData());
                }
                redrawPreview();
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

    public void setShader(int id){
        shaderID = id;
        //NOTE setData no update is unsafe and breaks the observer pattern. No subscribers will be notified by this change.
        this.shaderName.setDataNoUpdate(ShaderManager.getInstance().lookupName(id));
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
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.addProperty("name", this.name.getData());

        //IF this material was generated from source files, add references to those source files, otherwise add a serialized instance of the string.
        String albedoSource = SpriteBinder.getInstance().lookupFileFromTextureID(this.albedoID);
        if(albedoSource != null || this.albedoID == SpriteBinder.getInstance().getFileNotFoundID()) {
            out.addProperty("albedoID", albedoSource);
        }else{
            Sprite sprite = SpriteBinder.getInstance().getSprite(this.albedoID);
            int hash = sprite.hashCode();
            out.addProperty("albedoID", hash);
            if(meta.has("sprites")){
                meta.get("sprites").getAsJsonObject().add(hash+"", sprite.serialize());
            }
        }

        String metallicSource = SpriteBinder.getInstance().lookupFileFromTextureID(this.metallicID);
        if(metallicSource != null || this.metallicID == SpriteBinder.getInstance().getFileNotFoundID()) {
            out.addProperty("metallicID", metallicSource);
        }else{
            Sprite sprite = SpriteBinder.getInstance().getSprite(this.metallicID);
            int hash = sprite.hashCode();
            out.addProperty("metallicID", hash);
            if(meta.has("sprites")){
                meta.get("sprites").getAsJsonObject().add(hash+"", sprite.serialize());
            }
        }

        String ambientOcclusionSource = SpriteBinder.getInstance().lookupFileFromTextureID(this.ambientOcclusionID);
        if(ambientOcclusionSource != null || this.ambientOcclusionID == SpriteBinder.getInstance().getFileNotFoundID()) {
            out.addProperty("ambientOcclusionID", ambientOcclusionSource);
        }else{
            Sprite sprite = SpriteBinder.getInstance().getSprite(this.ambientOcclusionID);
            int hash = sprite.hashCode();
            out.addProperty("ambientOcclusionID", hash);
            if(meta.has("sprites")){
                meta.get("sprites").getAsJsonObject().add(hash+"", sprite.serialize());
            }
        }

        String normalSource = SpriteBinder.getInstance().lookupFileFromTextureID(this.normalID);
        if(normalSource != null || this.normalID == SpriteBinder.getInstance().getFileNotFoundID()) {
            out.addProperty("normalID", normalSource);
        }else{
            Sprite sprite = SpriteBinder.getInstance().getSprite(this.normalID);
            int hash = sprite.hashCode();
            out.addProperty("normalID", hash);
            if(meta.has("sprites")){
                meta.get("sprites").getAsJsonObject().add(hash+"", sprite.serialize());
            }
        }

        String roughnessSource = SpriteBinder.getInstance().lookupFileFromTextureID(this.roughnessID);
        if(roughnessSource != null || this.roughnessID == SpriteBinder.getInstance().getFileNotFoundID()) {
            out.addProperty("roughnessID", roughnessSource);
        }else{
            Sprite sprite = SpriteBinder.getInstance().getSprite(this.roughnessID);
            int hash = sprite.hashCode();
            out.addProperty("roughnessID", hash);
            if(meta.has("sprites")){
                meta.get("sprites").getAsJsonObject().add(hash+"", sprite.serialize());
            }
        }

        out.addProperty("shaderName", this.shaderName.getData());

        return out;
    }

    @Override
    public Material deserialize(JsonObject data) {
        if(data.has("name")){
            String name = data.get("name").getAsString();
            if(MaterialManager.getInstance().hasMaterial(name)){
                return MaterialManager.getInstance().getMaterial(name);
            }
            this.name.setData(name);
        }

        if(data.has("albedoID")){
            if(!data.get("albedoID").isJsonNull()){
                this.albedoID = SpriteBinder.getInstance().load(data.get("albedoID").getAsString()).getTextureID();
            }
        }

        if(data.has("metallicID")){
            if(!data.get("metallicID").isJsonNull()){
                this.metallicID = SpriteBinder.getInstance().load(data.get("metallicID").getAsString()).getTextureID();
            }
        }

        if(data.has("ambientOcclusionID")){
            if(!data.get("ambientOcclusionID").isJsonNull()){
                this.ambientOcclusionID = SpriteBinder.getInstance().load(data.get("ambientOcclusionID").getAsString()).getTextureID();
            }
        }

        if(data.has("normalID")){
            if(!data.get("normalID").isJsonNull()){
                this.normalID = SpriteBinder.getInstance().load(data.get("normalID").getAsString()).getTextureID();
            }
        }

        if(data.has("roughnessID")){
            if(!data.get("roughnessID").isJsonNull()){
                this.roughnessID = SpriteBinder.getInstance().load(data.get("roughnessID").getAsString()).getTextureID();
            }
        }

        if(data.has("shaderName")){
            this.shaderName.setData(data.get("shaderName").getAsString());
        }

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


    public int getShaderID() {
        return shaderID;
    }
}
