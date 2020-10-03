package entity;

import editor.Editor;
import editor.components.UIComponet;
import imgui.ImGui;
import imgui.enums.ImGuiDragDropFlags;
import imgui.enums.ImGuiSelectableFlags;
import imgui.enums.ImGuiTreeNodeFlags;
import imgui.enums.ImGuiWindowFlags;

import java.util.LinkedList;

public class Settings extends UIComponet{

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
}
