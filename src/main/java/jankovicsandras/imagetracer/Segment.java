package jankovicsandras.imagetracer;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

/**
 * @author sdejonge
 */
public class Segment {

    private Shape shape;
    private int color;

    public Segment(Shape shape, int color) {
        this.shape = shape;
        this.color = color;
    }

    public Shape shape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;


    }

    public void scale(double scale) {
        // Would like to do this but it converts to Path2D which don't want
        // AffineTransform transform = new AffineTransform();
        // transform.scale(scale, scale);
        // shape = transform.createTransformedShape(shape);
        if (shape instanceof Line2D) {
            Line2D line = (Line2D) shape;
            shape = new Line2D.Double(
                    line.getX1() * scale, line.getY1() * scale,
                    line.getX2() * scale, line.getY2() * scale);
        } else if (shape instanceof QuadCurve2D) {
            QuadCurve2D conic = (QuadCurve2D) shape;
            shape = new QuadCurve2D.Double(
                    conic.getX1() * scale, conic.getY1() * scale,
                    conic.getCtrlX() * scale, conic.getCtrlY() * scale,
                    conic.getX2() * scale, conic.getY2() * scale);
        } else if (shape instanceof CubicCurve2D) {
            CubicCurve2D cubic = (CubicCurve2D) shape;
            shape = new CubicCurve2D.Double(
                    cubic.getX1() * scale, cubic.getY1() * scale,
                    cubic.getCtrlX1() * scale, cubic.getCtrlY1() * scale,
                    cubic.getCtrlX2() * scale, cubic.getCtrlY2() * scale,
                    cubic.getX2() * scale, cubic.getY2() * scale);
        }
    }

    public Point2D start() {
        if (shape instanceof Line2D) {
            return ((Line2D) shape).getP1();
        } else if (shape instanceof QuadCurve2D) {
            return ((QuadCurve2D) shape).getP1();
        } else if (shape instanceof CubicCurve2D) {
            return ((CubicCurve2D) shape).getP1();
        }
        return null;
    }

    public int color() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static Segment line(double x1, double y1, double x2, double y2,
            int color) {
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        return new Segment(line, color);
    }

    public static Segment conic(double x1, double y1, double cx, double cy,
            double x2, double y2, int color) {
        QuadCurve2D conic = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
        return new Segment(conic, color);
    }

    public static Segment conic(double x1, double y1, double cx1, double cy1,
            double cx2, double cy2, double x2, double y2, int color) {
        CubicCurve2D cubic = new CubicCurve2D.Double(
                x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        return new Segment(cubic, color);
    }
}
