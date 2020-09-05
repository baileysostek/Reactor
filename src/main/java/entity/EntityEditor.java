package entity;

import camera.CameraManager;
import editor.Editor;
import editor.components.UIComponet;
import editor.components.container.Axis;
import entity.Entity;
import entity.component.Attribute;
import entity.component.Component;
import entity.component.Event;
import graphics.renderer.Renderer;
import graphics.sprite.Colors;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import graphics.sprite.SpriteSheet;
import imgui.*;
import imgui.enums.ImGuiCol;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiStyleVar;
import imgui.enums.ImGuiTreeNodeFlags;
import input.Keyboard;
import input.MousePicker;
import models.ModelManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import util.Callback;

import java.util.Collection;
import java.util.LinkedList;

import static entity.EditorTool.*;

public class EntityEditor extends UIComponet {
    private Callback mouseCallback;
    private Callback dropFileInWorld;
    boolean pressed = false;

    //The entity we are interacting with
    private Entity entity;
    private Sprite outline = null;
    private Entity outlineEntity;

    //Tools
    private EditorTool selectedTool = NONE;
    private Entity axisArrowX;
    private Entity axisArrowY;
    private Entity axisArrowR;

    private LinkedList<Entity> menuEntities = new LinkedList<>();

    //Editor resources
    Sprite entityNotFound;

    public EntityEditor(){
        //Load resources and set variables
        entityNotFound = SpriteBinder.getInstance().load("fileNotFound.png");

        //Gen start of outline
        outlineEntity = new Entity();
        outlineEntity.setModel(ModelManager.getInstance().loadModel("quad.tek"));
        outlineEntity.addAttribute(new Attribute<Integer>("zIndex", Integer.MAX_VALUE));

        //Arrows
        axisArrowX = new Entity();
        axisArrowX.setModel(ModelManager.getInstance().loadModel("quad.tek"));
        axisArrowX.setTexture(new Sprite(SpriteBinder.getInstance().load("arrow.png").outline(new Vector4f(1, 0, 0, 1))));
        axisArrowX.addAttribute(new Attribute<Integer>("zIndex", Integer.MAX_VALUE));
        axisArrowX.setRotation(new Vector3f(0, (float) -Math.toRadians(90f), 0));

        axisArrowY = new Entity();
        axisArrowY.setModel(ModelManager.getInstance().loadModel("quad.tek"));
        axisArrowY.setTexture(new Sprite(SpriteBinder.getInstance().load("arrow.png").outline(new Vector4f(0, 1, 0, 1))));
        axisArrowY.addAttribute(new Attribute<Integer>("zIndex", Integer.MAX_VALUE));

        axisArrowR = new Entity();
        axisArrowR.setModel(ModelManager.getInstance().loadModel("quad.tek"));
        axisArrowR.setTexture(new Sprite(SpriteBinder.getInstance().load("rotate.png").outline(new Vector4f(0, 0, 1, 1))));
        axisArrowR.addAttribute(new Attribute<Integer>("zIndex", Integer.MAX_VALUE));

        //Add all menu entities
        menuEntities.addLast(axisArrowX);
        menuEntities.addLast(axisArrowY);
        menuEntities.addLast(axisArrowR);

        //Genreate mouse callback
        mouseCallback = new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(EntityEditor.super.isVisable()) {
                    int button = (int) objects[0];
                    int action = (int) objects[1];

                    if(button == MousePicker.MOUSE_LEFT) {
                        //On Release Set selected to none
                        if (action == GLFW.GLFW_RELEASE) {
                            selectedTool = NONE;
                        }

                        pressed = (action == GLFW.GLFW_PRESS);
                        if (pressed) {
                            raycastToWorld();
                        }
                    }
                }
                return null;
            }
        };

        //The callbacks
        Keyboard.getInstance().addPressCallback(Keyboard.DELETE, new Callback() {
            @Override
            public Object callback(Object... objects) {
            System.out.println("Callback");
            if(entity != null){
                EntityManager.getInstance().removeEntity(entity);
                clearSelected();
            }
            return null;
        }
    });
    }

    public void setTarget(Entity target){
        this.entity = target;
    }

    private void raycastToWorld() {
        if(pressed){
            //Raycast to plane
            Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
            if(pos != null) {
                if (!this.selectedTool.equals(NONE)) {
                    switch (selectedTool) {
                        case MOVE_XYZ:
                            break;
                        case MOVE_X: {
                            Vector3f deltaX = new Vector3f(pos);
                            this.axisArrowX.setPosition(new Vector3f(deltaX.x(), axisArrowX.getPosition().y(), axisArrowX.getPosition().z()));
                            this.entity.setPosition(new Vector3f(axisArrowX.getPosition()).add(new Vector3f(this.entity.getScale()).mul(-1, 0, 0)));
                            this.outlineEntity.setPosition(this.entity.getPosition());

                            //Remove unused tools

                            break;
                        }
                        case MOVE_Y: {
                            Vector3f deltaY = new Vector3f(pos);
                            this.axisArrowY.setPosition(new Vector3f(axisArrowY.getPosition().x(), axisArrowY.getPosition().y(), deltaY.z()));
                            this.entity.setPosition(new Vector3f(axisArrowY.getPosition()).add(new Vector3f(this.entity.getScale()).mul(0, 0, 1)));
                            this.outlineEntity.setPosition(this.entity.getPosition());
                            break;
                        }
                        case MOVE_Z:
                            break;
                        case ROTATE_X:
                            break;
                        case ROTATE_Y:
                            break;
                        case ROTATE_Z:
                            break;
                        case SCALE_XYZ:
                            break;
                    }
                    //Set pos of other tools
                    //Set the pos of the editor arrows
                    axisArrowX.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(1, 0, 0).mul(this.entity.getScale())));
                    axisArrowY.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(0, 0, -1).mul(this.entity.getScale())));
                    axisArrowR.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(1, 0, -1).mul(this.entity.getScale())));
                } else {
                    //No tool selected, lets see if we hit a tool or hit an entity
                    LinkedList<Entity> menuHits = EntityManager.getInstance().getHitEntities(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), menuEntities);
                    if (!menuHits.isEmpty()) {
                        //We have an entity selected
                        if (this.entity != null) {
                            //Check for hit
                            Entity hit = menuHits.getFirst();
                            //Get the first hit
                            if (hit.equals(axisArrowX)) {
                                this.selectedTool = MOVE_X;
                            }
                            if (hit.equals(axisArrowY)) {
                                this.selectedTool = MOVE_Y;
                            }
                        }
                    } else {
                        //We hit nothing so lets do the next action.
                        LinkedList<Entity> hits = EntityManager.getInstance().getHitEntities(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()));
                        if (hits.size() > 0) {
                            if (this.entity != hits.getFirst() && !hits.getFirst().equals(outlineEntity)) {
                                for (Entity e : hits) {
                                    System.out.println(e.serialize());
                                }
                                this.entity = hits.getFirst();
                                outline = new Sprite(SpriteBinder.getInstance().getSprite(this.entity.getTextureID())).outline(Colors.RED);
                                outlineEntity.setTexture(outline);
                                outlineEntity.setPosition(new Vector3f(this.entity.getPosition()));
                                EntityManager.getInstance().addEntity(outlineEntity);

                                //Set the pos of the editor arrows
                                axisArrowX.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(1, 0, 0).mul(this.entity.getScale())));
                                axisArrowY.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(0, 0, -1).mul(this.entity.getScale())));
                                axisArrowR.setPosition(new Vector3f(this.entity.getPosition()).add(new Vector3f(1, 0, -1).mul(this.entity.getScale())));
                                EntityManager.getInstance().addEntity(axisArrowX);
                                EntityManager.getInstance().addEntity(axisArrowY);
                                EntityManager.getInstance().addEntity(axisArrowR);
                            }

                        } else {
                            clearSelected();
                        }
                    }
                }
            }
        }

    }

    public void clearSelected(){
        this.entity = null;
        EntityManager.getInstance().removeEntity(outlineEntity);
        EntityManager.getInstance().removeEntity(axisArrowX);
        EntityManager.getInstance().removeEntity(axisArrowY);
        EntityManager.getInstance().removeEntity(axisArrowR);
    }

    @Override
    public void onAdd() {
        MousePicker.getInstance().addCallback(mouseCallback);
        MousePicker.getInstance().addCallback(dropFileInWorld);
    }

    @Override
    public void onRemove() {
        MousePicker.getInstance().removeCallback(mouseCallback);
        MousePicker.getInstance().removeCallback(dropFileInWorld);
    }

    @Override
    public void self_update(double delta) {
        raycastToWorld();

    }

    public void getTexture(){

    }

    @Override
    public void preUIRender(){
        if(this.entity != null) {
            //Direct Draw?
            Renderer.getInstance().drawAABB(entity);
        }
    }

    @Override
    public void self_render() {
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        //If we have an entity
        if(this.entity != null) {
            //Draw a line
            if (entity.getTextureID() >= 0) {
                if(ImGui.imageButton(entity.getTextureID(), ImGui.getWindowWidth(), ImGui.getWindowWidth())){
//                    this.entity.setTexture(4);
                    //TODO open setTexture dialogue
                }
            }else{
                if(ImGui.imageButton(entityNotFound.getTextureID(), ImGui.getWindowWidth(), ImGui.getWindowWidth())){
//                    this.entity.setTexture(4);
                    //TODO open setTexture dialogue
                }
            }
            //List all attributes that go into this entity
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;

            //Here we loop through all attributes that an entity has. Each attribute should have an ENUM describing what type of Attribute this is, default, constant, locked, hidden
            if(ImGui.collapsingHeader("Attributes", nodeFlags_attributes)) {
                AttributeRenderer.renderAttributes(entity.getAttributes());
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
        }
        ImGui.endChild();
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Properties";
    }
}

enum EditorTool{
    //None
    NONE,
    //Move
    MOVE_XYZ,
    MOVE_X,
    MOVE_Y,
    MOVE_Z,
    //Rotation
    ROTATE_X,
    ROTATE_Y,
    ROTATE_Z,
    //Scale
    SCALE_XYZ

}


class AttributeRenderer{
    protected static void renderAttributes(Collection<Attribute> attributes){
        //Loop through each attribute in the list.
        for (Attribute attribute : attributes) {
            //Try find a type
            loop:{
                if (attribute.getData() instanceof Vector3f) {
                    Vector3f data = (Vector3f) attribute.getData();
                    ImFloat x = new ImFloat(data.x);
                    ImFloat y = new ImFloat(data.y);
                    ImFloat z = new ImFloat(data.z);
                    ImGui.columns(3);
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                    ImGui.inputFloat("X", x, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                    ImGui.inputFloat("Y", y, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                    ImGui.inputFloat("Z", z, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.columns();

                    attribute.setData(new Vector3f(x.get(), y.get(), z.get()));

                    break loop;
                }

                if (attribute.getData() instanceof Vector2f) {
                    Vector2f data = (Vector2f) attribute.getData();
                    ImFloat x = new ImFloat(data.x);
                    ImFloat y = new ImFloat(data.y);

                    ImGui.columns(2);
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                    ImGui.inputFloat("X", x, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.nextColumn();
                    ImGui.pushID(Editor.getInstance().getNextID());
                    ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                    ImGui.inputFloat("Y", y, 1, 10);
                    ImGui.popStyleColor();
                    ImGui.popID();
                    ImGui.columns();

                    attribute.setData(new Vector2f(x.get(), y.get()));

                    break loop;
                }

                if (attribute.getData() instanceof Boolean) {
                    boolean data = (boolean) attribute.getData();
                    ImBool value = new ImBool(data);

                    ImGui.checkbox(attribute.getName(), value);

                    attribute.setData(value.get());

                    break loop;
                }

                if (attribute.getData() instanceof Integer) {
                    int data = (int) attribute.getData();
                    ImInt value = new ImInt(data);

                    ImGui.inputInt(attribute.getName(), value);

                    attribute.setData(value.get());

                    break loop;
                }

                //End If no type was found, render default string.
                ImGui.inputText(attribute.getName(), new ImString(attribute.getData() + ""));
            }
        }
    }
}