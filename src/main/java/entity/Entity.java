package entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.component.*;
import entity.interfaces.Transformable;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import material.Material;
import material.MaterialManager;
import models.AABB;
import models.Model;
import models.ModelManager;
import org.joml.*;
import serialization.Serializable;
import util.Callback;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Entity implements Transformable, Serializable<Entity> {

    //Entity Transform in world.
    private Matrix4f transform = new Matrix4f().identity();

    //Parent entity
    private Entity parent = null;

    //Attributes are the raw pieces of data that make up an entity;
    private LinkedHashMap<String, Attribute> attributes    = new LinkedHashMap<>();

    //Components reference and set attributes on an entity, the Attributes are a state and the components are how they are modified.
    private LinkedList<Component> components = new LinkedList<Component>();

    //Model data for this entity
    private Model model;
    private String boneParent;

    private AnimationComponent animationComponent = null;


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

        LinkedList<Material> tmp = new LinkedList<>();
        this.addAttribute("Material",  new  Attribute<LinkedList<Material>>("materials", tmp));
//        this.addAttribute("2D",        new Attribute<Integer> ("zIndex"       , 0));
//        this.addAttribute("2D",        new Attribute<Boolean> ("autoScale"    , false));
        this.addAttribute("Title",     new Attribute<String>  ("name"         , "Undefined"));
        this.addAttribute("Title",     new Attribute<String>  ("type"         , this.toString()));
//        this.addAttribute("2D",        new Attribute<Vector2f>("t_scale"      , new Vector2f(1)));
        this.addAttribute(new Attribute<Boolean>("updateInEditor", false));
        this.addAttribute(new Attribute<Boolean>("visible"      , true));

//        this.addComponent(new ComponentShader());

    }

    //Adds an attribute to this entity, and creates a subscriber that iterates through all components which may subscribe to this event.
    //Attribute interface
    public final Attribute addAttribute(Attribute attribute){
        return addAttribute("Miscellaneous", attribute);
    }

    public final Attribute addAttribute(String category, Attribute attribute){
        String name = attribute.getName();

        //Set the category
        attribute.setCategory(category);

        if(!this.hasAttribute(name)){
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
    }

    public final void removeAttribute(String attribute){
        if(this.attributes.containsKey(attribute)) {
            this.attributes.remove(attribute);
        }
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
        //Dont bother adding a null component.
        if(component == null){
            return;
        }

        //Make sure this component does not already exist on us. If it is added twice it will trigger the onUpdate method once per instance of the object.
        if(!this.components.contains(component)) {
            this.components.add(component);
            component.onAdd(this);
            if(component instanceof AnimationComponent){
                this.animationComponent = (AnimationComponent) component;
            }
        }
    }

    public void removeComponent(Component component){

        if(component == null){
            return;
        }

        if(this.components.contains(component)){
            component.onRemove();
            this.components.remove(component);
        }
    }

    protected final Collection<Component> getComponents(){
        return this.components;
    }

    public final Collection<Component> getComponentsByClass(Class className){
        if(className == null){
            System.out.println("Error: Passed a null class to getComponentByClass(). Expected Parameter: instanceof Component");
            return null;
        }

        if(Component.class.isAssignableFrom(className)){
            LinkedList<Component> outPool = new LinkedList<>();

            for(Component component : components){
                if(className.isInstance(component)){
                    outPool.add(component);
                }
            }

            return outPool;
        }else{
            System.out.println("Error: passed class [" + className + "] is not instance of Component.class");
            return null;
        }
    }

    public boolean hasComponentOfType(Class className){
        if(className == null){
            System.out.println("Error: Passed a null class to getComponentByClass(). Expected Parameter: instanceof Component");
            return false;
        }

        if(Component.class.isAssignableFrom(className)){
            for(Component component : components){
                if(className.isInstance(component)){
                    return true;
                }
            }
        }

        return false;
    }

//    public final Component getComponentByName(){
//        return null
//    }

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

        //Null check
        if(transform == null){
            transform = new Matrix4f();
        }

        //Reset our transform
        transform = transform.identity();

        //Check to see if we have a parent.
        if(this.parent != null){
            //Recursive function to determine absolute transofrm as a sum of parent transforms.
            Matrix4f parent = new Matrix4f(this.parent.getTransform());
            transform.mul(parent, transform);
        }

        //Get local rotation
        Object rotationData = this.attributes.get("rotation").getData();
        Quaternionf orientation = new Quaternionf();

        //apply rotation from Vector or Quaternion.
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

        //Apply final Bone movement
        if(this.hasParent()){
            if(this.getParent().isAnimated()){
                this.transform.translate(this.getParent().getAnimationComponent().getRelativePositionOfBone(this.boneParent));
            }
        }

        //apply local translation rotation and scale, in that order.
        transform.translate((Vector3f) this.attributes.get("position").getData(), transform);
        transform.rotate(orientation);
        transform.scale(getScale(), transform);

        //Return transform.
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
        return;
    }

    public final Vector3f[] getAABB(){
        //Get the model transform
        //2 index Vec3 array, [min, max]
        if(this.hasAttribute("disableAABB")){
            Object data = this.getAttribute("disableAABB").getData();
            if(data instanceof Boolean){
                if((boolean) data){
                    return new Vector3f[]{new Vector3f(0), new Vector3f(0)};
                }
            }
        }


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
            //This entity does not have an AABB, check if it has children.
            if(EntityManager.getInstance().entityHasChildren(this)) {
                //Create our out AABB
                AABB tmp = new AABB();

                //This entity has children, lets recursive get their AABB
                LinkedList<Entity> childBuffer = EntityManager.getInstance().getEntitiesChildren(this);
                //For each child
                for(Entity child : childBuffer){
                    //get their AABB
                    Vector3f[] childAABB = child.getAABB();

                    //Recalculate TMP based of each child's bounds.
                    tmp.recalculateFromPoint(childAABB[0]);
                    tmp.recalculateFromPoint(childAABB[1]);
                }

                //Return our AABB
                return new Vector3f[]{tmp.getMIN(), tmp.getMAX()};
            }else{
                //No children, just return default shape.
                AABB defaultShape = new AABB(new Vector3f(-1).mul(this.getScale()), new Vector3f(1).mul(this.getScale()));
                return new Vector3f[]{defaultShape.getMIN().add(this.getPosition()), defaultShape.getMAX().add(this.getPosition())};
            }
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
        if(this.getMaterials().getData().size() > 0){
            return this.getMaterials().getData().getFirst().getAlbedoID();
        }

        if(this.hasAttribute("textureID")){
            return (int) this.getAttribute("textureID").getData();
        }

        return SpriteBinder.getInstance().getFileNotFoundID();
    }

    public Attribute<LinkedList<Material>> getMaterials(){
        if(this.hasAttribute("materials")){
            return (Attribute<LinkedList<Material>>) this.getAttribute("materials");
        }
        return null;
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

        //Now set our animationComponent
        removeComponent(animationComponent);
        if(this.model.hasAnimations()) {
            animationComponent = new AnimationComponent();
            animationComponent.setModel(this.model);
            this.addComponent(animationComponent);
        }
        return this;
    }

    public final void setModel(Collection<Model> models) {

        if(models.size() == 1){
            Model ourModel = (Model) models.toArray()[0];
            setModel(ourModel);
            return;
        }

        //Now make children, later make smart children that combine to form a single model.
        for(Model m : models){
            Entity e = new Entity();

            e.setModel(m);
            e.setMaterial(e.getMaterial());
            e.setParent(this);
        }
    }

    //Parent
    public final Entity getParent(){
        return this.parent;
    }

    /*
        This checks to see if this entity is decended from the decendant check.
     */
    public final boolean isDecendedFrom(Entity decendant){
        //Null Check
        if(decendant == null){
            return false;
        }

        //If we dont have a parent we are decended from nothing.
        if(this.parent == null){
            return false;
        }else{
            if(this.parent.equals(decendant)){
                return true;
            }
        }

        Entity grandparent = this.parent.getParent();

        while(grandparent != null){
            if(grandparent.equals(decendant)){
                return true;
            }

            grandparent = grandparent.getParent();
        }

        return false;
    }

    public final void setParent(Entity parent, String bone){
        this.setParent(parent);
        this.attachToParentsBone(bone);
    }

    public final void setParent(Entity parent){
        //Check to make sure that we are not already parented to a child
        if(this.parent != null){

            //If we are setting the parent to null, do this.
            if(parent == null){
                EntityManager.getInstance().unlink(this.parent, this);
                this.parent = null;
                this.boneParent = null;
                return;
            }

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

        this.boneParent = null;
    }

    public void attachToParentsBone(String bone){
        this.boneParent = bone;
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
    public final JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();

        if(meta == null){
            meta = new JsonObject();
            meta.add("sprites",   new JsonObject());
            meta.add("materials", new JsonObject());
            meta.add("models",    new JsonArray());

            out.add("meta", meta);
        }

//        meta.get("models").getAsJsonArray().add(this.getModel().get);

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

        //Materials
        //Loop through our materials and add our materials to our output object
        LinkedList<Material> materials = (LinkedList<Material>)this.getAttribute("materials").getData();
        JsonArray materialsJson = new JsonArray(materials.size());
        if(this.attributes.containsKey("materials")){
            for(Material mat : materials){
                JsonObject serializedMaterial = mat.serialize(meta);
                meta.get("materials").getAsJsonObject().add(mat.getName(),serializedMaterial);
                materialsJson.add(mat.getName());
            }
        }
        out.add("materials", materialsJson);

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
        if(!this.hasAttribute("serializeChildren") || (boolean)this.getAttribute("serializeChildren").getData()) {
            LinkedList<Entity> childrenEntities = EntityManager.getInstance().getEntitiesChildren(this);
            JsonArray children = new JsonArray(childrenEntities.size());
            for (Entity child : childrenEntities) {
                children.add(child.serialize(meta));
            }
            out.add("children", children);
        }

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
//        if(data.has("materials")){
//            JsonArray materials = data.get("materials").getAsJsonArray();
//            for(int i = 0; i < materials.size(); i++){
//                String materialName = materials.get(i).getAsString();
//                if(MaterialManager.getInstance().hasMaterial(materialName)){
//                    this.setMaterial(MaterialManager.getInstance().getMaterial(materialName));
//                }
//            }
//        }
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
                    System.out.println("Creating component:" + classType);
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

    public void addMaterial(Material material){
        this.getMaterials().getData().addLast(material);
    }

    public Material setMaterial(Material material){
        LinkedList<Material> materials = this.getMaterials().getData();
        materials.clear();
        materials.add(material);
        ((Attribute<LinkedList<Material>>)this.getAttribute("materials")).setData(materials);
        return getMaterial();
    }

//    public Material setMaterial(Sprite sprite){
//        materials.getData().clear();
//        materials.getData().add(MaterialManager.getInstance().getMaterial(sprite.getTextureID()));
//        return material;
//    }
//
//    public Material setMaterial(int textureID){
//        materials.getData().clear();
//        Material material = new Material(textureID);
//        materials.getData().add(material);
//        return material;
//    }

    public Material getMaterial(){
        LinkedList<Material> materials = ((LinkedList<Material>)this.getAttribute("materials").getData());
        if(materials.size() > 0){
            return materials.getFirst();
        }else{
            return MaterialManager.getInstance().getDefaultMaterial();
        }
    }

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

    public boolean hasModel() {
        return this.model != null;
    }

    public AnimationComponent getAnimationComponent() {
        return animationComponent;
    }

    public boolean isAnimated(){
        if(this.animationComponent != null){
            return this.animationComponent.getAnimationsByName().size() > 0;
        }
        return false;
    }
}
