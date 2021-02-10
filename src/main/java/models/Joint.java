package models;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class Joint {

    private int index;
    private String name;

    //TODO refactor array instead of List for speed.
    private Joint parent = null;
    private List<Joint> children = new ArrayList<Joint>();

    private Matrix4f animationTransform = new Matrix4f();

    private Matrix4f localBindTransform;
    private Matrix4f inverseBindTransform;

    public Joint(int index, String name, Matrix4f bindLocalTransform){
        this.index = index;
        this.name = name;
        this.localBindTransform = bindLocalTransform;
        this.inverseBindTransform = new Matrix4f(localBindTransform).invert();
    }

    public Matrix4f getLocalBindTransform(){
        return this.localBindTransform;
    }

    public Matrix4f getInverseBindTransform(){
        return this.inverseBindTransform;
    }

    public void addChild(Joint child){
        children.add(child);
        child.parent = this;
    }

    public Matrix4f getAnimationTransform() {
        return animationTransform;
    }

    public void setAnimationTransform(Matrix4f animatedTransform) {
        this.animationTransform = animatedTransform;
    }

    public List<Joint> getChildren() {
        return this.children;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
