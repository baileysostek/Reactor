package editor.components;

import java.awt.*;
import java.util.LinkedList;

public abstract class UIComponet {

    //Parent Component
    private UIComponet parent;
    //Children
    private LinkedList<UIComponet> children = new LinkedList<>();

    //Visible boolean
    private boolean visable = true;

    //Local variables
    //Maybe have an component name
    //maybe have a component type enum

    //Parent interface to update this method, this recursive updates child elements.
    public void update(double delta){
        self_update(delta);
        for(UIComponet component : children){
            component.update(delta);
        }
    }

    public void preUIRender(){
        return;
    }

    public void render(){
        if(visable) {
            self_render();
            self_render_children();
            self_post_render();
        }
    }

    protected void self_render_children(){
        for (UIComponet component : children) {
            component.render();
        }
    }

    //Abstract overrides that child classes implement.
    public abstract void onAdd();
    public abstract void onRemove();
    public abstract void self_update(double delta);
    public abstract void self_render();
    public abstract void self_post_render();

    public void onShutdown(){
        return;
    };
    public void onEnterPlay(){
        return;
    };
    public void onEnterDevelopment(){
        return;
    };

    public UIComponet getParent(){
        return this.parent;
    }

    /**
     * TODO
     * @param child
     * @param index
     */
    public void addChild(UIComponet child, Object ... index){
        //Child is null dont cause NPE
        if(child == null){
            return;
        }

        //First set the child's parent to this.
        child.parent = this;
        //Check how we need to parent the child to this object.
        if(index.length > 0){
            //Check if we passed an append position
            if(index[0] instanceof AppendPosition){
                AppendPosition pos = (AppendPosition)index[0];
                switch (pos){
                    //Easy case add to end
                    case END:{
                        this.children.addLast(child);
                        child.onAdd();
                        return;
                    }
                    //Easy case add to start
                    case START:{
                        this.children.addFirst(child);
                        child.onAdd();
                        return;
                    }
                    //Now we need to see if there is an addition param to index into our children by,
                    case INDEX:{
                        if(index.length > 1){
                            if(index[1] instanceof Number){
                               int addIndex = (int)index[1];
                               this.children.add(addIndex, child);
                               child.onAdd();
                               return;
                            }
                        }
                    }
                }
            }
        }

        //If we haven't returned yet just add to end
        this.children.addLast(child);
        child.onAdd();
    }

    public void setVisable(boolean visable){
        this.visable = visable;
    }

    public boolean isVisable(){
        return visable;
    }

    public abstract String getName();
}
