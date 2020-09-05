package entity;

import camera.CameraManager;
import editor.Editor;
import editor.components.UIComponet;
import imgui.ImGui;
import imgui.enums.ImGuiSelectableFlags;
import org.joml.Vector3f;

import java.util.LinkedList;

public class WorldOutliner extends UIComponet {

    //The entity we are interacting with
    private Entity entity;

    //Editor resources
    EntityEditor editor;

    public WorldOutliner(EntityEditor editor){
        this.editor = editor;
    }

    public void setTarget(Entity target){
        this.entity = target;
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

    public void getTexture(){

    }

    @Override
    public void self_render() {
        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        for(Entity e : new LinkedList<Entity>(EntityManager.getInstance().getEntities())){
            int selected = ImGuiSelectableFlags.AllowDoubleClick;
            ImGui.image(e.getTextureID(), 16, 16);
            ImGui.sameLine();
            if(ImGui.selectable(e.toString(), e.equals(this.entity), selected)){
                this.entity = e;
                this.editor.setTarget(e);
//                CameraManager.getInstance().getActiveCamera().setPosition(new Vector3f(e.getPosition()).mul(1, 0, 1).add(new Vector3f( CameraManager.getInstance().getActiveCamera().getPosition()).mul(0, 1, 0)));
            }
        }
        ImGui.endChild();
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Scene";
    }
}
