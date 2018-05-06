package jankovicsandras.imagetracer;

import jankovicsandras.imagetracer.ImageTracer.IndexedImage;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class SVGUtils {

    /**
     * Getting SVG path element string from a traced path
     *
     * @param builder
     * @param desc
     * @param segments
     * @param colorStr
     * @param options
     */
    public static void svgPathString(StringBuilder builder, String desc,
            List<Segment> segments, String colorStr, final Options options) {

        segments.forEach(segment -> segment.scale(options.scale()));

        boolean start = true;
        for (Segment segment : segments) {
            if (start) {
                Point2D startPoint = segment.start();
                if (startPoint != null) {
                    // Start path and move to
                    builder.append("<path ")
                            .append(desc)
                            .append(colorStr)
                            .append("d=\"")
                            .append("M ")
                            .append(startPoint.getX())
                            .append(" ")
                            .append(startPoint.getY())
                            .append(" ");
                    start = false;
                }
            } else {
                Shape shape = segment.shape();
                if (shape instanceof Line2D) {
                    Line2D line = (Line2D) shape;
                    builder.append("L ")
                            .append(round(line.getX2(), options))
                            .append(" ")
                            .append(round(line.getY2(), options))
                            .append(" ");
                } else if (shape instanceof QuadCurve2D) {
                    QuadCurve2D conic = (QuadCurve2D) shape;
                    builder.append("Q ")
                            .append(round(conic.getCtrlX(), options))
                            .append(" ")
                            .append(round(conic.getCtrlY(), options))
                            .append(" ")
                            .append(round(conic.getX2(), options))
                            .append(" ")
                            .append(round(conic.getY2(), options))
                            .append(" ");
                }
            }
        }

        builder.append("Z\" />");

//        // Rendering control points
//        for (int i = 0; i < segments.size(); i++) {
//            if ((options.lcpr() > 0) && (segments.get(i)[0] == 1.0)) {
//                builder.append("<circle cx=\"")
//                        .append(segments.get(i)[3])
//                        .append("\" cy=\"")
//                        .append(segments.get(i)[4])
//                        .append("\" r=\"")
//                        .append(options.lcpr())
//                        .append("\" fill=\"white\" stroke-width=\"")
//                        .append(options.lcpr() * 0.2)
//                        .append("\" stroke=\"black\" />");
//            }
//            if (options.qcpr() > 0 && segments.get(i)[0] == 2.0) {
//                builder.append("<circle cx=\"")
//                        .append(segments.get(i)[3])
//                        .append("\" cy=\"")
//                        .append(segments.get(i)[4])
//                        .append("\" r=\"")
//                        .append(options.qcpr())
//                        .append("\" fill=\"cyan\" stroke-width=\"")
//                        .append(options.qcpr() * 0.2)
//                        .append("\" stroke=\"black\" />");
//
//                builder.append("<circle cx=\"")
//                        .append(segments.get(i)[5])
//                        .append("\" cy=\"")
//                        .append(segments.get(i)[6])
//                        .append("\" r=\"")
//                        .append(options.qcpr())
//                        .append("\" fill=\"white\" stroke-width=\"")
//                        .append(options.qcpr() * 0.2)
//                        .append("\" stroke=\"black\" />");
//
//                builder.append("<line x1=\"")
//                        .append(segments.get(i)[1])
//                        .append("\" y1=\"")
//                        .append(segments.get(i)[2])
//                        .append("\" x2=\"")
//                        .append(segments.get(i)[3])
//                        .append("\" y2=\"")
//                        .append(segments.get(i)[4])
//                        .append("\" stroke-width=\"")
//                        .append(options.qcpr() * 0.2)
//                        .append("\" stroke=\"cyan\" />");
//
//                builder.append("<line x1=\"")
//                        .append(segments.get(i)[3])
//                        .append("\" y1=\"")
//                        .append(segments.get(i)[4])
//                        .append("\" x2=\"")
//                        .append(segments.get(i)[5])
//                        .append("\" y2=\"")
//                        .append(segments.get(i)[6])
//                        .append("\" stroke-width=\"")
//                        .append(options.qcpr() * 0.2)
//                        .append("\" stroke=\"cyan\" />");
//            }
//        }
    }

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
        int w = (int) (indexedImage.width * options.scale()), h = (int) (indexedImage.height * options.scale());
        String viewboxorviewport = options.isViewBox() ? "viewBox=\"0 0 " + w + " " + h + "\" " : "width=\"" + w + "\" height=\"" + h + "\" ";
        StringBuilder svgstr = new StringBuilder("<svg " + viewboxorviewport + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
        if (options.isDesc()) {
            svgstr.append("desc=\"Created with ImageTracer.java version " + ImageTracer.VersionNumber + "\" ");
        }
        svgstr.append(">");

        // creating Z-index
        TreeMap<Double, Integer[]> zindex = new TreeMap<>();
        double label;
        // Layer loop
        for (int k = 0; k < indexedImage.traceData.size(); k++) {

            // Path loop
            for (int pcnt = 0; pcnt < indexedImage.traceData.get(k).size(); pcnt++) {
                Segment segment = indexedImage.traceData.get(k).get(pcnt).get(0);
                Point2D start = segment.start();
                if (start == null) {
                    continue;
                }

                // Label (Z-index key) is the startpoint of the path, linearized
                label = (start.getY() * w) + start.getX();
                // Creating new list if required
                if (!zindex.containsKey(label)) {
                    zindex.put(label, new Integer[2]);
                }
                // Adding layer and path number to list
                zindex.get(label)[0] = new Integer(k);
                zindex.get(label)[1] = new Integer(pcnt);
            }
        }

        // Sorting Z-index is not required, TreeMap is sorted automatically

        // Drawing
        // Z-index loop
        String thisdesc = "";
        for (Entry<Double, Integer[]> entry : zindex.entrySet()) {
            if (options.isDesc()) {
                thisdesc = "desc=\"l " + entry.getValue()[0] + " p " + entry.getValue()[1] + "\" ";
            } else {
                thisdesc = "";
            }
            svgPathString(svgstr, thisdesc,
                    indexedImage.traceData.get(entry.getValue()[0]).get(entry.getValue()[1]),
                    toSvgColorStr(indexedImage.palette[entry.getValue()[0]]),
                    options);
        }

        // SVG End
        svgstr.append("</svg>");

        return svgstr.toString();
    }

    public static double round(double value, Options options) {
        if (options.roundCoords() == -1) {
            return value;
        }
        return Math.round(value * Math.pow(10, options.roundCoords()))
                / Math.pow(10, options.roundCoords());
    }

    static String toSvgColorStr(byte[] c) {
        return String.format("fill=\"rgb(%s,%s,%s)\" stroke=\"rgb(%s,%s,%s)\" " +
                        "stroke-width=\"1\" opacity=\"%s\" ",
                c[0] + 128, c[1] + 128, c[2] + 128,
                c[0] + 128, c[1] + 128, c[2] + 128,
                (c[3] + 128) / 255.0);
    }
}
