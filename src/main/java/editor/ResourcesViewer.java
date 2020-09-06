package editor;

import camera.CameraManager;
import editor.components.UIComponet;
import engine.FraudTek;
import entity.Entity;
import entity.EntityManager;
import entity.component.Attribute;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import imgui.ImGui;
import imgui.enums.ImGuiTreeNodeFlags;
import input.MousePicker;
import models.ModelManager;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.system.MemoryUtil;
import util.Callback;
import util.FileObject;

import java.util.LinkedList;

public class ResourcesViewer extends UIComponet {
    //Represents all resources
    FileObject resources;

    //These are selected files and stuff;
    private FileObject draggedFile = null;

    private boolean IS_DRAGGING_FILE = false;

    //Mouse Callback
    private Callback dropFileInWorld;

    public ResourcesViewer(){
        resources = new FileObject("");

        //GLFW callback for dropping an item in the world
        //Set the GLFW drop callback
        //Callback stuff
        GLFW.glfwSetDropCallback(FraudTek.WINDOW_POINTER, new GLFWDropCallback() {
            @Override
            public void invoke(long window, int count, long names) {
            PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
            for (int i = 0; i < count; i++) {
                String name = MemoryUtil.memUTF8(charPointers.get(i));
                System.err.println(name); // <- test: print out the path
            }
            }
        });


        //Genreate mouse callback for when we release the mouse
        dropFileInWorld = new Callback() {
            @Override
            public Object callback(Object... objects) {
                int button = (int) objects[0];
                int action = (int) objects[1];

                //Drop an entity in the world
                //If we have been dragging a file
                if(isDraggingFile()) {
                    if(action == GLFW.GLFW_RELEASE) {
                        //Raycast for pos
                        //TODO maybe change?
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()).sub(CameraManager.getInstance().getActiveCamera().getOffset()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

                        if(pos == null){
                            pos = new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition());
                        }

                        if(pos != null) {
                            switch (draggedFile.getFileExtension()) {
                                case (".tek"): {
                                    //If the file is a TEK file
                                    if(draggedFile.getRelativePath().contains("/models/")) {
                                        String filename = draggedFile.getRelativePath().replace("/models/", "");
                                        Entity newEntity = new Entity();
                                        newEntity.setModel(ModelManager.getInstance().loadModel(filename));
                                        newEntity.setPosition(pos);
                                        newEntity.addAttribute(new Attribute<Integer>("zIndex", 1));
                                        newEntity.addAttribute(new Attribute<String>("name", filename));
                                        EntityManager.getInstance().addEntity(newEntity);
                                        break;
                                    }else{
                                        Entity newEntity = new Entity(draggedFile.getRelativePath());
                                        newEntity.setPosition(pos);
                                        EntityManager.getInstance().addEntity(newEntity);
                                        break;
                                    }
                                }
                                case (".png"): {
                                    Entity newEntity = new Entity();
                                    String filename = draggedFile.getRelativePath().replace("/textures/", "");
                                    newEntity.setModel(ModelManager.getInstance().loadModel("quad.tek"));
                                    newEntity.setTexture(SpriteBinder.getInstance().load(filename));
                                    newEntity.setPosition(pos);
                                    newEntity.addAttribute(new Attribute<Integer>("zIndex", 1));
                                    newEntity.addAttribute(new Attribute<String>("name", filename));
                                    EntityManager.getInstance().addEntity(newEntity);
                                    break;
                                }
                                case (".obj"): {
                                    String filename = draggedFile.getRelativePath().replace("/models/", "");
                                    Entity newEntity = new Entity();
                                    newEntity.setModel(ModelManager.getInstance().loadModel(filename));
//                                    newEntity.setTexture(SpriteBinder.getInstance().load(draggedFile.getRelativePath().replace("/textures/", "")));
                                    newEntity.setPosition(pos);
                                    newEntity.addAttribute(new Attribute<Integer>("zIndex", 1));
                                    newEntity.addAttribute(new Attribute<String>("name", filename));
                                    EntityManager.getInstance().addEntity(newEntity);
                                    break;
                                }
                            }
                        }
                    }
                }
                return null;
            }
        };
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

    public void getTexture(){

    }

    @Override
    public void self_render() {
        //Start by saying that we are not dragging a file
        IS_DRAGGING_FILE = false;
        draggedFile = null;

        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        renderFileObject(resources);
        ImGui.endChild();
    }

    private void renderFileObject(FileObject object){
        if(object.isDirectory()){
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
            if(ImGui.collapsingHeader(object.getName(), nodeFlags_attributes)) {
                ImGui.indent();
                for (FileObject fileObject : new LinkedList<FileObject>(object.getChildren())) {
                    renderFileObject(fileObject);
                }
                ImGui.unindent();
            }
        }else{
            ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), 16);
            ImGui.selectable(object.getName());
            if(ImGui.beginDragDropSource()){
                //Set properties
                IS_DRAGGING_FILE = true;
                draggedFile = object;

                //Render Tooltip
                ImGui.setDragDropPayload(object.getFileExtension(), new byte[]{1}, 1);
                ImGui.beginTooltip();
                ImGui.text(object.getName());
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
    public void onShutdown(){
        this.closeListeners(resources);
    }

    public void closeListeners(FileObject object){
        if(object.isDirectory()) {
            object.onShutdown();
            for (FileObject child : object.getChildren()) {
                closeListeners(child);
            }
        }
    }

    @Override
    public String getName(){
        return "Resources";
    }

    public boolean isDraggingFile(){
        return this.IS_DRAGGING_FILE;
    }

//    public boolean getSelectedFile() {
//
//    }

    public FileObject getDraggedFile() {
        return this.draggedFile;
    }
}

enum ResourceType{
    IMAGE,
    MODEL,
    SCRIPT,
    SOUND,
    ENTITY
}