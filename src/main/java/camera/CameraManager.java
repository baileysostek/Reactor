package camera;

import math.Vector3f;

public class CameraManager {
    //Singleton instance
    private static CameraManager cameraManager;
    private Camera activeCamera;

    //This cameras generation info
    private CameraManager(){
        this.activeCamera = new Camera();

        this.activeCamera.setPosition(new Vector3f(0f, -6f, -6f));
//        this.activeCamera.setRotation(new Vector3f(-90f, 0f, 0f));

        System.out.println("Forward: "+ activeCamera.getForwardDir());
    }

    //protected methods
    public Camera getActiveCamera(){
        return this.activeCamera;
    }

    //Singleton generation and access
    public static void initialize(){
        if(cameraManager == null){
            cameraManager = new CameraManager();
        }
    }

    public static CameraManager getInstance(){
        return cameraManager;
    }

}
