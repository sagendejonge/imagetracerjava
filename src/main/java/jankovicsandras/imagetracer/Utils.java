package jankovicsandras.imagetracer;

import jankovicsandras.imagetracer.ImageTracer.ImageData;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author sdejonge
 */
public class Utils {

    /**
     * The bitshift method in loadImageData creates signed bytes where -1 -> 255 unsigned ; -128 -> 128 unsigned ;
     * 127 -> 127 unsigned ; 0 -> 0 unsigned ; These will be converted to -128 (representing 0 unsigned) ...
     * 127 (representing 255 unsigned) and tosvgcolorstr will add +128 to create RGB values 0..255
     *
     * @param b
     * @return
     */
    public static byte byteTrans(byte b) {
        if (b < 0) {
            return (byte) (b + 128);
        } else {
            return (byte) (b - 128);
        }
    }

    public static byte[][] getPalette(Options options, BufferedImage image,
            ImageData imageData, boolean old) {
        byte[][] bytePalette;
        if (old) {
            if (options.isColorSampling()) {
                bytePalette = Quantize2.samplePalette(options.numberOfColors(),
                        imageData);
            } else {
                bytePalette = Quantize2.generatePalette(options.numberOfColors());
            }
        } else {
            int[][] pixels = new int[image.getWidth()][image.getHeight()];

            for (int i = 0; i < image.getWidth(); i++) {
                for (int j = 0; j < image.getHeight(); j++) {
                    pixels[i][j] = image.getRGB(i, j);
                }
            }
            int[] palette = Quantize.quantizeImage(pixels,
                    options.numberOfColors());
            bytePalette = new byte[options.numberOfColors()][4];

            for (int i = 0; i < palette.length; i++) {
                Color c = new Color(palette[i]);
                bytePalette[i][0] = (byte) c.getRed();
                bytePalette[i][1] = (byte) c.getGreen();
                bytePalette[i][2] = (byte) c.getBlue();
                bytePalette[i][3] = 0;
            }
        }
        return bytePalette;
    }
}
