package entity;

import editor.AttributeRenderer;
import editor.Editor;
import editor.components.UIComponet;
import entity.component.Attribute;
import imgui.ImGui;
import imgui.enums.ImGuiDragDropFlags;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import imgui.enums.ImGuiWindowFlags;

import java.util.LinkedList;

public class Settings extends UIComponet{

    private LinkedList<Attribute> attribtues = new LinkedList<>();

    public Settings(){

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


    @Override
    public void self_render() {
        int flags = ImGuiWindowFlags.NoCollapse;
        ImGui.beginChild(Editor.getInstance().getNextID(), ImGui.getWindowWidth(), ImGui.getWindowHeight(),  true, flags);
        AttributeRenderer.renderAttributes(attribtues);
        ImGui.showDemoWindow();
        ImGui.endChild();
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Settings";
    }

    public void addWatchedAttribute(Attribute attribute) {
        attribtues.add(attribute);
    }
}
