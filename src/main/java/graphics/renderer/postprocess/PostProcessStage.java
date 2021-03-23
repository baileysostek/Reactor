package graphics.renderer.postprocess;

import java.util.LinkedList;

public class PostProcessStage {

    PostProcessConstants command;
    LinkedList<Object> params = new LinkedList<>();

    protected PostProcessStage(PostProcessConstants command, Object ... paramas){
        int expectedParams = command.getNumberOfExpectedParameters();
        if(paramas.length != expectedParams){
            if(paramas.length < expectedParams){
                System.out.println("Error: Too few parameters passed into the Post processing stage ["+command+"] " + expectedParams+" parameter" + (expectedParams == 1 ? "" : "s") + " are expected.");
            }else{
                System.out.println("Warning: Too many parameters passed into the Post processing stage ["+command+"] only " + expectedParams+" parameter" + (expectedParams == 1 ? "" : "s") + " expected.");
            }
        }

        for(int i = 0; i < expectedParams; i++){
            this.params.addLast(paramas[i]);
        }
    }
}