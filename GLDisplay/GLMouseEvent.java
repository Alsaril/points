package GLDisplay;

import java.awt.*;
import java.awt.event.MouseWheelEvent;

public class GLMouseEvent extends MouseWheelEvent {

    private double x;
    private double y;
    private double scale;

    public GLMouseEvent(Component component, int modifiers, int wheelRotation, double x, double y, double scale) {
        super(component, 0, 0, modifiers, 0, 0, 0, false, 0, 0, wheelRotation);
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public double getRX() {
        return x;
    }

    public double getRY() {
        return y;
    }

    public double getScale() {
        return scale;
    }
}
