package camera;

import math.MatrixUtils;
import math.Vector3f;

public class Camera {
    private Vector3f pos = new Vector3f(0f, -0f, 0f);
    private Vector3f rot = new Vector3f(0f, 0f, 0f);

    public Camera(){
        //Pass by reference or value
//        System.out.println("Forward: "+ getForwardDir());
    }

    public Camera setPosition(Vector3f vec){
        pos = new Vector3f(vec);
        return this;
    }

    public Camera setRotation(Vector3f vec){
        rot = new Vector3f(vec);
        return this;
    }

    public Camera setPositionRef(Vector3f vec){
        pos = vec;
        return this;
    }

    public float[] getTransform(){
        float[] modelMatrix = new float[]{
            1, 0, 0, -pos.x(),
            0, 1, 0, pos.y(),
            0, 0, 1, pos.z(),
            0, 0, 0, 1
        };

//        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.z(), 0f,0f, 1f);
//        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.y(), 0f,1f, 0f);
//        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.x(), 1f,0f, 0f);

        return modelMatrix;
    }

    public Vector3f getForwardDir(){
        float[] forward = {0, 0, -1, 1}; //Worldspace direction
        float[] out     = {0, 0, 0, 0};
        out = MatrixUtils.multiplyMV(out, getTransform(), 0, forward, 0);
//        System.out.println("Forward:"+out[0]+","+out[1]+","+out[2]+","+out[3]);
        return new Vector3f(out[0] / out[3], out[1] / out[3], out[2] / out[3]);
    }

    public Vector3f getPosition() {
        return this.pos;
    }

    public Vector3f getRotation() {
        return this.rot;
    }

    public float[] getView() {
        float[] modelMatrix = MatrixUtils.getIdentityMatrix();

        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.z(), 0f,0f, 1f);
        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.y(), 0f,1f, 0f);
        modelMatrix = MatrixUtils.rotateM(modelMatrix, 0,  rot.x(), 1f,0f, 0f);

        return modelMatrix;
    }
}
