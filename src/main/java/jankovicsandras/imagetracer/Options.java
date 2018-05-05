package jankovicsandras.imagetracer;

import com.beust.jcommander.Parameter;

/**
 * @author sdejonge
 */
public class Options {

    @Parameter(names = "-input", description = "Input file name", required = true, order = 0)
    private String input;

    @Parameter(names = "-output", description = "Output file name", required = true, order = 1)
    private String output;

    @Parameter(names = "-ltres", description = "Error threshold for straight lines")
    private double ltres = 1;

    @Parameter(names = "-qtres", description = "Error threshold for quadratic splines")
    private double qtres = 1;

    @Parameter(names = "-pathomit", description = "Edge node paths shorter than this will be discarded for noise reduction")
    private int pathOmit = 8;

    @Parameter(names = "-colorsampling", description = "Enable or disable color sampling", arity = 1)
    private boolean colorSampling = true;

    @Parameter(names = "-numberofcolors", description = "Number of colors to use on palette if pal object is not defined")
    private int numberOfColors = 16;

    @Parameter(names = "-mincolorratio", description = "Color quantization will randomize a color if fewer pixels than (total pixels*mincolorratio) has it")
    private double minColorRatio = 0.02;

    @Parameter(names = "-colorquantcycles", description = "Color quantization will be repeated this many times")
    private int colorQuantCycles = 3;

    @Parameter(names = "-blurradius", description = "Set this to 1f..5f for selective Gaussian blur preprocessing")
    private double blurRadius = 0;

    @Parameter(names = "-blurdelta", description = "RGBA delta treshold for selective Gaussian blur preprocessing")
    private double blurDelta = 20;

    @Parameter(names = "-scale", description = "Every coordinate will be multiplied with this, to scale the SVG")
    private double scale = 1;

    @Parameter(names = "-roundcoords", description = "Rounding coordinates to a given decimal place. 1f means rounded to 1 decimal place like 7.3 ; 3f means rounded to 3 places, like 7.356")
    private int roundCoords = 1;

    @Parameter(names = "-viewbox", description = "Enable or disable SVG viewBox", arity = 1)
    private boolean viewBox = false;

    @Parameter(names = "-desc", description = "Enable or disable SVG descriptions", arity = 1)
    private boolean desc = true;

    @Parameter(names = "-lcpr", description = "Straight line control point radius, if this is greater than zero, small circles will be drawn in the SVG. Do not use this for big/complex images")
    private double lcpr = 0;

    @Parameter(names = "-qcpr", description = "Quadratic spline control point radius, if this is greater than zero, small circles and lines will be drawn in the SVG. Do not use this for big/complex images")
    private double qcpr = 0;

    public String input() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String output() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public double ltres() {
        return ltres;
    }

    public void setLtres(double ltres) {
        this.ltres = ltres;
    }

    public double qtres() {
        return qtres;
    }

    public void setQtres(double qtres) {
        this.qtres = qtres;
    }

    public int pathOmit() {
        return pathOmit;
    }

    public void setPathOmit(int pathOmit) {
        this.pathOmit = pathOmit;
    }

    public boolean isColorSampling() {
        return colorSampling;
    }

    public void setColorSampling(boolean colorSampling) {
        this.colorSampling = colorSampling;
    }

    public int numberOfColors() {
        return numberOfColors;
    }

    public void setNumberOfColors(int numberOfColors) {
        this.numberOfColors = numberOfColors;
    }

    public double minColorRatio() {
        return minColorRatio;
    }

    public void setMinColorRatio(double minColorRatio) {
        this.minColorRatio = minColorRatio;
    }

    public int colorQuantCycles() {
        return colorQuantCycles;
    }

    public void setColorQuantCycles(int colorQuantCycles) {
        this.colorQuantCycles = colorQuantCycles;
    }

    public double blurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(double blurRadius) {
        this.blurRadius = blurRadius;
    }

    public double blurDelta() {
        return blurDelta;
    }

    public void setBlurDelta(double blurDelta) {
        this.blurDelta = blurDelta;
    }

    public double scale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public int roundCoords() {
        return roundCoords;
    }

    public void setRoundCoords(int roundCoords) {
        this.roundCoords = roundCoords;
    }

    public boolean isViewBox() {
        return viewBox;
    }

    public void setViewBox(boolean viewBox) {
        this.viewBox = viewBox;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public double lcpr() {
        return lcpr;
    }

    public void setLcpr(double lcpr) {
        this.lcpr = lcpr;
    }

    public double qcpr() {
        return qcpr;
    }

    public void setQcpr(double qcpr) {
        this.qcpr = qcpr;
    }
}
