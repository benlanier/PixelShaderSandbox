package benlanier.github.io.pixelshadersandbox;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MGLR implements GLSurfaceView.Renderer {

    float[] perspM = new float[16];
    float[] mvM = new float[16];

    private static final String vertexShaderSrc =
            "#version 100\n" +
            "attribute vec4 aVertexPosition;\n" +
            "\n" +
            "uniform mat4 uMVMatrix;\n" +
            "uniform mat4 uPMatrix;\n" +
            "\n" +
            "varying mediump vec2 vTextureCoord;\n" +
            "\n" +
            "void main(void) {\n" +
            "  gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition.xy, 0.0, 1.0);\n" +
            "  vTextureCoord = aVertexPosition.zw;\n" +
            "}";

    private static final String fragShaderSrc =
            "#version 100\n" +
            "#extension GL_OES_standard_derivatives : enable\n" +

            "#define PIP2    1.5707963       // PI/2\n" +
            "#define PI      3.1415629\n" +
            "#define TWOPI   6.2831853       // 2PI\n" +

            "precision mediump float;\n" +
            "uniform float iGlobalTime;\n" +
            "uniform vec2 iResolution;\n" +
            "uniform sampler2D tex;\n" +
            "uniform mat4 rotation;\n" +

            "varying vec2 vTextureCoord;\n" +

            "void main(void) {\n" +
            "    float d = vTextureCoord.x * vTextureCoord.x + vTextureCoord.y * vTextureCoord.y;\n" +
            "\n" +
            "    if (d > 1.0) {\n" +
            "        gl_FragColor = vec4(1.0, 0.7, 0.3, 1.0);//discard;\n" +
                    "return;\n" +
            "    }\n" +
            "\n" +
            "    // we're in, compute the exact Z\n" +
            "    float z = sqrt(1.0 - d);\n" +
            "\n" +
            "    // get light intensity\n" +
            "    vec4 point = vec4(vTextureCoord.xy, z, 1.0);\n" +
            "\n" +
            "    //float l = clamp(dot(point, lightPos), minLight, 1.0);\n" +
            "\n" +
            "    // rotate\n" +
            "    point *= rotation;\n" +
            "\n" +
            "    // get texture coordinates (I believe this could be replaced with a\n" +
            "    // precomputed texture lookup, if you need more performance)\n" +
            "    float x = (atan(point.x, point.z) + PI) / TWOPI,\n" +
            "          y = (asin(point.y) + PIP2) / PI;\n" +
            "\n" +
            "    // get texel, shade, colorize and output it\n" +
            "    vec4 texel = texture2D(tex, vec2(x, y)) * vec4(1.0, 1.0, 1.0, 1.0);// * color;\n" +
            "    gl_FragColor = texel;//vec4(0.8, 0.0, 0.6, 1.0);\n" +
            "}";

    private int program;
    private int vertexPositionAttribute;

    private int[] vtxBufHandle = new int[1];
    private int[] texHandle = new int[1];

    private int globalTimeLoc;
    private long prevTime;
    private long elapsedTime;
    private int iResolutionLoc;
    private long nowTime;
    private int height;
    private int width;

    private int texDataLoc;

    private int rotationLoc;
    private float[] rotationMatrix = new float[16];

    Bitmap b;
    private int angle;

    public MGLR(Bitmap b) {
        this.b = b;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glDepthMask(true);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSrc);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderSrc);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);

        vertexPositionAttribute = GLES20.glGetAttribLocation(program, "aVertexPosition");
        GLES20.glEnableVertexAttribArray(vertexPositionAttribute);

//        globalTimeLoc = GLES20.glGetUniformLocation(program, "iGlobalTime");
//        if (globalTimeLoc == -1) {
//            throw new RuntimeException("ugh");
//        }

        FloatBuffer fb = FloatBuffer.wrap(
                new float[]{
                        0f, 1f, -1f, -1f,
                        1f, 1f, 1f, -1f,
                        1f, 0f, 1f, 1f,
                        0f, 0f, -1f, 1f
                });
        GLES20.glGenBuffers(1, vtxBufHandle, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vtxBufHandle[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, fb.capacity() * 4, fb, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glGenTextures(1, texHandle, 0);
        if (texHandle[0] != 0) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandle[0]);
            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            b.recycle();
        } else {
            throw new RuntimeException("couldn't glGenTextures!");
        }

        texDataLoc = GLES20.glGetUniformLocation(program, "tex");

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, 0, 0, 0, 1);

        rotationLoc = GLES20.glGetUniformLocation(program, "rotation");
        prevTime = System.nanoTime();
        elapsedTime = 0L;
//        GLES20.glUniform1f(globalTimeLoc, 1.f * elapsedTime);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.height = height;
        this.width = width;
        iResolutionLoc = GLES20.glGetUniformLocation(program, "iResolution");
        if (iResolutionLoc != -1) {
            GLES20.glUniform2f(iResolutionLoc, width, height);
        }
        angle = 0;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

//        Matrix.frustumM(perspM, 0, 0, 1, 0, 1, .1f, 100.f);
        Matrix.orthoM(perspM, 0, 0, 1, 0, 1f * height / width, .1f, 100.f);
        Matrix.setIdentityM(mvM, 0);
        Matrix.translateM(mvM, 0, 0, 0, -.1f);

//        nowTime = System.nanoTime();
//        elapsedTime += nowTime - prevTime;
//        GLES20.glUniform1f(globalTimeLoc, (elapsedTime / 1000000000f));

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texHandle[0]);
        GLES20.glUniform1i(texDataLoc, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vtxBufHandle[0]);

        Matrix.rotateM(rotationMatrix, 0, -.2f, 0, 1, 0);
        GLES20.glUniformMatrix4fv(rotationLoc, 1, false, rotationMatrix, 0);

        GLES20.glVertexAttribPointer(vertexPositionAttribute, 4, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uPMatrix"), 1, false, perspM, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVMatrix"), 1, false, mvM, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

//        prevTime = nowTime;
    }

    public static int loadShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        Log.d("blanierblanier", "shader compile log: " + GLES20.glGetShaderInfoLog(shader));

        return shader;
    }
}
