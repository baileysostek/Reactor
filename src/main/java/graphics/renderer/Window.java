package graphics.renderer;

import engine.Reactor;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL46;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Window {

    private long window_p;
    private int width;
    private int height;
    private boolean fullscreen = false;
    private boolean resize = true;
    private String title = "";
    private int vSync = 0;

    private float aspectRatio = 1;

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

    public Window maximize(){
        glfwMaximizeWindow(window_p);
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

    //Taken from:https://gamedev.stackexchange.com/questions/105555/setting-window-icon-using-glfw-lwjgl-3
    public void setWindowIcon(String path){
        IntBuffer w = memAllocInt(1);
        IntBuffer h = memAllocInt(1);
        IntBuffer comp = memAllocInt(1);

        // Icons
        {
            ByteBuffer icon16;
            ByteBuffer icon32;
            try {
                icon16 = StringUtils.loadRaw(StringUtils.getPathToResources() + path, 2048);
                icon32 = StringUtils.loadRaw(StringUtils.getPathToResources() + path, 4096);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            try ( GLFWImage.Buffer icons = GLFWImage.malloc(2) ) {
                ByteBuffer pixels16 = STBImage.stbi_load_from_memory(icon16, w, h, comp, 4);
                icons
                        .position(0)
                        .width(w.get(0))
                        .height(h.get(0))
                        .pixels(pixels16);

                ByteBuffer pixels32 = STBImage.stbi_load_from_memory(icon32, w, h, comp, 4);
                icons
                        .position(1)
                        .width(w.get(0))
                        .height(h.get(0))
                        .pixels(pixels32);

                icons.position(0);
                glfwSetWindowIcon(window_p, icons);

                STBImage.stbi_image_free(pixels32);
                STBImage.stbi_image_free(pixels16);
            }
        }

        memFree(comp);
        memFree(h);
        memFree(w);
    }

    private void setup(){
        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_SAMPLES, 4);

        if(this.resize) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        }else{
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable
        }

        if(Reactor.isDev()) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        }

        //Disable top window bar
//        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        // Create the window
        if(fullscreen) {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            this.width = mode.width();
            this.height = mode.height();
            this.window_p = glfwCreateWindow(this.width, this.height, this.title, monitor, NULL);
            if (window_p == NULL)
                throw new RuntimeException("Failed to create the GLFW window");
        }else{
            this.window_p = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
            if (window_p == NULL)
                throw new RuntimeException("Failed to create the GLFW window");
        }

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
                    GL46.glViewport(0, 0, width, height);
                    Renderer.getInstance().resize(width, height);
                    System.out.println("The window changed size, the aspect ratio is "+aspectRatio);
                }
            }
        });
    }

    public long getWindow_p(){
        return this.window_p;
    }

    public void setFullscreen(boolean full) {
        this.fullscreen = full;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
