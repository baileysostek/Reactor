package models;

import org.joml.Matrix4f;

public class KeyFrame {
    public double timelinePosition;
    public Matrix4f position;

    public KeyFrame(double timelinePosition, Matrix4f position){
        this.timelinePosition = timelinePosition;
        this.position         = position;
    }
}
