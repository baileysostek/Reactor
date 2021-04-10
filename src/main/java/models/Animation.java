package models;

import entity.Entity;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Animation {
    public String name;

    public double duration  = 1;
    public double frameRate = 1;

    //Local copy of some Joints used for this animation
    private LinkedHashMap<String, Joint> animationTransform = new LinkedHashMap<>();

    private HashMap<String, KeyFrame[]> keyFrames = new HashMap<>();

    public Animation(double timescale, float frameRate){
        this.duration = timescale;
        this.frameRate = frameRate;
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

    public LinkedHashMap<String, Joint> getBoneTransformsForTime(LinkedList<Joint> rootJoints, double delta_t) {
        delta_t *= duration;
        delta_t %= duration;
        LinkedHashMap<String , Matrix4f> out = new LinkedHashMap<>();

        for(String bone : keyFrames.keySet()){
            KeyFrame lowerKey = new KeyFrame(0, new Matrix4f().identity());
            KeyFrame upperKey = null;

            double deltaTime = 0;

            int index = 0;
            for(KeyFrame frame : keyFrames.get(bone)){
                if(frame.timelinePosition >= delta_t){
                    upperKey = frame;
                    if(index >= 1){
                        lowerKey = keyFrames.get(bone)[index - 1];
                    }
                    deltaTime = (delta_t - lowerKey.timelinePosition) / (upperKey.timelinePosition - lowerKey.timelinePosition);
                    break;
                }
                index++;
            }

            if(upperKey != null) {
                Matrix4f pos;
                if(Double.isNaN(deltaTime)){
                    pos = new Matrix4f(upperKey.position);
                }else {
                    pos = new Matrix4f(lowerKey.position).lerp(new Matrix4f(upperKey.position), (float) deltaTime);
                }
                out.put(bone, pos);
            }else{
                out.put(bone, lowerKey.position);
            }
        }

        for(Joint joint : rootJoints){
            applyPoseToJoints(out, joint, new Matrix4f().identity(), animationTransform);
        }

        return animationTransform;
    }

    private void applyPoseToJoints(HashMap<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform, LinkedHashMap<String, Joint> animationTranform) {
        if(currentPose.containsKey(joint.getName())) {
            Matrix4f currentLocalTransform = currentPose.get(joint.getName());
            Matrix4f currentTransform = new Matrix4f(parentTransform).mul(currentLocalTransform);
            for (Joint childJoint : joint.getChildren()) {
                applyPoseToJoints(currentPose, childJoint, currentTransform, animationTranform);
            }
            currentTransform = currentTransform.mul(joint.getLocalBindTransform());
            if(!animationTranform.containsKey(joint.getName())){
                Joint clone = new Joint(joint);
                animationTranform.put(clone.getName(), clone);
            }

            animationTranform.get(joint.getName()).setAnimationTransform(currentTransform);
        }
    }

    public double getDuration() {
        return duration;
    }

    public double getFramesPerSecond() {
        return frameRate;
    }
}
