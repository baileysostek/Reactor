package entity.component;

import imgui.ImGui;

import java.util.LinkedList;

public class ComponentShader extends Component {

    //Our attributes
    Attribute<String> shaderName = new Attribute<String>("Shader Name", "main");

    public ComponentShader() {
        super("Shader");
    }

    @Override
    public void onRemove() {

    }

    @Override
    protected LinkedList<Attribute> initialize() {
        LinkedList<Attribute> attributes = new LinkedList<>();

        attributes.add(shaderName);

        return attributes;
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public String getName() {
        return "Shader";
    }

    @Override
    public void onAttributeUpdate(Attribute observed) {

    }

    @Override
    public void onRenderUI(){
        ImGui.text(shaderName.getData());
    }
}
