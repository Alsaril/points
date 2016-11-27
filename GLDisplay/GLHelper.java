package GLDisplay;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class GLHelper {
    private static final int CIRCLE_VERTEX_COUNT = 36;
    private static Texture texture = null;
    private static boolean disableTexturing = false;

    public static void setColor(GL2 gl, double r, double g, double b, double a) {
        gl.glColor4d(r, g, b, a);
    }

    public static void setColor(GL2 gl, double r, double g, double b) {
        setColor(gl, r, g, b, 1.0);
    }

    public static void drawRect(GL2 gl, double x, double y, double w, double h, boolean fill) {
        if (fill) {
            gl.glBegin(GL2.GL_QUADS);
        } else {
            gl.glBegin(GL2.GL_LINE_LOOP);
        }
        gl.glVertex2d(x, y);
        gl.glVertex2d(x + w, y);
        gl.glVertex2d(x + w, y + h);
        gl.glVertex2d(x, y + h);
        gl.glEnd();
    }

    public static void drawCircle(GL2 gl, double x, double y, double r, boolean fill) {
        if (fill) {
            gl.glBegin(GL2.GL_TRIANGLE_FAN);
            gl.glVertex2d(x, y);
        } else {
            gl.glBegin(GL2.GL_LINE_STRIP);
        }
        for (int i = 0; i < CIRCLE_VERTEX_COUNT + 1; i++) {
            gl.glVertex2d(x + r * Math.cos(2 * Math.PI / CIRCLE_VERTEX_COUNT * i),
                    y + r * Math.sin(2 * Math.PI / CIRCLE_VERTEX_COUNT * i));
        }
        gl.glEnd();
    }

    public static void drawString(GL2 gl, double x, double y, double width, double height, String s, boolean scale) {
        if (disableTexturing) return;
        if (texture == null) {
            try {
                gl.glEnable(GL2.GL_TEXTURE_2D);
                texture = TextureIO.newTexture(new File("tex.png"), false);
                gl.glMatrixMode(GL2.GL_TEXTURE);
                gl.glLoadIdentity();
                gl.glScaled(1 / 16.0, 1 / 16.0, 1);
                gl.glMatrixMode(GL2.GL_MODELVIEW);
            } catch (IOException | GLException e) {
                e.printStackTrace();
                disableTexturing = true;
                return;
            }
        }

        byte[] b;
        try {
            b = s.getBytes("cp866");
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
            return;
        }
        texture.enable(gl);
        texture.bind(gl);
        gl.glEnable(GL2.GL_QUADS);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glScaled(20, 20, 1);
        gl.glTranslated(x, y, 0);
        gl.glBegin(7);
        gl.glNormal3f(0,0,1);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2d(0.0, 0.0);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex2d(1, 0.0);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex2d(1, 1);
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex2d(0.0, 1);
        for (int i = 0; i < b.length; ++i) {

          /*  gl.glNormal3f(0, 0, 1);
            gl.glTexCoord2d((double) (b[i] & 15), (double) (15 - (Byte.toUnsignedInt(b[i]) >> 4)));
            gl.glVertex2d(x + width * (double) i, y);
            gl.glTexCoord2d((double) ((b[i] & 15) + 1), (double) (15 - (Byte.toUnsignedInt(b[i]) >> 4)));
            gl.glVertex2d(x + width + width * (double) i, y);
            gl.glTexCoord2d((double) ((b[i] & 15) + 1), (double) (16 - (Byte.toUnsignedInt(b[i]) >> 4)));
            gl.glVertex2d(x + width + width * (double) i, y + height);
            gl.glTexCoord2d((double) (b[i] & 15), (double) (16 - (Byte.toUnsignedInt(b[i]) >> 4)));
            gl.glVertex2d(x + width * (double) i, y + height);*/
        }
        gl.glEnd();
        gl.glDisable(3553);
        gl.glPopMatrix();
        texture.disable(gl);
    }
}