
package jankovicsandras.imagetracer;

import com.beust.jcommander.JCommander;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class ImageTracer {

    public static final String VersionNumber = "1.1.3";
    private Options options;
    private BufferedImage image;
    private byte[][] palette;
    private int[] rawData;
    private ImageData imageData;

    /**
     * Path to image
     *
     * @param path
     */
    public ImageTracer(String path, Options options) throws Exception {
        image = ImageIO.read(new File(path));
        this.options = options;

        int width = image.getWidth();
        int height = image.getHeight();
        rawData = image.getRGB(0, 0, width, height, null, 0, width);
        byte[] data = new byte[rawData.length * 4];
        for (int i = 0; i < rawData.length; i++) {
            data[(i * 4) + 3] = Utils.byteTrans((byte) (rawData[i] >>> 24));
            data[i * 4] = Utils.byteTrans((byte) (rawData[i] >>> 16));
            data[(i * 4) + 1] = Utils.byteTrans((byte) (rawData[i] >>> 8));
            data[(i * 4) + 2] = Utils.byteTrans((byte) (rawData[i]));
        }
        imageData = new ImageData(width, height, data);

        palette = Utils.getPalette(options, image, imageData, true);
    }

    /**
     * Loading an image from a file, tracing when loaded, then returning the
     * SVG String
     *
     * @return
     * @throws Exception
     */
    public String toSvg() throws Exception {
        IndexedImage ii = trace();
        return SVGUtils.getSvgString(ii, options);
    }

    /**
     * Tracing ImageData, then returning IndexedImage with tracedata in layers
     */
    public IndexedImage trace() {
        // 1. Color quantization
        IndexedImage indexedImage = VectorizingUtils.colorquantization(
                imageData, palette, options);

        // 2. Layer separation and edge detection
        int[][][] rawlayers = VectorizingUtils.layering(indexedImage);

        // 3. Batch pathscan
        List<List<List<Integer[]>>> bps = VectorizingUtils
                .batchpathscan(rawlayers, options.pathOmit());

        // 4. Batch interpollation
        List<List<List<Double[]>>> bis = VectorizingUtils
                .batchinternodes(bps);

        // 5. Batch tracing
        indexedImage.traceData = VectorizingUtils.batchtracelayers(
                bis, options.ltres(), options.qtres());

        return indexedImage;
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
            ImageTracer tracer = new ImageTracer(options.input(), options);
            String svg = tracer.toSvg();
            Files.write(Paths.get(options.output()), svg.getBytes());
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
        // array[x][y] of palette colors
        public int[][] array;
        // array[palettelength][4] RGBA color palette
        public byte[][] palette;
        public List<List<List<Double[]>>> traceData;

        public IndexedImage(int[][] array, byte[][] palette) {
            this.array = array;
            this.palette = palette;
            width = array[0].length - 2;
            // Color quantization adds +2 to the original width and height
            height = array.length - 2;
        }
    }

    /**
     * https://developer.mozilla.org/en-US/docs/Web/API/ImageData
     */
    public static class ImageData {

        public int width, height;
        // raw byte data: R G B A R G B A ...
        public byte[] data;

        public ImageData(int width, int height, byte[] data) {
            this.width = width;
            this.height = height;
            this.data = data;
        }
    }
}
