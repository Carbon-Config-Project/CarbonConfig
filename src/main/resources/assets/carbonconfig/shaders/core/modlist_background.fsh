
#version 150 core

uniform float GameTime; 
uniform vec2 ScreenSize;
uniform vec3 Offset; //z is zoom

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

#define iTime GameTime

const vec3 colors[] = vec3[](vec3(0.6, 0.2, 0.2), vec3(0.6, 0.6, 0.2), vec3(0.6, 0.2, 0.6));

vec3 nrand3(vec2 co)
{
	vec3 a = fract(cos(co.x*8.3e-3 + co.y) * vec3(1.25e5, 4.8e6, 2.5e5));
	vec3 b = fract(sin(co.x*0.5e2 + co.y) * vec3(8.1e5, 1.0e6, 0.1e5));
	vec3 c = mix(a, b, 0.5);
	return c;
}

vec4 starLayer(vec2 p, float time, vec3 color)
{
	vec2 seed = 1.9 * p.xy;
	seed = floor(seed * max(ScreenSize.x, 600.0) / 5.5);
	vec3 rnd = nrand3(seed);
	vec4 col = vec4(pow(rnd.y, 16.0));
	float mul = 10.0 * rnd.x;
	col.xyz *= sin(time * mul + mul) * 0.5 + 1.35;
	return col * vec4(color, 1.0);
}

void main()
{
	vec2 uv = 2.0 * gl_FragCoord.xy / ScreenSize.xy;
  	vec2 uvs = uv * ScreenSize.xy / max(ScreenSize.x, ScreenSize.y);
    vec3 p0 = vec3(uvs / Offset.z, 0.0);
    vec4 finalColor = vec4(0.0f);
    for(int i = 0, n = 3; i<n; i++) {
        vec3 px = p0 + nrand3(vec2(i * 16, i + 48)) + ( i == 0 ? (Offset * 0.01) : (i == 1 ? Offset * 0.32 : Offset * -0.13));
        finalColor += starLayer(px.xy, iTime, colors[i]);
    }
    fragColor = finalColor;
}