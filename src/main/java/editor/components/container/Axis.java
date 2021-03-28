package editor.components.container;

import editor.components.UIComponet;
import editor.components.input.Slider;
import org.joml.Vector3f;

public class Axis extends UIComponet {

    //Slider components
    private Slider x = new Slider().setLabel("X");
    private Slider y = new Slider().setLabel("Y");
    private Slider z = new Slider().setLabel("Z");
    private Vector3f inputVec = null;
    private String label = "Axis";

    public Axis(){

    }

    public Axis(float x_i, float y_i, float z_i){
        x.setValue(x_i);
        y.setValue(y_i);
        z.setValue(z_i);
    }

    public Axis(Vector3f vec3){
        this.inputVec = vec3;
    }

    //Methods for setting properties
    public Axis setRange(float min, float max){
        this.x.setRanage(min, max);
        this.y.setRanage(min, max);
        this.z.setRanage(min, max);
        return this;
    }

    public Axis setValue(float value){
        this.x.setValue(value);
        this.y.setValue(value);
        this.z.setValue(value);
        return this;
    }

    @Override
    public void onAdd() {
        this.addChild(x);
        this.addChild(y);
        this.addChild(z);
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void self_update(double delta) {
        if(inputVec != null){
            inputVec.x = x.getValue();
            inputVec.y = y.getValue();
            inputVec.z = z.getValue();
        }
    }

    @Override
    public void selfRender() {

    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Axis";
    }
}
