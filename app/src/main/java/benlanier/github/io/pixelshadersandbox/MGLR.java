package benlanier.github.io.pixelshadersandbox;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MGLR implements GLSurfaceView.Renderer {

    float[] perspM = new float[16];
    float[] mvM = new float[16];

    private static final String vertexShaderSrc =
            "attribute vec3 aVertexPosition;\n" +
            "\n" +
            "uniform mat4 uMVMatrix;\n" +
            "uniform mat4 uPMatrix;\n" +
            "\n" +
            "void main(void) {\n" +
            "  gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);\n" +
            "}";

    private static final String fragShaderSrc =
            "void main(void) {\n" +
            "    gl_FragColor = vec4(gl_FragCoord.x / 500.0, 0.0, 1.0, 1.0);\n" +
            "}";

    private int program;
    private int vertexPositionAttribute;

    private int[] vtxBufHandle = new int[1];
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderSrc);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        vertexPositionAttribute = GLES20.glGetAttribLocation(program, "aVertexPosition");
        GLES20.glEnableVertexAttribArray(vertexPositionAttribute);

        FloatBuffer fb = FloatBuffer.wrap(new float[]{1f, 1f, 0f, -1f, 1f, 0f, 1f, -1f, 0f, -1f, -1f, 0f});
        GLES20.glGenBuffers(1, vtxBufHandle, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vtxBufHandle[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, fb.capacity() * 4, fb, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.frustumM(perspM, 0, -1, 1, -1, 1, .1f, 100.f);
        Matrix.setIdentityM(mvM, 0);
        Matrix.translateM(mvM, 0, 0, 0, -.1f);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vtxBufHandle[0]);

        GLES20.glVertexAttribPointer(vertexPositionAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uPMatrix"), 1, false, perspM, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVMatrix"), 1, false, mvM, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public static int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        Log.d("blanierblanier", "shader compile log: " + GLES20.glGetShaderInfoLog(shader));

        return shader;
    }
}
