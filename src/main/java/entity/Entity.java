package entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.component.*;
import entity.interfaces.Transformable;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import models.AABB;
import models.Model;
import models.ModelManager;
import org.joml.*;
import serialization.Serializable;
import util.Callback;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Entity implements Transformable, Serializable<Entity> {

    private Matrix4f transform = new Matrix4f().identity();

    //Model data for this entity
    private Model model;

    private Entity parent = null;

//    private EnumEntityType type = EnumEntityType.UNKNOWN;


    //Attributes are the raw pieces of data that make up an entity;
    private HashMap<String, Attribute> attributes    = new HashMap<>();
//    private HashMap<String, LinkedList<Attribute>> categories = new HashMap<>();
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
        this.addAttribute("Transform", new Attribute<Vector3f>("position"     , new Vector3f(0f)));
        this.addAttribute("Transform", new Attribute<Vector3f>("rotation"     , new Vector3f(0f)).setType(EnumAttributeType.SLIDERS));
        this.addAttribute("Transform", new Attribute<Vector3f>("scale"        , new Vector3f(1f)));
        this.addAttribute("Material",  new Attribute<Integer> ("textureID"    , SpriteBinder.getInstance().getFileNotFoundID()));
        this.addAttribute("Material",  new Attribute<Integer> ("normalID"     , SpriteBinder.getInstance().getDefaultNormalMap()));
        this.addAttribute("Material",  new Attribute<Integer> ("metallicID"   , SpriteBinder.getInstance().getDefaultMetallicMap()));
        this.addAttribute("Material",  new Attribute<Integer> ("roughnessID"  , SpriteBinder.getInstance().getDefaultRoughnessMap()));
        this.addAttribute("Material",  new Attribute<Integer> ("aoID"         , SpriteBinder.getInstance().getDefaultAmbientOcclusionMap()));
        this.addAttribute("2D",        new Attribute<Integer> ("zIndex"       , 0));
        this.addAttribute("2D",        new Attribute<Boolean> ("autoScale"    , false));
        this.addAttribute("Title",     new Attribute<String>  ("name"         , "Undefined"));
        this.addAttribute("Title",     new Attribute<String>  ("type"         , this.toString()));
        this.addAttribute("2D",        new Attribute<Vector2f>("t_scale"      , new Vector2f(1)));
        this.addAttribute(new Attribute<Boolean>("updateInEditor", false));
        this.addAttribute(new Attribute<Boolean>("visible"      , true));
        this.addAttribute(new Attribute<Float>("mat_m", 0.5f));
        this.addAttribute(new Attribute<Float>("mat_r", 0.5f));

//        this.addComponent(new ComponentShader());

    }

    //Type
//    public final EnumEntityType getType() {
//        return this.type;
//    }

    public final Entity setType(EnumEntityType type) {
//        this.type = type;
        return this;
    }

    //Adds an attribute to this entity, and creates a subscriber that iterates through all components which may subscribe to this event.
    //Attribute interface
    public final Attribute addAttribute(Attribute attribute){
        return addAttribute("Miscellaneous", attribute);
    }

    public final Attribute addAttribute(String category, Attribute attribute){
        String name = attribute.getName();

        if(!this.hasAttribute(name)){
//            if(!category.isEmpty()){
//                if(!this.categories.containsKey(category)){
//                    this.categories.put(category, new LinkedList<Attribute>());
//                }
//                this.categories.get(category).addLast(attribute);
//                attribute.setCategory(category);
//            }

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

                    //No need to return anything.
                    return null;
                }
            });
        }else{
            //Parent has an attribute with our name
            //Check if parent value is null if so set parent to our attribute.
            if(AttributeUtils.differ(this.getAttribute(name), attribute)){
                //Parent Value is null, so overwrite parent
                this.setAttribute(attribute);
            }
            //Append new subscribers.
            this.getAttribute(name).subscribe(attribute);
        }

        return this.getAttribute(name);
    }

    //Adds an attribute to this entity, and creates a subscriber that iterates through all components which may subscribe to this event.
    //Attribute interface
    public final void removeAttribute(Attribute attribute){
        if(this.attributes.containsKey(attribute.getName())) {
            this.attributes.remove(attribute.getName());
        }
//        for(String category : categories.keySet()){
//            if(categories.get(category).contains(attribute)){
//                categories.remove(attribute);
//            }
//        }
    }

    public final void removeAttribute(String attribute){
        if(this.attributes.containsKey(attribute)) {
            this.attributes.remove(attribute);
        }
//        for(String category : categories.keySet()){
//            for(Attribute att : categories.get(category)){
//                if(att.getName().equals(attribute)){
//                    categories.remove(att);
//                }
//            }
//        }
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
                    c.onAttributeUpdate(observed);
                }
            }
        }
    }

    public void onAttributeChanged(Attribute observed){
        return;
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

    //NOTE this will clear all attributes, even the default and locked ones, use carefully
    protected final void clearAttributes(){
        this.attributes.clear();
    }

    //Transformation
    @Override
    public final Entity setPosition(Vector3f vector3f) {
        this.attributes.get("position").setData(new Vector3f(vector3f));
        return this;
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.attributes.get("position").setData(new Vector3f(x, y, z));
    }

    @Override
    public final Vector3f getPosition(){
        Vector3f out = new Vector3f();
        this.getTransform().getTranslation(out);
        return out;
    }

    @Override
    public final Vector3f getPositionSelf(){
        return (Vector3f) this.attributes.get("position").getData();
    }

    @Override
    public Vector3f translate(float x, float y, float z) {
        Vector3f pos = (Vector3f) this.attributes.get("position").getData();
        pos.x += x;
        pos.y += y;
        pos.z += z;
        return pos;
    }

    @Override
    public Vector3f translate(Vector3f offset) {
        Vector3f pos = (Vector3f) this.attributes.get("position").getData();
        pos.add(offset);
        return pos;
    }

    @Override
    public final Entity setRotation(Vector3f rot) {
        this.attributes.get("rotation").setData(rot);
        return this;
    }


    public void setRotation(Quaternionf rotation) {
        this.attributes.get("rotation").setData(rotation);
    }

    @Override
    public final Vector3f getRotation() {
        return (Vector3f) this.attributes.get("rotation").getData();
    }

    @Override
    public final Vector3f getScale() {
        return new Vector3f((Vector3f) this.attributes.get("scale").getData());
    }

    @Override
    public final Entity setScale(Vector3f scale) {
        this.attributes.get("scale").setData(new Vector3f(scale));
        return this;
    }

    @Override
    public final Entity setScale(float scale) {
        this.attributes.get("scale").setData(new Vector3f(scale));
        return this;
    }

    //Get transform
    public final Matrix4f getTransform(){
        transform = transform.identity();

        if(this.parent != null){
            Matrix4f parent = new Matrix4f(this.parent.getTransform());
            transform.mul(parent, transform);
        }

        Object rotationData = this.attributes.get("rotation").getData();
        Quaternionf orientation = new Quaternionf();

        if(rotationData instanceof Vector3f) {
            Vector3f rotation = (Vector3f) rotationData;
            Quaternionf qPitch = new Quaternionf().fromAxisAngleDeg(new Vector3f(1, 0, 0), rotation.x);
            Quaternionf qRoll = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), rotation.y);
            Quaternionf qYaw = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 0, 1), rotation.z);

            orientation = ((qPitch.mul(qYaw)).mul(qRoll)).normalize();
        }

        if(rotationData instanceof Quaternionf) {
            orientation = (Quaternionf) rotationData;
        }

        transform.translate((Vector3f) this.attributes.get("position").getData(), transform);
        transform.rotate(orientation);
        transform.scale(getScale(), transform);

        return this.transform;
    }

    //Cannot change this one
    public final void selfUpdate(double delta) {
        for(Component component : this.components){
            component.update(delta);
        }
        //Call to child
        this.update(delta);

        if(this.getModel() != null){
            this.getModel().update(delta);
        }
    }

    //Update Method
    public void update(double delta){

    }

    public final Vector3f[] getAABB(){
        //Get the model transform
        //2 index Vec3 array, [min, max]


        //If we have a model use that AABB, otherwise use a normalized AABB
        if(this.getModel() != null) {
            AABB tmp = new AABB();
            for (Vector3f point : this.getModel().getAABB().getVerteces()) {
                Vector3f tmpPoint = new Vector3f(point);
//                tmpPoint.mul(new Vector3f(this.getScale()));
                tmpPoint.mulDirection(this.getTransform());
                tmpPoint.add(this.getPosition());
                tmp.recalculateFromPoint(tmpPoint);
            }

            return new Vector3f[]{tmp.getMIN(), tmp.getMAX()};
        }else{
            AABB defaultShape = new AABB(new Vector3f(-1).mul(this.getScale()), new Vector3f(1).mul(this.getScale()));
            return new Vector3f[]{defaultShape.getMIN().add(this.getPosition()), defaultShape.getMAX().add(this.getPosition())};
        }
    }

    //Override me
    public void onAdd(){
        return;
    }
    public void onRemove(){
        return;
    }

    //Texture
    public final int getTextureID(){
        if(!this.hasAttribute("textureID")){
            this.addAttribute(new Attribute("textureID", -1));
        }
        return (int) this.getAttribute("textureID").getData();
    }

    //TODO unify setTexture
    public final void setTexture(int id){
        if(!this.hasAttribute("textureID")){
            this.addAttribute(new Attribute("textureID", id));
        }else {
            this.getAttribute("textureID").setData(id);
        }
        autoScaleSprite();
    }

    public final void setTexture(Sprite sprite){
        setTextureHelper("textureID", sprite);
        autoScaleSprite();
    }

    public final void setNormal(Sprite sprite){
        setTextureHelper("normalID", sprite);
    }

    public final void setMetallic(Sprite sprite){
        setTextureHelper("metallicID", sprite);
    }

    public final void setMetallic(int spriteId){
        this.getAttribute("metallicID").setData(spriteId);
    }

    public final void setRoughness(Sprite sprite){
        setTextureHelper("roughnessID", sprite);
    }

    public final void setAO(Sprite sprite){
        setTextureHelper("aoID", sprite);
    }

    private final void setTextureHelper(String attributeName, Sprite sprite){
        if(sprite != null) {
            if(!this.hasAttribute(attributeName)){
                this.addAttribute(new Attribute(attributeName, sprite.getTextureID()));
            }else {
                this.getAttribute(attributeName).setData(sprite.getTextureID());
            }
        }else{
            this.getAttribute(attributeName).setData(-1);
        }
    }

    private final void autoScaleSprite(){
        int texID = this.getTextureID();
        if(texID >= 0) {
            Sprite sprite = SpriteBinder.getInstance().getSprite(texID);
            if (this.hasAttribute("autoScale")) {
                if ((boolean) this.getAttribute("autoScale").getData()) {
                    //TODO if 2D set y = 0 other preserve y
                    this.setScale(new Vector3f(sprite.getWidth() / 16f, 1, sprite.getHeight() / 16f));
                }
            }
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

        //Check to make sure that we are not already parented to a child
        if(this.parent != null){
            if(this.parent.equals(parent)){
                //We are setting the parent to our current parent, this is redundant and results in a noop
                return;
            }
        }

        //If we have a parent, lets unlink
        if(this.parent != null){
            EntityManager.getInstance().unlink(this.parent, this);
        }
        //Link to new parent
        EntityManager.getInstance().link(parent, this);

        //Set our parent to this new parent.
        this.parent = parent;
    }

    //Rendering hooks
    public void renderInEditor(boolean selected){
        return;
    }

    //To String
    @Override
    public String toString(){
        return this.getClass().getSimpleName();
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
            if(attribute.shouldBeSerialized()) {
                attributesObject.add(attribute.getName(), attribute.serialize());
            }
        }

        //Add our attributes to out
        out.add("attributes", attributesObject);

        //TODO better serialization of components
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
            if(this.getAttribute("textureID").shouldBeSerialized()) {
                Sprite temp = SpriteBinder.getInstance().getSprite(this.getTextureID());
                if (temp != null) {
                    out.add("image", temp.serialize());
                }
            }
        }

        //Add our children
        LinkedList<Entity> childrenEntities =  EntityManager.getInstance().getEntitiesChildren(this);
        JsonArray children = new JsonArray(childrenEntities.size());
        for(Entity child : childrenEntities){
            children.add(child.serialize());
        }
        out.add("children", children);

        return out;
    }

    //From json to entity.
    @Override
    public Entity deserialize(JsonObject data) {
        System.out.println(data);
        //If we have a model
        if(data.has("model")) {
            this.model = new Model(ModelManager.getInstance().getNextID()).deserialize(data.get("model").getAsJsonObject());
        }
        //If we have any attributes
        if(data.has("attributes")) {
            for(String key : data.get("attributes").getAsJsonObject().keySet()){
                Attribute attribute = new Attribute(data.getAsJsonObject("attributes").getAsJsonObject(key));
                this.addAttribute(attribute.getCategory(), attribute);
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
//            for(Component c : this.components){
//                c.postInitialize();
//            }
        }
        //If we have a sprite
        if(data.has("image")) {
            Sprite sprite = new Sprite(data.get("image").getAsJsonObject());
            System.out.println("Deserializing sprite:"+sprite);
            this.setTexture(sprite.getTextureID());
        }

        //If we have any children
        if(data.has("children")) {
            JsonArray components = data.get("children").getAsJsonArray();
            for(int i = 0; i < components.size(); i++){
                JsonObject childData = components.get(i).getAsJsonObject();
                Entity child = new Entity().deserialize(childData);
                child.setParent(this);
                EntityManager.getInstance().addEntity(child);
            }
        }

        return this;
    }

    public String getName() {
        String name = this.toString();
        if (this.hasAttribute("name")) {
            name = (String) this.getAttribute("name").getData();
        }

        if(name.equals("Undefined")){
            if (this.hasAttribute("type")) {
                name = (String) this.getAttribute("type").getData();
            }
        }

        return name;
    }

    //If this entity does not have a visible property it is not rendered.
    public boolean isVisible(){
        if (this.hasAttribute("visible")) {
           return (boolean) this.getAttribute("visible").getData();
        }
        return false;
    }

    public void setVisible(boolean visible){
        if (this.hasAttribute("visible")) {
            this.getAttribute("visible").setData(visible);
        }
    }

    public boolean hasParent() {
        return this.parent != null;
    }

//    public Collection<String> getAttributeCategories() {
//        return this.categories.keySet();
//    }
//
//    public Collection<Attribute> getAttributesOfCategory(String category) {
//        return this.categories.get(category);
//    }

    public void setAttribute(Attribute attribute) {
        if(this.hasAttribute(attribute.getName())){
            this.attributes.put(attribute.getName(), attribute);
        }
    }

    public Attribute overrideFromChild(Attribute attribute) {
        if(this.hasAttribute(attribute.getName())){
            return attributes.get(attribute.getName());
        }
        return attribute;
    }


    protected final void cleanup(){
        for(Component c : components){
            c.onRemove();
        }
    }
}
