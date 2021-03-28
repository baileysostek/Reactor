package entity;

import camera.CameraManager;
import com.google.gson.JsonObject;
import editor.AttributeRenderer;
import editor.Editor;
import editor.components.UIComponet;
import engine.Reactor;
import entity.component.Attribute;
import entity.component.Component;
import entity.component.EnumAttributeType;
import entity.component.Event;
import graphics.renderer.DirectDraw;
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
import math.VectorUtils;
import org.joml.*;
import org.lwjgl.glfw.GLFW;
import platform.EnumDevelopment;
import platform.PlatformManager;
import serialization.SerializationHelper;
import util.Callback;
import util.Debouncer;
import util.StringUtils;

import java.lang.Math;
import java.security.Key;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import static entity.EditorAxis.*;

public class EntityEditor extends UIComponet {
    private Callback mouseCallback;
    private Callback dropFileInWorld;
    boolean pressed = false;

    //The entity we are interacting with
    private Entity entity;
    private HashMap<Entity, EntityMetaData> selectedEntities = new HashMap<>();
    private Stack<Entity> pastSelections = new Stack<Entity>();

    private Vector3f delta = new Vector3f(0);
    private Vector3f initialCastHit = new Vector3f(0);
    private Vector3f rayToHit = new Vector3f(0);
    private Vector3f initialRotationPoint = new Vector3f(0);
    private float distanceToCam = 0;

    private Debouncer shiftClick = new Debouncer(false);

    //Tools
    private EditorMode toolType = EditorMode.NONE;
    private EditorAxis selectedTool = NONE;

    //Debouncer boolean used to prevent entity modification right when clicking on an entity.
    private boolean hasReleased = true;

    //Callbacks for Different Actions that a user could want to do. Like on entity Select, Deselect, TryPlace in world, Place in World, Delete from world.

    //Select-Deselect
    private LinkedList<Callback> onSelect          = new LinkedList<>();
    private LinkedList<Callback> onDeselect        = new LinkedList<>();
    private LinkedList<Callback> onDelete          = new LinkedList<>();
    private LinkedList<Callback> onClone           = new LinkedList<>();

    //translations
    private LinkedList<Callback> onMove            = new LinkedList<>();
    private LinkedList<Callback> onRotate          = new LinkedList<>();
    private LinkedList<Callback> onScale           = new LinkedList<>();

    //transactions
    private LinkedList<Callback> onActionStart     = new LinkedList<>();
    private LinkedList<Callback> onActionStop      = new LinkedList<>();

    //Settings
    private Vector3f moveToNearest   = new Vector3f(1);
    private Vector3f rotateToNearest = new Vector3f(5);
    private Vector3f scaleToNearest  = new Vector3f(0.1f);


    //Editor resources
    Sprite entityNotFound;

    public EntityEditor(){
        //Load resources and set variables
        entityNotFound = SpriteBinder.getInstance().load("fileNotFound.png");

        //Genreate mouse callback
        mouseCallback = new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    if (EntityEditor.super.isVisable()) {
                        int button = (int) objects[0];
                        int action = (int) objects[1];

                        if (button == MousePicker.MOUSE_LEFT) {
                            //On Release Set selected to none
                            if (action == GLFW.GLFW_RELEASE) {
                                //We didnt stop an action if we are doing nothing...
                                if(!selectedTool.equals(NONE)){
                                    //Call the callbacks
                                    for (Callback c : onActionStop) {
                                        c.callback(selectedTool, selectedEntities.keySet());
                                    }
                                }

                                //Deselect the tool
                                selectedTool = NONE;
                                hasReleased = true;
                            }

                            pressed = (action == GLFW.GLFW_PRESS);
                            if (pressed) {
                                raycastToWorld();
                                for (Callback c : onActionStart) {
                                    c.callback(selectedTool, selectedEntities.keySet());
                                }
                            }
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
                if(Reactor.isDev()) {
                    for (Entity e : selectedEntities.keySet()) {
                        EntityManager.getInstance().removeEntity(e);
                    }
                    for (Callback callback : onDelete) {
                        callback.callback(selectedEntities.keySet());
                    }

                    clearSelected();

                    //Selected Stack
                    if(pastSelections.size() > 0){
                        pastSelections.pop();
                        if(pastSelections.size() > 0) {
                            setTarget(pastSelections.pop());
                        }
                    }
                }
                return null;
            }
        });

        Keyboard.getInstance().addPressCallback(Keyboard.ONE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    toolType = EditorMode.TRANSLATE;
                }
                return null;
            }
        });
        Keyboard.getInstance().addPressCallback(Keyboard.TWO, new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    toolType = EditorMode.ROTATE;
                }
                return null;
            }
        });
        Keyboard.getInstance().addPressCallback(Keyboard.THREE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    toolType = EditorMode.SCALE;
                }
                return null;
            }
        });

        Keyboard.getInstance().addPressCallback(Keyboard.ESCAPE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    clearSelected();
                }
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
                    initialRotationPoint = null;
                    rayToHit = new Vector3f(0);
                    distanceToCam = 0;

                    if(Keyboard.getInstance().isKeyPressed(Keyboard.ALT_LEFT)){
                        Entity clone = cloneSelected();

                        System.out.println(clone);

                        //Trigger the onCopy
                        for(Callback c : onClone){
                            c.callback(clone);
                        }

                        //Need to actually draw the arrows on the screen,
                        this.preUIRender();

                    }

                    //Get meta-data for selected entity and see if the entity
                    EntityMetaData metaData = this.selectedEntities.get(this.entity);

                    //Store our hits in this vector.
                    Vector3f hits = new Vector3f(0);

                    float closest = Float.MAX_VALUE;

                    if(metaData.ddd_x != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_x.getAABB());
                        if (pos != null) {
                            delta = new Vector3f(pos).sub(entity.getPositionSelf());
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            if(toolType.equals(EditorMode.ROTATE)){
                                float hitPoint = pos.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                                if(hitPoint < closest){
                                    closest = hitPoint;
                                    hits = new Vector3f(1, 0, 0);
                                }
                            }else {
                                hits.x = 1;
                            }
                        }
                    }
                    if(metaData.ddd_y != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_y.getAABB());
                        if (pos != null) {
                            delta = new Vector3f(pos).sub(entity.getPositionSelf());
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            if(toolType.equals(EditorMode.ROTATE)){
                                float hitPoint = pos.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                                if(hitPoint < closest){
                                    closest = hitPoint;
                                    hits = new Vector3f(0, 1, 0);
                                }
                            }else {
                                hits.y = 1;
                            }
                        }
                    }
                    if(metaData.ddd_z != null) {
                        Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_z.getAABB());

//                        Renderer.getInstance().drawLine(new Vector3f(0), new Vector3f(pos), new Vector3f(1));

                        if (pos != null) {
                            delta = new Vector3f(pos).sub(entity.getPositionSelf());
                            initialCastHit = new Vector3f(pos);
                            rayToHit = new Vector3f(pos).sub(entity.getPositionSelf()).normalize();
                            if(toolType.equals(EditorMode.ROTATE)){
                                float hitPoint = pos.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                                if(hitPoint < closest){
                                    closest = hitPoint;
                                    hits = new Vector3f(0, 0, 1);
                                }
                            }else {
                                hits.z = 1;
                            }

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

                                if(pos == null){
                                    break;
                                }

                                Vector3f deltaPos = new Vector3f(pos).sub(delta);

                                //Snap to snap res
                                if(Keyboard.getInstance().isKeyPressed(Keyboard.SHIFT_LEFT)){
                                    deltaPos = deltaPos.round(moveToNearest);
                                }

                                if(pos != null) {
                                    //TODO add delta from select.
                                    for(Entity e : selectedEntities.keySet()){
                                        e.setPosition(deltaPos.x , e.getPositionSelf().y() , e.getPositionSelf().z());
                                    }
                                }
                                break;
                            }
                            case ROTATE:{
                                Quaternionf rotation = new Matrix4f(entity.getTransform()).getNormalizedRotation(new Quaternionf());

                                Vector4f rotx = new Vector4f(1, 0, 0, 1).mul(new Matrix4f().identity().rotate(rotation));
                                Vector3f planeNormal = new Vector3f(rotx.x, rotx.y, rotx.z);

                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(planeNormal));

                                if(pos == null){
                                    break;
                                }

                                if(this.initialRotationPoint == null){
                                    this.initialRotationPoint = new Vector3f(pos);
                                }

                                DirectDraw.getInstance().drawLine(new Vector3f(planeNormal).add(entity.getPosition()), entity.getPosition(), new Vector3f(1, 0, 0));

                                float angle = new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).angleSigned(new Vector3f(pos).sub(entity.getPosition()), planeNormal);
                                float offsetAngle = new Vector3f(0, 0, -1).angleSigned(new Vector3f(this.initialRotationPoint).sub(entity.getPosition()), planeNormal);

                                //Draw arch
                                float biggestDimension = VectorUtils.maxComponent(new Vector3f(entity.getAABB()[1]).sub(entity.getAABB()[0]));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(pos).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(1, 0, 0));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(1, 0, 0));
                                DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 2, biggestDimension + 2, 0.0625f), new Vector3f(planeNormal), new Vector2i(32, 9), (float) Math.toDegrees(angle), (float) Math.toDegrees(offsetAngle) - 45f, new Vector3f(1, 0, 0));

                                Vector3f rot = entity.getRotation();

                                rot.x = (float) Math.toDegrees(angle);

                                this.entity.setRotation(rot);

                                break;
                            }
                            case SCALE:{
                                break;
                            }

                        }
                        break;
                    }
                    case MOVE_Y: {
                        switch(toolType){
                            case TRANSLATE:{
                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(CameraManager.getInstance().getActiveCamera().getLookingDirection()).mul(-1));

                                if(pos == null){
                                    break;
                                }

                                Vector3f deltaPos = new Vector3f(pos).sub(delta);

                                //Snap to snap res
                                if(Keyboard.getInstance().isKeyPressed(Keyboard.SHIFT_LEFT)){
                                    deltaPos = deltaPos.round(moveToNearest);
                                }

                                if(pos != null) {
                                    for(Entity e : selectedEntities.keySet()){
                                        e.setPosition(e.getPositionSelf().x() , deltaPos.y , e.getPositionSelf().z());
                                    }
                                }
                                break;
                            }
                            case ROTATE:{
                                Quaternionf rotation = new Matrix4f(entity.getTransform()).getNormalizedRotation(new Quaternionf());

                                Vector4f roty = new Vector4f(0, 1, 0, 1).mul(new Matrix4f().identity().rotate(rotation));
                                Vector3f planeNormal = new Vector3f(roty.x, roty.y, roty.z);

                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(planeNormal));

                                if(pos == null){
                                    break;
                                }

                                if(this.initialRotationPoint == null){
                                    this.initialRotationPoint = new Vector3f(pos);
                                }

                                DirectDraw.getInstance().drawLine(new Vector3f(planeNormal).add(entity.getPosition()), entity.getPosition(), new Vector3f(0, 1, 0));

                                float angle = new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).angleSigned(new Vector3f(pos).sub(entity.getPosition()), planeNormal);
                                float offsetAngle = new Vector3f(0, 0, 1).angleSigned(new Vector3f(this.initialRotationPoint).sub(entity.getPosition()), planeNormal);

                                //Draw arch
                                float biggestDimension = VectorUtils.maxComponent(new Vector3f(entity.getAABB()[1]).sub(entity.getAABB()[0]));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(pos).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(0, 1, 0));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(0, 1, 0));
                                DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 2, biggestDimension + 2, 0.0625f), new Vector3f(planeNormal), new Vector2i(32, 9), (float) Math.toDegrees(angle), (float) Math.toDegrees(offsetAngle) - 90f, new Vector3f(0, 1, 0));

                                Vector3f rot = entity.getRotation();

                                rot.y = (float) Math.toDegrees(angle);

                                this.entity.setRotation(rot);

                                break;
                            }
                            case SCALE:{
                                break;
                            }

                        }
                        break;
                    }
                    case MOVE_Z: {
                        //Raycast to plane
                        switch(toolType){
                            case TRANSLATE:{
                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(0, 1, 0));

                                if(pos == null){
                                    break;
                                }

                                Vector3f deltaPos = new Vector3f(pos).sub(new Vector3f(delta).mul(1, 1, 1));

                                //Snap to snap res
                                if(Keyboard.getInstance().isKeyPressed(Keyboard.SHIFT_LEFT)){
                                    deltaPos = deltaPos.round(moveToNearest);
                                }

                                if(pos != null) {
                                    for(Entity e : selectedEntities.keySet()){
                                        e.setPosition(e.getPositionSelf().x() , e.getPositionSelf().y() , deltaPos.z);
                                    }
                                }
                                break;
                            }
                            case ROTATE:{
                                Quaternionf rotation = new Matrix4f(entity.getTransform()).getNormalizedRotation(new Quaternionf());

                                Vector4f rotz = new Vector4f(0, 0, 1, 1).mul(new Matrix4f().identity().rotate(rotation));
                                Vector3f planeNormal = new Vector3f(rotz.x, rotz.y, rotz.z);

                                //Raycast to plane
                                Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(entity.getPosition()), new Vector3f(planeNormal));

                                if(pos == null){
                                    break;
                                }

                                if(this.initialRotationPoint == null){
                                    this.initialRotationPoint = new Vector3f(pos);
                                }

                                DirectDraw.getInstance().drawLine(new Vector3f(planeNormal).add(entity.getPosition()), entity.getPosition(), new Vector3f(0, 0, 1));

                                float angle = new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).angleSigned(new Vector3f(pos).sub(entity.getPosition()), planeNormal);
                                float offsetAngle = new Vector3f(0, 0, 1).angleSigned(new Vector3f(this.initialRotationPoint).sub(entity.getPosition()), planeNormal);

                                //Draw arch
                                float biggestDimension = VectorUtils.maxComponent(new Vector3f(entity.getAABB()[1]).sub(entity.getAABB()[0]));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(pos).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(0, 0, 1));
                                DirectDraw.getInstance().drawCylinder(entity.getPosition(), new Vector3f(this.initialRotationPoint).sub(entity.getPosition()).normalize().mul(biggestDimension + 2), new Vector3f(0.125f), 13, new Vector3f(0, 0, 1));
                                DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 2, biggestDimension + 2, 0.0625f), new Vector3f(planeNormal), new Vector2i(32, 9), (float) Math.toDegrees(angle), (float) Math.toDegrees(offsetAngle) - 150f, new Vector3f(0, 0, 1));

                                Vector3f rot = entity.getRotation();

                                rot.z = (float) Math.toDegrees(angle);

                                this.entity.setRotation(rot);

                                break;
                            }
                            case SCALE:{
                                break;
                            }

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
                        if (Keyboard.getInstance().isKeyPressed(Keyboard.SHIFT_LEFT, Keyboard.SHIFT_RIGHT)) {
                            //TODO add debouncer
//                            if (this.selectedEntities.containsKey(hits.getFirst())) {
//                                this.selectedEntities.remove(hits.getFirst());
//                            } else {
//                                this.addTarget(hits.getFirst());
//                            }
                            this.addTarget(hits.getFirst());
                        } else {
                            this.setTarget(hits.getFirst());
                            this.pastSelections.push(hits.getFirst());
                        }
                    }
                } else {
                    clearSelected();
                }
            }

        }

    }

    public Entity cloneSelected() {
        Entity clone = cloneTarget(this.entity);
        EntityManager.getInstance().addEntity(clone);
        return clone;
    }

    public Entity cloneTarget(Entity target){
        Entity entity;

        //This has broken attributes, set from parent
        JsonObject serialziedEntity = target.serialize();

        //Deserialize the entity
        if(target.getClass().equals(Entity.class)) {
            //Regula old entity
            entity = new Entity().deserialize(serialziedEntity);
        }else{
            //Fancy entity from another class or namespace :)
            try {
                Class<?> classType = Class.forName(target.getClass().getName());
                entity = ((Entity) SerializationHelper.getGson().fromJson(serialziedEntity, classType)).deserialize(serialziedEntity);

            } catch (ClassNotFoundException e) {
                //TODO play sound that that entity is unknown, maybe show message dialogue too.
                e.printStackTrace();
                return null;
            }
//            catch (Exception e) {
//                entity = new Entity();
//                entity.getAttribute("name").setData("Error");
//                e.printStackTrace();
//            }
        }

        //Check if the moved entity was a child
        if(target.hasParent()){
            entity.setParent(target.getParent());
        }

        this.setTarget(entity);

        return entity;
    }

    public void clearSelected(){
        //Deregister from our list of entities which may have arrows on the screen.
        for(Entity e : new LinkedList<>(this.selectedEntities.keySet())){
            this.selectedEntities.remove(e);
        }
        //Reset our distance and delta
        delta = new Vector3f(0);
        initialCastHit = new Vector3f(0);
        initialRotationPoint = null;
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

        if(this.selectedEntities.containsKey(this.entity)) {
            //Get meta-data for selected entity and see if the entity
            EntityMetaData metaData = this.selectedEntities.get(this.entity);

            if(metaData.ddd_z != null) {
                Vector3f pos = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), new Vector3f(MousePicker.getInstance().getRay()), metaData.ddd_z.getAABB());
                if (pos != null) {
                    DirectDraw.getInstance().drawLine(new Vector3f(0), new Vector3f(pos), new Vector3f(1));
                }
            }
        }
    }

    public void getTexture(){

    }

    @Override
    public void preUIRender(){
        if(this.entity != null) {
            //Direct draw AABB
            for(Entity e: selectedEntities.keySet()){
                e.renderInEditor(true);
                DirectDraw.getInstance().drawAABB(e, new Vector3f(1));
                DirectDraw.getInstance().drawBones(e);
            }

            Vector3f YELLOW = new Vector3f(1, 1, 0);
            Vector3f MAGENTA = new Vector3f(1, 0, 1);

            Vector3f drawColor = YELLOW;
            if(Keyboard.getInstance().isKeyPressed(Keyboard.ALT_LEFT)){
                drawColor = MAGENTA;
            }

            //Rotate
            if(toolType.equals(EditorMode.ROTATE) && selectedTool.equals(NONE)) {

                float biggestDimension = VectorUtils.maxComponent(new Vector3f(entity.getAABB()[1]).sub(entity.getAABB()[0]));

                Quaternionf rotation = new Matrix4f(entity.getTransform()).getNormalizedRotation(new Quaternionf());

                Vector4f rotx = new Vector4f(1, 0, 0, 1).mul(new Matrix4f().identity().rotate(rotation));
                Vector4f roty = new Vector4f(0, 1, 0, 1).mul(new Matrix4f().identity().rotate(rotation));
                Vector4f rotz = new Vector4f(0, 0, 1, 1).mul(new Matrix4f().identity().rotate(rotation));

                float closest = Float.MAX_VALUE;
                DirectDrawData closestVolume = null;

                DirectDrawData ddd_x = DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 1, biggestDimension + 1, 0.0625f), new Vector3f(rotx.x, rotx.y, rotx.z), new Vector2i(32, 9), 360f, new Vector3f(1, 0, 0));
                Vector3f xTest = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_x.getAABB());
                if(xTest != null){
                    float distance = xTest.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                    if(distance < closest){
                        closest = distance;
                        closestVolume = ddd_x;
                    }
                }
                DirectDrawData ddd_y = DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 1, biggestDimension + 1, 0.0625f), new Vector3f(roty.x, roty.y, roty.z), new Vector2i(32, 9), 360f, new Vector3f(0, 1, 0));
                Vector3f yTest = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_y.getAABB());
                if(yTest != null){
                    float distance = yTest.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                    if(distance < closest){
                        closest = distance;
                        closestVolume = ddd_y;
                    }
                }
                DirectDrawData ddd_z = DirectDraw.getInstance().drawRing(new Vector3f(entity.getPosition()), new Vector3f(biggestDimension + 1, biggestDimension + 1, 0.0625f), new Vector3f(rotz.x, rotz.y, rotz.z), new Vector2i(32, 9), 360f, new Vector3f(0, 0, 1));
                Vector3f zTest = MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_z.getAABB());
                if(zTest != null){
                    float distance = zTest.distance(CameraManager.getInstance().getActiveCamera().getPosition());
                    if(distance < closest){
                        closest = distance;
                        closestVolume = ddd_z;
                    }
                }

                //Draw closest in yellow
                if(closestVolume != null) {
                    DirectDraw.getInstance().redrawTriangleColor(closestVolume, drawColor);
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
                DirectDrawData ddd_x = DirectDraw.getInstance().drawArrow(entity.getPosition(), new Vector3f(scale, 0, 0).mul(Math.max(entity.getScale().x / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(1, 0, 0));
                //If no tool selected, render if hits
                if (selectedTool.equals(NONE)) {
                    //render if hits
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_x.getAABB()) != null) {
                        //render in yellow
                        DirectDraw.getInstance().redrawTriangleColor(ddd_x, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_X) || selectedTool.equals(MOVE_XY) || selectedTool.equals(MOVE_ZX) || selectedTool.equals(MOVE_XYZ)) {
                        DirectDraw.getInstance().redrawTriangleColor(ddd_x, drawColor);
                    }
                }
                DirectDrawData ddd_y = DirectDraw.getInstance().drawArrow(entity.getPosition(), new Vector3f(0, scale, 0).mul(Math.max(entity.getScale().y / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(0, 1, 0));
                if (selectedTool.equals(NONE)) {
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_y.getAABB()) != null) {
                        DirectDraw.getInstance().redrawTriangleColor(ddd_y, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_Y) || selectedTool.equals(MOVE_XY) || selectedTool.equals(MOVE_YZ) || selectedTool.equals(MOVE_XYZ)) {
                        DirectDraw.getInstance().redrawTriangleColor(ddd_y, drawColor);
                    }
                }

                DirectDrawData ddd_z = DirectDraw.getInstance().drawArrow(entity.getPosition(), new Vector3f(0, 0, scale).mul(Math.max(entity.getScale().z / 2f, 1)).add(entity.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f * scale), 13, new Vector3f(0, 0, 1));
                if (selectedTool.equals(NONE)) {
                    if (MousePicker.rayHitsAABB(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), ddd_z.getAABB()) != null) {
                        DirectDraw.getInstance().redrawTriangleColor(ddd_z, drawColor);
                    }
                } else {
                    //We are using a tool, if its a valid tool, set our color to yellow.
                    if (selectedTool.equals(MOVE_Z) || selectedTool.equals(MOVE_ZX) || selectedTool.equals(MOVE_YZ) || selectedTool.equals(MOVE_XYZ)) {
                        DirectDraw.getInstance().redrawTriangleColor(ddd_z, drawColor);
                    }
                }

                //Set our direct draw volumes
                this.selectedEntities.get(this.entity).ddd_x = ddd_x;
                this.selectedEntities.get(this.entity).ddd_y = ddd_y;
                this.selectedEntities.get(this.entity).ddd_z = ddd_z;
            }
        }

        //Now render all entities that are not selected
        for(Entity e : EntityManager.getInstance().getEntities()){
            if(!selectedEntities.containsKey(e)){
                e.renderInEditor(false);
            }
        }
    }

    @Override
    public void selfRender() {
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
//            for(String category : entity.getAttributeCategories()){
//                int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick | ImGuiTreeNodeFlags.DefaultOpen;
//
//                //Here we loop through all attributes that an entity has. Each attribute should have an ENUM describing what type of Attribute this is, default, constant, locked, hidden
//                if(ImGui.collapsingHeader(category, nodeFlags_attributes)) {
//                    AttributeRenderer.renderAttributes(entity.getAttributesOfCategory(category));
//                }
//            }
            AttributeRenderer.renderAttributes(entity.getAttributes());

            //List all components, then all triggers, then all events
            int nodeFlags_components = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick | ImGuiTreeNodeFlags.DefaultOpen;

            //Create the components header
            if(ImGui.collapsingHeader("Components", nodeFlags_components)){
                for (Component component : this.entity.getComponents()) {
                    //For Each component
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
                        component.onRenderUI();
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

    //Hook entrypoints
    public void addOnActionStart(Callback callback){
        this.onActionStart.addLast(callback);
    }

    public void addOnActionStop(Callback callback){
        this.onActionStop.addLast(callback);
    }

    public void addOnDelete(Callback callback){
        this.onDelete.addLast(callback);
    }

    public void addOnClone(Callback callback){
        this.onClone.addLast(callback);
    }

    public Collection<Entity> getSelected() {
        return this.selectedEntities.keySet();
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

class EntityMetaData{
    DirectDrawData ddd_x;
    DirectDrawData ddd_y;
    DirectDrawData ddd_z;

    EntityMetaData(){

    }
}