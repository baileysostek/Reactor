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
        if(this.hasAttribute("updateInEditor")) {
            this.getAttribute("updateInEditor").setData(true);
        }
        super.onAdd();
    }
}
