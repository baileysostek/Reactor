package platform;

public class PlatformManager{

    private static PlatformManager platformManager;
    private static EnumPlatform target;

    //Private constructor in line with singleton design pattern.
    private PlatformManager(){
        System.out.println("Setting Target Platform to:" + target);
    }

    public void onShutdown() {

    }

    public static PlatformManager setTargetPlatform(EnumPlatform target){
        if(PlatformManager.target != null){
            System.err.println("Tried to set target platform, however target is aleady set to:"+PlatformManager.target);
        }else{
            PlatformManager.target = target;
            return platformManager;
        }
        return null;
    }

    public boolean targetIs(EnumPlatform targetCheck){
        return target.equals(targetCheck);
    }

    public static void initialize(){
        if(platformManager == null){
            platformManager = new PlatformManager();
        }
    }

    public static PlatformManager getInstance(){
        return platformManager;
    }

}
