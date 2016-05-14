package benlanier.github.io.pixelshadersandbox;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
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
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            renderer = new MGLR(BitmapFactory.decodeResource(getResources(), R.drawable.land_ocean_ice_2048));

            setRenderer(renderer);

            setRenderMode(RENDERMODE_CONTINUOUSLY);
        }
    }
}
