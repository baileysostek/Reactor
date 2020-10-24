package particle;

public enum EmissionType {
    BURST_SINGLE("Single Burst"),
    BURST_LOOP("Looping Burst"),
    CONTINUOUS("Continuous");

    private String description;
    EmissionType(String name){
        this.description = name;
    }

    @Override
    public String toString(){
        return description;
    }

}
