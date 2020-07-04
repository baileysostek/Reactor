package editor.components;

import imgui.ImGui;

public class FrameTime extends UIComponet {

    float[] values = new float[256];
    float average = 0;

    public FrameTime(){
        for(int i = 0; i < values.length; i++){
            values[i] = 0;
        }
    }


    @Override
    public void update(double delta) {
        average = 0;
        for(int i = 0; i < values.length-1; i++){
            values[i] = values[i+1];
            average+=values[i];
        }
        average+=delta;
        average/=(float)values.length;
        values[values.length-1] = (float) delta;
    }

    @Override
    public void render() {
        ImGui.plotLines("average:"+average, values, values.length, 0, "Frame Times", 0.001f, 0.01f, 256, 100 );
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

    }

    @Override
    public void self_post_render() {

    }

    @Override
    public String getName(){
        return "Frame Time";
    }
}
