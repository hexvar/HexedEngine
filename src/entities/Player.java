package entities;


import models.TexturedModel;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWKeyCallback;
import renderEngine.MasterRenderer;
import terrains.Terrain;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

public class Player extends Entity {

    private static final float  RUN_SPEED = 70;
    private static final float  TURN_SPEED = 180;
    private static final float GRAVITY = -98;
    private static final float JUMP_POWER = 40;

    private float currentSpeed = 0;
    private float currentTurnSpeed = 0;
    private float upwardsSpeed = 0;

    private boolean isJumping = false;

    public Player(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(model, position, rotX, rotY, rotZ, scale);
        GLFWKeyCallback checkInput;
        glfwSetKeyCallback(MasterRenderer.getWindow(), checkInput = GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            System.out.println("keyboard event: " + ((char) key));
            if (key == GLFW_KEY_W) {
                this.currentSpeed = RUN_SPEED;
            }
            else if (key == GLFW_KEY_S) {
                this.currentSpeed = -RUN_SPEED;
            }
            else
                this.currentSpeed = 0;

            if (key == GLFW_KEY_A) {
                this.currentTurnSpeed = TURN_SPEED;
            }
            else if (key == GLFW_KEY_D) {
                this.currentTurnSpeed = -TURN_SPEED;
            }
            else
                this.currentTurnSpeed = 0;

            if (key == GLFW_KEY_SPACE)
                jump();
        }));
    }

    public void move(Terrain terrain) {
        super.increaseRotation(0, currentTurnSpeed * MasterRenderer.getFrameTimeSeconds(), 0);
        float distance = currentSpeed * MasterRenderer.getFrameTimeSeconds();
        float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
        float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
        super.increasePosition(dx, 0, dz);

        upwardsSpeed += GRAVITY * MasterRenderer.getFrameTimeSeconds();
        super.increasePosition(0, upwardsSpeed * MasterRenderer.getFrameTimeSeconds(), 0);

        float terrainHeight = terrain.getHeightOfTerrain(super.getPosition().x, super.getPosition().z);
        if (super.getPosition().y < terrainHeight) {
            upwardsSpeed = 0;
            isJumping = false;
            super.getPosition().y = terrainHeight;
        }
    }

    private void jump() {
        if (!isJumping) {
            upwardsSpeed = JUMP_POWER;
            isJumping = true;
        }
    }
}
