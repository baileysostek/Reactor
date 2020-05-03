package input;

import camera.CameraManager;
import engine.Engine;
import engine.FraudTek;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import math.Maths;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import util.Callback;

import java.nio.DoubleBuffer;
import java.util.LinkedList;

public class MousePicker extends Engine {

    private static MousePicker mousePicker;
    private Matrix4f viewMatrix;
    private Vector3f ray = new Vector3f();

    private float MouseX = 0;
    private float MouseY = 0;

    public LinkedList<Callback> callbacks = new LinkedList<Callback>();

    public boolean mousePressed = false;

    private MousePicker(){
        viewMatrix = Maths.createViewMatrix(CameraManager.getInstance().getActiveCamera());
        GLFW.glfwSetMouseButtonCallback(FraudTek.WINDOW_POINTER, (long window, int button, int action, int mods) -> {
            if(action == GLFW.GLFW_PRESS){
                if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                    mousePressed = true;
                    for(Callback c : callbacks){
                        c.callback();
                    }
                }
            }
            if(action == GLFW.GLFW_RELEASE){
                if(button == GLFW.GLFW_MOUSE_BUTTON_1){
                    mousePressed = false;
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

        MouseX = (float) newX;
        MouseY = (float) newY;

        double deltaX = newX - (Renderer.getInstance().getWIDTH()/2);
        double deltaY = newY - (Renderer.getInstance().getHEIGHT()/2);

        MouseY *= -1;
        MouseY += Renderer.getInstance().getHEIGHT();

        this.viewMatrix = Maths.createViewMatrix(CameraManager.getInstance().getActiveCamera());
        ray = calculateMouseRay();
    }

    public Vector3f calculateMouseRay() {
        float mouseX = MouseX;
        float mouseY = MouseY;
        Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        Vector3f worldRay = toWorldCoords(eyeCoords);
        return worldRay;
    }

    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = new Matrix4f();
        invertedView.set(viewMatrix);
        invertedView.invert(invertedView);
        Vector4f rayWorld = new Vector4f();
        invertedView.transform(eyeCoords, rayWorld);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalize(mouseRay);
        return mouseRay;
    }

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = new Matrix4f();
        Renderer.getInstance().getProjectionMatrix().invert(invertedProjection);
        Vector4f eyeCoords = new Vector4f();
        invertedProjection.transformAffine(clipCoords, eyeCoords);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2.0f * mouseX) / Renderer.getInstance().getWIDTH() - 1f;
        float y = (2.0f * mouseY) / Renderer.getInstance().getHEIGHT() - 1f;
        return new Vector2f(x, y);
    }

    public static MousePicker getInstance(){
        if(mousePicker == null){
            mousePicker = new MousePicker();
        }

        return mousePicker;
    }

    public Vector3f getRay(){
        return this.ray;
    }

    public Vector2f getScreenCoords(){
        return new Vector2f(MouseX, Renderer.getInstance().getHEIGHT()-MouseY);
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

}
