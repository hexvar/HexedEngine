#version 330 core

const int lights = 6;

in vec3 position;
in vec2 textureCoords;
in vec3 normal;

out vec2 pass_textureCoords;
out vec3 surfaceNormal;
out vec3 toLightVector[lights];
out vec3 toCameraVector;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition[lights];
uniform float fakeLightning;

uniform float numberOfRows;
uniform vec2 offset;

const float density = 0.002;
const float gradient = 5.0;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    vec4 positionRelativeToCamera = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCamera;
	pass_textureCoords = (textureCoords / numberOfRows) + offset;

	vec3 actualNormal = normal;
	if (fakeLightning > 0.5) {
	    actualNormal = vec3(0.0, 1.0, 0.0);
	}

	surfaceNormal = (transformationMatrix * vec4(actualNormal, 0.0)).xyz;

	for (int i = 0; i < lights; i++) {
	    toLightVector[i] = lightPosition[i] - worldPosition.xyz;
    }

	toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

	float distance = length(positionRelativeToCamera.xyz);
	visibility =  exp(-pow((distance * density), gradient));
	//visibility = clamp(visibility, 0.0, 1.0);
	visibility = (visibility > 1.0)? 1.0 : (visibility < 0.0)? 0.0 : visibility;
}