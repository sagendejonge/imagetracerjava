package jankovicsandras.imagetracer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

/**
 * @author sdejonge
 */
public class Segment {

    private Shape shape;
    private Color color;
    private int l;
    private int p;

    public Segment(Shape shape) {
        this.shape = shape;
    }

    public Shape shape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;


    }

    public void scale(double scale) {
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        shape = transform.createTransformedShape(shape);
    }

    public Point2D start() {
        if (shape instanceof Line2D) {
            return ((Line2D) shape).getP1();
        } else if (shape instanceof QuadCurve2D) {
            return ((QuadCurve2D) shape).getP1();
        } else if (shape instanceof CubicCurve2D) {
            return ((CubicCurve2D) shape).getP1();
        } else if (shape instanceof Path2D) {
            Path2D path = (Path2D) shape;
            PathIterator iterator = path.getPathIterator(null);
            double values[] = new double[6];

            while (!iterator.isDone()) {
                int type = iterator.currentSegment(values);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        return new Point2D.Double(values[0], values[1]);
                }
            }
        }
        return null;
    }

    public Color color() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int l() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int p() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public static Segment line(double x1, double y1, double x2, double y2) {
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        return new Segment(line);
    }

    public static Segment conic(double x1, double y1, double cx, double cy,
            double x2, double y2) {
        QuadCurve2D conic = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
        return new Segment(conic);
    }

    public static Segment conic(double x1, double y1, double cx1, double cy1,
            double cx2, double cy2, double x2, double y2) {
        CubicCurve2D cubic = new CubicCurve2D.Double(
                x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        return new Segment(cubic);
    }
}
