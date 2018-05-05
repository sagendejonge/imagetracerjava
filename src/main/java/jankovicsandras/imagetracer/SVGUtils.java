package jankovicsandras.imagetracer;

import jankovicsandras.imagetracer.ImageTracer.IndexedImage;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
            List<Double[]> segments, String colorStr, final Options options) {

        segments = segments.stream().map(segment -> {
            Double scaled[] = new Double[segment.length];
            for (int i = 0; i < segment.length; i++) {
                scaled[i] = segment[i] * options.scale();
            }
            return scaled;
        }).collect(Collectors.toList());

        // Path
        builder.append("<path ")
                .append(desc)
                .append(colorStr)
                .append("d=\"")
                .append("M ")
                .append(segments.get(0)[1])
                .append(" ")
                .append(segments.get(0)[2])
                .append(" ");

        if (options.roundCoords() == -1) {
            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i)[0] == 1.0) {
                    builder.append("L ")
                            .append(segments.get(i)[3])
                            .append(" ")
                            .append(segments.get(i)[4])
                            .append(" ");
                } else {
                    builder.append("Q ")
                            .append(segments.get(i)[3])
                            .append(" ").append(segments.get(i)[4])
                            .append(" ").append(segments.get(i)[5])
                            .append(" ").append(segments.get(i)[6])
                            .append(" ");
                }
            }
        } else {
            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i)[0] == 1.0) {
                    builder.append("L ")
                            .append(round(segments.get(i)[3], options.roundCoords()))
                            .append(" ")
                            .append(round(segments.get(i)[4], options.roundCoords()))
                            .append(" ");
                } else {
                    builder.append("Q ")
                            .append(round(segments.get(i)[3], options.roundCoords()))
                            .append(" ")
                            .append(round(segments.get(i)[4], options.roundCoords()))
                            .append(" ")
                            .append(round(segments.get(i)[5], options.roundCoords()))
                            .append(" ")
                            .append(round(segments.get(i)[6], options.roundCoords()))
                            .append(" ");
                }
            }
        }

        builder.append("Z\" />");

        // Rendering control points
        for (int i = 0; i < segments.size(); i++) {
            if ((options.lcpr() > 0) && (segments.get(i)[0] == 1.0)) {
                builder.append("<circle cx=\"")
                        .append(segments.get(i)[3])
                        .append("\" cy=\"")
                        .append(segments.get(i)[4])
                        .append("\" r=\"")
                        .append(options.lcpr())
                        .append("\" fill=\"white\" stroke-width=\"")
                        .append(options.lcpr() * 0.2)
                        .append("\" stroke=\"black\" />");
            }
            if (options.qcpr() > 0 && segments.get(i)[0] == 2.0) {
                builder.append("<circle cx=\"")
                        .append(segments.get(i)[3])
                        .append("\" cy=\"")
                        .append(segments.get(i)[4])
                        .append("\" r=\"")
                        .append(options.qcpr())
                        .append("\" fill=\"cyan\" stroke-width=\"")
                        .append(options.qcpr() * 0.2)
                        .append("\" stroke=\"black\" />");

                builder.append("<circle cx=\"")
                        .append(segments.get(i)[5])
                        .append("\" cy=\"")
                        .append(segments.get(i)[6])
                        .append("\" r=\"")
                        .append(options.qcpr())
                        .append("\" fill=\"white\" stroke-width=\"")
                        .append(options.qcpr() * 0.2)
                        .append("\" stroke=\"black\" />");

                builder.append("<line x1=\"")
                        .append(segments.get(i)[1])
                        .append("\" y1=\"")
                        .append(segments.get(i)[2])
                        .append("\" x2=\"")
                        .append(segments.get(i)[3])
                        .append("\" y2=\"")
                        .append(segments.get(i)[4])
                        .append("\" stroke-width=\"")
                        .append(options.qcpr() * 0.2)
                        .append("\" stroke=\"cyan\" />");

                builder.append("<line x1=\"")
                        .append(segments.get(i)[3])
                        .append("\" y1=\"")
                        .append(segments.get(i)[4])
                        .append("\" x2=\"")
                        .append(segments.get(i)[5])
                        .append("\" y2=\"")
                        .append(segments.get(i)[6])
                        .append("\" stroke-width=\"")
                        .append(options.qcpr() * 0.2)
                        .append("\" stroke=\"cyan\" />");
            }
        }
    }

    /**
     * Converting tracedata to an SVG string, paths are drawn according to a Z-index
     * the optional lcpr and qcpr are linear and quadratic control point radiuses
     *
     * @param ii
     * @param options
     * @return
     */
    public static String getSvgString(IndexedImage ii, Options options) {
        // SVG start
        int w = (int) (ii.width * options.scale()), h = (int) (ii.height * options.scale());
        String viewboxorviewport = options.isViewBox() ? "viewBox=\"0 0 " + w + " " + h + "\" " : "width=\"" + w + "\" height=\"" + h + "\" ";
        StringBuilder svgstr = new StringBuilder("<svg " + viewboxorviewport + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
        if (options.isDesc()) {
            svgstr.append("desc=\"Created with ImageTracer.java version " + ImageTracer.VersionNumber + "\" ");
        }
        svgstr.append(">");

        // creating Z-index
        TreeMap<Double, Integer[]> zindex = new TreeMap<Double, Integer[]>();
        double label;
        // Layer loop
        for (int k = 0; k < ii.layers.size(); k++) {

            // Path loop
            for (int pcnt = 0; pcnt < ii.layers.get(k).size(); pcnt++) {

                // Label (Z-index key) is the startpoint of the path, linearized
                label = (ii.layers.get(k).get(pcnt).get(0)[2] * w) + ii.layers.get(k).get(pcnt).get(0)[1];
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
                    ii.layers.get(entry.getValue()[0]).get(entry.getValue()[1]),
                    toSvgColorStr(ii.palette[entry.getValue()[0]]),
                    options);
        }

        // SVG End
        svgstr.append("</svg>");

        return svgstr.toString();
    }

    public static double round(double value, int places) {
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }

    static String toSvgColorStr(byte[] c) {
        return "fill=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke=\"rgb(" + (c[0] + 128) + "," + (c[1] + 128) + "," + (c[2] + 128) + ")\" stroke-width=\"1\" opacity=\"" + ((c[3] + 128) / 255.0) + "\" ";
    }
}
