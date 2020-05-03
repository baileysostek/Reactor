package platform;

import engine.Engine;

public class PlatformManager extends Engine {

    private static PlatformManager platformManager;
    private static EnumPlatform target;
    private static EnumDevelopment level = EnumDevelopment.DEVELOPMENT;

    //Private constructor in line with singleton design pattern.
    private PlatformManager(){
        System.out.println("Setting Target Platform to:" + target);
    }

    @Override
    public void onShutdown() {

    }

    public static PlatformManager setTargetPlatform(EnumPlatform target){
        if(platformManager != null){
            System.err.println("Tried to set target platform, however target is aleady set to:"+PlatformManager.target);
        }else{
            PlatformManager.target = target;
            platformManager = new PlatformManager();
            return platformManager;
        }
        return null;
    }

    public boolean targetIs(EnumPlatform targetCheck){
        return target.equals(targetCheck);
    }

    public EnumDevelopment getDevelopmentStatus(){
        return level;
    }

    public void setDevelopmentLevel(EnumDevelopment dev) {
        level = dev;
    }

    public static PlatformManager getInstance(){
        return platformManager;
    }

}
