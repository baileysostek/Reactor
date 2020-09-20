package lighting;

import entity.Entity;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Light extends Entity {

    public Light(){
        //Default is white light.
        addAttribute(new Attribute("color", new Vector3f(1)));
        addAttribute(new Attribute("frustum", new Vector3f(10, 1, 7.5f)));
    }

    @Override
    public void onAdd() {
        this.getAttribute("updateInEditor").setData(true);
        super.onAdd();
    }

    @Override
    public void renderInEditor(boolean selected){
        Renderer.getInstance().drawArrow(new Vector3f(this.getPosition()), new Vector3f(0, 0, 0).sub(this.getPosition()).normalize().add(this.getPosition()), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, (Vector3f) this.getAttribute("color").getData());
    }


    public float[] getLightspaceTransform() {
        Matrix4f view = new Matrix4f().lookAt(new Vector3f(this.getPosition()), new Vector3f(0), new Vector3f(0, 1, 0));
        Vector3f frustum = (Vector3f) this.getAttribute("frustum").getData();
        return new Matrix4f().ortho(-frustum.x, frustum.x, -frustum.x,frustum.x, frustum.y, frustum.z).mul(view).get(new float[16]);
    }
}
