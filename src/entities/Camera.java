package entities;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import renderEngine.MasterRenderer;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {

    private float distanceFromPlayer = 120;
    private float angleAroundPlayer = 0;

    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch = 15;
    private float yaw = 0;
    private float roll;

    private Player player;

    public Camera(Player player) {
        this.player = player;
    }

    public void move() {
        //calculateZoom();
        calculatePitch();
        calculateAngleAroundPlayer();

        float horizontalDistance = calculateHorizonalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (player.getRotY() + angleAroundPlayer);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getRoll() {
        return roll;
    }

    private void calculateCameraPosition(float horizontalDistance, float verticalDistance) {
        float theta = player.getRotY() + angleAroundPlayer;
        float offsetX = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));

        position.x = player.getPosition().x - offsetX;
        position.y = player.getPosition().y + verticalDistance;
        position.z = player.getPosition().z - offsetZ;
    }

    private float calculateHorizonalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
    }

    private void calculateZoom() {
        float zoomLevel = 1; //Mouse.getDWheel() * 0.1f;  // scroll_callback...
        distanceFromPlayer -= zoomLevel;
    }

    private void calculatePitch() {
        if (glfwGetMouseButton(MasterRenderer.getWindow(), GLFW_MOUSE_BUTTON_2) != 0) {
            System.out.println("button2_down");
            DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(MasterRenderer.getWindow(), b1, b2);
            float pitchChange = (float)(b2.get(0)) * 0.1f;
            pitch -= pitchChange;
        }
    }

    private void calculateAngleAroundPlayer() {
        if (glfwGetMouseButton(MasterRenderer.getWindow(), GLFW_MOUSE_BUTTON_1) != 0) {
            System.out.println("button1_down");
            DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);
            glfwGetCursorPos(MasterRenderer.getWindow(), b1, b2);
            float angleChange = (float)(b1.get(0)) * 0.3f;
            angleAroundPlayer -= angleChange;
        }
    }
}
