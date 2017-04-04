package renderEngine;

import entities.Camera;
import entities.Entity;
import entities.Light;
import gui.GuiRenderer;
import gui.GuiTexture;
import models.TexturedModel;

import org.joml.Vector2f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import shaders.StaticShader;
import terrains.TerrainRenderer;
import terrains.TerrainShader;
import skybox.SkyboxRenderer;
import terrains.Terrain;

import java.nio.IntBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class MasterRenderer {
    private static Logger log;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS_CAP = 120;
    private static String title;
    private static long window;
    private Loader loader;

    private static long lastFrameTime;
    private static float delta;

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000f;

    private static final float RED = 0.4f;
    private static final float GREEN = 0.4f;
    private static final float BLUE = 0.4f;

    Matrix4f projectionMatrix;

    private StaticShader shader;
    private GuiRenderer guiRenderer;
    private SkyboxRenderer skyboxRenderer;
    private EntityRenderer entityRenderer;  // move shader inside renderer ?
    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader;  // move shader inside renderer ?
    private List<Terrain> terrains;
    private Map<TexturedModel, List<Entity>> entities;


    public MasterRenderer(Loader loader) {
        log = Logger.getLogger("MasterRenderer");
        log.setLevel(Level.INFO);
        initGL();
        this.loader = loader;
    }

    public static void initGL() {
        log.info("Initializing GL");
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        log.info("LWJGL: " + Version.getVersion() + "!");

        //glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        title = new String("hex");
        window = glfwCreateWindow(WIDTH, HEIGHT, title, 0, 0);
        if (window == GLFW_FALSE) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true);
        });

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window,(vidmode.width() - pWidth.get(0)) / 2,(vidmode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
        glClearColor(0.5f, 0.1f, 0.5f, 0.0f);
        //GL11.glViewport(0, 0, WIDTH, HEIGHT);
        lastFrameTime = getCurrentTime();
    }

    public void initShaders() {
        shader = new StaticShader();
        terrainShader = new TerrainShader();
        entities = new HashMap<TexturedModel, List<Entity>>();
        terrains = new ArrayList<Terrain>();

        enableCulling();
        createProjectionMatrix();

        GL11.glEnable (GL11.GL_LINE_SMOOTH);
        GL11.glEnable (GL11.GL_BLEND);

        //GL11.glShadeModel(GL_SMOOTH); // not available on intel?

        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

        entityRenderer = new EntityRenderer(shader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);

        guiRenderer = new GuiRenderer(loader);
    }

    public void loadGUI() {
        //List<GuiTexture> guis = new ArrayList<GuiTexture>();
        //GuiTexture gui = new GuiTexture(loader.loadTexture("cube"), new Vector2f(-0.85f, 0.8f), new Vector2f(0.10f, 0.15f));
        //guis.add(gui);
        //GuiTexture gui = new GuiTexture(loader.loadText("prova"), new Vector2f(-0.80f, 0.9f), new Vector2f(0.10f, 0.15f));
        //gui.add(gui);
    }

    public static void updateGL() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public static void updateTime() {
        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrameTime) / 1000f;
        lastFrameTime = currentFrameTime;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static long getWindow() { return window; }

    public static float getFrameTimeSeconds() {
        return delta;
    }

    private static long getCurrentTime() {
        return Instant.now().toEpochMilli();
    }

    public static boolean isClosing() {
        return glfwWindowShouldClose(window);
    }

    public static void closeGL() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void render(List<Light> lights, Camera camera) {
        prepareGL();

        shader.start();
        shader.loadSkyColour(RED, GREEN, BLUE);
        shader.loadLights(lights);
        shader.loadViewMatrix(camera);
        //entityRenderer.render(entities);
        shader.stop();

        /*terrainShader.start();
        terrainShader.loadSkyColour(RED, GREEN, BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();*/

        skyboxRenderer.render(camera, RED, GREEN, BLUE);

        //guiRenderer.render(guis);

        //terrains.clear();
        //entities.clear();
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processEntity (Entity entity) {
        TexturedModel entityModel = entity.getModel();
        List<Entity> batch = entities.get(entityModel);
        if (batch != null) {
            batch.add(entity);
        }
        else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }
    }

    public void prepareGL() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(RED, GREEN, BLUE, 1);
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) getWidth() / (float) getHeight();
        float y_scale = (float) ((1f /Math.tan(Math.toRadians(FOV /  2f))) * aspectRatio);
        float x_scale = y_scale / aspectRatio;
        float frustum_length = FAR_PLANE - NEAR_PLANE;

        projectionMatrix = new Matrix4f();
        projectionMatrix.set(x_scale,0,0,0,
                             0,y_scale,0,0,
                             0,0,-((FAR_PLANE + NEAR_PLANE)) / frustum_length,-1,
                             0,0,-((2 * NEAR_PLANE * FAR_PLANE)) / frustum_length,0);
    }

    public static void enableCulling() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void disableCulling() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    public void cleanUpShaders() {
        shader.cleanUp();
        terrainShader.cleanUp();
        guiRenderer.cleanUpShaders();
    }
}
