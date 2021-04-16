package logging;

import engine.Reactor;
import org.lwjgl.opengl.GLUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

public class LogManager {
    //Singleton instance
    private static LogManager logManager;

    private LogManager(){
        if(Reactor.isDev()) {
            enableGLDebug();
        }
    }

    private void enableGLDebug(){
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
                        //Loop through print message to find severity level
                        for(String msgLine : logMessage){
                            if(line.startsWith("Severity:")){
                                severity = line.replace("Severity: ", "").replace("\n", "");
                            }
                            message+=msgLine;
                        }

                        //Print if severe
                        if(!(severity.toUpperCase().equals("NOTIFICATION"))){
                            System.out.println(message);
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

    public LogManager getInstance(){
        return logManager;
    }

}
