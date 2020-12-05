package editor;

import camera.CameraManager;
import editor.components.UIComponet;
import entity.Entity;
import entity.EntityEditor;
import entity.EntityManager;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import graphics.sprite.Sprite;
import graphics.sprite.SpriteBinder;
import imgui.ImGui;
import imgui.enums.ImGuiDragDropFlags;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import input.MousePicker;
import lighting.DirectionalLight;
import lighting.PointLight;
import material.Material;
import models.Model;
import models.ModelManager;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import particle.ParticleSystem;
import util.Callback;

import java.util.HashMap;
import java.util.LinkedList;

public class EntityRegistry extends UIComponet {

    //The entity we are interacting with
    private Entity entity;
    private Entity dragged;

    //Editor resources
    EntityEditor editor;

    private Callback dropFileInWorld;

    private boolean isDraggingTemplate = false;

    private static HashMap<String, LinkedList<Entity>> registeredEntities = new HashMap<>();
    private final HashMap<Entity, Integer> snapshots = new HashMap<Entity, Integer>();

    private final int svgTest;

    //Callback hooks
    private LinkedList<Callback> onTryPlaceInWorld = new LinkedList<>();

    private int scale = 64;

    public EntityRegistry(EntityEditor editor){

        svgTest = SpriteBinder.getInstance().loadSVG("engine/svg/radiation.svg", 1, 1, 96);

        this.editor = editor;
        Entity whiteCube = new Entity();
        whiteCube.setModel(ModelManager.getInstance().loadModel("cube2.obj").getFirst());
        whiteCube.getAttribute("name").setData("Cube");
        whiteCube.setMaterial(new Material(SpriteBinder.getInstance().load("water.png")));
        addEntity("Geometry", whiteCube);

        Entity sphere = new Entity();
        sphere.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
        sphere.getAttribute("name").setData("Sphere");
        addEntity("Geometry", sphere);

        Entity quad = new Entity();
        quad.setModel(ModelManager.getInstance().loadModel("quad.obj").getFirst());
        quad.getAttribute("name").setData("Quad");
        addEntity("Geometry", quad);

//        Entity dragon = new Entity();
//        dragon.setModel(ModelManager.getInstance().loadModel("dragon.obj").getFirst());
//        dragon.getAttribute("name").setData("Dragon");
//        addEntity("Geometry", dragon);
//
//        Entity garden = new Entity();
//        garden.setModel(ModelManager.getInstance().loadModel("garden.obj").getFirst());
//        garden.getAttribute("name").setData("Garden");
//        garden.setTexture(SpriteBinder.getInstance().load("Garden_BaseColor.png"));
//        addEntity("Geometry", garden);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.getAttribute("name").setData("Directional Light");
        addEntity("Lighting", directionalLight);

        PointLight pointLight = new PointLight();
        pointLight.getAttribute("name").setData("Point Light");
        addEntity("Lighting", pointLight);

        ParticleSystem system = new ParticleSystem();
        addEntity("Particles", system);


        //Genreate mouse callback for when we release the mouse
        dropFileInWorld = new Callback() {
            @Override
            public Object callback(Object... objects) {
                int button = (int) objects[0];
                int action = (int) objects[1];

                //Drop an entity in the world
                //If we have been dragging a file
                if(isDraggingTemplate) {
                    if(action == GLFW.GLFW_RELEASE) {
                        //Raycast for pos
                        //TODO maybe change?
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

                        if(pos == null){
                            pos = new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition());
                        }

                        if(pos != null) {
                            Entity clone = editor.cloneTarget(dragged).setPosition(pos);

                            //Raycast for hit entities.
                            LinkedList<Entity> hitEntities = EntityManager.getInstance().getHitEntities(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()));
                            for(Callback c : onTryPlaceInWorld){
                                //First param is the list of hit entities.
                                //We pass the object that we are trying to add as the second param
                                Object result = c.callback(clone, hitEntities);
                                if(result instanceof Boolean){
                                    boolean shouldBreak = (boolean)result;
                                    if(shouldBreak){
                                        return null;
                                    }
                                }
                            }

                            //Add too world
                            EntityManager.getInstance().addEntity(clone);
                        }
                    }
                }
                return null;
            }
        };
    }

    //Update the registry
    public void addEntity(String category, Entity entity){
        if(!registeredEntities.containsKey(category)){
            registeredEntities.put(category, new LinkedList<Entity>());
        }
        registeredEntities.get(category).addLast(entity);
        int textureID = Renderer.getInstance().generateRenderedPreview(entity);
        this.snapshots.put(entity, textureID);
    }



    @Override
    public void onAdd() {
        MousePicker.getInstance().addCallback(dropFileInWorld);
    }

    @Override
    public void onRemove() {
        MousePicker.getInstance().removeCallback(dropFileInWorld);
    }

    @Override
    public void self_update(double delta) {

    }

    @Override
    public void self_render() {
        isDraggingTemplate = false;
        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        for(String category : registeredEntities.keySet()){
            ImGui.text(category);
            ImGui.separator();
            renderEntity(registeredEntities.get(category));
        }
        ImGui.endChild();
    }

    public void renderEntity(LinkedList<Entity> entities){
        for(Entity templateEntity : entities) {
            //Drag container
            ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), scale);
            int selected = ImGuiSelectableFlags.AllowDoubleClick;

            int textureID = svgTest;
            if(snapshots.containsKey(templateEntity)){
                textureID = snapshots.get(templateEntity);
            }

            ImGui.image(textureID, scale, scale, 0, 1, 1, 0);
            ImGui.sameLine();

            if (ImGui.selectable(templateEntity.getName(), templateEntity.equals(this.entity), selected)) {
                this.entity = templateEntity;
                //                CameraManager.getInstance().getActiveCamera().setPosition(new Vector3f(e.getPosition()).mul(1, 0, 1).add(new Vector3f( CameraManager.getInstance().getActiveCamera().getPosition()).mul(0, 1, 0)));
            }
            if (ImGui.beginDragDropSource()) {
                dragged = templateEntity;
                isDraggingTemplate = true;

                ImGui.beginTooltip();
                ImGui.text(templateEntity.getName());
                ImGui.endTooltip();
                ImGui.endDragDropSource();
            }


            ImGui.endChildFrame();
        }
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName() {
        return "Entity Registry";
    }

    //Callback registries
    public void onTryPlaceInWorld(Callback c){
        this.onTryPlaceInWorld.addLast(c);
    }
}
