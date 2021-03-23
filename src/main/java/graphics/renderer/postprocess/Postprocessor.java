package graphics.renderer.postprocess;

public class Postprocessor {
    private static Postprocessor singleton;

    private Postprocessor() {

    }

    public static void initialize() {
        if (singleton == null) {
            singleton = new Postprocessor();
        }
    }

    public static Postprocessor getInstance() {
        return singleton;
    }
}
