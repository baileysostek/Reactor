package steam;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import engine.Reactor;
import platform.EnumDevelopment;
import platform.PlatformManager;
import util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class SteamManager {
    private static SteamManager steamManager;

    private boolean hasInstance = false;

    private SteamManager(){
        //Try to init steam
        try {
            System.out.println("Try init Steamworks...");
            String libraryPath = StringUtils.getRelativePath() + "libs/steam";
            libraryPath = libraryPath.replace("\\resources\\", "");
            System.out.println("Lib Path:"+libraryPath);

            //Before we initalize libraries we need to see if this file exists.
            File file = new File(libraryPath);
            if(file.exists()){
                SteamAPI.loadLibraries(libraryPath);
                System.out.println(SteamAPI.isSteamRunning());
                if (!SteamAPI.init()) {
                    // Steamworks initialization error, e.g. Steam client not running
                    if(Reactor.isDev()) {
                        SteamAPI.printDebugInfo(System.out);
                    }
                }else{
                    System.out.println("Steam Running:"+SteamAPI.isSteamRunning());
                }
                System.out.println("sucess.");
                hasInstance = true;
            }else{
                throw new FileNotFoundException();
            }
        } catch (SteamException e) {
            System.out.println("failed.");
            // Error extracting or loading native libraries
            e.printStackTrace();
        }catch (FileNotFoundException e) {
            System.out.println("Steam natives could not be found on this system. Is Steam installed?");
            e.printStackTrace();
        }
    }

    public static void initialize(){
        if(steamManager == null){
            steamManager = new SteamManager();
        }
    }

    public static SteamManager getInstance(){
        return steamManager;
    }


}
