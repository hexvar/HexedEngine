package textures;

public class ModelTexture {

    private int textureID;

    private float shineDamper = 1;
    private float reflectivity = 0;

    private boolean transparent = false;
    private boolean fakeLighting = false;

    private int numberOfRows = 1;

    public ModelTexture(int id) {
        this.textureID = id;
    }

    public int getID() {
        return textureID;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    public boolean isFakeLighting() {
        return fakeLighting;
    }

    public void setFakeLighting(boolean fakeLighting) {
        this.fakeLighting = fakeLighting;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }
}
