package lighting;

import entity.component.Attribute;
import graphics.renderer.Renderer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PointLight extends Light {

    public PointLight(){
        addAttribute(new Attribute<Float>("brightness", (float) 1));
    }

    @Override
    public void update(double delta){

    }

    @Override
    public void renderInEditor(boolean selected){
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(1, 0, 0), 32, (Vector3f) this.getAttribute("color").getData());
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 1, 0), 32, (Vector3f) this.getAttribute("color").getData());
        Renderer.getInstance().drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 0, 1), 32, (Vector3f) this.getAttribute("color").getData());
    }

}
