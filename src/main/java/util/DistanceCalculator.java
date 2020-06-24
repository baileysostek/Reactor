/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import org.joml.Vector3f;


/**
 *
 * @author Bayjose
 */
public class DistanceCalculator {
    
    
    public static float CalculateXYZDifference(float x, float xx, float y, float yy, float z, float zz){
        return (float) Math.sqrt(((xx - x)*(xx - x))+((yy - y)*(yy - y))+((zz - z)*(zz - z)));
    }

    public static float distance(float x, float xx, float y, float yy){
        return (float) Math.sqrt(((xx - x)*(xx - x))+((yy - y)*(yy - y)));
    }
    
    public static float distance(Vector3f min, Vector3f max){
        return (float) Math.sqrt(((max.x - min.x)*(max.x - min.x))+((max.y - min.y)*(max.y - min.y))+((max.z - min.z)*(max.z - min.z)));
    }
}
