package editor.components.container;

import editor.components.UIComponet;
import imgui.ImGui;

public class CollapsibleHeader extends UIComponet {

    private String label = "Header";

    public CollapsibleHeader setLabel(String label){
        this.label = label;
        return this;
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
    public void render() {
        if(ImGui.collapsingHeader(label)){
            self_render_children();
        }
    }


    @Override
    public void self_render() {

    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Header";
    }
}
