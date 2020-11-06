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
        this.activeCamera = new DynamicCamera();
    }

    //protected methods
    public Camera getActiveCamera(){
        return this.activeCamera;
    }

    //Set the camera
    public void setActiveCamera(Camera cam){
        if(cam != null) {
            if (this.activeCamera != null) {
                this.activeCamera.onDeactivated();
            }
            this.activeCamera = cam;
            cam.onActive();
        }
    }

    //Do all our transformations and camera updates here
    public void update(double delta){
        this.activeCamera.update(delta);
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
