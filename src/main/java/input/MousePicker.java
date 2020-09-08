package input;

import camera.CameraManager;
import editor.Editor;
import engine.Engine;
import engine.FraudTek;
import entity.Entity;
import graphics.renderer.Renderer;
import imgui.ImGui;
import models.AABB;
import org.joml.*;
import math.Maths;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import util.Callback;

import java.lang.Math;
import java.nio.DoubleBuffer;
import java.util.LinkedList;

public class MousePicker extends Engine {

    private static MousePicker mousePicker;
    private Vector3f ray = new Vector3f();

    private float MouseX = 0;
    private float MouseY = 0;
    private float MouseDeltaX = 0;
    private float MouseDeltaY = 0;

    private float MouseOffsetX = 0;
    private float MouseOffsetY = 0;
    private float ScreenScaleX = 1;
    private float ScreenScaleY = 1;

    private boolean lockMouse = false;

    //Mouse Buttons
    public static final int MOUSE_LEFT  = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    public static final int MOUSE_RIGHT = GLFW.GLFW_MOUSE_BUTTON_RIGHT;

    public LinkedList<Callback> callbacks = new LinkedList<Callback>();

    public boolean mousePressed = false;

    private MousePicker(){
        GLFW.glfwSetMouseButtonCallback(FraudTek.WINDOW_POINTER, (long window, int button, int action, int mods) -> {
            //Update Editor
            Editor.getInstance().onClick(button, action, mods);
            //Check for Intersection
            if(!ImGui.getIO().getWantCaptureMouse()){
                if(action == GLFW.GLFW_PRESS){
                    if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                        mousePressed = true;
                    }
                }
                if(action == GLFW.GLFW_RELEASE){
                    if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                        mousePressed = false;
                    }
                }
                for(Callback c : callbacks){
                    if(c != null) {
                        c.callback(button, action);
                    }
                }
            }
        });
    }

    @Override
    public void onShutdown() {

    }

    public void tick(double delta){
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        GLFW.glfwGetCursorPos(FraudTek.WINDOW_POINTER, x, y);
        x.rewind();
        y.rewind();

        double newX = x.get();
        double newY = y.get();

        MouseX = ((float) newX) + MouseOffsetX;
        MouseY = ((float) newY) + MouseOffsetY;

//        System.out.println(MouseDeltaX+","+MouseDeltaY);
        if(lockMouse) {
            MouseDeltaX = (float) (newX - (Renderer.getInstance().getWIDTH()/2));
            MouseDeltaY = (float) (newY - (Renderer.getInstance().getHEIGHT()/2));

            GLFW.glfwSetCursorPos(FraudTek.WINDOW_POINTER, Renderer.getWIDTH() / 2, Renderer.getHEIGHT() / 2);
        }else{
            MouseDeltaX = 0;
            MouseDeltaY = 0;
        }

        //Flip y?
//        MouseY *= -1;
//        MouseY += Renderer.getInstance().getHEIGHT();


        ray = calculateMouseRay();

        //Reset mouse pos for this frame
        MouseX -= MouseOffsetX;
        MouseY -= MouseOffsetY;

        resetOffset();
    }

    public Vector3f calculateMouseRay() {
        float mouseX = MouseX;
        float mouseY = MouseY;
        Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        Vector3f worldRay = toWorldCoords(eyeCoords);
        worldRay.mul(1, -1, 1);
        return worldRay;
    }

    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = new Matrix4f().set(CameraManager.getInstance().getActiveCamera().getTransform()).setTranslation(0, 0, 0).invert();
        Vector4f rayWorld = new Vector4f();
        invertedView.transform(eyeCoords, rayWorld);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y * -1f, rayWorld.z);
        mouseRay.normalize(mouseRay);
        return mouseRay;
    }

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = new Matrix4f().set(Renderer.getInstance().getProjectionMatrix()).invert();
        Vector4f eyeCoords = new Vector4f();
        invertedProjection.transformAffine(clipCoords, eyeCoords);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2.0f * mouseX) / (Renderer.getInstance().getWIDTH()  * ScreenScaleX) - 1f;
        float y = (2.0f * mouseY) / (Renderer.getInstance().getHEIGHT() * ScreenScaleY) - 1f;
        return new Vector2f(Math.min(Math.max(x, -1), 1), Math.min(Math.max(y, -1), 1) * -1);
    }

    public static MousePicker getInstance(){
        if(mousePicker == null){
            mousePicker = new MousePicker();
        }

        return mousePicker;
    }

    public Vector3f getRay(){
        return new Vector3f(this.ray);
    }

    public Vector2f getScreenCoords(){
        return new Vector2f(MouseX, (Renderer.getInstance().getHEIGHT() * ScreenScaleY) - MouseY);
    }

    public Vector2f getGLScreenCoords(){
        Vector2f pos = new Vector2f(MouseX, Renderer.getInstance().getHEIGHT()-MouseY);
        pos.x = Math.max(Math.min(Renderer.getInstance().getWIDTH(), pos.x), 0);
        pos.y = Math.max(Math.min(Renderer.getInstance().getHEIGHT(), pos.y), 0);
        pos.x /= Renderer.getInstance().getWIDTH();
        pos.y /= Renderer.getInstance().getHEIGHT();
        pos.x *= 2;
        pos.y *= 2;
        pos.x -= 1;
        pos.y -= 1;
        //Invert Y axis
        pos.y *= -1;
        return pos;
    }

    public void addCallback(Callback callback){
        this.callbacks.add(callback);
    }

    public void removeCallback(Callback mouseCallback) {
        this.callbacks.remove(mouseCallback);
    }

    public void setOffset(float mouseOffsetX, float mouseOffsetY, float screenScaleX, float screenScaleY){
        this.MouseOffsetX = mouseOffsetX;
        this.MouseOffsetY = mouseOffsetY;
        this.ScreenScaleX = screenScaleX;
        this.ScreenScaleY = screenScaleY;
    }

    public void resetOffset(){
        //Reset any offsets
        MouseOffsetX = 0;
        MouseOffsetY = 0;
        ScreenScaleX = 1;
        ScreenScaleY = 1;
    }

    public boolean requestLockMouse(){
        //No logic yet, will always lock mouse
        lockMouse = true;
        //If our mouse has been locked, lets move the mouse to the default position and reset the mouse ray
        if(lockMouse){
            this.tick(0);
            MouseDeltaX = 0;
            MouseDeltaY = 0;
        }
        //Returns if the mouse has been locked or not.
        return lockMouse;
    }

    public float getMouseDeltaX(){
        return MouseDeltaX;
    }

    public float getMouseDeltaY(){
        return MouseDeltaY;
    }

    public void unlockMouse(){
        if(this.lockMouse){
            this.lockMouse = false;
        }
    }

    //TODO move to intersection class
    public static Vector3f rayHitsPlane(Vector3f pos, Vector3f dir, Vector3f planeOrigin, Vector3f planeNormal){
        //Invert the plane normal
        planeNormal = new Vector3f(planeNormal);
        //Invert Pos
        pos = new Vector3f(pos).mul(-1);

        float scale = Intersectionf.intersectRayPlane(pos, dir, planeOrigin, planeNormal, 0.02f);
//        Intersectionf.intersectRay
        if(scale >= 0){
            return new Vector3f(dir).mul(scale).add(pos);
        }else{
            scale = Intersectionf.intersectRayPlane(pos, dir, planeOrigin, planeNormal.mul(-1), 0.02f);
            if(scale >= 0){
                return new Vector3f(dir).mul(scale).add(pos);
            }else{
                return null;
            }
        }
    }

    public static Vector3f rayHitsAABB(Vector3f pos, Vector3f dir, AABB aabb) {
        return rayHitsAABB(pos, dir, aabb.getMIN(), aabb.getMAX());
    }

    public static Vector3f rayHitsAABB(Vector3f pos, Vector3f dir, Vector3f aabbMin, Vector3f aabbMax){
        //Invert Pos
        pos = new Vector3f(pos).mul(-1);

        Vector2f result = new Vector2f();
        boolean hit = Intersectionf.intersectRayAab(pos, dir, aabbMin, aabbMax, result);

        if(hit){
            return new Vector3f(dir).mul(new Vector3f(result.x, result.y, 1)).add(pos);
        }else{
            return null;
        }
    }
}
