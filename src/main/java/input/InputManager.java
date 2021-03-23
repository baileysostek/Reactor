package input;

public class InputManager {
    private static InputManager singleton;

    private InputManager() {

    }



    public static void initialize() {
        if (singleton == null) {
            singleton = new InputManager();
        }
    }

    public static InputManager getInstance() {
        return singleton;
    }
}
