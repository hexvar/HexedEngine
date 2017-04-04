package engineTest;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.TexturedModel;
import renderEngine.*;
import models.RawModel;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joml.Vector3f;

public class testGame {
    private static Logger log;
    static Loader loader = new Loader();
    static BufferedImage imageBlend = null;

    public static void loadBlendMap() {
        try {
            imageBlend = ImageIO.read(new File("res/blendMap.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        log = Logger.getLogger("test");
        log.setLevel(Level.INFO);

        log.info("Loading masterRender...");
        MasterRenderer masterRenderer = new MasterRenderer(loader);

        loadBlendMap();
        log.info("Loading textures...");
        TexturedModel modelStall = loadModel("stall", "stall", 7.0f, 0.1f);
        TexturedModel modelTreeLowPoly = loadModel("lowPolyTree", "lowPolyTree", 10.0f, 0.1f);
        TexturedModel modelTree = loadModel("pine", "pine", 2.0f, 0.1f);
        TexturedModel modelFern = loadModel("fern", "fern", 3.0f, 0.1f);
        modelFern.getTexture().setNumberOfRows(2);
        modelFern.getTexture().setFakeLighting(false);
        TexturedModel modelLamp = loadModel("lamp", "lamp", 2.0f, 0.1f);
        modelLamp.getTexture().setFakeLighting(true);
        TexturedModel modelHouse = loadModel("house", "white", 2.0f, 0.1f);
        TexturedModel modelGrass = loadModel("grassModel", "grassTexture", 7.0f, 0.3f);
        modelGrass.getTexture().setFakeLighting(true);
        modelGrass.getTexture().setTransparent(true);

        Light light = new Light(new Vector3f(0, 1000, -6000), new Vector3f(.7f, .7f, .7f));
        List<Light> lights = new ArrayList<Light>();
        lights.add(light);

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        Terrain terrain =  new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");
        Terrain terrain2 = new Terrain(1, -1, loader, texturePack, blendMap, "heightmap");

        Terrain[][] terrains = new Terrain[2][2];
        terrains[0][0] = terrain;
        terrains[1][0] = terrain2;

        List<Entity> entities = new ArrayList<Entity>();
        Random random = new Random(676452);

        float xx = random.nextFloat() * 800;
        float zz = random.nextFloat() * -800;
        entities.add(new Entity(modelHouse, random.nextInt(4), new Vector3f(xx, terrain.getHeightOfTerrain(xx, zz), zz), 0,
                random.nextFloat() * 360, 0, 6f));

        for (int i = 0; i < 3000 ; i++) {
            float x = random.nextFloat() * 800;
            float z = random.nextFloat() * -800;
            float y = terrain.getHeightOfTerrain(x, z);

            if (onPath(x, z))
                continue;

            if (i % 3 == 0) {
                entities.add(new Entity(modelGrass, random.nextInt(4), new Vector3f(x, y, z), 0,
                        random.nextFloat() * 360, 0, 0.9f));
            }
            else if (i % 5 == 0) {
                entities.add(new Entity(modelFern, random.nextInt(4), new Vector3f(x, y, z), 0,
                        random.nextFloat() * 360, 0, 0.9f));
            }
            else if (i % 7 == 0) {
                entities.add(new Entity(modelTree, new Vector3f(x, y, z), 0,
                        random.nextFloat() * 360, 0, random.nextFloat() * 2f + 0.6f));
            }
            else if (i % 79 == 0) {
                if (random.nextFloat() > 0.5f)
                    continue;
                entities.add(new Entity(modelLamp, new Vector3f(x, y, z), 0,
                        random.nextFloat() * 360, 0, 1f));
                lights.add(new Light(new Vector3f(x, y + 12.8f, z), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
            }
        }

        RawModel pModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel texturedpModel = new TexturedModel(pModel, new ModelTexture(loader.loadTexture("playerTexture")));
        Player player = new Player(texturedpModel, new Vector3f(100, 0, -50), 0, 0, 0, 1);

        Camera camera = new Camera(player);

        masterRenderer.initShaders();

        log.info("Starting rendering loop...");
        int i = 0;
        while (! MasterRenderer.isClosing()) {
            //System.out.println( "(" + player.getPosition().x + "," + player.getPosition().y + "," + player.getPosition().z + ")");
            sortLights(lights, player.getPosition());

            int gridX = (int) (player.getPosition().x / Terrain.SIZE);
            int gridZ = (int) (player.getPosition().z / Terrain.SIZE);
            Terrain currentTerrain = terrains[gridX][gridZ];

            camera.move();
            player.move(currentTerrain);

            //masterRenderer.processEntity(player);
            //masterRenderer.processTerrain(terrain);
            //masterRenderer.processTerrain(terrain2);

            //for (Entity e:entities) {
                //e.increaseRotation(0, 1, 1);
            //    masterRenderer.processEntity(e);
            //}

            masterRenderer.render(lights, camera);
            masterRenderer.updateGL();
            masterRenderer.updateTime();
            if (i++%256 == 0)
                System.out.println("fps: " + Math.round(1f / MasterRenderer.getFrameTimeSeconds()));
        }

        masterRenderer.cleanUpShaders();
        loader.cleanUp();
        MasterRenderer.closeGL();
    }

    public static float getDistanceSquared(Vector3f a, Vector3f b)
    {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        float dz = a.z - b.z;

        return (float) (dx * dx + dy * dy + dz * dz);
    }

    // TODO select only the lights in the player fov
    private static void sortLights(List<Light> lights, final Vector3f pos) {
        Comparator<Light> LightComparator = new Comparator<Light>()  {
            @Override
            public int compare(Light x, Light y) {
                return getDistanceSquared(x.getPostion(), pos) <
                        getDistanceSquared(y.getPostion(), pos) ? -1:1;
            }
        };

        Light first = lights.get(0);
        lights.remove(0);
        Collections.sort(lights, LightComparator);
        lights.add(0, first);
    }

    private static boolean onPath(float x, float z) {
        float ratioX = imageBlend.getHeight() / 800f;
        float ratioY = imageBlend.getWidth() / 800f;
        int imgX = (int) (ratioX * (x) );
        int imgY = (int) (ratioY * (z + 800f));

        if (imgX < 0 || imgX >= imageBlend.getHeight() || imgY < 0 || imgY >= imageBlend.getWidth()) {
            return false;
        }

        int blue = imageBlend.getRGB(imgX, imgY) & 0x000000ff;
        if (blue > 0) {
            return true;
        }

        return false;
    }

    private static TexturedModel loadModel(String modelName, String textureName,
                             float damper, float reflectivity) {
        RawModel model = OBJLoader.loadObjModel(modelName, loader);
        TexturedModel texturedModel = new TexturedModel(model, new ModelTexture(loader.loadTexture(textureName)));
        ModelTexture texture = texturedModel.getTexture();
        texture.setShineDamper(damper);
        texture.setReflectivity(reflectivity);

        return texturedModel;
    }
}
