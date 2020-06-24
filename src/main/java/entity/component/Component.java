package entity.component;

import com.google.gson.JsonObject;
import entity.Entity;
import serialization.Serializable;
import util.Callback;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class Component implements Serializable<Component>{
    private int componentID;
    protected Entity parent;
    private HashMap<String, Attribute> attributes = new HashMap<>();

    public Component(){}

    //This is called when this component gets added to an entity
    public void onAdd(Entity e){
        parent = e;
        syncAttributes(initialize());
    }

    private void syncAttributes(LinkedList<Attribute> attributes){
        //Loop through each attribute
        for(Attribute attribute : attributes){
            addAttribute(attribute);
        }
    }

    public void addAttribute(Attribute attribute){
        //Check if parent has attribute
        if(!this.parent.hasAttribute(attribute.getName())){
            this.parent.addAttribute(new Attribute(attribute.getName(), attribute.getData()));
        }else{
            //If the parent dose have this attribute with a value of null, set the value to this
            if(this.parent.getAttribute(attribute.getName()).getData() == null){
                this.parent.getAttribute(attribute.getName()).setData(attribute.getData());
            }
        }
        //Add to our list
        attribute.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                LinkedList<Component> closedList = new LinkedList<Component>();
                closedList.add(Component.this);
                parent.getAttribute(attribute.getName()).setDataUnsafe(attribute.getData());
                //Sync with all other attributes
                parent.syncAttributes(attribute, closedList);
                return null;
            }
        });
        this.attributes.put(attribute.getName(), attribute);
    }

    public boolean hasAttribute(String name){
        return this.attributes.containsKey(name);
    }

    public void setAttribute(Attribute observed, LinkedList<Component> components) {
        if(hasAttribute(observed.getName())){
            //Set data unsafe because we dont want this to recurse
            this.attributes.get(observed.getName()).setDataUnsafe(observed.getData());
            if(!components.contains(this)){
                components.add(this);
            }
            this.parent.syncAttributes(observed, components);
        }else{
            System.out.println("Tried to set attribute:"+observed.getName()+" however this component does not have this attribute.");
        }
    }

    //abstract methods
    protected abstract LinkedList<Attribute> initialize();
    public abstract void update(double delta);


    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();

        return out;
    }

    @Override
    public Component deserialize(JsonObject data) {

        return this;
    }

    public Attribute getAttribute(String attributeName){
        if(this.attributes.containsKey(attributeName)){
            return this.attributes.get(attributeName);
        }else{
            return null;
        }
    }
}
