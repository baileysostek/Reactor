package editor.components.container;

import editor.components.UIComponet;

import java.util.LinkedList;

public class List <T> extends UIComponet {

    private LinkedList<T> objects = new LinkedList<>();

    private void add(T object){
        objects.addLast(object);
    }

    @Override
    public void onAdd() {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public void self_update(double delta) {

    }

    @Override
    public void self_render() {

    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "List";
    }
}
