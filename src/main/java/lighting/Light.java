package lighting;

import entity.Entity;
import entity.component.Attribute;
import entity.component.EnumAttributeType;
import graphics.sprite.Sprite;
import models.ModelManager;
import org.joml.Vector3f;
import org.joml.Vector4f;
import util.Callback;

public abstract class Light extends Entity {

    private Sprite sprite;

    public Light(){
        //Default is white light.
        Attribute color = new Attribute("color", new Vector3f(1)).setType(EnumAttributeType.COLOR);
        color.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                Vector3f outColor = (Vector3f) color.getData();
                sprite.setPixelColor(0, 0, new Vector4f(outColor, 1));
                sprite.flush();
                return null;
            }
        });
        addAttribute(color);
        addAttribute(new Attribute("frustum", new Vector3f(10, 1, 16f)));
        addAttribute(new Attribute("castsShadows", false));
        addAttribute(new Attribute<Float>("brightness", (float) 1).setType(EnumAttributeType.SLIDERS));

        //Remove unneeded attributes
        removeAttribute("normalID");
        removeAttribute("metallicID");
        removeAttribute("roughnessID");
        removeAttribute("aoID");

        sprite = new Sprite(1, 1);

        this.setTexture(sprite);

    }

    public Vector3f getColor(){
        return (Vector3f) this.getAttribute("color").getData();
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

    public float getBrightness(){
        return (float) this.getAttribute("brightness").getData();
    }
}
