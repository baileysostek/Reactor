package entity;

import editor.Editor;
import editor.components.UIComponet;
import entity.component.Collision;
import graphics.renderer.DirectDraw;
import graphics.renderer.Renderer;
import graphics.sprite.SpriteBinder;
import imgui.ImGui;
import imgui.ImString;
import imgui.ImVec2;
import imgui.enums.ImGuiDragDropFlags;
import imgui.enums.ImGuiInputTextFlags;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import material.Material;
import material.MaterialManager;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public class WorldOutliner extends UIComponet {

    //The entity we are interacting with
    private Entity entity;

    private Entity dragged;

    //Editor resources
    EntityEditor editor;

    private final int SEARCH;

    private String filterString = "";

    private static int count = 0;

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
    public void selfRender() {
        //World Outliner, list of all entities in the world
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight());
        //Search / filter for a specific type of Entity
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), 32);
        ImGui.image(SEARCH, 16, 16);
        ImGui.sameLine();

        ImString value = new ImString(filterString);

        int flags = ImGuiInputTextFlags.CallbackResize | ImGuiInputTextFlags.AutoSelectAll ;

        ImGui.pushItemWidth(ImGui.getColumnWidth() - 3);
        ImGui.inputText(Editor.getInstance().getNextID()+"", value, flags);
        ImGui.popItemWidth();

        filterString = value.get();

        ImGui.endChildFrame();
        //Render all entities in the world.
        renderEntity(new LinkedList<Entity>(EntityManager.getInstance().getEntities()));
        ImGui.endChild();
    }

    public void renderEntity(LinkedList<Entity> entities){
        count = 0;
        for(Entity e : entities){
            if(e.getParent() == null) {
                //No parent, therefore is parent and should render at this level
                renderEntity(e);
                if(ImGui.isItemHovered()){
                    DirectDraw.getInstance().Draw3D.drawAABB(e, new Vector3f(1));
                }
            }
        }
    }

    public void renderEntity(Entity parent){
        if(count >= 128){
            return;
        }
        count++;
        //Drag container
        if(parent.getName().toLowerCase().contains(filterString.toLowerCase())) {
            ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), 16);
            int selected = ImGuiSelectableFlags.AllowDoubleClick;
            ImGui.image(parent.getTextureID(), 16, 16);
            ImGui.sameLine();

            if (ImGui.selectable(parent.getName(), parent.equals(this.entity), selected)) {
                this.entity = parent;
                this.editor.setTarget(parent);
                //                CameraManager.getInstance().getActiveCamera().setPosition(new Vector3f(e.getPosition()).mul(1, 0, 1).add(new Vector3f( CameraManager.getInstance().getActiveCamera().getPosition()).mul(0, 1, 0)));
            }
            if (ImGui.beginDragDropSource()) {
                //Render Tooltip
                if (ImGui.setDragDropPayload("ENTITY", new byte[]{1}, 1)) {
                    System.out.println("Drag:" + parent.getName());
                    dragged = parent;
                }
                ImGui.beginTooltip();
                ImGui.text(parent.getName());
                ImGui.endTooltip();
                ImGui.endDragDropSource();
            }

            if (ImGui.beginDragDropTarget()) {
                int target_flags = ImGuiDragDropFlags.None;
                byte[] data = ImGui.acceptDragDropPayload("ENTITY", target_flags);
                if (data != null) {
                    if (dragged != null) {
                        System.out.println("Drop:" + dragged.getName());
                        LinkedList<Entity> grandChildren = EntityManager.getInstance().getEntitiesChildren(dragged);
                        if (!grandChildren.contains(parent)) {
                            dragged.translate(new Vector3f(parent.getPosition()).mul(-1));
                            dragged.setParent(parent);
                        }
                        dragged = null;
                    }
                }

                ImGui.endDragDropTarget();
            }
            ImGui.endChildFrame();

            boolean rightClick = false;

            if (ImGui.isItemHovered() && ImGui.getIO().getMouseDown(1)) {
                rightClick = true;
            }

            String popupIDRight = Editor.getInstance().getNextID()+"";

            if (rightClick) {
                ImGui.openPopup(popupIDRight);
            }

            //Popup for right click
            ImGui.setNextWindowSize(ImGui.getColumnWidth(), -1);
            ImVec2 vec2 = new ImVec2();
            ImGui.getWindowPos(vec2);
//            ImGui.setNextWindowPos(vec2.x, vec2.y);
            boolean clicked = false;
            if (ImGui.beginPopup(popupIDRight)) {
                if(ImGui.button("Clone", ImGui.getWindowWidth(), 32)){
                    Entity clone = EntityUtils.cloneTarget(parent);
                    EntityManager.getInstance().addEntity(clone);
                    this.editor.setTarget(clone);
                    clicked = true;
                }
                if(ImGui.button("Delete", ImGui.getWindowWidth(), 32)){
                    EntityManager.getInstance().removeEntity(parent);
                    clicked = true;
                }
                ImGui.separator();
                if(parent.hasParent()){
                    if(ImGui.button("Unparent", ImGui.getWindowWidth(), 32)){
                        parent.setParent(null);
                    }
                    clicked = true;
                }
                ImGui.separator();
                //TODO make arrow
                if(ImGui.button("Add Component", ImGui.getWindowWidth(), 32)){
                    parent.addComponent(new Collision());
                }
                ImGui.endPopup();
            }

            if(clicked){
                ImGui.closeCurrentPopup();
            }
        }


        //Render Children
        LinkedList<Entity> children = EntityManager.getInstance().getEntitiesChildren(parent);
        if(children.size() > 0) {
            //Child so tab in one and make drop down
            int nodeFlags_attributes = ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.OpenOnDoubleClick;
//            ImGui.sameLine();
            ImGui.indent();
            for (Entity child : new LinkedList<>(children)) {
                renderEntity(child);
                if(ImGui.isItemHovered()){
                    DirectDraw.getInstance().Draw3D.drawAABB(child, new Vector3f(1));
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
