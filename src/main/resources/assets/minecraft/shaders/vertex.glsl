#version 320 es

// Alta precisão para posições e matrizes para evitar bugs geométricos em blocos distantes
precision highp float;

in vec3 aPos;
in vec2 aTexCoord;

// Média precisão para vetores de textura economiza largura de banda na Mali-G52
out mediump vec2 vTexCoord;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);
    vTexCoord = aTexCoord;
}
