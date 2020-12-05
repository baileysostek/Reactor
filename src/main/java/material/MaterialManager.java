package material;

import entity.Entity;
import graphics.renderer.Renderer;
import models.ModelManager;

import java.util.HashMap;

public class MaterialManager {

    private static MaterialManager materialManager;

    private static HashMap<Integer, Material> materials = new HashMap<>();

    private static Material defaultMaterial;

    private Entity preview;

    private MaterialManager(){
        preview = new Entity();
        preview.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
    }

    public void addMaterial(Material material){

    }

    public int generatePreview(Material material){
        preview.setMaterial(material);
        return Renderer.getInstance().generateRenderedPreview(preview);
    }

    public static void initialize(){
        if(materialManager == null){
            materialManager = new MaterialManager();
            defaultMaterial = new Material();
        }
    }

    public Material getDefaultMaterial(){
        return defaultMaterial;
    }

    public static MaterialManager getInstance(){
        return materialManager;
    }

}
