package entity.component;

import com.google.gson.JsonObject;
import serialization.Serializable;
import serialization.SerializationHelper;
import util.Callback;

import java.util.LinkedList;

public class Attribute<T> implements Serializable<Attribute<T>> {

    private String name;
    private T attribute;
    protected LinkedList<Callback> subscribers = new LinkedList<Callback>(){};

    //Rendering helpers
    private String category = "";
    private boolean locked  = false;
    private boolean visible = true;
    private EnumAttributeType type = EnumAttributeType.NONE;
    private boolean shouldBeSerialized = true;
//    private Callback onAdd = new Callback() {
//        @Override
//        public Object callback(Object... objects) {
//            System.out.println("On Add.");
//            return null;
//        }
//    };

    public Attribute(Attribute att){
        this.name = att.getName();
        this.attribute = (T) att.attribute;
    }

    public Attribute(String name, T data){
        this.name = name;
        this.attribute = data;
    }

    public Attribute(JsonObject attributes) {
        deserialize(attributes);
    }

    public Attribute setLocked(boolean locked){
        this.locked = locked;
        return this;
    }

    public Attribute setShouldBeSerialized(boolean shouldBeSerialized){
        this.shouldBeSerialized = shouldBeSerialized;
        return this;
    }

    public Attribute setVisible(boolean visible){
        this.visible = visible;
        return this;
    }

    public boolean setData(T newData){
        if(AttributeUtils.differ(newData, attribute)) {
            T oldData = this.attribute;
            this.attribute = newData;

            if(subscribers == null){
                subscribers = new LinkedList<Callback>(){};
            }
            for (Callback callback : subscribers) {
                callback.callback(this, oldData, newData);
            }
            return true;
        }
        return false;
    }

    public void setDataNoUpdate(T newData){
        this.attribute = newData;
    }

    public T getData(){
        return attribute;
    }

    public void subscribe(Callback c){
        if(!this.subscribers.contains(c)) {
            this.subscribers.addLast(c);
        }
    }

    public void subscribe(Attribute<T> attribute){
        for(Callback c : attribute.subscribers){
            subscribe(c);
        }
    }

    public void unsubscribe(Callback c){
        if(this.subscribers.contains(c)) {
            this.subscribers.remove(c);
        }
    }

    public void unsubscribe(Attribute<T> attribute){
        for(Callback c : attribute.subscribers){
            unsubscribe(c);
        }
    }


    public String getName(){
        return this.name;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public EnumAttributeType getType() {
        return type;
    }

    public Attribute setType(EnumAttributeType type) {
        this.type = type;
        return this;
    }

    public boolean shouldBeSerialized() {
        return this.shouldBeSerialized;
    }

    @Override
    public JsonObject serialize(JsonObject meta) {
        JsonObject out = new JsonObject();
        out.addProperty("name", name);
        out.addProperty("locked", locked);
        out.addProperty("visible", visible);
        out.addProperty("type", type.toString());
        out.addProperty("category", category);
        if(attribute != null){
            out.add("data", SerializationHelper.addClass(attribute));
        }else{
            out.add("data", null);
            System.err.println("Error: Attribute had no associated data:" + name);
        }
        return out;
    }

    @Override
    public Attribute<T> deserialize(JsonObject data) {
        if(data.has("name")){
            name = data.get("name").getAsString();
        }
        if(data.has("locked")){
            locked = data.get("locked").getAsBoolean();
        }
        if(data.has("visible")){
            visible = data.get("visible").getAsBoolean();
        }
        if(data.has("type")){
            type = EnumAttributeType.valueOf(data.get("type").getAsString());
        }
        if(data.has("category")){
            category = data.get("category").getAsString();
        }

        //Generics are cool!
        if(data.has("data")){
            attribute = (T) SerializationHelper.toClass(data.get("data").getAsJsonObject());
        }

        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory(){
        return this.category;
    }

//    //These methods are used for the Editor only
//    public void setOnAdd(Callback c){
//        this.onAdd = c;
//    }
//
//    public Callback getOnAdd() {
//        return this.onAdd;
//    }
}
