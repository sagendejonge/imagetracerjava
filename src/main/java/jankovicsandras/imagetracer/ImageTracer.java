
package jankovicsandras.imagetracer;

import com.beust.jcommander.JCommander;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;


public class ImageTracer {

    public static final String VersionNumber = "1.1.3";
    private int[] rawData;

    public ImageTracer() {
    }

    /**
     * Saving a String as a file
     *
     * @param filename
     * @param str
     * @throws Exception
     */
    public void saveString(String filename, String str) throws Exception {
        File file = new File(filename);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(str);
        bw.close();
    }

    /**
     * Loading a file to ImageData, ARGB byte order
     *
     * @param filename
     * @return
     * @throws Exception
     */
    public ImageData loadImageData(String filename) throws Exception {
        BufferedImage image = ImageIO.read(new File(filename));
        return loadImageData(image);
    }

    public ImageData loadImageData(BufferedImage image) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();
        rawData = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] data = new byte[rawData.length * 4];
        for (int i = 0; i < rawData.length; i++) {
            data[(i * 4) + 3] = bytetrans((byte) (rawData[i] >>> 24));
            data[i * 4] = bytetrans((byte) (rawData[i] >>> 16));
            data[(i * 4) + 1] = bytetrans((byte) (rawData[i] >>> 8));
            data[(i * 4) + 2] = bytetrans((byte) (rawData[i]));
        }
        return new ImageData(width, height, data);
    }

    /**
     * The bitshift method in loadImageData creates signed bytes where -1 -> 255 unsigned ; -128 -> 128 unsigned ;
     * 127 -> 127 unsigned ; 0 -> 0 unsigned ; These will be converted to -128 (representing 0 unsigned) ...
     * 127 (representing 255 unsigned) and tosvgcolorstr will add +128 to create RGB values 0..255
     *
     * @param b
     * @return
     */
    public byte bytetrans(byte b) {
        if (b < 0) {
            return (byte) (b + 128);
        } else {
            return (byte) (b - 128);
        }
    }

    public byte[][] getPalette(BufferedImage image, Options options) {
        int[][] pixels = new int[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        }
        int[] palette = Quantize.quantizeImage(pixels,
                options.numberOfColors());
        byte[][] bytepalette = new byte[options.numberOfColors()][4];

        for (int i = 0; i < palette.length; i++) {
            Color c = new Color(palette[i]);
            bytepalette[i][0] = (byte) c.getRed();
            bytepalette[i][1] = (byte) c.getGreen();
            bytepalette[i][2] = (byte) c.getBlue();
            bytepalette[i][3] = 0;
        }
        return bytepalette;
    }

    /**
     * Loading an image from a file, tracing when loaded, then returning the
     * SVG String
     *
     * @param filename
     * @param options
     * @return
     * @throws Exception
     */
    public String imageToSVG(String filename, Options options) throws Exception {
        ImageData imgd = loadImageData(filename);
        return imageDataToSVG(imgd, options, getPalette(ImageIO.read(new File(filename)), options));
    }

    /**
     * Tracing ImageData, then returning the SVG String
     *
     * @param imgd
     * @param options
     * @param palette
     * @return
     */
    public String imageDataToSVG(ImageData imgd, Options options,
            byte[][] palette) {
        IndexedImage ii = imagedataToTracedata(imgd, options, palette);
        return SVGUtils.getSvgString(ii, options);
    }

    /**
     * Loading an image from a file, tracing when loaded, then returning
     * IndexedImage with tracedata in layers
     *
     * @param filename
     * @param options
     * @param palette
     * @return
     * @throws Exception
     */
    public IndexedImage imageToTracedata(String filename, Options options,
            byte[][] palette) throws Exception {
        ImageData imgd = loadImageData(filename);
        return imagedataToTracedata(imgd, options, palette);
    }

    public IndexedImage imageToTracedata(BufferedImage image, Options options,
            byte[][] palette) throws Exception {
        ImageData imgd = loadImageData(image);
        return imagedataToTracedata(imgd, options, palette);
    }

    /**
     * Tracing ImageData, then returning IndexedImage with tracedata in layers
     */
    public IndexedImage imagedataToTracedata(ImageData imgd, Options options,
            byte[][] palette) {
        // 1. Color quantization
        IndexedImage ii = VectorizingUtils.colorquantization(imgd, palette,
                options);
        // 2. Layer separation and edge detection
        int[][][] rawlayers = VectorizingUtils.layering(ii);
        // 3. Batch pathscan
        List<List<List<Integer[]>>> bps = VectorizingUtils
                .batchpathscan(rawlayers, options.pathOmit());
        // 4. Batch interpollation
        List<List<List<Double[]>>> bis = VectorizingUtils
                .batchinternodes(bps);
        // 5. Batch tracing
        ii.layers = VectorizingUtils.batchtracelayers(bis, options.ltres(),
                options.qtres());
        return ii;
    }

    public static void main(String[] args) {
        Options options = new Options();

        JCommander commander = JCommander.newBuilder()
                .addObject(options)
                .build();

        try {
            commander.parse(args);
        } catch (Exception e) {
            System.out.print("Parse Error: " + e.getLocalizedMessage() + "\n\n");
            commander.usage();
            return;
        }

        try {
            ImageTracer tracer = new ImageTracer();
            tracer.saveString(options.output(), tracer.imageToSVG(
                    options.input(), options));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Container for the color-indexed image before and tracedata after
     * vectorizing
     */
    public static class IndexedImage {

        public int width, height;
        public int[][] array; // array[x][y] of palette colors
        public byte[][] palette;// array[palettelength][4] RGBA color palette
        public List<List<List<Double[]>>> layers;// tracedata

        public IndexedImage(int[][] marray, byte[][] mpalette) {
            array = marray;
            palette = mpalette;
            width = marray[0].length - 2;
            height = marray.length - 2;// Color quantization adds +2 to the original width and height
        }
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/API/ImageData
     */
    public static class ImageData {

        public int width, height;
        public byte[] data; // raw byte data: R G B A R G B A ...

        public ImageData(int mwidth, int mheight, byte[] mdata) {
            width = mwidth;
            height = mheight;
            data = mdata;
        }
    }
}
