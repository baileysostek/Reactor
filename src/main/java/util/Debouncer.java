package util;

public class Debouncer {
    private boolean value;
    public Debouncer(boolean value){
        this.value = value;
    }

    public Debouncer(){
        this.value = false;
    }

    public boolean risingAction(boolean value){
        if(value != this.value){
            if(value){
                this.value = true;
                return true;
            }
        }
        this.value = value;
        return false;
    }

    public boolean fallingAction(boolean value){
        if(value != this.value){
            if(!value){
                this.value = false;
                return true;
            }
        }
        this.value = value;
        return false;
    }
}
