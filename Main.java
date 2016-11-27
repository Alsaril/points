import GLDisplay.Display;
import GLDisplay.GLMouseEvent;
import com.jogamp.opengl.GL2;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Main implements Display.Draw {

    private static final int POWER = 5;
    private static final double EPS = 1E-5;

    private static final double R = 5;

    private Complex[] a = new Complex[POWER];
    private Complex[] z = new Complex[POWER];

    private volatile boolean selected = false;
    private volatile boolean selectedType = false;
    private volatile int selectedIndex = 0;

    public Main() {
        for (int i = 0; i < a.length; i++) {
            a[i] = new Complex();
            z[i] = new Complex();
        }
    }

    private Complex f(Complex[] poly, Complex root, boolean diff) {
        Complex result = new Complex();
        for (int n = 0; n < poly.length; n++) {
            result = Complex.add(result, Complex.mul(poly[n], root.pow(n)));
        }
        return !diff ? Complex.add(result, root.pow(poly.length)) : result;
    }

    private Complex df(Complex[] poly, Complex root) {
        final Complex[] newPoly = new Complex[poly.length];
        for (int n = 0; n < newPoly.length - 1; n++) {
            newPoly[n] = Complex.mul(poly[n + 1], n + 1);
        }
        newPoly[newPoly.length - 1] = new Complex(poly.length);
        return f(newPoly, root, true);
    }

    private void updateRoots() {
        Complex[] poly = a;
        final List<Complex> roots = new ArrayList<>();
        while (roots.size() < POWER * POWER * POWER) {
            Complex root = new Complex(Math.random() * 100 - 50, Math.random() * 100 - 50);
            while (f(poly, root, false).abs() > EPS) {
                root = Complex.sub(root, Complex.div(f(poly, root, false), df(poly, root)));
            }
            roots.add(root);
            //poly = divide(poly, root);
        }
        int index = 0;
        for (int i = 0; i < roots.size(); i++) {
            boolean hasCopy = false;
            for (int j = i + 1; j < roots.size(); j++) {
                if (roots.get(i).equals(roots.get(j))) {
                    hasCopy = true;
                    break;
                }
            }
            if (!hasCopy || index == POWER) {
                z[index] = roots.get(i);
                index++;
            }
        }
        if (index == 0) z[0] = roots.get(0);
        while (index < POWER) {

            z[index] = z[0];
            index++;
        }
    }

    private void updateCoefs() {
        for (int i = 0; i < POWER; i++) {
            Complex q = new Complex();
            for (List<Integer> roots : Comb.comb(i + 1, POWER)) {
                Complex part = new Complex(1);
                for (Integer index : roots) {
                    part = Complex.mul(part, z[index - 1]);
                }
                q = Complex.add(q, part);
            }
            a[POWER - i - 1] = i % 2 == 0 ? Complex.mul(q, -1) : q;
        }
    }

    private void setRoot(Complex value) {
        z[selectedIndex] = value;
        updateCoefs();
    }

    private void setCoef(Complex value) {
        a[selectedIndex] = value;
        updateRoots();
    }

    public void trySelect(double x, double y, double scale) {
        final Complex p = new Complex(x, y);
        for (int i = 0; i < z.length; i++) {
            if (Complex.sub(z[i], p).abs() < R / scale) {
                selected = true;
                selectedType = false;
                selectedIndex = i;
            }
        }
        for (int i = 0; i < a.length; i++) {
            if (Complex.sub(a[i], p).abs() < R / scale) {
                selected = true;
                selectedType = true;
                selectedIndex = i;
            }
        }
    }

    public void unselect() {
        selected = false;
    }

    public void move(double x, double y) {
        if (!selected) return;
        final Complex p = new Complex(x, y);
        if (selectedType) {
            setCoef(p);
        } else {
            setRoot(p);
        }
    }

    @Override
    public void init(GL2 gl) {
        gl.glPointSize(10);
    }

    @Override
    public void display(GL2 gl) {
        gl.glColor3d(0.7, 0.7, 0.7);
        //gl.glScaled(10, 10, 10);
        gl.glBegin(GL2.GL_LINES);
        for (int i = -10; i <= 10; i++) {
            gl.glVertex2d(-10, i);
            gl.glVertex2d(10, i);
            gl.glVertex2d(i, -10);
            gl.glVertex2d(i, 10);
        }
        gl.glEnd();
        gl.glLineWidth(2);
        gl.glBegin(GL2.GL_LINES);
        gl.glColor3d(0.2, 0.2, 0.2);
        gl.glVertex2d(-10, 0);
        gl.glVertex2d(10, 0);
        gl.glVertex2d(0, -10);
        gl.glVertex2d(0, 10);
        gl.glEnd();
        gl.glLineWidth(1);
        //gl.glScaled(0.1, 0.1, 0.1);
        gl.glBegin(GL2.GL_POINTS);
        gl.glColor3d(0.8, 0.2, 0.2);
        for (int i = 0; i < z.length; i++) {
            final Complex c = z[i];
            gl.glVertex2d(c.re(), c.im());
            //GLHelper.drawString(gl, c.re(), c.im(), 10, 15, "z" + i, false);
        }
        gl.glColor3d(0.2, 0.8, 0.2);
        for (int i = 0; i < a.length; i++) {
            final Complex c = a[i];
            gl.glVertex2d(c.re(), c.im());
            //GLHelper.drawString(gl, c.re(), c.im(), 10, 15, "a" + i, false);
        }
        gl.glEnd();
    }

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    public static void main(String[] args) {
        final Main main = new Main();
        final Display display = new Display("Points", WIDTH, HEIGHT, Display.CoordSystem.CENTER, main);
        display.addMouseAdapter(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e instanceof GLMouseEvent) {
                    GLMouseEvent re = (GLMouseEvent) e;
                    main.trySelect(re.getRX(), re.getRY(), re.getScale());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                main.unselect();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e instanceof GLMouseEvent) {
                    GLMouseEvent re = (GLMouseEvent) e;
                    main.move(re.getRX(), re.getRY());
                }
            }
        });
    }
}
