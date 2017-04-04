package skybox;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import entities.Camera;

import org.joml.Vector3f;
import renderEngine.MasterRenderer;
import shaders.ShaderProgram;
import utils.Maths;

public class SkyboxShader extends ShaderProgram{

	private static final String VERTEX_FILE = "src/skybox/skybox.vert";
	private static final String FRAGMENT_FILE = "src/skybox/skybox.frag";

    private static float ROTATE_SPEED = 1f;

	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_fogColour;
	private int location_cubeMap;
	private int location_cubeMap2;
	private int location_blendFactor;

    private float rotation = 0;

	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix){
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(Camera camera){
		Matrix4f matrix = Maths.createViewMatrix(camera);
		matrix.setColumn(3, new Vector4f(0.0f,0.0f,0.0f, 1.0f )); // m33 is always 1
        //matrix.m30 = 0;
        //matrix.m31 = 0;
        //matrix.m32 = 0;

        rotation += ROTATE_SPEED * MasterRenderer.getFrameTimeSeconds();
        matrix.rotate((float) Math.toRadians(rotation), new Vector3f(0, 1, 0), matrix);
        //Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0, 1, 0), matrix, matrix);
		super.loadMatrix(location_viewMatrix, matrix);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		location_cubeMap = super.getUniformLocation("cubeMap");
		location_cubeMap2 = super.getUniformLocation("cubeMap2");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

    public void loadFogColour(float r, float g, float b) {
        super.loadVector(location_fogColour, new Vector3f(r, g, b));
    }

    public void connectTextureUnits() {
        super.loadInt(location_cubeMap, 0);
        super.loadInt(location_cubeMap2, 1);
    }

    public void loadBlendFactor(float factor) {
        super.loadFloat(location_blendFactor, factor);
    }
}
