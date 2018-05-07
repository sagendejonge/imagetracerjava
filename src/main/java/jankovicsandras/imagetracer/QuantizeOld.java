package jankovicsandras.imagetracer;

import jankovicsandras.imagetracer.ImageTracer.ImageData;

/**
 * @author sdejonge
 */
public class QuantizeOld {

    /**
     * Generating a palette with numberofcolors, array[numberofcolors][4] where
     * [i][0] = R ; [i][1] = G ; [i][2] = B ; [i][3] = A
     *
     * @param numberofcolors
     * @return
     */
    public static byte[][] generatePalette(int numberofcolors) {
        byte[][] palette = new byte[numberofcolors][4];
        if (numberofcolors < 8) {
            // Grayscale
            double graystep = 255.0 / (double) (numberofcolors - 1);
            for (byte ccnt = 0; ccnt < numberofcolors; ccnt++) {
                palette[ccnt][0] = (byte) (-128 + Math.round(ccnt * graystep));
                palette[ccnt][1] = (byte) (-128 + Math.round(ccnt * graystep));
                palette[ccnt][2] = (byte) (-128 + Math.round(ccnt * graystep));
                palette[ccnt][3] = (byte) 127;
            }
        } else {
            // RGB color cube
            int colorqnum = (int) Math.floor(Math.pow(numberofcolors, 1.0 / 3.0)); // Number of points on each edge on the RGB color cube
            int colorstep = (int) Math.floor(255 / (colorqnum - 1)); // distance between points
            int ccnt = 0;
            for (int rcnt = 0; rcnt < colorqnum; rcnt++) {
                for (int gcnt = 0; gcnt < colorqnum; gcnt++) {
                    for (int bcnt = 0; bcnt < colorqnum; bcnt++) {
                        palette[ccnt][0] = (byte) (-128 + (rcnt * colorstep));
                        palette[ccnt][1] = (byte) (-128 + (gcnt * colorstep));
                        palette[ccnt][2] = (byte) (-128 + (bcnt * colorstep));
                        palette[ccnt][3] = (byte) 127;
                        ccnt++;
                    }
                }
            }

            // Rest is random
            for (int rcnt = ccnt; rcnt < numberofcolors; rcnt++) {
                palette[ccnt][0] = (byte) (-128 + Math.floor(Math.random() * 255));
                palette[ccnt][1] = (byte) (-128 + Math.floor(Math.random() * 255));
                palette[ccnt][2] = (byte) (-128 + Math.floor(Math.random() * 255));
                palette[ccnt][3] = (byte) (-128 + Math.floor(Math.random() * 255));
            }
        }

        return palette;
    }

    public static byte[][] samplePalette(int numberofcolors, ImageData imgd) {
        byte[][] palette = new byte[numberofcolors][4];
        for (int i = 0; i < numberofcolors; i++) {
            int idx = (int) (Math.floor((Math.random() * imgd.data.length) / 4) * 4);
            palette[i][0] = imgd.data[idx];
            palette[i][1] = imgd.data[idx + 1];
            palette[i][2] = imgd.data[idx + 2];
            palette[i][3] = imgd.data[idx + 3];
        }
        return palette;
    }
}
