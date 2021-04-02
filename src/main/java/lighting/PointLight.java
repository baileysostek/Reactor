package lighting;

import entity.component.Attribute;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PointLight extends Light {

    public PointLight(){

    }

    @Override
    public void update(double delta){

    }

    @Override
    public void renderInEditor(boolean selected){
        DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(1, 0, 0), 32, (Vector3f) this.getAttribute("color").getData());
        DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 1, 0), 32, (Vector3f) this.getAttribute("color").getData());
        DirectDraw.getInstance().Draw3D.drawRing(this.getPosition(), new Vector2f((Float) this.getAttribute("brightness").getData()), new Vector3f(0, 0, 1), 32, (Vector3f) this.getAttribute("color").getData());

        DirectDraw.getInstance().Draw3D.drawBillboard(new Vector3f(this.getPosition()), new Vector2f(1), (Vector3f) this.getAttribute("color").getData(), LightingManager.getInstance().getLightBulbSVG());
    }

}
