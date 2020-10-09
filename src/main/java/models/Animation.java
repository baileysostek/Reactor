package models;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.LinkedList;

public class Animation {
    public String name;
    public double timescale = 1;

    private HashMap<String, KeyFrame[]> keyFrames = new HashMap<>();

    public Animation(double timescale){
        this.timescale = timescale;
    }

    public void importKeyFrames(HashMap<String, LinkedList<KeyFrame>> keyframes){
        for(String key : keyframes.keySet()){
            LinkedList<KeyFrame> frames = keyframes.get(key);
            this.keyFrames.put(key, new KeyFrame[frames.size()]);
            int i = 0;
            for(KeyFrame frame : frames){
                this.keyFrames.get(key)[i] = frame;
                i++;
            }
        }
    }

    public HashMap<String, Matrix4f> getBoneTransformsForTime(double delta) {
        delta %= timescale;
        HashMap<String , Matrix4f> out = new HashMap<>();

        for(String bone : keyFrames.keySet()){
            KeyFrame lowerKey = new KeyFrame(0, new Matrix4f().identity());
            KeyFrame upperKey = null;

            double deltaTime = 0;

            int index = 0;
            for(KeyFrame frame : keyFrames.get(bone)){
                if(frame.timelinePosition > delta){
                    upperKey = frame;
                    if(index >= 1){
                        lowerKey = keyFrames.get(bone)[index - 1];
                    }
                    deltaTime = (delta - lowerKey.timelinePosition) / (upperKey.timelinePosition - lowerKey.timelinePosition);
                    break;
                }
                index++;
            }

            if(upperKey != null) {
                Matrix4f pos = new Matrix4f(lowerKey.position).lerp(upperKey.position, (float) deltaTime);
                out.put(bone, pos);
            }
        }

        return out;
    }
}
