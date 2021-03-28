package editor.components.container;

import editor.components.UIComponet;
import imgui.ImGui;

public class Image extends UIComponet {

    int textureID = 0;

    public Image(int textureID){
        this.textureID = textureID;
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
    public void selfRender() {
        ImGui.image(this.textureID, ImGui.getWindowWidth(), ImGui.getWindowWidth());
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Image";
    }
}
