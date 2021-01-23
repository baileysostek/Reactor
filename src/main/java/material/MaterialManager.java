package material;

import entity.Entity;
import graphics.renderer.Renderer;
import graphics.renderer.ShaderManager;
import graphics.sprite.Sprite;
import models.ModelManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class MaterialManager {

    private static MaterialManager materialManager;

    private static HashMap<String, Material> materials = new HashMap<>();
    private static HashMap<Material, Entity> previews  = new HashMap<>();

    private static Material defaultMaterial;

    private static int count = 0;

    private MaterialManager(){

    }

//    public Material generateMaterial(){
//
//    }
//
//    public Material generateMaterial(Material clone){
//
//    }
//
    //New mat from textureID
    public Material generateMaterial(int textureID){
        Material material = new Material(textureID);

        addMaterial(material);

        return material;
    }

    //New material from Sprite. As the sprite updates so will the material because it references the textureID.
    public Material generateMaterial(Sprite sprite){
        Material material = new Material(sprite.getTextureID());

        addMaterial(material);

        return material;
    }

    public Material generateMaterial(Material mat){
        Material material = new Material(mat);

        addMaterial(material);

        return material;
    }

    public Material getMaterial(String name){
        if(materials.containsKey(name)){
            return materials.get(name);
        }else{
            return defaultMaterial;
        }
    }

    private void addMaterial(Material material){
        materials.put(material.getName(), material);
    }

    public int generatePreview(Material material){
        //Maybe optimise this to not create a new entity every time an entity wants to be generate.
        Entity preview;

        //Check if we have buffered this material texture yet. If we have not, add it to the buffer.
        if(previews.containsKey(material)){
            preview = previews.get(material);
        }else{
            //Create new entity
            preview = new Entity();
            //Set material texture to sphere
            preview.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
            //Set the material for this new entity to our material.
            preview.setMaterial(material);
            //Add this material to the hashmap so we can redraw the material without creating a new FBO or entity in the future.
            previews.put(material, preview);
        }

        //Ask the renderer to draw our material nicely.
        return Renderer.getInstance().generateRenderedPreview(preview);
    }

    public void updateMapping(String oldName, String newName){
        if(materials.containsKey(oldName)){
            Material material = materials.get(oldName);
            materials.remove(material);
            materials.put(newName, material);
        }else{
            //TODO add log message. from Log manager.
            System.err.println("[Material Manager] Tried to update material, however a mapping for this material does not exist.");
        }
    }

    public void generateAtlas(Collection<Entity> entities){
        LinkedHashSet materials = new LinkedHashSet();
        for(Entity e : entities){
            materials.add(e.getMaterial());
        }
        System.out.println("Materials in Batch:");
    }

    //Managed Variables
    public int getNextID() {
        return count++;
    }

    //Defaults
    public Material getDefaultMaterial(){
        return defaultMaterial;
    }

    //Singleton things
    public static void initialize(){
        if(materialManager == null){
            materialManager = new MaterialManager();
            defaultMaterial = new Material();

            defaultMaterial.setShader(ShaderManager.getInstance().getDefaultShader());

        }
    }

    public static MaterialManager getInstance(){
        return materialManager;
    }

    public Collection<Material> getAllMaterials() {
        return materials.values();
    }
}
