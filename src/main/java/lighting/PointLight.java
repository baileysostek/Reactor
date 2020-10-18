package lighting;

import camera.CameraManager;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PointLight extends Light {

    Matrix4f transform = new Matrix4f();

    public PointLight(){

    }

    @Override
    public void update(double delta){
        transform = new Matrix4f(CameraManager.getInstance().getActiveCamera().getTransformationMatrix());
        super.setRotation(transform.getNormalizedRotation(new Quaternionf()));
    }

    @Override
    public void renderInEditor(boolean selected){
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(1, 0, 0), 32, (Vector3f) this.getAttribute("color").getData());
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 1, 0), 32, (Vector3f) this.getAttribute("color").getData());
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 0, 1), 32, (Vector3f) this.getAttribute("color").getData());
    }

}
