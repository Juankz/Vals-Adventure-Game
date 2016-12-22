#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

uniform vec2 resolution;
uniform float radius;

const float SOFTNESS = 0.05;
const float VIGNETTE_OPACITY = 1.0;

void main() {
	vec4 texColor = texture2D(u_texture, v_texCoord);
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
	position.x *= resolution.x / resolution.y;
	float len = length(position);
	float vignette = smoothstep(radius, radius-SOFTNESS, len);
	texColor.rgb = mix(texColor.rgb, texColor.rgb * vignette, VIGNETTE_OPACITY);
	float a = v_color.w;
	a = step(0.05,a);
	texColor.w=a*texColor.w;
    gl_FragColor = vec4(texColor.r, texColor.g, texColor.b, texColor.a);
}