package lighting;

import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import entity.component.EnumAttributeType;
import org.joml.Vector3f;
import particle.ParticleSystem;

public abstract class Light extends Entity {

    Attribute<Vector3f> color        = new Attribute("color", new Vector3f(1)).setType(EnumAttributeType.COLOR);
    Attribute<Vector3f> frustum      = new Attribute("frustum", new Vector3f(10, 1, 7.5f));
    Attribute<Boolean>  castsShadows = new Attribute("castsShadows", false);
    Attribute<Float>    brightness   = new Attribute("brightness", (float) 10f);

    public Light(){
        //Default is white light.
        addAttribute(color);
        addAttribute(frustum);
        addAttribute(castsShadows);
        addAttribute(brightness);

        //Remove unneeded attributes
        removeAttribute("normalID");
        removeAttribute("metallicID");
        removeAttribute("roughnessID");
        removeAttribute("aoID");
    }

    public Vector3f getColor(){
        return (Vector3f) this.getAttribute("color").getData();
    }

    public float getBrightness(){
        return (float) this.getAttribute("brightness").getData();
    }

    @Override
    public void onAdd() {
        //Add self to LightingManager
        LightingManager.getInstance().add(this);

        super.setTexture(LightingManager.getInstance().getLightBulbSVG());

        if(this.hasAttribute("updateInEditor")) {
            this.getAttribute("updateInEditor").setVisible(false).setData(true);
        }
        if(this.hasAttribute("scale")) {
            this.getAttribute("scale").setVisible(false);
        }
        if(this.hasAttribute("rotation")) {
            this.getAttribute("rotation").setVisible(false);
        }
        if(this.hasAttribute("zIndex")) {
            this.getAttribute("zIndex").setVisible(false);
        }
        if(this.hasAttribute("autoScale")) {
            this.getAttribute("autoScale").setVisible(false);
        }
        if(this.hasAttribute("t_scale")) {
            this.getAttribute("t_scale").setVisible(false);
        }
//        if(this.hasAttribute("textureID")) {
//            this.getAttribute("textureID").setShouldBeSerialized(false).setVisible(true);
//        }
        super.onAdd();
    }

    @Override
    public void onRemove() {
        //Add self to LightingManager
        LightingManager.getInstance().remove(this);
    }

    @Override
    public JsonObject serialize(){
        return super.serialize();
    }

    @Override
    public Light deserialize(JsonObject data) {
        super.deserialize(data);

        color   = AttributeUtils.synchronizeWithParent(color  , this);
        frustum = AttributeUtils.synchronizeWithParent(frustum, this);
//        castsShadows  = AttributeUtils.synchronizeWithParent(castsShadows , this);
//        brightness  = AttributeUtils.synchronizeWithParent(brightness , this);

        return this;
    }
}
