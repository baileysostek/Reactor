package editor.components.input;

import editor.components.UIComponet;
import imgui.ImGui;
import imgui.enums.ImGuiCol;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Slider extends UIComponet {

    //Value
    private float[]  value         = new float[]{0f};
    private String   label         = "Slider";
    private Vector2f range         = new Vector2f(-1f, 1f);
    private Vector4f color_primary = new Vector4f();
    private Vector4f color_grab    = new Vector4f();

    private int ID;

    //Constructors
    public Slider(){}

    public Slider(float value){
        this.value[0] = value;
    }

    //Access private members
    public Slider setValue(float value){
        this.value[0] = value;
        return this;
    }

    public Slider setLabel(String label){
        this.label = label;
        return this;
    }

    public Slider setRanage(float start, float end){
        this.range.x = start;
        this.range.y = end;
        return this;
    }

    //Get the sliders value
    public float getValue(){
        return this.value[0];
    }

    @Override
    public void onAdd() {
        this.ID = (int) (Math.random() * Integer.MAX_VALUE);
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void self_update(double delta) {

    }

    @Override
    public void self_render() {
        ImGui.pushID(ID);
        ImGui.sliderFloat(label, value, range.x, range.y);
        ImGui.popID();
    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Slider";
    }
}
