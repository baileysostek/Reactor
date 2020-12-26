package lighting;

import entity.Entity;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import org.joml.Vector3f;

public abstract class Light extends Entity {

    public Light(){
        //Default is white light.
        addAttribute(new Attribute("color", new Vector3f(1)).setType(EnumAttributeType.COLOR));
        addAttribute(new Attribute("frustum", new Vector3f(10, 1, 7.5f)));
        addAttribute(new Attribute("castsShadows", false));
        addAttribute(new Attribute<Float>("brightness", (float) 1));

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
        if(this.hasAttribute("textureID")) {
            this.getAttribute("textureID").setShouldBeSerialized(false).setVisible(true);
        }
        super.onAdd();
    }

    @Override
    public void onRemove() {
        //Add self to LightingManager
        LightingManager.getInstance().remove(this);
    }
}
