package GLDisplay;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;

public class Display {

    private JFrame frame;
    private GLCanvas canvas;
    private JPanel side;
    private JTextArea bottom;

    private GLEL glel;

    public Display(String title, int width, int height, CoordSystem coordSystem, Draw draw) {
        this.frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        canvas = new GLCanvas();
        canvas.setPreferredSize(new Dimension(width, height));
        glel = new GLEL(width, height, draw, coordSystem.value);
        canvas.addGLEventListener(glel);
        MouseAdapter ma = new MouseAdapter() {
            boolean pressed = false;
            int x, y;

            @Override
            public void mousePressed(MouseEvent e) {
                if (!e.isControlDown()) return;
                pressed = true;
                x = e.getX();
                y = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!e.isControlDown()) return;
                glel.scale(e.getX(), e.getY(), e.getWheelRotation() < 0);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!e.isControlDown()) return;
                if (pressed) {
                    glel.translate(e.getX() - x, y - e.getY());
                }
                x = e.getX();
                y = e.getY();
            }
        };
        canvas.addMouseListener(ma);
        canvas.addMouseMotionListener(ma);
        canvas.addMouseWheelListener(ma);
        frame.add(canvas, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        final Animator animator = new Animator(canvas);
        animator.setRunAsFastAsPossible(true);
        animator.start();
    }

    public JPanel enableSide(int width, int height) {
        side = new JPanel();
        side.setPreferredSize(new Dimension(width, height));
        frame.add(side, BorderLayout.EAST);
        frame.pack();
        return side;
    }

    public JTextArea enableBottom(int width, int height) {
        bottom = new JTextArea();
        bottom.setPreferredSize(new Dimension(width, height));
        frame.add(bottom, BorderLayout.SOUTH);
        frame.pack();
        return bottom;
    }

    public void addKeyListener(KeyListener listener) {
        canvas.addKeyListener(listener);
    }

    private MouseEvent delegate(MouseEvent e) {
        return glel.delegate(e);
    }

    public void addMouseAdapter(MouseAdapter adapter) {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseClicked(delegate(e));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!e.isControlDown()) adapter.mousePressed(delegate(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseReleased(delegate(e));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseEntered(delegate(e));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseExited(delegate(e));
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!e.isControlDown()) adapter.mouseExited(delegate(e));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseDragged(delegate(e));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!e.isControlDown()) adapter.mouseMoved(delegate(e));
            }
        };
        canvas.addMouseMotionListener(ma);
        canvas.addMouseListener(ma);
    }

    public interface Draw {
        void init(GL2 gl);

        void display(GL2 gl);
    }

    public enum CoordSystem {
        CENTER(true), CORNER(false);

        public final boolean value;

        CoordSystem(boolean value) {
            this.value = value;
        }
    }

    private static final class GLEL implements GLEventListener {
        private int width;
        private int height;
        private Draw draw;

        private double scale = 1.0;
        private double[] trans = new double[2];

        private boolean coordSystem;

        private GLEL(int width, int height, Draw draw, boolean coordSystem) {
            this.width = width;
            this.height = height;
            this.draw = draw;
            this.coordSystem = coordSystem;
        }

        public void scale(int x, int y, boolean type) {
            scale *= type ? 1.5 : 1 / 1.5;
        }

        public void translate(int dx, int dy) {
            trans[0] += dx / scale;
            trans[1] += dy / scale;
        }

        public MouseEvent delegate(MouseEvent e) {
            double x = (e.getX() - width / 2) / scale - trans[0];
            double y = (height / 2 - e.getY()) / scale - trans[1];
            int wr = 0;
            if (e instanceof MouseWheelEvent) {
                wr = ((MouseWheelEvent) e).getWheelRotation();
            }
            return new GLMouseEvent(e.getComponent(), e.getModifiers(), wr, x, y, scale);
        }


        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            final GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.glClearColor(1, 1, 1, 1);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL2.GL_NICEST);
            gl.glEnable(GL2.GL_LINE_SMOOTH);
            gl.glEnable(GL2.GL_POLYGON_SMOOTH);
            gl.glEnable(GL2.GL_MULTISAMPLE);
            draw.init(gl);
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            final GL2 gl = glAutoDrawable.getGL().getGL2();
            gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glScaled(scale, scale, 1);
            gl.glTranslated(trans[0], trans[1], 0);
            draw.display(gl);
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h) {
            final GL2 gl = glAutoDrawable.getGL().getGL2();
            width = w;
            height = h;
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            if (coordSystem) {
                gl.glOrtho(-w / 2, w / 2, -h / 2, h / 2, -1, 1);
            } else {
                gl.glOrtho(0.0D, w, 0.0D, h, -1.0D, 1.0D);
            }
            gl.glMatrixMode(GL2.GL_MODELVIEW);
        }
    }
}
