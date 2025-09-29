#version 150 core

uniform float GameTime; 
uniform vec2 ScreenSize;

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

#define iTime GameTime

vec3 hash( in vec3 p )
{
	p = vec3( dot(p,vec3(127.1,311.7, 74.7)),
			  dot(p,vec3(269.5,183.3,246.1)),
			  dot(p,vec3(113.5,271.9,124.6)));

	return -1. + 2.*fract(sin(p*0.05f));
}
float noise( in vec3 p )
{
    vec3 i = floor( p );
    vec3 f = fract( p );
	
	vec3 u = f*f*(3.0-2.0*f);
    vec3 hashed = hash(u);
    return hashed.x * hashed.y * hashed.z;
}

void main()
{
    vec2 uv = texCoord/ScreenSize;
    
    vec3 stars_direction = normalize(vec3(uv * 4.f - 1.0f, 1.0)); 
	float stars_threshold = 400.0f; // modifies the number of stars that are visible
	float stars_exposure = 40000.0f; // modifies the overall strength of the stars
	float stars = pow(clamp(noise(stars_direction * (2000.0f+iTime*0.00025f)), 0.0f, 1.0f), stars_threshold) * stars_exposure;
	stars *= mix(0.15, 1.f, noise(stars_direction * 10.0f + vec3(iTime*.001f)));
	
    float col = cos(texCoord.x * texCoord.y * 0.005f);
    fragColor = vec4(vec3(stars) * vec3(0.6*col*col, 0.2*col*col*col, 0.2 * col*col),1.0);
}