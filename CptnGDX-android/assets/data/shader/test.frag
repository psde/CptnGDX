#ifdef GL_ES
	#define LOWP lowp
	precision mediump float
#else
	#define LOWP
#endif

varying vec4 LOWP v_color;
varying vec2 v_texCoords; 

uniform sampler2D u_texture; 

uniform float delta;

void main()                                   
{
	vec4 c = v_color * texture2D(u_texture, v_texCoords);
	c.r *= sin(delta);
	gl_FragColor = c;
}