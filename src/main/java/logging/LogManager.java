package logging;

import org.lwjgl.opengl.GLUtil;
import util.StringUtils;

import java.io.*;
import java.util.LinkedList;

public class LogManager {
    //Singleton instance
    private static LogManager logManager;

    private static final String LOGS_DIRECTORY = "/logs/";

    private LogManager(){

    }

    public void routeOutputToFile(){
        try {
            System.out.println("Logs folder:" + StringUtils.getRelativePath()+LOGS_DIRECTORY+System.currentTimeMillis()+"_log.txt");
            File file = new File(StringUtils.getRelativePath()+LOGS_DIRECTORY+System.currentTimeMillis()+"_log.txt");
            FileOutputStream fileStream = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fileStream);
            System.setOut(ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void logLine(){
        System.out.println("----------------------------------------------------------------");
    }

    public void enableGLDebug(){
        final StringBuilder[] string = {new StringBuilder()};
        LinkedList<String> logMessage = new LinkedList<>();
        GLUtil.setupDebugMessageCallback(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                //buffer the char
                char buffer = (char)b;

                if (buffer == '\t') {
                    //ignore tab characters
                    return;
                }else{
                    //Append to our array if not tab
                    string[0].append(buffer);
                }
                //On New line we got some work to do
                if(buffer == '\n'){
                    //Buffer the line
                    String line = string[0].toString();
                    if(line.startsWith("[LWJGL]")){
                        String message = "";
                        String severity = "";
                        String type = "";
                        //Loop through print message to find severity level
                        for(String msgLine : logMessage){
                            if(msgLine.startsWith("Severity:")){
                                severity = msgLine.replace("Severity: ", "").replace("\n", "");
                            }
                            if(msgLine.startsWith("Type:")){
                                type = msgLine.replace("Type: ", "").replace("\n", "");
                            }
                            message+=msgLine;
                        }

                        //Print if severe
                        if(type.equals("ERROR")) {
                            if (!(severity.toUpperCase().equals("NOTIFICATION"))) {
                                System.out.println(message);
                            }
                        }

                        //Clear our logMessages
                        logMessage.clear();
                    }
                    //Add the line to our message
                    logMessage.addLast(line);
                    string[0] = new StringBuilder();
                }
            }
        }));
    }

    //Singleton methods
    public static void initialize(){
        if(logManager == null){
            logManager = new LogManager();
        }
    }

    public static LogManager getInstance(){
        return logManager;
    }

}
