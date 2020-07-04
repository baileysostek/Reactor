package entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.component.Attribute;
import entity.component.Component;
import entity.interfaces.Transformable;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import math.MatrixUtils;
import models.Model;
import models.ModelManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import serialization.Serializable;
import serialization.SerializationHelper;
import util.Callback;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Entity implements Transformable, Serializable<Entity> {

    //Transform properties of an entity
    private Vector3f rotation = new Vector3f(0f);
    private Vector3f scale    = new Vector3f(1f);

    private Matrix4f transform = new Matrix4f().identity();

    //Model data for this entity
    private Model model;

    private Entity parent = null;

    private EnumEntityType type = EnumEntityType.UNKNOWN;


    //Attributes are the raw pieces of data that make up an entity;
    private HashMap<String, Attribute> attributes = new HashMap<>();
    //Components reference and set attributes on an entity, the Attributes are a state and the components are how they are modified.
    private LinkedList<Component> components = new LinkedList<Component>();

    //New entity template.
    public Entity(){
        //Every entity has some default attributes
        addDefaultAttributes();
    }

    //New entity from primative file
    public Entity(String save){
        //Every entity has some default attributes
        addDefaultAttributes();
        this.deserialize(StringUtils.loadJson(save));
    }

    //new entity from JsonObject, deserialize entity.
    //Load an entity from a saveObject
    public Entity(JsonObject saveData) {
        //Add default
        addDefaultAttributes();
        //initializes self.
        deserialize(saveData);
    }

    private final void addDefaultAttributes(){
        //Every entity has some default attributes
        this.addAttribute(new Attribute<Vector3f>("position" , new Vector3f(0f)));
        this.addAttribute(new Attribute<Vector3f>("rotation" , new Vector3f(0f)));
        this.addAttribute(new Attribute<Vector3f>("scale"    , new Vector3f(1f)));
        this.addAttribute(new Attribute<Integer> ("textureID", -1));
        this.addAttribute(new Attribute<Boolean> ("autoScale", true));
    }

    //Type
    public final EnumEntityType getType() {
        return this.type;
    }

    public final Entity setType(EnumEntityType type) {
        this.type = type;
        return this;
    }

    //Adds an attribute to this entity, and creates a subscriber that iterates through all components which may subscribe to this event.
    //Attribute interface
    public final void addAttribute(Attribute attribute){
        //Add to our list of entities
        //TODO this may break thing

        this.attributes.put(attribute.getName(), attribute);
        //Create subscriber to this attribute changing states
        attribute.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
            //Cast passed attribute
            Attribute attribute = (Attribute) objects[0];
            //Update all components
            syncAttributes(attribute, new LinkedList<Component>());
            //IF this is an attribute with a classification that we sort based on we need to resort the entities.
            //TODO have attribute classificaitons
            if (attribute.getName().equals("zIndex")) {
                EntityManager.getInstance().resort();
            }

                System.out.println(attribute.getName()+" is being set to:"+attribute.getData());

            //No need to return anything.
            return null;
            }
        });

    }

    //Attributes when they update emmit a message, this is where that message is sent.
    public final void syncAttributes(Attribute observed, LinkedList<Component> closedList){
        //For every component
        for(Component c : components){
            //Not on the closed list
            if(!closedList.contains(c)) {
                //Add to closed list
                closedList.add(c);
                if (c.hasAttribute(observed.getName())) {
                    //Update component
                    c.setAttribute(observed, closedList);
                }
            }
        }
    }

    public final boolean hasAttribute(String attributeName) {
        return this.attributes.containsKey(attributeName);
    }

    public final Attribute getAttribute(String name){
        if(attributes.containsKey(name)){
            return attributes.get(name);
        }else{
            return null;
        }
    }

    //Add this component to an entity
    public void addComponent(Component component) {
        this.components.add(component);
        component.onAdd(this);
    }

    protected final Collection<Component> getComponents(){
        return this.components;
    }

    protected final Collection<Attribute> getAttributes(){
        return this.attributes.values();
    }

    //Transformation
    @Override
    public final Entity setPosition(Vector3f vector3f) {
        this.attributes.get("position").setData(new Vector3f(vector3f));
        return this;
    }

    @Override
    public final Vector3f getPosition(){
        return (Vector3f) this.attributes.get("position").getData();
    }

    @Override
    public final Entity setRotation(Vector3f rot) {
         this.rotation = rot;
         return this;
    }

    @Override
    public final Vector3f getRotation() {
        return this.rotation;
    }

    @Override
    public final Vector3f getScale() {
        return this.scale;
    }

    @Override
    public final Entity setScale(Vector3f scale) {
        this.scale = scale;
        return this;
    }

    //Get transform
    public final Matrix4f getTransform(){
        transform = transform.identity();
        transform.translate((Vector3f) this.attributes.get("position").getData(), transform);
        transform.rotateAffineXYZ(rotation.x, rotation.y, rotation.z, transform);
        transform.scale(scale, transform);

        if(this.parent != null){
            Matrix4f parent = new Matrix4f(this.parent.getTransform());
            transform.mul(parent, transform);
        }

        return this.transform;
    }

    //Cannot change this one
    public final void selfUpdate(double delta) {
        for(Component component : this.components){
            component.update(delta);
        }
        //Call to child
        this.update(delta);
    }

    //Update Method
    public void update(double delta){

    }

    //Gets aabb in worldspace, I guess at this point its just a bb? because there is no more aa
    public final Vector3f[] getAABB(){
        //Get the model transform
        float[] transform = MatrixUtils.getIdentityMatrix();
//        Matrix.rotateM(transform, 0, (float) Math.toRadians(rotation.z()), 0f,0f, 1f);
//        Matrix.rotateM(transform, 0, (float) Math.toRadians(rotation.y()), 0f,1f, 0f);
//        Matrix.rotateM(transform, 0, (float) Math.toRadians(rotation.x()), 1f,0f, 0f);

        //Raw unNormaized and unTranslated AABB
        math.Vector3f[] out = this.model.getAABB();

        float[] min = out[0].toVec4();
        float[] min_out = new float[]{0, 0, 0, 1};
        min_out = MatrixUtils.multiplyMV(min_out, transform, 0, min, 0);

        float[] max = out[1].toVec4();
        float[] max_out = new float[]{0, 0, 0, 1};
        max_out = MatrixUtils.multiplyMV(max_out, transform, 0, max, 0);

        return new Vector3f[]{};
    }

    //Override me
    public void onAdd(){
        return;
    }

    //Texture
    public final int getTextureID(){
        if(!this.hasAttribute("textureID")){
            this.addAttribute(new Attribute("textureID", -1));
            System.out.println("[232] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }
        return (int) this.getAttribute("textureID").getData();
    }

    //TODO unify setTexture
    public final void setTexture(int id){
        if(!this.hasAttribute("textureID")){
            this.addAttribute(new Attribute("textureID", id));
            System.out.println("[241] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }else {
            this.getAttribute("textureID").setData(id);
            System.out.println("[244] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }
    }

    public final void setTexture(Sprite sprite){
        if(sprite != null) {
            if(!this.hasAttribute("textureID")){
                this.addAttribute(new Attribute("textureID", sprite.getTextureID()));
                System.out.println("[251] Set the texture ID to:"+this.getAttribute("textureID").getData());
            }else {
                this.getAttribute("textureID").setData(sprite.getTextureID());
            }
            System.out.println("[255] Set the texture ID to:"+this.getAttribute("textureID").getData());
            //TODO if preserve scale
            if(this.hasAttribute("autoScale")) {
                if ((boolean) this.getAttribute("autoScale").getData()) {
                    this.setScale(new Vector3f(sprite.getWidth() / 16f, 1, sprite.getHeight() / 16f));
                }
            }
        }else{
            this.getAttribute("textureID").setData(-1);
            System.out.println("[262] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }
    }

    public final void setTexture(String texture){
        if(!this.hasAttribute("textureID")){
            this.addAttribute(new Attribute("textureID", -1));
            System.out.println("[269] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }
        if(!texture.isEmpty()){
            this.getAttribute("textureID").setData(SpriteBinder.getInstance().load(texture).getTextureID());
            System.out.println("[273] Set the texture ID to:"+this.getAttribute("textureID").getData());
        }
    }

    //Model
    public final Model getModel(){
        return model;
    }

    public final Entity setModel(Model m) {
        this.model = m;
        return this;
    }

    //Parent
    public final Entity getParent(){
        return this.parent;
    }

    public final void setParent(Entity parent){
        EntityManager.getInstance().link(parent, this);
        this.parent = parent;
    }

    //To String
    @Override
    public String toString(){
        return this.getType().toString();
    }

    //Going from an entity to json
    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        //Add our Model if it exists
        if(this.model != null) {
            out.add("model", this.model.serialize());
        }
        //Create a json object to hold our attributes
        JsonObject attributesObject = new JsonObject();
        //Add all of our attributes to an out array
        for(Attribute attribute : this.attributes.values()){
            //SOO we need to parse this object back to the initial class type, so we will make an intermediate object
            JsonObject helperObject = new JsonObject();
            helperObject.addProperty("class", attribute.getData().getClass().getName());
            helperObject.add("value", SerializationHelper.getGson().toJsonTree(attribute.getData()));
            attributesObject.add(attribute.getName(), helperObject);
        }

        //Add our attributes to out
        out.add("attributes", attributesObject);

        //Create a json object to hold our components
        JsonArray componentsArray = new JsonArray(components.size());
        //Add all of our components to an out array
        for(int i = 0; i < this.components.size(); i++){
            Component component = this.components.get(i);
            JsonObject helperObject = new JsonObject();
            helperObject.addProperty("class", component.getClass().getName());
            helperObject.add("value", component.serialize());
            componentsArray.add(helperObject);
        }

        //Add our attributes to out
        out.add("components", componentsArray);

        //Add our sprite
        if(this.hasAttribute("textureID")){
            out.add("image", SpriteBinder.getInstance().getSprite(this.getTextureID()).serialize());
        }

        return out;
    }

    //From json to entity.
    @Override
    public Entity deserialize(JsonObject data) {
        //If we have a model
        if(data.has("model")) {
            this.model = new Model(ModelManager.getInstance().getNextID()).deserialize(data.get("model").getAsJsonObject());
        }
        //If we have any attributes
        if(data.has("attributes")) {
            for(String key : data.get("attributes").getAsJsonObject().keySet()){
                try {
                    JsonObject helperObject = data.getAsJsonObject("attributes").getAsJsonObject(key);
                    Class<?> classType = Class.forName(helperObject.get("class").getAsString());
                    Attribute<?> attribute = new Attribute<>(key, SerializationHelper.getGson().fromJson(helperObject.get("value"), classType));
                    //Add our attribute
                    this.addAttribute(attribute);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        //If we have any components
        if(data.has("components")) {
            JsonArray components = data.get("components").getAsJsonArray();
            for(int i = 0; i < components.size(); i++){
                JsonObject component = components.get(i).getAsJsonObject();
                try {
                    Class<?> classType = Class.forName(component.get("class").getAsString());
                    //Add our component
                    this.addComponent(((Component)classType.newInstance()).deserialize(component.getAsJsonObject("value")));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        //If we have a sprite
        if(data.has("image")) {
            Sprite sprite = new Sprite(data.get("image").getAsJsonObject());
            System.out.println("Deserializing sprite:"+sprite);
            this.setTexture(sprite.getTextureID());
        }

        return this;
    }
}
