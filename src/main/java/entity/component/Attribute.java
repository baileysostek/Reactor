package entity.component;

import util.Callback;

public class Attribute<T> {

    private String name;
    private T attribute;
    private Callback onUpdate = new Callback() {
        @Override
        public Object callback(Object... objects) {
            return null;
        }
    };

    public Attribute(String name, T data){
        this.name = name;
        this.attribute = data;
    }

    public void setData(T newData){
        this.attribute = newData;
        this.onUpdate.callback(this);
    }

    protected void setDataUnsafe(T newData){
        this.attribute = newData;
    }

    public T getData(){
        return attribute;
    }

    public void subscribe(Callback c){
        this.onUpdate = c;
    }

    public String getName(){
        return this.name;
    }
}
