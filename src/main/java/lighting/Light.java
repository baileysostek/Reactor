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
    }

    @Override
    public void onAdd() {
        if(this.hasAttribute("updateInEditor")) {
            this.getAttribute("updateInEditor").setVisible(false).setData(true);
        }
        if(this.hasAttribute("scale")) {
            this.getAttribute("scale").setVisible(false);
        }
        if(this.hasAttribute("textureID")) {
            this.getAttribute("textureID").setVisible(false);
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
        super.onAdd();
    }
}
