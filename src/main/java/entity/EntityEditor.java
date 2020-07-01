package entity;

import camera.CameraManager;
import editor.Editor;
import editor.components.UIComponet;
import entity.Entity;
import entity.component.Attribute;
import entity.component.Component;
import entity.component.Event;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import graphics.sprite.SpriteSheet;
import imgui.ImGui;
import imgui.ImString;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import input.MousePicker;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import util.Callback;

import java.util.LinkedList;

public class EntityEditor extends UIComponet {
    private Callback mouseCallback;
    boolean pressed = false;

    //The entity we are interacting with
    private Entity entity;

    //Editor resources
    Sprite entityNotFound;

    public EntityEditor(){
        //Load resources and set variables
        entityNotFound = SpriteBinder.getInstance().load("fileNotFound.png");

//        entity = new Entity("mushroom.tek");
//        EntityManager.getInstance().addEntity(entity);

        //Genreate mouse callback
        mouseCallback = new Callback() {
            @Override
            public Object callback(Object... objects) {
            int button = (int) objects[0];
            int action = (int) objects[1];

            pressed = (action == GLFW.GLFW_PRESS);
            if(pressed) {
                raycastToWorld();
            }
            return null;
            }
        };
    }

    public void setTarget(Entity target){
        this.entity = target;
    }

    private void raycastToWorld() {
        if(pressed){
            //Raycast to plane
            Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
            LinkedList<Entity> hits = EntityManager.getInstance().getHitEntities(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), new Vector3f(MousePicker.getInstance().getRay()));
            if(hits.size() > 0){
                this.entity = hits.getFirst();
            }else {
                Entity entity = new Entity(this.entity.serialize());
                entity.setPosition(pos);
                EntityManager.getInstance().addEntity(entity);
            }
        }

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
        raycastToWorld();

    }

    public void getTexture(){

    }

    @Override
    public void self_render() {
        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight() * 0.25f);
        for(Entity e : new LinkedList<Entity>(EntityManager.getInstance().getEntities())){
            int selected = ImGuiSelectableFlags.AllowDoubleClick;
            ImGui.image(e.getTextureID(), 16, 16);
            ImGui.sameLine();
            if(ImGui.selectable(e.toString(), this.entity.equals(e), selected)){
                this.entity = e;
            }
        }
        ImGui.endChild();
        //If we have an entity
        if(this.entity != null) {
            //Draw a line
            if (entity.getTextureID() >= 0) {
                if(ImGui.imageButton(entity.getTextureID(), ImGui.getWindowWidth(), ImGui.getWindowWidth())){
//                    this.entity.setTexture(4);
                }
            }else{
                if(ImGui.imageButton(entityNotFound.getTextureID(), ImGui.getWindowWidth(), ImGui.getWindowWidth())){
//                    this.entity.setTexture(4);
                }
            }
            //List all attributes that go into this entity
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
            if(ImGui.collapsingHeader("Attributes", nodeFlags_attributes)) {
//                ImGui.beginChild(this.entity.toString() + "_attributes");
                for (Attribute attribute : entity.getAttributes()) {
//                int nodeFlags_attribute = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
                    ImGui.inputText(attribute.getName(), new ImString(attribute.getData() + ""));
                }
//                ImGui.endChild();
            }

            //List all components, then all triggers, then all events
            int nodeFlags_components = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
            if(ImGui.collapsingHeader("Components", nodeFlags_components)){
                for (Component component : this.entity.getComponents()) {
                    int nodeFlags_component = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
                    if(ImGui.treeNodeEx(component.getName(), nodeFlags_component)){
                        for(String trigger : component.getTriggers()){
                            int nodeFlags_trigger = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
                            if(ImGui.treeNodeEx(trigger, nodeFlags_trigger)){
                                for(Event event : component.getTriggeredEvents(trigger)){
                                    ImGui.text(event.toString());
                                }
                                ImGui.treePop();
                            }
                        }
                        ImGui.treePop();
                    }
                }
            }



//        if (ImGui.beginCombo("", this.sheet.getName())){
//            for (SpriteSheet sheet : spriteSheets){
//                boolean is_selected = (this.sheet.getName().equals(sheet.getName()));
//                if (ImGui.selectable(sheet.getName(), is_selected)){
//                    this.sheet = sheet;
//                }
//                if (is_selected){
//                    ImGui.setItemDefaultFocus();
//                }
//            }
//            ImGui.endCombo();
//        }
        }
    }

    @Override
    public void self_post_render() {

    }
}
