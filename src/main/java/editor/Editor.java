package editor;

import editor.components.UIComponet;
import editor.components.container.Axis;
import editor.components.container.Transform;
import engine.FraudTek;
import graphics.renderer.Renderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callbacks.ImStrConsumer;
import imgui.callbacks.ImStrSupplier;
import imgui.enums.*;
import imgui.gl3.ImGuiImplGl3;
import input.MousePicker;
import org.joml.Vector2f;

import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;

public class Editor {

    //Singleton Reference
    public static Editor editor;

    //GL instance
    private static ImGuiImplGl3 IMGUIGL;
    private ImGuiIO io;

    // Mouse cursors provided by GLFW
    private static final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    //All of our UIComponets
    private LinkedList<UIComponet> UIComponets = new LinkedList<>();

    private Editor(){
        //Create imgui context
        ImGui.createContext();

        //Now that we have our instance
        // Initialize ImGuiIO config
        io = ImGui.getIO();

        io.setIniFilename(null); // We don't want to save .ini file
        io.setConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Navigation with keyboard
        io.setBackendFlags(ImGuiBackendFlags.HasMouseCursors); // Mouse cursors to display while resizing windows etc.
        io.setBackendPlatformName("imgui_java_impl_glfw"); // For clarity reasons
        io.setBackendRendererName("imgui_java_impl_lwjgl"); // For clarity reasons

        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
        final int[] keyMap = new int[ImGuiKey.COUNT];
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;
        io.setKeyMap(keyMap);

        // Mouse cursors mapping
        mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);

        // ------------------------------------------------------------
        // Here goes GLFW callbacks to update user input in Dear ImGui

        glfwSetKeyCallback(FraudTek.WINDOW_POINTER, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        });

        glfwSetCharCallback(FraudTek.WINDOW_POINTER, (w, c) -> {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetScrollCallback(FraudTek.WINDOW_POINTER, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(FraudTek.WINDOW_POINTER, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                return glfwGetClipboardString(FraudTek.WINDOW_POINTER);
            }
        });

        //Link our context to this GL window.
        IMGUIGL = new ImGuiImplGl3();
        IMGUIGL.init();


//        //Add our UIComponets

    }

    //Public interfaces
    public void resize(){

    }

    public void addComponent(UIComponet UIComponet){
        this.UIComponets.addLast(UIComponet);
        UIComponet.onAdd();
    }

    public void onClick(int button, int action, int mods){

        final boolean[] mouseDown = new boolean[5];

        mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
        mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
        mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
        mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
        mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

        io.setMouseDown(mouseDown);

        if (!io.getWantCaptureMouse() && mouseDown[1]) {
            ImGui.setWindowFocus(null);
        }
    }

    public void update(double delta){
        //Update display size
        final ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(Renderer.getInstance().getWIDTH(), Renderer.getInstance().getHEIGHT());
        io.setDisplayFramebufferScale((float) 1, (float) 1);
        Vector2f mousePos = new Vector2f(MousePicker.getInstance().getScreenCoords());
        //Y axis garbo
        mousePos.sub(0, Renderer.getInstance().getHEIGHT());
        mousePos.mul(1, -1);


        io.setMousePos((float) mousePos.x(), (float) mousePos.y());
        io.setDeltaTime((float) delta);

        // Update mouse cursor
        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(FraudTek.WINDOW_POINTER, mouseCursors[imguiCursor]);
        glfwSetInputMode(FraudTek.WINDOW_POINTER, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

        //Update all UIComponets
        //Buffer at start
        LinkedList<UIComponet> safeItterate = new LinkedList<>(UIComponets);
        for(UIComponet UIComponet : safeItterate){
            UIComponet.update(delta);
        }
    }

    public void render(){
        Renderer.getInstance().render();

        ImGui.newFrame();
        ImGui.setNextWindowSize(600, 300, ImGuiCond.Once);
        ImGui.setNextWindowPos(10, 10, ImGuiCond.Once);

        ImGui.begin("Custom window");  // Start Custom window
        //Render our UIComponets
        LinkedList<UIComponet> safeItterate = new LinkedList<>(UIComponets);
        for(UIComponet UIComponet : safeItterate){
            UIComponet.render();
        }
        ImGui.end();


        ImGui.showDemoWindow();

        ImGui.render();
        IMGUIGL.render(ImGui.getDrawData());
    }

    public void onShutdown() {
        IMGUIGL.dispose();
        ImGui.destroyContext();
    }

    //Singleton methods
    public static void initialize(){
        if(editor == null){
            editor = new Editor();
        }
    }

    public static Editor getInstance(){
        return editor;
    }

}
