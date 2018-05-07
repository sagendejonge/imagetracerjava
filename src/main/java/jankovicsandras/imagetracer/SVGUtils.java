package jankovicsandras.imagetracer;

import jankovicsandras.imagetracer.ImageTracer.IndexedImage;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class SVGUtils {

    /**
     * Converting tracedata to an SVG string, paths are drawn according to a Z-index
     * the optional lcpr and qcpr are linear and quadratic control point radiuses
     *
     * @param indexedImage
     * @param options
     * @return
     */
    public static String getSvgString(IndexedImage indexedImage,
            Options options) {
        // SVG start
        int width = (int) (indexedImage.width * options.scale());
        int height = (int) (indexedImage.height * options.scale());
        String viewboxorviewport = options.isViewBox()
                ? "viewBox=\"0 0 " + width + " " + height + "\" "
                : "width=\"" + width + "\" height=\"" + height + "\" ";
        StringBuilder builder = new StringBuilder("<svg " + viewboxorviewport
                + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
        if (options.isDesc()) {
            builder.append("desc=\"Created with ImageTracer.java version "
                    + ImageTracer.VersionNumber + "\" ");
        }
        builder.append(">");

        // Drawing
        indexedImage.segments.forEach(segment
                -> svgPathString(builder, segment, options));

        // SVG End
        builder.append("</svg>");

        return builder.toString();
    }

    /**
     * Getting SVG path element string from a traced path
     *
     * @param builder
     * @param segment
     * @param options
     */
    private static void svgPathString(StringBuilder builder, Segment segment,
            Options options) {

        String desc = "";
        if (options.isDesc()) {
            desc = "desc=\"l " + segment.l() + " p " + segment.p() + "\" ";
        }

        String colorStr = toSvgColor(segment.color());

        // Start path and move to
        builder.append("<path ")
                .append(desc)
                .append(colorStr)
                .append("d=\"");

        segment.scale(options.scale());

        Shape shape = segment.shape();
        if (shape instanceof Path2D) {
            Path2D path = (Path2D) shape;
            PathIterator iterator = path.getPathIterator(null);
            double values[] = new double[6];
            double prevX = 0;
            double prevY = 0;
            while (!iterator.isDone()) {
                int type = iterator.currentSegment(values);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        builder.append("M ")
                                .append(round(values[0], options))
                                .append(" ")
                                .append(round(values[1], options))
                                .append(" ");
                        prevX = values[0];
                        prevY = values[1];
                        break;
                    case PathIterator.SEG_LINETO:
                        builder.append("L ")
                                .append(round(values[0], options))
                                .append(" ")
                                .append(round(values[1], options))
                                .append(" ");
                        prevX = values[0];
                        prevY = values[1];
                        break;
                    case PathIterator.SEG_QUADTO:
                        builder.append("Q ")
                                .append(round(values[0], options))
                                .append(" ")
                                .append(round(values[1], options))
                                .append(" ")
                                .append(round(values[2], options))
                                .append(" ")
                                .append(round(values[3], options))
                                .append(" ");
                        prevX = values[2];
                        prevY = values[3];
                        break;
                    case PathIterator.SEG_CUBICTO:
                        builder.append("C ")
                                .append(round(values[0], options))
                                .append(" ")
                                .append(round(values[1], options))
                                .append(" ")
                                .append(round(values[2], options))
                                .append(" ")
                                .append(round(values[3], options))
                                .append(" ")
                                .append(round(values[4], options))
                                .append(" ")
                                .append(round(values[5], options))
                                .append(" ");
                        prevX = values[4];
                        prevY = values[5];
                        break;
                    case PathIterator.SEG_CLOSE:
                        builder.append("Z\"");
                        break;
                }
                renderCtrlPts(builder, type, prevX, prevY, values, options);
                iterator.next();
            }
        }

        builder.append(" />");

    }

    private static void renderCtrlPts(StringBuilder builder, int type,
            double prevX, double prevY, double[] values, Options options) {
        if ((options.lcpr() > 0) && (type == PathIterator.SEG_LINETO)) {
            builder.append("<circle cx=\"")
                    .append(values[0])
                    .append("\" cy=\"")
                    .append(values[1])
                    .append("\" r=\"")
                    .append(options.lcpr())
                    .append("\" fill=\"white\" stroke-width=\"")
                    .append(options.lcpr() * 0.2)
                    .append("\" stroke=\"black\" />");
        }
        if (options.qcpr() > 0 && (type == PathIterator.SEG_QUADTO)) {
            builder.append("<circle cx=\"")
                    .append(values[0])
                    .append("\" cy=\"")
                    .append(values[1])
                    .append("\" r=\"")
                    .append(options.qcpr())
                    .append("\" fill=\"cyan\" stroke-width=\"")
                    .append(options.qcpr() * 0.2)
                    .append("\" stroke=\"black\" />");

            builder.append("<circle cx=\"")
                    .append(values[2])
                    .append("\" cy=\"")
                    .append(values[3])
                    .append("\" r=\"")
                    .append(options.qcpr())
                    .append("\" fill=\"white\" stroke-width=\"")
                    .append(options.qcpr() * 0.2)
                    .append("\" stroke=\"black\" />");

            builder.append("<line x1=\"")
                    .append(prevX)
                    .append("\" y1=\"")
                    .append(prevY)
                    .append("\" x2=\"")
                    .append(values[0])
                    .append("\" y2=\"")
                    .append(values[1])
                    .append("\" stroke-width=\"")
                    .append(options.qcpr() * 0.2)
                    .append("\" stroke=\"cyan\" />");

            builder.append("<line x1=\"")
                    .append(values[0])
                    .append("\" y1=\"")
                    .append(values[1])
                    .append("\" x2=\"")
                    .append(values[2])
                    .append("\" y2=\"")
                    .append(values[3])
                    .append("\" stroke-width=\"")
                    .append(options.qcpr() * 0.2)
                    .append("\" stroke=\"cyan\" />");
        }
    }

    private static double round(double value, Options options) {
        if (options.roundCoords() == -1) {
            return value;
        }
        return Math.round(value * Math.pow(10, options.roundCoords()))
                / Math.pow(10, options.roundCoords());
    }

    private static String toSvgColor(Color color) {
        return String.format("fill=\"rgb(%s,%s,%s)\" stroke=\"rgb(%s,%s,%s)\" " +
                        "stroke-width=\"1\" opacity=\"%s\" ",
                color.getRed(), color.getGreen(), color.getBlue(),
                color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha() / 255.0);
    }
}
