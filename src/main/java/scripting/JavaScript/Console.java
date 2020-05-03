package scripting.JavaScript;

import scripting.ConsoleColors;

public class Console {
//    public void log(Object ... params){
//        String out = "";
//        for(Object obj : params){
//            out += obj.toString()+" ";
//        }
//        System.out.println(out.substring(0, out.length()-1));
//    }

    //Replace with muli
    public void log(Object line){
        System.out.println(ConsoleColors.CYAN + line + ConsoleColors.RESET);
    }

}
