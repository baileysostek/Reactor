package input;

import camera.CameraManager;
import editor.Editor;
import engine.Reactor;
import entity.Entity;
import graphics.renderer.Renderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import models.AABB;
import models.Model;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import scene.Scene;
import scene.SceneManager;
import util.Callback;

import java.lang.Math;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MousePicker{

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
    public static final int MOUSE_LEFT   = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    public static final int MOUSE_RIGHT  = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    public static final int MOUSE_MIDDLE = GLFW.GLFW_MOUSE_BUTTON_MIDDLE;

    private LinkedList<Callback> buttonCallbacks = new LinkedList<Callback>();
    private LinkedList<Callback> motionCallbacks = new LinkedList<Callback>();
    private LinkedList<Callback> scrollCallbacks = new LinkedList<Callback>();

    private HashMap<Callback, Scene> toRemoveOnSceneChange = new HashMap<>();

    private HashMap<Integer, Boolean> mouseKeys = new HashMap<>();

    public boolean mousePressed = false;

    public float scrollDeltaX = 0;
    public float scrollDeltaY = 0;

    //Lock for locking our entity set
    private Lock lock;

    private MousePicker(){
        GLFW.glfwSetMouseButtonCallback(Reactor.WINDOW_POINTER, (long window, int button, int action, int mods) -> {
            //Update Editor
            if(Reactor.isDev()) {
                Editor.getInstance().onClick(button, action, mods);
                //Check for Intersection
                if(!ImGui.getIO().getWantCaptureMouse()){
                    processMouse(window, button, action, mods);
                }
            }else{
                processMouse(window, button, action, mods);
            }
        });

        GLFW.glfwSetScrollCallback(Reactor.WINDOW_POINTER, (long window, double xoffset, double yoffset) -> {
            // Default for Engine opperation
            scrollDeltaX = (float) xoffset;
            scrollDeltaY = (float) yoffset;
            if(scrollDeltaX != 0 || scrollDeltaY != 0){
                for(Callback c : scrollCallbacks){
                    c.callback(scrollDeltaX, scrollDeltaY);
                }
            }

            //If dev
            if(Reactor.isDev()){
                ImGuiIO io = ImGui.getIO();
                io.setMouseWheelH(io.getMouseWheelH() + (float) xoffset);
                io.setMouseWheel(io.getMouseWheel() + (float) yoffset);
            }
        });

        lock = new ReentrantLock();
    }

    private void processMouse(long window, int button, int action, int mods){
        if(action == GLFW.GLFW_PRESS){
            mouseKeys.put(button, true);
            if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                mousePressed = true;
            }
        }
        if(action == GLFW.GLFW_RELEASE){
            mouseKeys.put(button, false);
            if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                mousePressed = false;
            }
        }
        for(Callback c : new LinkedList<>(buttonCallbacks)){
            if(c != null) {
                c.callback(button, action);
            }
        }

        //Only for debug.
        System.out.println("Mouse Pressed:" + button);
    }

    public void onShutdown() {

    }

    public void tick(double delta){
        DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

        GLFW.glfwGetCursorPos(Reactor.WINDOW_POINTER, x, y);
        x.rewind();
        y.rewind();

        double newX = x.get();
        double newY = y.get();

        if(MouseX != newX || MouseY != newY){
            for(Callback callback : motionCallbacks){
                callback.callback(MouseX, MouseY, (newX - MouseX), (newY - MouseY));
            }
        }

        MouseX = ((float) newX) + MouseOffsetX;
        MouseY = ((float) newY) + MouseOffsetY;

        if(lockMouse) {
            MouseDeltaX = (float) (newX - (Renderer.getInstance().getWIDTH()/2));
            MouseDeltaY = (float) (newY - (Renderer.getInstance().getHEIGHT()/2));

            GLFW.glfwSetCursorPos(Reactor.WINDOW_POINTER, Renderer.getWIDTH() / 2, Renderer.getHEIGHT() / 2);
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
//        return new Vector2f(MouseX, (Renderer.getInstance().getHEIGHT() * ScreenScaleY) - MouseY);
        return new Vector2f(MouseX, MouseY);
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

    public void addButtonCallback(Callback callback){
        if(!this.buttonCallbacks.contains(callback)) {
            this.buttonCallbacks.add(callback);
        }
    }

    public void addButtonCallbackTiedToScene(Callback callback){
        addButtonCallback(callback);
        try {
            lock.lock();
            this.toRemoveOnSceneChange.put(callback, SceneManager.getInstance().getCurrentScene());
        }finally {
            lock.unlock();
        }
    }

    public void addMotionCallback(Callback callback){
        if(!this.motionCallbacks.contains(callback)) {
            this.motionCallbacks.add(callback);
        }
    }

    public void addMotionCallbackTiedToScene(Callback callback){
        addMotionCallback(callback);
        try {
            lock.lock();
            this.toRemoveOnSceneChange.put(callback, SceneManager.getInstance().getCurrentScene());
        }finally {
            lock.unlock();
        }
    }

    public void addScrollCallback(Callback callback){
        if(!scrollCallbacks.contains(callback)) {
            this.scrollCallbacks.add(callback);
        }
    }

    public void addScrollCallbackTiedToScene(Callback callback){
        addScrollCallback(callback);
        try {
            lock.lock();
            this.toRemoveOnSceneChange.put(callback, SceneManager.getInstance().getCurrentScene());
        }finally {
            lock.unlock();
        }
    }

    public void removeCallback(Callback callback) {
        if(buttonCallbacks.contains(callback)){
            buttonCallbacks.remove(callback);
        }
        if(scrollCallbacks.contains(callback)){
            scrollCallbacks.remove(callback);
        }
        if(motionCallbacks.contains(callback)){
            motionCallbacks.remove(callback);
        }
        if(toRemoveOnSceneChange.containsKey(callback)) {
            toRemoveOnSceneChange.remove(callback);
        }
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
        //If our mouse has been locked, lets move the mouse to the default position and reset the mouse ray
        if(!lockMouse){
            this.tick(0);
            MouseDeltaX = 0;
            MouseDeltaY = 0;
        }

        //No logic yet, will always lock mouse
        lockMouse = true;

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

    public boolean isMousePressed(int mouseKey){
        if(mouseKeys.containsKey(mouseKey)){
            return mouseKeys.get(mouseKey);
        }
        return false;
    }

    /**
     * @param pos Position the ray is cast from in 3D worldspace.
     * @param dir Direction the ray is looking from the {pos} point into the world.
     * @param planeOrigin The origin point for the plane that this ray is being cast towards.
     * @param planeNormal The normal vector tangent to the surface of the plane.
     * @return Vector3f representing the point in space that is hit.
     */
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

    public static Vector3f rayHitsAABB(Vector3f pos, Vector3f dir, Vector3f[] aabb) {
        return rayHitsAABB(pos, dir, aabb[0], aabb[1]);
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
            return new Vector3f(dir).mul(new Vector3f(result.x)).add(pos);
        }else{
            return null;
        }
    }

    public static Vector3f rayHitsEntity(Vector3f pos, Vector3f dir, Entity entity){
//        Vector3f broadPhase = rayHitsAABB(pos, dir, entity.getAABB());
//        if(broadPhase != null){

            System.out.println("Narrow phase");

            //Invert Pos
            pos = new Vector3f(pos).mul(-1);

            float length = 0;

            float epsilon = 0.1f;

            Model model = entity.getModel();
            float[] faceData = model.getHandshake().getAttributeRaw("vPosition");

            Vector4f p1 = new Vector4f();
            Vector4f p2 = new Vector4f();
            Vector4f p3 = new Vector4f();

            for(int i = 0; i < faceData.length / 9; i++){
                p1.x = faceData[i * 9 + 0]; // P1 x
                p1.y = faceData[i * 9 + 1]; // P1 y
                p1.z = faceData[i * 9 + 2]; // P1 z
                p1.w = 1.0f;

                p2.x = faceData[i * 9 + 3]; // P2 x
                p2.y = faceData[i * 9 + 4]; // P2 y
                p2.z = faceData[i * 9 + 5]; // P2 z
                p2.w = 1.0f;

                p3.x = faceData[i * 9 + 6]; // P3 x
                p3.y = faceData[i * 9 + 7]; // P3 y
                p3.z = faceData[i * 9 + 8]; // P3 z
                p3.w = 1.0f;

                //Transform
                p1 = p1.mul(entity.getTransform());
                p2 = p2.mul(entity.getTransform());
                p3 = p3.mul(entity.getTransform());

                length = Intersectionf.intersectRayTriangle(
                    pos.x, pos.y, pos.z,
                    dir.x, dir.y, dir.z,
                    p1.x, p1.y, p1.z,
                    p2.x, p2.y, p2.z,
                    p3.x, p3.y, p3.z
                , epsilon);

                if(length > 0){
                    // point(t) = origin + t * dir of the point of intersection.
                    return new Vector3f(dir).mul(length).add(pos);
                }
            }
//        }
        return null;
    }

    public float getScrollDeltaX() {
        return scrollDeltaX;
    }

    public float getScrollDeltaY() {
        return scrollDeltaY;
    }

    public void onSceneChange(Scene newScene){
        try {
            lock.lock();
            for (Callback callback : new LinkedList<Callback>(toRemoveOnSceneChange.keySet())) {
                if (!toRemoveOnSceneChange.get(callback).equals(newScene)) {
                    removeCallback(callback);
                }
            }
        }finally {
            lock.unlock();
        }
    }
}
