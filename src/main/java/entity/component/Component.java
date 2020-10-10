package entity.component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import entity.Entity;
import serialization.Serializable;
import util.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class Component implements Serializable<Component>{
    private int componentID;
    protected Entity parent;
    private HashMap<String, Attribute> attributes = new HashMap<>();

    private static int GLOBAL_ID = 0;
    private final  int ID;

    //When this component triggers, execute the events attached
//    private LinkedList<Event> events = new LinkedList<>();
    private HashMap<String, LinkedList<Event>> events = new HashMap<String, LinkedList<Event>>();

    public Component(){
        ID = GLOBAL_ID;
        GLOBAL_ID++;
    }

    //This is called when this component gets added to an entity
    public void onAdd(Entity e){
        parent = e;
        syncAttributes(initialize());
        setEventTargets(parent);
    }

    private void setEventTargets(Entity target) {
        for(LinkedList<Event> eventTriggers : events.values()){
            for(Event event : eventTriggers){
                event.setParent(target);
            }
        }
    }

    //Events
    public final void invoke(String name, Object ... objects){
        if(events.containsKey(name)) {
            LinkedList<Event> eventList = events.get(name);
            for (Event e : eventList) {
                e.invoke(objects);
            }
        }
    }

    public final void addEvent(String eventName, Event event){
        if(event != null) {
            //Set event entity parent
            event.setParent(this.getParent());
            //Add event
            if(!events.containsKey(eventName)){
                LinkedList<Event> eventList = new LinkedList<Event>();
                eventList.addLast(event);
                events.put(eventName, eventList);
            }else{
                events.get(eventName).addLast(event);
            }
        }
    }

    public final LinkedList<Event> getTriggeredEvents(String trigger){
        if(this.events.containsKey(trigger)){
            return this.events.get(trigger);
        }else{
            return new LinkedList<Event>();
        }
    }

    public Entity getParent(){
        return this.parent;
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
            this.attributes.get(observed.getName()).setData(observed.getData());
            if(!components.contains(this)){
                components.add(this);
            }
            this.parent.syncAttributes(observed, components);
        }else{
            System.out.println("Tried to set attribute:"+observed.getName()+" however this component does not have this attribute.");
        }
    }

    public Attribute getAttribute(String attributeName){
        if(this.attributes.containsKey(attributeName)){
            return this.attributes.get(attributeName);
        }else{
            return null;
        }
    }

    public final Collection<String> getTriggers(){
        return this.events.keySet();
    }

    //abstract methods
    protected abstract LinkedList<Attribute> initialize();
//    public abstract void postInitialize();
    public abstract void update(double delta);
    public abstract String getName();
    public abstract void onAttributeUpdate(Attribute observed);


    @Override
    public JsonObject serialize() {
        JsonObject out = new JsonObject();
        for(String s : this.getTriggers()){
            LinkedList<Event> triggeredEvents = this.events.get(s);
            JsonArray events = new JsonArray(triggeredEvents.size());
            for(Event e : triggeredEvents){
                //Gen helper object for this event
                JsonObject eventClass = new JsonObject();

                //encode name of this event class
                eventClass.addProperty("class", e.getClass().getName());
                //encode this classes serialized data.
                eventClass.add("value", e.serialize());

                //add to array of events
                events.add(eventClass);
            }
            out.add(s, events);
        }
        return out;
    }

    @Override
    public Component deserialize(JsonObject data) {
        for(String trigger : data.keySet()){
            LinkedList<Event> triggeredEvents = new LinkedList<Event>();
            JsonArray events = data.get(trigger).getAsJsonArray();
            for(JsonElement eventData : events){
                //Helper has a class and a value
                JsonObject helper = eventData.getAsJsonObject();
                try {
                    //Try resolve the class that was encoded
                    Class<?> classType = Class.forName(helper.get("class").getAsString());
                    //Add our event
                    triggeredEvents.addLast(((Event)classType.newInstance()).deserialize(helper.getAsJsonObject("value")));

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
            this.events.put(trigger, triggeredEvents);
        }
        return this;
    }

    public int getID(){
        return this.ID;
    }

}
