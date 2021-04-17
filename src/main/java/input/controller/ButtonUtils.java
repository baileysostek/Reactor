package input.controller;

public class ButtonUtils {
    public static EnumButtonType getButtonForName(String name){
        for(EnumButtonType type : EnumButtonType.values()){
            if(type.getIdentifyingString().equals(name)){
                return type;
            }
        }
        return null;
    }
}