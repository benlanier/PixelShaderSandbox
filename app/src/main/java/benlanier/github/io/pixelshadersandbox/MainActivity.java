package benlanier.github.io.pixelshadersandbox;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new MGLSV(this);
        setContentView(glSurfaceView);
    }

    class MGLSV extends GLSurfaceView {

        private final MGLR renderer;

        public MGLSV(Context context) {
            super(context);

            setEGLContextClientVersion(2);

            renderer = new MGLR();

            setRenderer(renderer);

            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }
}
