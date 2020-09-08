package models;

import org.joml.Vector3f;

public class AABB {
    private Vector3f min = new Vector3f(Integer.MAX_VALUE);
    private Vector3f max = new Vector3f(-Integer.MAX_VALUE);

    public AABB(){}

    public AABB(int size){
        min = new Vector3f(-Math.abs(size));
        max = new Vector3f( Math.abs(size));
    }

    public AABB(Vector3f min, Vector3f max){
        this.min = min;
        this.max = max;
    }

    //Takes in a new point and resizes AABB to abide by this new point possibly being bigger.
    public void recalculateFromPoint(Vector3f point){
        //X axis
        if(min.x > point.x){
            min.x = point.x;
        }
        if(max.x < point.x){
            max.x = point.x;
        }

        //Y axis
        if(min.y > point.y){
            min.y = point.y;
        }
        if(max.y < point.y){
            max.y = point.y;
        }

        //Z axis
        if(min.z > point.z){
            min.z = point.z;
        }
        if(max.z < point.z){
            max.z = point.z;
        }
    }

    public Vector3f getMIN(){
        return min;
    }

    public Vector3f getMAX(){
        return max;
    }
}
