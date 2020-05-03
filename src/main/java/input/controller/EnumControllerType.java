package input.controller;

/**
 * Created by bhsostek on 12/19/2018.
 */
public enum EnumControllerType {


    XBOX_360("xbox_360"),
    XBOX_ONE("xbox_one"),
    PS4("ps4"),
    UNKNOWN("unknown"),
    ;
    protected String kind;

    EnumControllerType(String kind){
        this.kind = kind;
    }
}
