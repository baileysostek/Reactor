package steam;

import com.codedisaster.steamworks.*;
import engine.Reactor;
import input.Keyboard;
import platform.EnumDevelopment;
import platform.PlatformManager;
import util.Callback;
import util.FrequencyLimiter;
import util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.security.Key;

public class SteamManager {
    private static SteamManager steamManager;

    private boolean hasInstance = false;

    private FrequencyLimiter limiter;

    // List of all of our Steam interfaces
    private SteamUser user;
    private SteamUserStats userStats;
    private SteamRemoteStorage remoteStorage;
    private SteamUGC ugc;
    private SteamUtils clientUtils;
    private SteamApps apps;
    private SteamFriends friends;

    private SteamLeaderboardHandle currentLeaderboard = null;

    private SteamUtilsCallback clUtilsCallback = new SteamUtilsCallback() {
        @Override
        public void onSteamShutdown() {
            System.err.println("Steam client requested to shut down!");
        }
    };

    private SteamUserStatsCallback userStatsCallback = new SteamUserStatsCallback() {
        @Override
        public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
            System.out.println("User stats received: gameId=" + gameId + ", userId=" + steamIDUser.getAccountID() +
                    ", result=" + result.toString());

            int numAchievements = userStats.getNumAchievements();
            System.out.println("Num of achievements: " + numAchievements);

            for (int i = 0; i < numAchievements; i++) {
                String name = userStats.getAchievementName(i);
                boolean achieved = userStats.isAchieved(name, false);
                System.out.println("# " + i + " : name=" + name + ", achieved=" + (achieved ? "yes" : "no"));
            }
        }

        @Override
        public void onUserStatsStored(long gameId, SteamResult result) {
            System.out.println("User stats stored: gameId=" + gameId +
                    ", result=" + result.toString());
        }

        @Override
        public void onUserStatsUnloaded(SteamID steamIDUser) {
            System.out.println("User stats unloaded: userId=" + steamIDUser.getAccountID());
        }

        @Override
        public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName,
                                            int curProgress, int maxProgress) {
            System.out.println("User achievement stored: gameId=" + gameId + ", name=" + achievementName +
                    ", progress=" + curProgress + "/" + maxProgress);
        }

        @Override
        public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
            System.out.println("Leaderboard find result: handle=" + leaderboard.toString() +
                    ", found=" + (found ? "yes" : "no"));

            if (found) {
                System.out.println("Leaderboard: name=" + userStats.getLeaderboardName(leaderboard) +
                        ", entries=" + userStats.getLeaderboardEntryCount(leaderboard));

                currentLeaderboard = leaderboard;
            }
        }

        @Override
        public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) {

            System.out.println("Leaderboard scores downloaded: handle=" + leaderboard.toString() +
                    ", entries=" + entries.toString() + ", count=" + numEntries);

            int[] details = new int[16];

            for (int i = 0; i < numEntries; i++) {

                SteamLeaderboardEntry entry = new SteamLeaderboardEntry();
                if (userStats.getDownloadedLeaderboardEntry(entries, i, entry, details)) {

                    int numDetails = entry.getNumDetails();

                    System.out.println("Leaderboard entry #" + i +
                            ": accountID=" + entry.getSteamIDUser().getAccountID() +
                            ", globalRank=" + entry.getGlobalRank() +
                            ", score=" + entry.getScore() +
                            ", numDetails=" + numDetails);

                    for (int detail = 0; detail < numDetails; detail++) {
                        System.out.println("  ... detail #" + detail + "=" + details[detail]);
                    }

                    if (friends.requestUserInformation(entry.getSteamIDUser(), false)) {
                        System.out.println("  ... requested user information for entry");
                    } else {
                        System.out.println("  ... user name is '" +
                                friends.getFriendPersonaName(entry.getSteamIDUser()) + "'");

                        int smallAvatar = friends.getSmallFriendAvatar(entry.getSteamIDUser());
                        if (smallAvatar != 0) {
                            int w = clientUtils.getImageWidth(smallAvatar);
                            int h = clientUtils.getImageHeight(smallAvatar);
                            System.out.println("  ... small avatar size: " + w + "x" + h + " pixels");

                            ByteBuffer image = ByteBuffer.allocateDirect(w * h * 4);

                            try {
                                if (clientUtils.getImageRGBA(smallAvatar, image)) {
                                    System.out.println("  ... small avatar retrieve avatar image successful");

                                    int nonZeroes = w * h;
                                    for (int y = 0; y < h; y++) {
                                        for (int x = 0; x < w; x++) {
                                            //System.out.print(String.format(" %08x", image.getInt(y * w + x)));
                                            if (image.getInt(y * w + x) == 0) {
                                                nonZeroes--;
                                            }
                                        }
                                        //System.out.println();
                                    }

                                    if (nonZeroes == 0) {
                                        System.err.println("Something's wrong! Avatar image is empty!");
                                    }

                                } else {
                                    System.out.println("  ... small avatar retrieve avatar image failed!");
                                }
                            } catch (SteamException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("  ... small avatar image not available!");
                        }

                    }
                }

            }
        }

        @Override
        public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
            System.out.println("Leaderboard score uploaded: " + (success ? "yes" : "no") +
                    ", handle=" + leaderboard.toString() +
                    ", score=" + score +
                    ", changed=" + (scoreChanged ? "yes" : "no") +
                    ", globalRankNew=" + globalRankNew +
                    ", globalRankPrevious=" + globalRankPrevious);
        }

        @Override
        public void onGlobalStatsReceived(long gameId, SteamResult result) {
            System.out.println("Global stats received: gameId=" + gameId + ", result=" + result.toString());
        }
    };

    private SteamManager(){
        limiter = new FrequencyLimiter(2, new Callback() {
            @Override
            public Object callback(Object... objects) {
                SteamAPI.runCallbacks();
                System.out.println("Test");
                return null;
            }
        });

        Keyboard.getInstance().addPressCallback(Keyboard.FIVE, new Callback() {
            @Override
            public Object callback(Object... objects) {
                System.out.println("Unlocked:" + unlockAchievement("NEW_ACHIEVEMENT_1_0"));
                return null;
            }
        });

        //Try to init steam
        try {
            System.out.println("Try init Steamworks...");
            String libraryPath = StringUtils.getRelativePath() + "libs/steam/win64/";
//            libraryPath = libraryPath.replaceAll("/", "\\\\");
            System.out.println("Lib Path:"+libraryPath);
            System.out.println("Workspace root:" + System.getProperty("user.dir"));

            //Before we initalize libraries we need to see if this file exists.
            SteamAPI.loadLibraries(libraryPath);
            if (!SteamAPI.init()) {
                // Steamworks initialization error, e.g. Steam client not running
                SteamAPI.printDebugInfo(System.err);
            }else{
                System.out.println("Steam Running:"+SteamAPI.isSteamRunning());

                SteamAPI.printDebugInfo(System.out);

                clientUtils = new SteamUtils(clUtilsCallback);
                userStats = new SteamUserStats(userStatsCallback);

//            clientUtils.setWarningMessageHook(clMessageHook);

                // SteamAPI.init() with your (kn)own app ID
                if (SteamAPI.restartAppIfNecessary(clientUtils.getAppID())) {
                    System.out.println("SteamAPI_RestartAppIfNecessary returned 'false'");
                }

                System.out.println("success.");
                hasInstance = true;
            }
        } catch (SteamException e) {
            System.out.println("failed.");
            // Error extracting or loading native libraries
            e.printStackTrace();
        }
    }

    public boolean unlockAchievement(String name){
        if(!hasInstance){
            System.out.println("No Steam instance detected.");
        }
        return userStats.setAchievement(name);
    }

    public void update(double delta){
        if(hasInstance){
            limiter.update(delta);
        }
    }

    public void onShutdown(){
        if(hasInstance) {
            clientUtils.dispose();
            userStats.dispose();
        }
        SteamAPI.shutdown();
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
