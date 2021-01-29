package editor;

import camera.Camera;
import camera.Camera3D;
import camera.CameraManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import editor.components.UIComponet;
import engine.Reactor;
import entity.*;
import entity.component.Attribute;
import graphics.renderer.DirectDraw;
import graphics.renderer.FBO;
import graphics.renderer.Renderer;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.callbacks.ImStrConsumer;
import imgui.callbacks.ImStrSupplier;
import imgui.enums.*;
import imgui.gl3.ImGuiImplGl3;
import input.Keyboard;
import input.MousePicker;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import platform.EnumDevelopment;
import serialization.SerializationHelper;
import util.Callback;
import util.Debouncer;
import util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.*;

public class Editor {

    //Singleton Reference
    public static Editor editor;

    //GL instance
    private static ImGuiImplGl3 IMGUIGL;
    private ImGuiIO io;

    // Mouse cursors provided by GLFW
    private static final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT];

    //All of our UIComponets
    private HashMap<EnumEditorLocation,  LinkedList<UIComponet>> UIComponets = new HashMap<EnumEditorLocation,  LinkedList<UIComponet>>();

    private static int IDs = 1;

    //Editor config file
    JsonObject config;

    //Tab selected
    private int selectedTab = 0;

    //Our Components
    ResourcesViewer resourcesViewer;
    EntityEditor entityEditor;

    //Play Pause
    Debouncer playPause = new Debouncer(false);

    //Store variables for later
    private Camera editorCamera = new Camera3D();
    private Camera gameCamera   = null;

    //If this is the first time the window is rendering, we can set the window sizing information
    private final int COLUMN_WIDTHS = 256;
    private boolean firstDraw = true;

    //Entity Registry
    private EntityRegistry registry;

    //FBO associated with the elements
    private FBO overlay;

    //Camera constants
    private float rotationSpeed = 45f;
    private float cameraSpeed = 0.1f;


    private enum RightClickAction{
        ORBIT,
        FIRST_PERSON
    }

    private Attribute<RightClickAction> rightClickAction = new  Attribute<RightClickAction>("Editor Style", RightClickAction.FIRST_PERSON);

    //Hooks for editor
    private LinkedList<Callback> onPlayCallbacks   = new LinkedList<>();
    private LinkedList<Callback> onEditorCallbacks = new LinkedList<>();

    int gameWindowID = 0;

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

        //Set Inital colors
        ImGui.getStyle().setColor(ImGuiCol.Text,                  0.90f, 0.90f, 0.90f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.TextDisabled,          0.60f, 0.60f, 0.60f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.WindowBg,              0.00f, 0.00f, 0.00f, 0.70f);
        ImGui.getStyle().setColor(ImGuiCol.ChildBg,               0.00f, 0.00f, 0.00f, 0.00f);
        ImGui.getStyle().setColor(ImGuiCol.PopupBg,               0.11f, 0.11f, 0.14f, 0.92f);
        ImGui.getStyle().setColor(ImGuiCol.Border,                0.50f, 0.50f, 0.50f, 0.50f);
        ImGui.getStyle().setColor(ImGuiCol.BorderShadow,          0.00f, 0.00f, 0.00f, 0.00f);
        ImGui.getStyle().setColor(ImGuiCol.FrameBg,               0.43f, 0.43f, 0.43f, 0.39f);
        ImGui.getStyle().setColor(ImGuiCol.FrameBgHovered,        0.82f, 0.54f, 0.00f, 0.40f);
        ImGui.getStyle().setColor(ImGuiCol.FrameBgActive,         0.64f, 0.42f, 0.00f, 0.69f);
        ImGui.getStyle().setColor(ImGuiCol.TitleBg,               0.54f, 0.36f, 0.00f, 0.83f);
        ImGui.getStyle().setColor(ImGuiCol.TitleBgActive,         0.63f, 0.42f, 0.00f, 0.87f);
        ImGui.getStyle().setColor(ImGuiCol.TitleBgCollapsed,      0.80f, 0.53f, 0.00f, 0.20f);
        ImGui.getStyle().setColor(ImGuiCol.MenuBarBg,             0.55f, 0.36f, 0.00f, 0.80f);
        ImGui.getStyle().setColor(ImGuiCol.ScrollbarBg,           0.20f, 0.25f, 0.30f, 0.60f);
        ImGui.getStyle().setColor(ImGuiCol.ScrollbarGrab,         0.80f, 0.53f, 0.00f, 0.30f);
        ImGui.getStyle().setColor(ImGuiCol.ScrollbarGrabHovered,  0.80f, 0.53f, 0.00f, 0.40f);
        ImGui.getStyle().setColor(ImGuiCol.ScrollbarGrabActive,   0.80f, 0.53f, 0.00f, 0.60f);
        ImGui.getStyle().setColor(ImGuiCol.CheckMark,             0.90f, 0.90f, 0.90f, 0.50f);
        ImGui.getStyle().setColor(ImGuiCol.SliderGrab,            1.00f, 1.00f, 1.00f, 0.30f);
        ImGui.getStyle().setColor(ImGuiCol.SliderGrabActive,      0.80f, 0.53f, 0.00f, 0.60f);
        ImGui.getStyle().setColor(ImGuiCol.Button,                0.15f, 0.15f, 0.15f, 0.62f);
        ImGui.getStyle().setColor(ImGuiCol.ButtonHovered,         0.71f, 0.47f, 0.00f, 0.79f);
        ImGui.getStyle().setColor(ImGuiCol.ButtonActive,          0.80f, 0.53f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.Header,                0.90f, 0.59f, 0.00f, 0.45f);
        ImGui.getStyle().setColor(ImGuiCol.HeaderHovered,         0.90f, 0.59f, 0.00f, 0.80f);
        ImGui.getStyle().setColor(ImGuiCol.HeaderActive,          0.87f, 0.57f, 0.00f, 0.80f);
        ImGui.getStyle().setColor(ImGuiCol.Separator,             1.00f, 1.00f, 1.00f, 0.60f);
        ImGui.getStyle().setColor(ImGuiCol.SeparatorHovered,      0.70f, 0.46f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.SeparatorActive,       0.90f, 0.59f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.ResizeGrip,            1.00f, 1.00f, 1.00f, 0.16f);
        ImGui.getStyle().setColor(ImGuiCol.ResizeGripHovered,     0.78f, 0.82f, 1.00f, 0.60f);
        ImGui.getStyle().setColor(ImGuiCol.ResizeGripActive,      0.78f, 0.82f, 1.00f, 0.90f);
        ImGui.getStyle().setColor(ImGuiCol.Tab,                   0.40f, 0.40f, 0.40f, 0.79f);
        ImGui.getStyle().setColor(ImGuiCol.TabHovered,            0.79f, 0.79f, 0.79f, 0.80f);
        ImGui.getStyle().setColor(ImGuiCol.TabActive,             0.76f, 0.76f, 0.76f, 0.84f);
        ImGui.getStyle().setColor(ImGuiCol.TabUnfocused,          0.50f, 0.50f, 0.50f, 0.82f);
        ImGui.getStyle().setColor(ImGuiCol.TabUnfocusedActive,    0.50f, 0.50f, 0.50f, 0.84f);
        ImGui.getStyle().setColor(ImGuiCol.PlotLines,             1.00f, 1.00f, 1.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.PlotLinesHovered,      0.90f, 0.70f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.PlotHistogram,         0.90f, 0.70f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.PlotHistogramHovered,  1.00f, 0.60f, 0.00f, 1.00f);
        ImGui.getStyle().setColor(ImGuiCol.TextSelectedBg,        0.00f, 0.00f, 1.00f, 0.35f);
        ImGui.getStyle().setColor(ImGuiCol.DragDropTarget,        1.00f, 1.00f, 0.00f, 0.90f);
        ImGui.getStyle().setColor(ImGuiCol.NavHighlight,          0.45f, 0.45f, 0.90f, 0.80f);
        ImGui.getStyle().setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f);
        ImGui.getStyle().setColor(ImGuiCol.NavWindowingDimBg,     0.80f, 0.80f, 0.80f, 0.20f);
        ImGui.getStyle().setColor(ImGuiCol.ModalWindowDimBg,      0.20f, 0.20f, 0.20f, 0.35f);



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

        glfwSetCharCallback(Reactor.WINDOW_POINTER, (w, c) -> {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetScrollCallback(Reactor.WINDOW_POINTER, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(Reactor.WINDOW_POINTER, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                return glfwGetClipboardString(Reactor.WINDOW_POINTER);
            }
        });

        //Link our context to this GL window.
        IMGUIGL = new ImGuiImplGl3();
        IMGUIGL.init();

//        //Add our UIComponets
        config = new JsonObject();
        config.addProperty("widgetWidth", 256);

        //Init our containers
        for(EnumEditorLocation location : EnumEditorLocation.values()){
            this.UIComponets.put(location, new LinkedList<UIComponet>());
        }

        //populate our editor
        entityEditor = new EntityEditor();
        addComponent(EnumEditorLocation.RIGHT, entityEditor);
        WorldOutliner worldOutliner = new WorldOutliner(entityEditor);
        addComponent(EnumEditorLocation.LEFT_TAB, worldOutliner);
//        LevelEditor levelEditor = new LevelEditor();
//        addComponent(EnumEditorLocation.LEFT_TAB, levelEditor);
        registry = new EntityRegistry(entityEditor);
        addComponent(EnumEditorLocation.LEFT_TAB, registry);
        Settings settings = new Settings();
        settings.addWatchedAttribute(rightClickAction);
        addComponent(EnumEditorLocation.LEFT_TAB, settings);
        resourcesViewer = new ResourcesViewer();
        addComponent(EnumEditorLocation.LEFT_BOTTOM, resourcesViewer);

        Keyboard.getInstance().addPressCallback(Keyboard.P, new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()){
//                    saveProject();
                    Reactor.setDevelopmentLevel(EnumDevelopment.PRODUCTION);
                    onPlay();
                }else{
//                    loadProject();
                    Reactor.setDevelopmentLevel((EnumDevelopment.DEVELOPMENT));
                    onExitPlay();
                }
                return null;
            }
        });

        Keyboard.getInstance().addPressCallback(Keyboard.I, new Callback() {
            @Override
            public Object callback(Object... objects) {
                JsonObject data = EntityManager.getInstance().serialize();
                System.out.println(data);
                return null;
            }
        });

        Keyboard.getInstance().addPressCallback(Keyboard.U, new Callback() {
            @Override
            public Object callback(Object... objects) {
                JsonObject data = EntityManager.getInstance().serialize();
                EntityManager.getInstance().deserializeAndClear(data);
                return null;
            }
        });

        MousePicker.getInstance().addCallback(new Callback() {
            @Override
            public Object callback(Object... objects) {
                if(Reactor.isDev()) {
                    int button = (int) objects[0];
                    int action = (int) objects[1];

                    if (button == MousePicker.MOUSE_RIGHT) {
                        //On Release Set selected to none
                        if (action == GLFW.GLFW_RELEASE) {
                            MousePicker.getInstance().unlockMouse();
                        }
                        if (action == GLFW.GLFW_PRESS) {
                            MousePicker.getInstance().requestLockMouse();
                        }
                    }

                    //For our Orbit calculations.
                    if (button == MousePicker.MOUSE_MIDDLE) {
                        if (action == GLFW.GLFW_RELEASE) {
                            MousePicker.getInstance().unlockMouse();
                        }
                        if (action == GLFW.GLFW_PRESS) {
                            MousePicker.getInstance().requestLockMouse();
                        }
                    }
                }

                return null;
            }
        });

        this.onExitPlay();

    }

    //Public interfaces
    public void resize(){

    }

    public void addComponent(EnumEditorLocation location, UIComponet UIComponet){
        if(!this.UIComponets.get(location).contains(UIComponet)) {
            this.UIComponets.get(location).addLast(UIComponet);
        }
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
//        //Y axis garbo
//        mousePos.sub(0, Renderer.getInstance().getHEIGHT());
//        mousePos.mul(1, -1);

        //Mouse input
        io.setMousePos((float) mousePos.x(), (float) mousePos.y());
        io.setDeltaTime((float) delta);

        // Update mouse cursor
        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(Reactor.WINDOW_POINTER, mouseCursors[imguiCursor]);
        glfwSetInputMode(Reactor.WINDOW_POINTER, GLFW_CURSOR, GLFW_CURSOR_NORMAL);


        //Update all UIComponets
        //Buffer at start
        for(EnumEditorLocation location : EnumEditorLocation.values()) {
            LinkedList<UIComponet> safeItterate = new LinkedList<>(UIComponets.get(location));
            for (editor.components.UIComponet UIComponet : safeItterate) {
                UIComponet.update(delta);
            }
        }

        //Get the camera
        Camera cam = CameraManager.getInstance().getActiveCamera();

        //Right Click rotation
        if(MousePicker.getInstance().isMousePressed(MousePicker.MOUSE_RIGHT)){
            if(rightClickAction.getData().equals(RightClickAction.FIRST_PERSON)) {
                cam.setRotation(new Vector3f((float) (MousePicker.getInstance().getMouseDeltaY() / (Renderer.getHEIGHT() / 2f)) * rotationSpeed, (float) (MousePicker.getInstance().getMouseDeltaX() / (Renderer.getWIDTH() / 2f)) * rotationSpeed, 0).add(cam.getRotationV()));
            }
            if(rightClickAction.getData().equals(RightClickAction.ORBIT)) {
                //Update Rotation
                Vector3f deltaRot = new Vector3f((float) -(MousePicker.getInstance().getMouseDeltaY() / (Renderer.getHEIGHT() / 2f)) * rotationSpeed, (float) (MousePicker.getInstance().getMouseDeltaX() / (Renderer.getWIDTH() / 2f)) * rotationSpeed, 0);
                //X axis
                Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -90).normalize();
                Vector3f xAxis = (new Vector3f(0, 0, 1).rotate(offsetRot.mul(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -1 * cam.getRotationV().y())).normalize()).normalize());
                cam.setRotation(new Vector3f(deltaRot).add(cam.getRotationV()));
                Vector3f pos = new Vector3f(cam.getPosition());
                pos.rotateAxis((float) Math.toRadians(deltaRot.y), 0, -1, 0);
                pos.rotateAxis((float) Math.toRadians(deltaRot.x), xAxis.x, xAxis.y, xAxis.z);
                cam.setPosition(new Vector3f(pos).mul(-1));

            }
        }

        //Scroll wheel zoom in and out
        if(! ImGui.getIO().getWantCaptureMouse()) {
            cam.translate(cam.getLookingDirection().mul(-ImGui.getIO().getMouseWheel()));
        }

        //Orbit
        if(MousePicker.getInstance().isMousePressed(GLFW_MOUSE_BUTTON_3)){
            if(rightClickAction.getData().equals(RightClickAction.FIRST_PERSON)) {
                //Update Rotation
                Vector3f deltaRot = new Vector3f((float) -(MousePicker.getInstance().getMouseDeltaY() / (Renderer.getHEIGHT() / 2f)) * rotationSpeed, (float) (MousePicker.getInstance().getMouseDeltaX() / (Renderer.getWIDTH() / 2f)) * rotationSpeed, 0);
                //X axis
                Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -90).normalize();
                Vector3f xAxis = (new Vector3f(0, 0, 1).rotate(offsetRot.mul(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -1 * cam.getRotationV().y())).normalize()).normalize());
                cam.setRotation(new Vector3f(deltaRot).add(cam.getRotationV()));
                Vector3f pos = new Vector3f(cam.getPosition());
                pos.rotateAxis((float) Math.toRadians(deltaRot.y), 0, -1, 0);
                pos.rotateAxis((float) Math.toRadians(deltaRot.x), xAxis.x, xAxis.y, xAxis.z);
                cam.setPosition(new Vector3f(pos).mul(-1));
            }
            if(rightClickAction.getData().equals(RightClickAction.ORBIT)) {
                Quaternionf offsetRot = new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -90).normalize();
                cam.translate((new Vector3f(0, MousePicker.getInstance().getMouseDeltaY(), MousePicker.getInstance().getMouseDeltaX()).mul(0.1f)).rotate(offsetRot.mul(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), -1 * cam.getRotationV().y())).normalize()));
            }
        }
    }

    public void preUIRender(){
        if(Reactor.isDev()){
            //Draw Axis
            int size = 4096;
            DirectDraw.getInstance().drawLine(new Vector3f(-size, 0, 0), new Vector3f(size, 0, 0), new Vector3f(1, 0, 0));
            DirectDraw.getInstance().drawLine(new Vector3f(0, -size, 0), new Vector3f(0, size, 0), new Vector3f(0, 1, 0));
            DirectDraw.getInstance().drawLine(new Vector3f(0, 0, -size), new Vector3f(0, 0, size), new Vector3f(0, 0, 1));

            //Draw arrows point in +Axis direction.
//            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(1, 0, 0));
//            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(0, 1, 0));
//            Renderer.getInstance().drawArrow(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0.5f, 0.5f, 1.25f).mul(0.25f), 13, new Vector3f(0, 0, 1));

            //Draw Grid
            size = 50;
            for(int i = -size; i <= size; i++){
                if(i == 0){
                    i++;
                }
                Vector3f color = new Vector3f(0.5f);
                if(i % 10 == 0){
                    color.add(0.5f , 0.5f, 0.5f);
                }
                DirectDraw.getInstance().drawLine(new Vector3f(-size, 0, i), new Vector3f(size, 0, i), color);
                DirectDraw.getInstance().drawLine(new Vector3f(i, 0, -size), new Vector3f(i, 0, size), color);
            }
        }

        //Render our UI elements
        for(EnumEditorLocation location : EnumEditorLocation.values()) {
            for (UIComponet uiComponet : UIComponets.get(location)) {
                uiComponet.preUIRender();
            }
        }
    }

    public void render(){
        //Reset IDs;
        IDs = 1;

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Once);

        //Styles
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1);

//        ImGui.getFont().setScale(2);

        ImGui.setNextWindowSize(Renderer.getInstance().getWIDTH(), Renderer.getInstance().getHEIGHT());
        ImGui.setNextWindowContentSize(Renderer.getInstance().getWIDTH(), Renderer.getInstance().getHEIGHT());
        ImGui.begin("Editor_Window", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar);

        //Padding Var
        ImVec2 padding = new ImVec2();
        ImGui.getStyle().getFramePadding(padding);

        ImGui.columns(3);


        //Set column initial widths
        if(firstDraw) {
            //Set column sizes
            ImGui.setColumnWidth(0, COLUMN_WIDTHS);
            ImGui.setColumnWidth(1, Renderer.getWIDTH() - (COLUMN_WIDTHS * 2.5f));
            ImGui.setColumnWidth(2, COLUMN_WIDTHS * 1.5f);
            //Turn off first draw.
            firstDraw = false;
        }

        //Column 1
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 2);
        ImGui.beginChildFrame(getNextID(), ImGui.getColumnWidth() - (padding.x * 2), ImGui.getWindowHeight()/2);
        //Render our UIComponets
        ImGui.beginTabBar(getNextID()+"", ImGuiTabBarFlags.FittingPolicyDefault_);
        int index = 0;
        ImVec2 availDimensions = new ImVec2();
        ImGui.getContentRegionAvail(availDimensions);
        for(editor.components.UIComponet UIComponet : UIComponets.get(EnumEditorLocation.LEFT_TAB)){
            if(ImGui.beginTabItem(UIComponet.getName(),ImGuiTabBarFlags.FittingPolicyDefault_ | ImGuiTabBarFlags.NoCloseWithMiddleMouseButton)) {
                ImGui.beginChildFrame(getNextID(), ImGui.getColumnWidth() - (padding.x * 2), availDimensions.y);
                UIComponet.setVisable(true);
                UIComponet.render();
                ImGui.endChildFrame();
            }else{
                UIComponet.setVisable(false);
            }
            index++;
            ImGui.endTabItem();
        }
        ImGui.endTabBar();
        ImGui.endChildFrame();
        ImGui.beginChildFrame(getNextID(), ImGui.getColumnWidth() - (padding.x * 2), ImGui.getWindowHeight()/2);
        for(editor.components.UIComponet UIComponet : UIComponets.get(EnumEditorLocation.LEFT_BOTTOM)){
            UIComponet.render();
        }
        ImGui.endChildFrame();
        ImGui.popStyleVar();

        //Col 2
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 2, 2);
        ImGui.nextColumn();
            //Do our column math out here
            float adj_height = ((float) Renderer.getInstance().getHEIGHT() / (float) Renderer.getInstance().getWIDTH()) * ImGui.getColumnWidth();
            float colX = ImGui.getColumnOffset(ImGui.getColumnIndex());
            float colY = 0;

            ImGui.beginChildFrame(getNextID(), ImGui.getColumnWidth()  - (padding.x * 2), ImGui.getWindowHeight());
                ImGui.beginChildFrame(Editor.getInstance().getNextID(), ImGui.getColumnWidth(), adj_height);
                    ImGui.image(Renderer.getInstance().getFrameBuffer().getTextureID(), ImGui.getColumnWidth(), adj_height , 0, 1, 1, 0);
                    if(ImGui.isMouseHoveringRect(colX, colY, colX + ImGui.getColumnWidth(), colY + adj_height)){
                        //Override mouse
                        ImGui.getIO().setWantCaptureMouse(false);
                        //Set Mouse Scale stuff
                        MousePicker.getInstance().setOffset(-colX, 0,  ImGui.getColumnWidth() / Renderer.getInstance().getWIDTH(), adj_height / Renderer.getInstance().getHEIGHT());
                    }
                ImGui.endChildFrame();
                //Render our UIComponets
                renderComponentSet(EnumEditorLocation.CENTER);
            ImGui.endChildFrame();

        ImGui.popStyleVar();

        //Col 3
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 2, 2);
        ImGui.nextColumn();
        ImGui.beginChildFrame(getNextID(), ImGui.getColumnWidth() - (padding.x * 2), ImGui.getWindowHeight());
        //Render our UIComponets
        renderComponentSet(EnumEditorLocation.RIGHT);
        //Render out debug stuff
        ImGui.endChildFrame();
        ImGui.popStyleVar();
        ImGui.columns();

        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.popStyleVar();

        //End
        ImGui.end();

        ImGui.render();
        IMGUIGL.render(ImGui.getDrawData());
    }

    //Register Entities
    public void registerEntity(String category, Entity entity){
        registry.addEntity(category, entity);
    }

//    public void unregisterEntity(String category, Entity entity){
//        registry.addEntity(category, entity);
//    }

    private void renderComponentSet(EnumEditorLocation enumEditorLocation){
        LinkedList<UIComponet> safeItterate = new LinkedList<>(UIComponets.get(enumEditorLocation));
        for(editor.components.UIComponet UIComponet : safeItterate){
            UIComponet.render();
        }
    }

    public void onShutdown() {
        for(EnumEditorLocation location : UIComponets.keySet()){
            for(UIComponet component : new LinkedList<>(UIComponets.get(location))) {
                component.onShutdown();
            }
        }

//        saveProject();

        IMGUIGL.dispose();
        ImGui.destroyContext();
    }

    public void saveProject(){
        //Update our Project file
        JsonObject projectFile = new JsonObject();
        projectFile.addProperty("type", "project");
        //TODO re-implement this so that it saves as much memory as possible.
        JsonArray entities = new JsonArray(EntityManager.getInstance().getEntities().size());
        for(Entity e : EntityManager.getInstance().getEntities()){
            if(!e.hasParent()) {
                JsonObject entityMeta = new JsonObject();
                entityMeta.addProperty("class", e.getClass().getName());
                entityMeta.add("value", e.serialize());
                entities.add(entityMeta);
            }
        }

        projectFile.add("entities", entities);

        projectFile.add("camera", CameraManager.getInstance().getActiveCamera().serialize());

        StringUtils.write(projectFile.toString(), "/scenes/project.tek");
    }

    public void loadProject(){
        EntityManager.getInstance().clearEntities();
        //Load the project
        JsonObject project = StringUtils.loadJson("/scenes/project.tek");
        if(project != null) {
            if (project.has("camera")) {
                CameraManager.getInstance().setActiveCamera(new Camera3D().deserialize(project.getAsJsonObject("camera")));
            }
            if (project.has("entities")) {
                JsonArray entities = project.getAsJsonArray("entities");
                for (int i = 0; i < entities.size(); i++) {
                    if (entities.get(i).getAsJsonObject().has("class")) {
                        try {
                            Class<?> classType = Class.forName(entities.get(i).getAsJsonObject().get("class").getAsString());
                            Entity entity = ((Entity) SerializationHelper.getGson().fromJson(entities.get(i).getAsJsonObject().get("value"), classType)).deserialize(entities.get(i).getAsJsonObject().get("value").getAsJsonObject());

                            EntityManager.getInstance().addEntity(entity);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //Singleton methods
    public static void initialize(){
        if(editor == null){
            editor = new Editor();
        }
    }

    private void onPlay(){
        //restore the camera
        Renderer.getInstance().resize(Renderer.getWIDTH(), Renderer.getHEIGHT());
        if(this.gameCamera != null) {
            CameraManager.getInstance().setActiveCamera(this.gameCamera);
        }else{
            //We dont have a game camera yet, lets look for one, if we dont find one then editor camera will persist as active camera.
//            EntityManager.getInstance().getEntitiesOfType(Camera.class)
        }

        for(Callback c : onPlayCallbacks){
            c.callback();
        }

    }

    private void onExitPlay(){
        //set camera back to our Camera
        if(this.editorCamera != null) {
            CameraManager.getInstance().setActiveCamera(this.editorCamera);
        }

        MousePicker.getInstance().unlockMouse();

        for(Callback c : onEditorCallbacks){
            c.callback();
        }
    }

    public static Editor getInstance(){
        return editor;
    }

    public int getNextID() {
        return IDs++;
    }

    public ResourcesViewer getFileBrowser() {
        return resourcesViewer;
    }

    public void sendKeyEvent(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            io.setKeysDown(key, true);
        } else if (action == GLFW_RELEASE) {
            io.setKeysDown(key, false);
        }

        io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
        io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
        io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
        io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
    }

    //Callback registry
    public void onDragStart(Callback c){
        this.registry.onDragStart(c);
        this.resourcesViewer.onDragStart(c);
    }

    public void onTryDragEntityToWorld(Callback c){
        resourcesViewer.onTryPlaceInWorld(c);
        registry.onTryPlaceInWorld(c);
    }

    public void onActionStart(Callback c){
        this.entityEditor.addOnActionStart(c);
    }

    public void onActionStop(Callback c){
        this.entityEditor.addOnActionStop(c);
    }

    public void onDelete(Callback c){
        this.entityEditor.addOnDelete(c);
    }

    public void onPlayFocus(Callback c){
        this.onPlayCallbacks.addLast(c);
    }

    public void onEditorFocus(Callback c){
        this.onEditorCallbacks.addLast(c);
    }

    public void onClone(Callback c){
        this.entityEditor.addOnClone(c);
    }

    public Collection<Entity> getSelectedEntities() {
        return entityEditor.getSelected();
    }

}
