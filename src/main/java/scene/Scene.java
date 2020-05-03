package scene;


import math.Vector2f;

public abstract class Scene {

    public abstract void onLoad();
    public abstract void onUnload();
    public abstract void update(double delta);
    public abstract void render();

    //Input methods
    public abstract void onPress(Vector2f pos);
    public abstract void onMove(Vector2f delta);
    public abstract void onRelease(Vector2f pos);
}
