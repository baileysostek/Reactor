package camera;

import math.MatrixUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    //Transform variables
    private Vector3f pos    = new Vector3f(0f);
    private Vector3f rot    = new Vector3f(0f);
    private Vector3f offset = new Vector3f(0f);

    public Camera(){
        //Pass by reference or value
//        System.out.println("Forward: "+ getForwardDir());
    }

    public Camera setPosition(Vector3f vec){
        pos = new Vector3f(vec).mul(1, 1, -1);
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

    public void setOffset(Vector3f offset){
        this.offset = offset;
    }

    public Vector3f getOffset(){
        return this.offset;
    }

    public float[] getTransform(){
        Matrix4f transform = new Matrix4f().identity();

        transform.rotateAffineXYZ(rot.x, rot.y, rot.z, transform);

        transform.translate(new Vector3f(new Vector3f(pos).sub(offset)).mul(new Vector3f(-1, 1, 1)), transform);


        float[] modelMatrix = new float[]{
            transform.m00(), transform.m01(), transform.m02(), transform.m03(),
            transform.m10(), transform.m11(), transform.m12(), transform.m13(),
            transform.m20(), transform.m21(), transform.m22(), transform.m23(),
            transform.m30(), transform.m31(), transform.m32(), transform.m33()
        };

        return modelMatrix;
    }

    public Matrix4f getTransformationMarix(){
        Matrix4f transform = new Matrix4f().identity();

        transform.rotateAffineXYZ(rot.x, rot.y, rot.z, transform);

        transform.translate(new Vector3f(pos).mul(new Vector3f(-1, 1, 1)), transform);

        return transform;
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
}
