package entity.component;

import util.Callback;

import java.util.LinkedList;

public class Attribute<T> {

    private String name;
    private T attribute;
    private LinkedList<Callback> subscribers = new LinkedList<Callback>();

    public Attribute(String name, T data){
        this.name = name;
        this.attribute = data;
    }

    public void setData(T newData){
        if(attribute != newData) {
            this.attribute = newData;
            for (Callback callback : subscribers) {
                callback.callback(this);
            }
        }
    }

    protected void setDataUnsafe(T newData){
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

    public void unsubscribe(Callback c){
        if(this.subscribers.contains(c)) {
            this.subscribers.remove(c);
        }
    }

    public String getName(){
        return this.name;
    }
}
