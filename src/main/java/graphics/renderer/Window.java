package graphics.renderer;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long window_p;
    private int width;
    private int height;
    private boolean fullscreen = false;
    private boolean resize = true;
    private String title = "";
    private int vSync = 0;

    private float aspectRatio = 1;

    public Window(int width, int height){
        this.width = width;
        this.height = height;
        setup();
    }

    public Window(int width, int height, boolean fullscreen){
        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
        setup();
    }

    public Window(int width, int height, boolean fullscreen, boolean resize){
        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
        this.resize = resize;
        setup();
    }

    public Window setTitle(String title){
        this.title = title;
        glfwSetWindowTitle(window_p, this.title);
        return this;
    }

    public Window setResize(boolean resize){
        this.resize = resize;
        if(resize) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        }else{
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        }
        return this;
    }

    public void setVsyncEnabled(boolean vsync) {
        if(vsync){
            vSync = 1;
        }else{
            vSync = 0;
        }
        // Enable v-sync
        glfwSwapInterval(vSync);
    }

    private void setup(){
        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation

        if(this.resize) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        }else{
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        }

        // Create the window
        this.window_p = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if ( window_p == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window_p, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window_p, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(window_p, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window_p);
        // Enable v-sync
        glfwSwapInterval(vSync);

        // Make the window visible
        glfwShowWindow(window_p);

        // Create the framebuffer size callback
        glfwSetFramebufferSizeCallback(window_p, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long windowId, int width, int height) {
                // Update the window size
                if(width > 0 && height > 0){
                    glfwSetWindowSize(window_p, width, height);
                    aspectRatio = (float)width / (float)height;
                    GL11.glViewport(0, 0, width, height);
                    Renderer.getInstance().resize(width, height);
                    System.out.println("The window changed size, the aspect ratio is "+aspectRatio);
                }
            }
        });
    }

    public long getWindow_p(){
        return this.window_p;
    }
}
