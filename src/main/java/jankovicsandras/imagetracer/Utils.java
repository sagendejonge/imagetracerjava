package jankovicsandras.imagetracer;

/**
 * @author sdejonge
 */
public class Utils {

    public static int arrayContains(String[] arr, String str) {
        for (int j = 0; j < arr.length; j++) {
            if (arr[j].equalsIgnoreCase(str)) {
                return j;
            }
        }
        return -1;
    }

    public static float parseNext(String[] arr, int i) {
        if (i < (arr.length - 1)) {
            try {
                return Float.parseFloat(arr[i + 1]);
            } catch (Exception e) {
            }
        }
        return -1;
    }
}
