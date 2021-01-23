package editor.shaderGraph;

import editor.Editor;
import editor.components.UIComponet;
import entity.component.Attribute;
import graphics.renderer.EnumGLDatatype;
import imgui.ImGui;

public abstract class ShaderNode extends UIComponet {

    private String name;

    private Attribute<EnumGLDatatype>[] inputParams = new Attribute[]{
        new Attribute<EnumGLDatatype>("color", EnumGLDatatype.VEC4)
    };
    private Attribute<EnumGLDatatype> pass;

    public ShaderNode(String name){

    }

    //Actual implementation in code for this.
    public abstract String[] getGLSLData();

    @Override
    public void onAdd() {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public void self_update(double delta) {
        ImGui.pushID(Editor.getInstance().getNextID());
        ImGui.beginChildFrame(Editor.getInstance().getNextID(), 256, 64 + (16 * inputParams.length));
        
        ImGui.endChildFrame();
        ImGui.popID();
    }

    @Override
    public void self_render() {

    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName() {
        return null;
    }
}
