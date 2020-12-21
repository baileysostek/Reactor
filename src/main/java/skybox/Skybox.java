package skybox;

import com.google.gson.JsonObject;
import entity.Entity;
import entity.component.Attribute;
import entity.component.AttributeUtils;
import entity.component.EnumAttributeType;
import graphics.sprite.SpriteBinder;
import org.joml.Vector3f;
import org.joml.Vector4f;
import particle.ParticleSystem;
import util.Callback;

public class Skybox extends Entity {

    Attribute<String> texture;
    Attribute<Vector3f> color;

    private int skyBoxTexture = 0;
    private int colorTexture;

    public Skybox(){

        colorTexture = SpriteBinder.getInstance().generateCubeMap(new Vector4f(0, 0, 0, 1));

        texture = new Attribute<String>("skyboxTexture", "sky1");

        texture.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                skyBoxTexture = SpriteBinder.getInstance().loadCubeMap(texture.getData());
                return null;
            }
        });

        color = new Attribute<Vector3f>("skyboxColor", new Vector3f(0)).setType(EnumAttributeType.COLOR);

        color.subscribe(new Callback() {
            @Override
            public Object callback(Object... objects) {
                SpriteBinder.getInstance().updateCubeMap(colorTexture, new Vector4f(color.getData(), 1));
                skyBoxTexture = colorTexture;
                return null;
            }
        });


        skyBoxTexture = SpriteBinder.getInstance().loadCubeMap(texture.getData());

        super.addAttribute(texture);
        super.addAttribute(color);

        super.getAttribute("materials").setVisible(false);
    }

    @Override
    public void onAdd(){
        SkyboxManager.getInstance().addSkybox(this);
    }

    @Override
    public void onRemove(){
        SkyboxManager.getInstance().remove(this);
    }


    public int getSkyboxTexture(){
        return this.skyBoxTexture;
    }

    @Override
    public Skybox deserialize(JsonObject data) {
        super.deserialize(data);

        color     = AttributeUtils.synchronizeWithParent(color  , this);
        texture   = AttributeUtils.synchronizeWithParent(texture  , this);

        return this;
    }
}
