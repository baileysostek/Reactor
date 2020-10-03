package entity;

import camera.CameraManager;
import com.google.gson.JsonObject;
import editor.Editor;
import editor.components.UIComponet;
import entity.component.Attribute;
import entity.component.Component;
import entity.component.EnumAttributeType;
import entity.component.Event;
import graphics.renderer.DirectDrawData;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import imgui.*;
import imgui.enums.ImGuiCol;
import imgui.enums.ImGuiColorEditFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import imgui.enums.ImGuiWindowFlags;
import input.Keyboard;
import input.MousePicker;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import serialization.SerializationHelper;
import util.Callback;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import static entity.EditorAxis.*;

public class EntityEditor extends UIComponet {
    private Callback mouseCallback;
    private Callback dropFileInWorld;
    boolean pressed = false;

    //The entity we are interacting with
    private Entity entity;
    private HashMap<Entity, EntityMetaData> selectedEntities = new HashMap<>();
    private Vector3f delta = new Vector3f(0);
    private Vector3f initialCastHit = new Vector3f(0);
    private Vector3f rayToHit = new Vector3f(0);
    private float distanceToCam = 0;

    //Tools
    private EditorMode toolType = EditorMode.NONE;
    private EditorAxis selectedTool = NONE;

    //Debouncer boolean used to prevent entity modification right when clicking on an entity.
    private boolean hasReleased = true;

    //Editor resources
    Sprite entityNotFound;

    public EntityEditor(){
        //Load resources and set variables
        entityNotFound = SpriteBinder.getInstance().load("fileNotFound.png");

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
                            hasReleased = true;
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

        Keyboard.getInstance().addPressCallback(Keyboard.ONE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                toolType = EditorMode.TRANSLATE;
                return null;
            }
        });
        Keyboard.getInstance().addPressCallback(Keyboard.TWO, new Callback() {
            @Override
            public Object callback(Object... objects) {
                toolType = EditorMode.ROTATE;
                return null;
            }
        });
        Keyboard.getInstance().addPressCallback(Keyboard.THREE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                toolType = EditorMode.SCALE;
                return null;
            }
        });
    }

    public void setTarget(Entity target){
        this.clearSelected();
        this.entity = target;
        this.selectedEntities.put(this.entity, new EntityMetaData());
    }

    private void addTarget(Entity first) {
        this.selectedEntities.put(first, new EntityMetaData());
    }

    private void raycastToWorld() {
        if(pressed){
            //We need to do a check to see if we hit any of our direct draw volumes, like arrows.
            //If we have an entity in the world selected, we probably have an AABB as well.
            //NOTE it feels bad if you move an entity a little bit right when you click on it, add a debouncer looking for click falling action to fix this.
            if(this.entity != null && this.hasReleased){
                if(this.selectedTool.equals(NONE)) {
                    //Reset our delta
                    delta = new Vector3f(0);
                    initialCastHit = new Vector3f(0);
                    rayToHit = new Vector3f(0);
                    distanceToCam = 0;

                    if(Keyboard.getInstance().isKeyPressed(Keyboard.ALT_LEFT)){
                        Entity entity;
                        //Deserialize the entity
                        if(this.entity.getClass().equals(Entity.class)) {
                            //Regula old entity
                            entity = new Entity().deserialize(this.entity.serialize());
                        }else{
                            //Fancy entity from another class or namespace :)
                            try {
                                Class<?> classType = Class.forName(this.entity.getClass().getName());
                                //This has broken attributes, set from parent
                                JsonObject serialziedEntity = this.entity.serialize();
                                entity = ((Entity) SerializationHelper.getGson().fromJson(serialziedEntity, classType)).deserialize(serialziedEntity);

                            } catch (ClassNotFoundException e) {
                                //TODO play sound that that entity is unknown, maybe show message dialogue too.
                                e.printStackTrace();
                                return;
                            }
                        }

                        //Check if the moved entity was a child
                        if(this.entity.hasParent()){
                            entity.setParent(this.entity.getParent());
                        }

                        EntityManager.getInstance().addEntity(entity);
                    }

                    //Get meta-data for selected entity and see if the entity
                    EntityMetaData metaData = this.selectedEntities.get(this.entity);

                    //Store our hits in this vector.
                    Vector3f hits = new Vector3f(0);

                    if(metaData.ddd_x != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_x.getAABB());
                        if (pos != null) {
                            delta = new Vector3f(entity.getPositionSelf()).sub(pos);
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            hits.x = 1;
                        }
                    }
                    if(metaData.ddd_y != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_y.getAABB());
                        if (pos != null) {
                            delta = new Vector3f(entity.getPositionSelf()).sub(pos);
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            hits.y = 1;
                        }
                    }
                    if(metaData.ddd_z != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_z.getAABB());
                        if (pos != null) {
                            delta = new Vector3f(entity.getPosition()).sub(pos);
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            hits.z = 1;

                            //TODO this may be needed for other casts, its only used in XYZ pos now, so just do the check once in here. May need to be added to the X and Y checks later if other Tools use distanceToCam var.
                            distanceToCam = new Vector3f(entity.getPosition()).distance(CameraManager.getInstance().getActiveCamera().getPosition());
                        }
                    }

                    //Figure out what tool to use
                    loop:
                    {
                        //Single direction movements
                        if (hits.equals(1, 0, 0)) {
                            selectedTool = MOVE_X;
                            break loop;
                        }
                        if (hits.equals(0, 1, 0)) {
                            selectedTool = MOVE_Y;
                            break loop;
                        }
                        if (hits.equals(0, 0, 1)) {
                            selectedTool = MOVE_Z;
                            break loop;
                        }

                        //CrossProduct planes
                        if (hits.equals(1, 1, 0)) {
                            selectedTool = MOVE_XY;
                            break loop;
                        }
                        if (hits.equals(0, 1, 1)) {
                            selectedTool = MOVE_YZ;
                            break loop;
                        }
                        if (hits.equals(1, 0, 1)) {
                            selectedTool = MOVE_ZX;
                            break loop;
                        }

                        //XYZ
                        if (hits.equals(1, 1, 1)) {
                            selectedTool = MOVE_XYZ;
                            break loop;
                        }

                    }
                }
            }
            if (!this.selectedTool.equals(NONE)) {
                switch (selectedTool) {
                    case MOVE_XYZ: {
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(CameraManager.getInstance().getActiveCamera().getLookingDirection()).mul(-1));
                        if (pos != null) {
                            entity.setPosition(pos);
                        }
                        break;
                    }
                    case MOVE_X: {
                        switch(toolType){
                            case TRANSLATE:{
                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(0, 1, 0));

                                if(pos != null) {
                                    entity.setPosition(pos.x + delta.x , entity.getPositionSelf().y() , entity.getPositionSelf().z());
                                }
                                break;
                            }
                            case ROTATE:{
                                //Raycast to plane

                                break;
                            }
                            case SCALE:{
                                break;
                            }

                        }
                        break;
                    }
                    case MOVE_Y: {
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(CameraManager.getInstance().getActiveCamera().getLookingDirection()).mul(-1));
                        if(pos != null) {
                            entity.setPosition(entity.getPositionSelf().x() , pos.y + delta.y , entity.getPositionSelf().z());
                        }
                        break;
                    }
                    case MOVE_Z: {
                        //Raycast to plane
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(0, 1, 0));
                        if (pos != null) {
                            entity.setPosition(entity.getPositionSelf().x() , entity.getPositionSelf().y() , pos.z);
                        }
                        break;
                    }

                    //Cross Product movement dirs
                    case MOVE_XY: {
                        //Raycast to plane
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(0, 0, 1));
                        if(pos != null) {
                            entity.setPosition(pos.x + delta.x , pos.y + delta.y , entity.getPositionSelf().z());
                        }
                        break;
                    }

                    case MOVE_YZ: {
                        //Raycast to plane
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(1, 0, 0));
                        if(pos != null) {
                            entity.setPosition(entity.getPositionSelf().x() , pos.y + delta.y , pos.z);
                        }
                        break;
                    }

                    case MOVE_ZX: {
                        //Raycast to plane
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(0, 1, 0));
                        if(pos != null) {
                            entity.setPosition(pos.x + delta.x , entity.getPositionSelf().y() , pos.z);
                        }
                        break;
                    }

                    default:{
                        System.out.println("Tool:["+selectedTool+"] is not supported.");
                        break;
                    }
                }
            } else {
                //We hit nothing so lets do the next action.
                LinkedList<Entity> hits = EntityManager.getInstance().getHitEntities(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()));
                if (hits.size() > 0) {
                    if (this.entity != hits.getFirst()) {
                        hasReleased = false;
                        if(Keyboard.getInstance().isKeyPressed(Keyboard.SHIFT_LEFT, Keyboard.SHIFT_RIGHT)) {
                            this.addTarget(hits.getFirst());
                        }else{
                            this.setTarget(hits.getFirst());
                        }
                    }
                } else {
                    clearSelected();
                }
            }

        }

    }

    public void clearSelected(){
        //Deregister from our list of entities which may have arrows on the screen.
        for(Entity e : new LinkedList<>(this.selectedEntities.keySet())){
            this.selectedEntities.remove(e);
        }
        //Reset our distance and delta
        delta = new Vector3f(0);
        initialCastHit = new Vector3f(0);
        distanceToCam = 0;
        //Clear entity
        this.entity = null;
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
            //Direct draw AABB
            for(Entity e: selectedEntities.keySet()){
                e.renderInEditor(true);
                Renderer.getInstance().drawAABB(e);
            }

            Vector3f YELLOW = new Vector3f(1, 1, 0);
            Vector3f MAGENTA = new Vector3f(1, 0, 1);

            Vector3f drawColor = YELLOW;
            if(Keyboard.getInstance().isKeyPressed(Keyboard.ALT_LEFT)){
                drawColor = MAGENTA;
            }

            //Rotate
            if(toolType.equals(EditorMode.ROTATE)) {
                Vector3f size = new Vector3f(entity.getAABB()[1]).sub(entity.getAABB()[0]);
                float biggestDimension = Math.max(Math.max(size.x, size.y), size.z) / 2f;

                DirectDrawData ddd_x = Renderer.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension, biggestDimension, 0.0625f), new Vector3f(1, 0, 0), new Vector2i(32, 9), 360f, new Vector3f(1, 0, 0));
                if(MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_x.getAABB()) != null){
                    Renderer.getInstance().redrawTriangleColor(ddd_x, drawColor);
                }
                DirectDrawData ddd_y = Renderer.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension, biggestDimension, 0.0625f), new Vector3f(0, 1, 0), new Vector2i(32, 9), 360f, new Vector3f(0, 1, 0));
                if(MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_y.getAABB()) != null){
                    Renderer.getInstance().redrawTriangleColor(ddd_y, drawColor);
                }
                DirectDrawData ddd_z = Renderer.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension, biggestDimension, 0.0625f), new Vector3f(0, 0, 1), new Vector2i(32, 9), 360f, new Vector3f(0, 0, 1));
                if(MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_z.getAABB()) != null){
                    Renderer.getInstance().redrawTriangleColor(ddd_z, drawColor);
                }

                //Set our direct draw volumes
                this.selectedEntities.get(this.entity).ddd_x = ddd_x;
                this.selectedEntities.get(this.entity).ddd_y = ddd_y;
                this.selectedEntities.get(this.entity).ddd_z = ddd_z;
            }

            //TODO only draw Axis arrows if in Translate, New modes for rotate and scale.

            if(toolType.equals(EditorMode.TRANSLATE)) {
                float scale = new Vector3f(entity.getPosition()).distance(CameraManager.getInstance().getActiveCamera().getPosition()) / 16f;

                //Direct Draw Axis arrows
                DirectDrawData ddd_x = Renderer.getInstance().drawArrow(entity.getPosition(), new Vector3f(scale, 0, 0).mul(Math.max(entity.getScale().x / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(1, 0, 0));
                //If no tool selected, render if hits
                if (selectedTool.equals(NONE)) {
                    //render if hits
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_x.getAABB()) != null) {
                        //render in yellow
                        Renderer.getInstance().redrawTriangleColor(ddd_x, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_X) || selectedTool.equals(MOVE_XY) || selectedTool.equals(MOVE_ZX) || selectedTool.equals(MOVE_XYZ)) {
                        Renderer.getInstance().redrawTriangleColor(ddd_x, drawColor);
                    }
                }
                DirectDrawData ddd_y = Renderer.getInstance().drawArrow(entity.getPosition(), new Vector3f(0, scale, 0).mul(Math.max(entity.getScale().y / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(0, 1, 0));
                if (selectedTool.equals(NONE)) {
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_y.getAABB()) != null) {
                        Renderer.getInstance().redrawTriangleColor(ddd_y, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_Y) || selectedTool.equals(MOVE_XY) || selectedTool.equals(MOVE_YZ) || selectedTool.equals(MOVE_XYZ)) {
                        Renderer.getInstance().redrawTriangleColor(ddd_y, drawColor);
                    }
                }

                DirectDrawData ddd_z = Renderer.getInstance().drawArrow(entity.getPosition(), new Vector3f(0, 0, scale).mul(Math.max(entity.getScale().z / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(0, 0, 1));
                if (selectedTool.equals(NONE)) {
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_z.getAABB()) != null) {
                        Renderer.getInstance().redrawTriangleColor(ddd_z, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_Z) || selectedTool.equals(MOVE_ZX) || selectedTool.equals(MOVE_YZ) || selectedTool.equals(MOVE_XYZ)) {
                        Renderer.getInstance().redrawTriangleColor(ddd_z, drawColor);
                    }
                }

                //Set our direct draw volumes
                this.selectedEntities.get(this.entity).ddd_x = ddd_x;
                this.selectedEntities.get(this.entity).ddd_y = ddd_y;
                this.selectedEntities.get(this.entity).ddd_z = ddd_z;
            }
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
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick | ImGuiTreeNodeFlags.DefaultOpen;

            //Here we loop through all attributes that an entity has. Each attribute should have an ENUM describing what type of Attribute this is, default, constant, locked, hidden
            if(ImGui.collapsingHeader("Attributes", nodeFlags_attributes)) {
                AttributeRenderer.renderAttributes(entity.getAttributes());
            }

            //List all components, then all triggers, then all events
            int nodeFlags_components = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick | ImGuiTreeNodeFlags.DefaultOpen;
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

enum EditorAxis {
    //None
    NONE,

    //Move Single
    MOVE_X,
    MOVE_Y,
    MOVE_Z,

    //MoveDouble
    MOVE_XY,
    MOVE_YZ,
    MOVE_ZX,

    //Move
    MOVE_XYZ,
}

enum EditorMode{
    //None
    NONE,

    TRANSLATE,
    ROTATE,
    SCALE

}


class AttributeRenderer{
    protected static void renderAttributes(Collection<Attribute> attributes){
        //Loop through each attribute in the list.
        //These will be Key value pairs, where the key is in the left column.
        for (Attribute attribute : attributes) {

            //If this attribute should not be rendered, just skip this one.
            if(!attribute.isVisible()){
                continue;
            }

            //Try find a type
            ImGui.columns(2);
            ImGui.pushID(Editor.getInstance().getNextID());
            ImGui.pushItemWidth(ImGui.getColumnWidth());
            ImGui.labelText("", attribute.getName());
            ImGui.popItemWidth();
            ImGui.popID();
            ImGui.nextColumn();
//            ImGui.pushID(Editor.getInstance().getNextID());
            ImGui.beginChild(""+Editor.getInstance().getNextID(), ImGui.getColumnWidth(), ImGui.getColumnWidth());
            loop:{
                if (attribute.getData() instanceof Vector4f) {
                    Vector4f data = (Vector4f) attribute.getData();

                    if(attribute.getType().equals(EnumAttributeType.COLOR)){
                        float[] color = new float[]{data.x, data.y, data.z};
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.colorPicker3(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel);
                        attribute.setData(new Vector3f(color[0], color[1], color[2]));
                        ImGui.popID();
                    }else {
                        ImFloat x = new ImFloat(data.x);
                        ImFloat y = new ImFloat(data.y);
                        ImFloat z = new ImFloat(data.z);
                        ImFloat w = new ImFloat(data.w);

                        ImGui.columns(4);
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                        ImGui.inputFloat("X", x, .1f, 10);
                        ImGui.popStyleColor();
                        ImGui.popID();
                        ImGui.nextColumn();
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                        ImGui.inputFloat("Y", y, .1f, 10);
                        ImGui.popStyleColor();
                        ImGui.popID();
                        ImGui.nextColumn();
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                        ImGui.inputFloat("Z", z, .1f, 10);
                        ImGui.popStyleColor();
                        ImGui.popID();
                        ImGui.nextColumn();
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushStyleColor(ImGuiCol.Border, 1, 1, 1, 1);
                        ImGui.inputFloat("W", w, 1, 10);
                        ImGui.popStyleColor();
                        ImGui.popID();
                        ImGui.columns();

                        attribute.setData(new Vector4f(x.get(), y.get(), z.get(), w.get()));
                    }


                    break loop;
                }
                if (attribute.getData() instanceof Vector3f) {
                    Vector3f data = (Vector3f) attribute.getData();

                    if(attribute.getType().equals(EnumAttributeType.COLOR)){
                        float[] color = new float[]{data.x, data.y, data.z};
                        ImGui.pushID(Editor.getInstance().getNextID());
                        ImGui.pushItemWidth(ImGui.getColumnWidth());
                        ImGui.colorPicker3(attribute.getName(), color, ImGuiColorEditFlags.PickerHueWheel | ImGuiColorEditFlags.NoAlpha | ImGuiColorEditFlags.NoSidePreview | ImGuiColorEditFlags.NoSmallPreview | ImGuiColorEditFlags.NoTooltip | ImGuiColorEditFlags.NoLabel);
                        ImGui.popItemWidth();
                        attribute.setData(new Vector3f(color[0], color[1], color[2]));
                        ImGui.popID();
                    }else if(attribute.getType().equals(EnumAttributeType.SLIDERS)){
                        float[] sliderX = new float[]{data.x};
                        float[] sliderY = new float[]{data.y};
                        float[] sliderZ = new float[]{data.z};

                        ImGui.columns(3);
                        ImGui.pushID(Editor.getInstance().getNextID());
//                        ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                        ImGui.pushStyleColor(ImGuiCol.Border, 1, 0, 0, 1);
                        ImGui.sliderFloat("", sliderX, 0, 360);
                        ImGui.popStyleColor();
//                        ImGui.popItemWidth();
                        ImGui.popID();
                        ImGui.nextColumn();
                        ImGui.pushID(Editor.getInstance().getNextID());
//                        ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                        ImGui.pushStyleColor(ImGuiCol.Border, 0, 1, 0, 1);
                        ImGui.sliderFloat("", sliderY, 0, 360);
                        ImGui.popStyleColor();
//                        ImGui.popItemWidth();
                        ImGui.popID();
                        ImGui.nextColumn();
                        ImGui.pushID(Editor.getInstance().getNextID());
//                        ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
                        ImGui.pushStyleColor(ImGuiCol.Border, 0, 0, 1, 1);
                        ImGui.sliderFloat("", sliderZ, 0, 360);
                        ImGui.popStyleColor();
//                        ImGui.popItemWidth();
                        ImGui.popID();
                        ImGui.columns();

                        attribute.setData(new Vector3f(sliderX[0], sliderY[0], sliderZ[0]));

                    }else {
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
                    }


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

                if (attribute.getData() instanceof Float) {
                    float data = (float) attribute.getData();
                    ImFloat value = new ImFloat(data);

                    ImGui.inputFloat(attribute.getName(), value);

                    attribute.setData(value.get());

                    break loop;
                }

                if (attribute.getData() instanceof String) {
                    String data = (String) attribute.getData();
                    ImString value = new ImString(data);

                    ImGui.inputText(attribute.getName(), value);

                    attribute.setData(value.get());

                    break loop;
                }
                //End If no type was found, render default string.
                ImGui.inputText(attribute.getName(), new ImString(attribute.getData() + ""));
            }
            ImGui.endChild();
            ImGui.columns();
        }
        ImGui.columns();
    }
}

class EntityMetaData{
    DirectDrawData ddd_x;
    DirectDrawData ddd_y;
    DirectDrawData ddd_z;

    EntityMetaData(){

    }
}