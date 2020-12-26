package entity;

import editor.Editor;
import editor.components.UIComponet;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import imgui.ImGui;
import imgui.ImString;
import imgui.enums.ImGuiDragDropFlags;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import java.util.LinkedList;

public class WorldOutliner extends UIComponet {

    //The entity we are interacting with
    private Entity entity;

    private Entity dragged;

    //Editor resources
    EntityEditor editor;

    private final int SEARCH;

    public WorldOutliner(EntityEditor editor){

        SEARCH = SpriteBinder.getInstance().loadSVG("engine/svg/search.svg", 1, 1, 240f);

        this.editor = editor;
    }

    public void setTarget(Entity target){
        this.entity = target;
    }


    @Override
    public void onAdd() {
    }

    @Override
    public void onRemove() {
    }

    @Override
    public void self_update(double delta) {

    }

    public void getTexture(){

    }

    @Override
    public void self_render() {
        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        //Search / filter for a specific type of Entity
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), 32);
        ImGui.image(SEARCH, 32, 32);
        ImGui.sameLine();
        ImString filter = new ImString();
        ImGui.inputText("Search", filter);
        ImGui.endChildFrame();
        //Render all entities in the world.
        renderEntity(new LinkedList<Entity>(EntityManager.getInstance().getEntities()));
        ImGui.endChild();
    }

    public void renderEntity(LinkedList<Entity> entities){
        for(Entity e : entities){
            if(e.getParent() == null) {
                //No parent, therefore is parent and should render at this level
                renderEntity(e);
                if(ImGui.isItemHovered()){
                    DirectDraw.getInstance().drawAABB(e, new Vector3f(1));
                }
            }
        }
    }

    public void renderEntity(Entity parent){
        //Drag container
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), 16);
            int selected = ImGuiSelectableFlags.AllowDoubleClick;
            ImGui.image(parent.getTextureID(), 16, 16);
            ImGui.sameLine();

            if (ImGui.selectable(parent.getName(), parent.equals(this.entity), selected)) {
                this.entity = parent;
                this.editor.setTarget(parent);
    //                CameraManager.getInstance().getActiveCamera().setPosition(new Vector3f(e.getPosition()).mul(1, 0, 1).add(new Vector3f( CameraManager.getInstance().getActiveCamera().getPosition()).mul(0, 1, 0)));
            }
            if(ImGui.beginDragDropSource()){
                //Render Tooltip
                if(ImGui.setDragDropPayload("ENTITY", new byte[]{1}, 1)){
                    System.out.println("Drag:"+parent.getName());
                    dragged = parent;
                }
                ImGui.beginTooltip();
                ImGui.text(parent.getName());
                ImGui.endTooltip();
                ImGui.endDragDropSource();
            }

            if(ImGui.beginDragDropTarget()){
                int target_flags = ImGuiDragDropFlags.None;
                byte[] data = ImGui.acceptDragDropPayload("ENTITY", target_flags);
                if (data != null){
                    if(dragged != null) {
                        System.out.println("Drop:" + dragged.getName());
                        LinkedList<Entity> grandChildren = EntityManager.getInstance().getEntitiesChildren(dragged);
                        if(!grandChildren.contains(parent)) {
                            dragged.translate(new Vector3f(parent.getPosition()).mul(-1));
                            dragged.setParent(parent);
                        }
                        dragged = null;
                    }
                }

                ImGui.endDragDropTarget();
            }
        ImGui.endChildFrame();


        //Render Children
        LinkedList<Entity> children = EntityManager.getInstance().getEntitiesChildren(parent);
        if(children.size() > 0) {
            //Child so tab in one and make drop down
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
//            ImGui.sameLine();
            ImGui.indent();
            if (ImGui.collapsingHeader("["+children.size()+"] children", nodeFlags_attributes)) {
                for (Entity child : children) {
                    renderEntity(child);
                    if(ImGui.isItemHovered()){
                        DirectDraw.getInstance().drawAABB(child, new Vector3f(1));
                    }
                }
            }
            ImGui.unindent();
        }
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Scene";
    }
}
