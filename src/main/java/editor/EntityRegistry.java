package editor;

import camera.CameraManager;
import editor.components.UIComponet;
import entity.Entity;
import entity.EntityEditor;
import entity.EntityManager;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import graphics.sprite.Colors;
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
import material.MaterialManager;
import models.Model;
import models.ModelManager;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import particle.ParticleSystem;
import reflection.Probe;
import skybox.Skybox;
import sound.SoundEmitter;
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
    private LinkedList<Callback> onDragStartCallbacks   = new LinkedList<>();
    private LinkedList<Callback> onTryPlaceInWorld      = new LinkedList<>();

    private boolean DRAG_STOP = true;

    private int scale = 64;

    protected EntityRegistry(EntityEditor editor){

        svgTest = SpriteBinder.getInstance().loadSVG("engine/svg/radiation.svg", 1, 1, 96);

        this.editor = editor;
        Entity whiteCube = new Entity();
        whiteCube.setModel(ModelManager.getInstance().loadModel("cube2.obj").getFirst());
        whiteCube.getAttribute("name").setData("Cube");
        Material mat1 = MaterialManager.getInstance().generateMaterial(SpriteBinder.getInstance().load("white.png"));
        mat1.setShader("pbr");
        mat1.setNormalID(SpriteBinder.getInstance().load("white_normal.png").getTextureID());
        whiteCube.setMaterial(mat1);
        addEntity("Geometry", whiteCube);

        Entity sphere = new Entity();
        sphere.setModel(ModelManager.getInstance().loadModel("sphere_smooth.obj").getFirst());
        sphere.getAttribute("name").setData("Sphere");
        Material mat = MaterialManager.getInstance().generateMaterial(MaterialManager.getInstance().getDefaultMaterial());
        mat.setShader("normal");
        sphere.setMaterial(mat);

        addEntity("Geometry", sphere);

        Entity quad = new Entity();
        quad.setModel(ModelManager.getInstance().loadModel("quad.obj").getFirst());
        quad.getAttribute("name").setData("Quad");
        quad.setMaterial(mat1);
        addEntity("Geometry", quad);

        Sprite sprite = new Sprite(1,1);
        sprite.setPixelColor(0,0, Colors.RED);
        sprite.flush();
        Material matTest2 = MaterialManager.getInstance().generateMaterial(sprite);

        Sprite metallic = new Sprite(1,1);
        metallic.setPixelColor(0,0, new Vector4f(new Vector3f(0.125f), 1));
        metallic.flush();

        matTest2.setMetallicID(metallic.getTextureID());
        matTest2.setRoughnessID(metallic.getTextureID());

        matTest2.setShader("pbr");

        Entity animatedMode2 = new Entity();
        animatedMode2.setModel(ModelManager.getInstance().loadModel("Pilot_LP_Animated.fbx"));
        animatedMode2.addAttribute(new Attribute("updateInEditor", true));
        animatedMode2.setMaterial(matTest2);
        addEntity("Animation", animatedMode2);

//        Entity fox = new Entity();
//        fox.setModel(ModelManager.getInstance().loadModel("2.0/Fox/glTF/Fox.gltf"));
//        fox.addAttribute(new Attribute("updateInEditor", true));
//        fox.setMaterial(matTest2);
//        fox.setScale(0.1f);
//        addEntity("Animation", fox);


//        Entity dragon = new Entity();
//        dragon.setModel(ModelManager.getInstance().loadModel("dragon.obj").getFirst());
//        dragon.getAttribute("name").setData("Dragon");
//        Material mat2 = MaterialManager.getInstance().generateMaterial(MaterialManager.getInstance().getDefaultMaterial());
//        mat2.setShader("main");
//        dragon.setMaterial(mat2);
//        addEntity("Geometry", dragon);

//        Entity cerberus = new Entity();
//        cerberus.setModel(ModelManager.getInstance().loadModel("Cerberus_by_Andrew_Maximov/Cerberus_LP.FBX").getFirst());rende
//        cerberus.setScale(0.1f);
//        cerberus.setRotation(new Vector3f(-90, 0, 0));
//        cerberus.getAttribute("name").setData("Cerberus");
//        Material mat3 = MaterialManager.getInstance().generateMaterial(MaterialManager.getInstance().getDefaultMaterial());
//        mat3.setAlbedoID(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_A.png").getTextureID());
//        mat3.setNormalID(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_N.png").getTextureID());
//        mat3.setMetallicID(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_M.png").getTextureID());
//        mat3.setRoughnessID(SpriteBinder.getInstance().load("Cerberus_by_Andrew_Maximov/Textures/Cerberus_R.png").getTextureID());
//        mat3.setShader("pbr");
//        cerberus.setMaterial(mat3);
//        addEntity("PBR", cerberus);

//        Entity mask = new Entity();
//        mask.setModel(ModelManager.getInstance().loadModel("HelmetPresentationLightMap.fbx").getFirst());
//        mask.getAttribute("name").setData("Mask");
//        Material mat4 = MaterialManager.getInstance().generateMaterial(MaterialManager.getInstance().getDefaultMaterial());
//        mat4.setAlbedoID(SpriteBinder.getInstance().load("Mask/BaseColor.png").getTextureID());
//        mat4.setNormalID(SpriteBinder.getInstance().load("Mask/NormalMap.png").getTextureID());
//        mat4.setMetallicID(SpriteBinder.getInstance().load("Mask/Metalness.png").getTextureID());
//        mat4.setRoughnessID(SpriteBinder.getInstance().load("Maks/Roughness.png").getTextureID());
//        mat4.setAmbientOcclusionID(SpriteBinder.getInstance().load("Mask/AOMap.png").getTextureID());
//        mat4.setShader("pbr");
//        mask.setMaterial(mat4);
//        addEntity("PBR", mask);

        Material brick = MaterialManager.getInstance().generateMaterial(SpriteBinder.getInstance().load("normal.png"));
        brick.setNormalID(SpriteBinder.getInstance().load("white_normal.png").getTextureID());

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

        Skybox skybox = new Skybox();
        skybox.getAttribute("name").setData("Skybox");
        addEntity("Skybox", skybox);

        Probe probe = new Probe();
        probe.getAttribute("name").setData("Reflection Probe");
        addEntity("Skybox", probe);

        Entity soundEmitter = new SoundEmitter();
        soundEmitter.getAttribute("name").setData("Sound Emitter");
        addEntity("Sound", soundEmitter);

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

                DRAG_STOP = true;

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
        //TODO fix
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
    public void selfRender() {
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

                if(DRAG_STOP) {
                    DRAG_STOP = false;
                    for (Callback c : onDragStartCallbacks) {
                        c.callback(dragged);
                    }
                }

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
    public void onDragStart(Callback c){
        this.onDragStartCallbacks.addLast(c);
    }
}
