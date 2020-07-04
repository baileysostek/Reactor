package editor;

import camera.CameraManager;
import editor.components.UIComponet;
import entity.Entity;
import entity.EntityManager;
import imgui.ImGui;
import imgui.enums.ImGuiTreeNodeFlags;
import input.MousePicker;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import util.Callback;
import util.FileObject;

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
                        Vector3f pos = MousePicker.getInstance().rayHitsPlane(new Vector3f(CameraManager.getInstance().getActiveCamera().getPosition()), new Vector3f(MousePicker.getInstance().getRay()), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

                        //If the file is a TEK file
                        Entity newEntity = new Entity(draggedFile.getRelativePath());
                        newEntity.setPosition(pos);
                        EntityManager.getInstance().addEntity(newEntity);
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
                for (FileObject fileObject : object.getChildren()) {
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
                ImGui.setDragDropPayload("TEK", new byte[]{1}, 1);
                ImGui.beginTooltip();
                ImGui.text(object.getName() + "Drag?");
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