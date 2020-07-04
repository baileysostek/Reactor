package entity;

import camera.CameraManager;
import editor.Editor;
import editor.components.UIComponet;
import entity.component.Attribute;
import entity.component.Component;
import entity.component.Event;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import imgui.ImGui;
import imgui.ImString;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import imgui.enums.ImGuiWindowFlags;
import input.MousePicker;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import util.Callback;

import java.util.LinkedList;

public class WorldOutliner extends UIComponet {
    private Callback mouseCallback;
    boolean pressed = false;

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
        MousePicker.getInstance().addCallback(mouseCallback);
    }

    @Override
    public void onRemove() {
        MousePicker.getInstance().removeCallback(mouseCallback);
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
                CameraManager.getInstance().getActiveCamera().setPosition(new Vector3f(e.getPosition()).mul(1, 0, 1).add(new Vector3f( CameraManager.getInstance().getActiveCamera().getPosition()).mul(0, 1, 0)));
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
