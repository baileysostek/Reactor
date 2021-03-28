package models;

import graphics.renderer.DirectDraw;
import graphics.sprite.Colors;
import org.joml.Vector3f;

import java.util.LinkedList;

public class MeshBuilder {
    private static MeshBuilder singleton;

    private static float bias = 0;

    private MeshBuilder() {

    }

    public void update(double delta){
        bias += delta / 6;
    }

    public void buildPlane(float width, float height, int subdivisionsX, int subdivisionsY){
        float unitWidth  = width  / (float)subdivisionsX;
        float unitHeight = height / (float)subdivisionsY;

        // +2 is for first and last point, so with 0 subdivisions you get 4 points making a simple quad.
        int widthUnits  = subdivisionsY+2;
        int heightUnits = subdivisionsX+2;

        Vector3f[] vertices = new Vector3f[widthUnits * heightUnits];

        // Generate our verts
        for(int j = 0; j < heightUnits; j++){
            for(int i = 0; i < widthUnits; i++){
                vertices[i + (j * widthUnits)] = new Vector3f(i * unitWidth, (float) Math.sin((((float)(i + j) / widthUnits * 9) + (bias)) * Math.PI) / 2, j * unitHeight);
            }
        }

        //Here we have 6 verts per quad. Each quad is 2 tries = 6 verts.
        Vector3f[] faceData = new Vector3f[subdivisionsX * subdivisionsY * 6];

        // Generate our indices
        for(int j = 0; j < subdivisionsY; j++){
            for(int i = 0; i < subdivisionsX; i++){
                int faceIndex = (i + (j * subdivisionsY));
                boolean inverseFace = ((i + j) % 2) == 1;

                Vector3f vec0 = vertices[(i + 0) + ((j + 0) * widthUnits)];
                Vector3f vec1 = vertices[(i + 1) + ((j + 0) * widthUnits)];
                Vector3f vec2 = vertices[(i + 0) + ((j + 1) * widthUnits)];
                Vector3f vec3 = vertices[(i + 1) + ((j + 1) * widthUnits)];

                if(inverseFace){
                    // [0, 1, 3, 0, 3, 2]
                    faceData[(faceIndex * 6) + 0] = vec0;
                    faceData[(faceIndex * 6) + 1] = vec1;
                    faceData[(faceIndex * 6) + 2] = vec3;
                    faceData[(faceIndex * 6) + 3] = vec0;
                    faceData[(faceIndex * 6) + 4] = vec3;
                    faceData[(faceIndex * 6) + 5] = vec2;
                }else{
                    // [0, 1, 2, 1, 3, 2]
                    faceData[(faceIndex * 6) + 0] = vec0;
                    faceData[(faceIndex * 6) + 1] = vec1;
                    faceData[(faceIndex * 6) + 2] = vec2;
                    faceData[(faceIndex * 6) + 3] = vec1;
                    faceData[(faceIndex * 6) + 4] = vec3;
                    faceData[(faceIndex * 6) + 5] = vec2;
                }

                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (0)], faceData[(faceIndex * 6) + (1)], Colors.CYAN);
                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (1)], faceData[(faceIndex * 6) + (2)], Colors.CYAN);
                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (2)], faceData[(faceIndex * 6) + (0)], Colors.CYAN);

                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (3)], faceData[(faceIndex * 6) + (4)], Colors.CYAN);
                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (4)], faceData[(faceIndex * 6) + (5)], Colors.CYAN);
                DirectDraw.getInstance().drawLine(faceData[(faceIndex * 6) + (5)], faceData[(faceIndex * 6) + (3)], Colors.CYAN);
            }
        }
    }

    public static void initialize() {
        if (singleton == null) {
            singleton = new MeshBuilder();
        }
    }

    public static MeshBuilder getInstance() {
        return singleton;
    }
}
