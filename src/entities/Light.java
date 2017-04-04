package entities;

import org.joml.Vector3f;

public class Light {

    private Vector3f postion;
    private Vector3f colour;
    private Vector3f attenutation = new Vector3f(1, 0, 0);

    public Light(Vector3f postion, Vector3f colour) {
        this.postion = postion;
        this.colour = colour;
    }

    public Light(Vector3f postion, Vector3f colour, Vector3f attenutation) {
        this.postion = postion;
        this.colour = colour;
        this.attenutation = attenutation;
    }

    public Vector3f getPostion() {
        return postion;
    }

    public void setPostion(Vector3f postion) {
        this.postion = postion;
    }

    public Vector3f getColour() {
        return colour;
    }

    public void setColour(Vector3f colour) {
        this.colour = colour;
    }

    public Vector3f getAttenutation() {
        return attenutation;
    }

    public void setAttenutation(Vector3f attenutation) {
        this.attenutation = attenutation;
    }
}
