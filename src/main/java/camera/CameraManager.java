package camera;

import org.joml.Vector3f;

public class CameraManager {
    //Singleton instance
    private static CameraManager cameraManager;

    //This is the one active camera in the world
    private Camera activeCamera;

    //TODO add camera stack so we can pop back to last cam with smooth transition.

    //This cameras generation info
    private CameraManager(){
        this.activeCamera = new Camera();
    }

    //protected methods
    public Camera getActiveCamera(){
        return this.activeCamera;
    }

    //Set the camera
    public void setActiveCamera(Camera cam){
        this.activeCamera = cam;
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
