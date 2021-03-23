package graphics.renderer.postprocess;

public enum PostProcessConstants {
    //reflection
    REFLECT(1),
    REFRACT(1),

    //Constant manipulation
    ADD_CONSTANT(1),
    SUBTRACT_CONSTANT(1),
    DIVIDE_CONSTANT(1),
    MULTIPLY_CONSTANT(1),

    //Channel ALLOCATOR
    ;

    protected int numParams;
    private PostProcessConstants(int numParams){
        this.numParams = numParams;
    }

    public int getNumberOfExpectedParameters(){
        return numParams;
    }
}
