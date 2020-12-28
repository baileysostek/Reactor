package util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class StringUtils {

    private static HashMap<String, String> fileCache = new HashMap<>();

    private static String RESOURCES_DIRECTORY = "/resources/";

    private static String PATH = new File("").getAbsolutePath().replaceAll("\\\\", "/") + RESOURCES_DIRECTORY;

    private static JsonParser parser = new JsonParser();

    public static String load(String filePath){
        if(fileCache.containsKey(filePath)){
            return fileCache.get(filePath);
        }
        StringBuilder fileData = new StringBuilder();
        try{
            String path = PATH + filePath;
            System.out.println("PATH:" + PATH);
            System.out.println(path);
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                fileData.append(line).append("\n");
            }
            reader.close();
            fileCache.put(filePath, fileData.toString());
            return fileData.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean releaseCachedFile(String fileName){
        if(fileName.startsWith("/")){
            fileName = fileName.substring(1, fileName.length());
        }

        if(fileCache.containsKey(fileName)){
            fileCache.remove(fileName);
            return true;
        }
        return false;
    }

    public static void recacheFile(String fileName){
        if(releaseCachedFile(fileName)){
            load(fileName);
        }
    }

    public static ByteBuffer loadRaw(String filePath) {
        try{
            String path = PATH + filePath;
            File file = new File(path);

            byte[] bytes = new byte[(int) file.length()];
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            dis.readFully(bytes);

            return ByteBuffer.wrap(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonObject loadJson(String fileName){
        System.out.println("Loading JSON:"+PATH + fileName);
        String data = load(fileName);
        if(data != null) {
            return parser.parse(data).getAsJsonObject();
        }else{
            return null;
        }
    }

    public static JsonObject parseJson(String jsonData){
        JsonReader reader = new JsonReader(new StringReader(jsonData));
        reader.setLenient(true);
        return parser.parse(reader).getAsJsonObject();
    }

    public static String getRelativePath(){
        return PATH;
    }

    public static void write(String data, String filePath){
        try {
            String path = new File("").getAbsolutePath() + RESOURCES_DIRECTORY + filePath;
            FileWriter myWriter = new FileWriter(path);
            myWriter.write(data);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Take a String array and smoosh it into a single string.
    public String unify(String[] data){
        String out = "";
        for(String s : data){
            out = out + s;
        }
        return out;
    }


    //Return the index of the smallest string
    public static int minStringLength(String[] strings){
        int smallest = Integer.MAX_VALUE;
        int id = 0;
        int index = 0;
        for(String line : strings){
            if(line.length() < smallest){
                smallest = line.length();
                id = index;
            }
            index++;
        }
        return id;
    }

    //Return the index of the largest string
    public static int maxStringLength(String[] strings){
        int largest = Integer.MIN_VALUE;
        int id = 0;
        int index = 0;
        for(String line : strings){
            if(line.length() > largest){
                largest = line.length();
                id = index;
            }
            index++;
        }
        return id;
    }

    //Return the index of the largest string
    public static boolean isComment(String test, String[] commentCharacters){
        for(String comment : commentCharacters){
            if(test.equals(comment)){
                return true;
            }
        }
        return false;
    }

    //Return the index of the largest string
    public static boolean isSurroundedBy(String test, String[] start, String[] end){
        boolean startsWith = false;
        for(String startString : start){
            if(test.startsWith(startString)){
                startsWith = true;
                break;
            }
        }
        boolean endsWith = false;
        for(String endString : end){
            if(test.endsWith(endString)){
                endsWith = true;
                break;
            }
        }
        return startsWith && endsWith;
    }

    //Return the index of the largest string
    public static boolean isSurroundedByANY(String test, String[] characters){
        return isSurroundedBy(test, characters, characters);
    }

    //Return the index of the largest string
    public static boolean isNumber(String test){
        return test.matches("\\d.+") || test.matches("\\d+");
    }

    public static String replaceAll(String base, String[] all, String replacement){
        String out = base;
        for(String string: all){
            out = out.replaceAll(string, replacement);
        }
        return out;
    }

    public static String replaceFirst(String s, String replacement){
        if(s.length() < replacement.length()){
            return s;
        }
        if(s.equals(replacement)){
            return "";
        }
        for(int i = 0; i < s.length() - replacement.length(); i++){
            String shuttle = s.substring(i, replacement.length()+i);
            if(shuttle.equals(replacement)){
                String out = s.substring(0, i)+s.substring(replacement.length()+i, s.length());
                return out;
            }
        }
        return s;
    }

    public static String removeStringsFromLine(String checkLine, String commentCharacter){
        String line = checkLine;

        while(line.contains(commentCharacter)){
            int first = line.indexOf(commentCharacter);
            int next  = line.indexOf(commentCharacter, first+1);
            if(next < 0){
                return line;
            }
            String string = line.substring(line.indexOf(commentCharacter), next+1);
            line = replaceFirst(line, string);
        }

        return line;
    }

    public static boolean isComment(String test, String commentCharacters){
        return test.equals(commentCharacters);
    }

}
