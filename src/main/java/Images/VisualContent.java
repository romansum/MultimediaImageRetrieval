package Images;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by rsume on 05.01.2018.
 */
public class VisualContent {

    public VisualContent() {
        this.colorMomentsOnHSV = new ArrayList();
        this.colorMomentsOnHSV3x3 = new ArrayList();
        this.colorNamingHistogram = new ArrayList();
        this.colorNamingHistogram3x3 = new ArrayList();
        this.colorStructureDescriptor = new ArrayList();
        this.grayLevelRunLengthMatrix = new ArrayList();
        this.grayLevelRunLengthMatrix3x3 = new ArrayList();
        this.histogramOfOrientedGradients = new ArrayList();
        this.locallyBinaryPatternsOnGS = new ArrayList();
        this.locallyBinaryPatternsOnGS3x3 = new ArrayList();
    }
    //All different existing visual features for an image
    private List<Double> colorMomentsOnHSV;
    private List<Double> colorMomentsOnHSV3x3;
    private List<Double> colorNamingHistogram;
    private List<Double> colorNamingHistogram3x3;
    private List<Double> colorStructureDescriptor;
    private List<Double> grayLevelRunLengthMatrix;
    private List<Double> grayLevelRunLengthMatrix3x3;
    private List<Double> histogramOfOrientedGradients;
    private List<Double> locallyBinaryPatternsOnGS;
    private List<Double> locallyBinaryPatternsOnGS3x3;

    //Getter and setter methods for the different visual features
    public List<Double> getColorMomentsOnHSV() {
        return colorMomentsOnHSV;
    }

    public void setColorMomentsOnHSV(List<Double> colorMomentsOnHSV) {
        this.colorMomentsOnHSV = colorMomentsOnHSV;
    }

    public void setColorMomentsOnHSV3x3(List<Double> colorMomentsOnHSV3x3) {
        this.colorMomentsOnHSV3x3 = colorMomentsOnHSV3x3;
    }
    public List<Double> getColorMomentsOnHSV3x3() {
        return this.colorMomentsOnHSV3x3;
    }

    public List<Double> getColorNamingHistogram() {
        return colorNamingHistogram;
    }

    public void setColorNamingHistogram(List<Double> colorNamingHistogram) {
        this.colorNamingHistogram = colorNamingHistogram;
    }
    public void setColorNamingHistogram3x3(List<Double> colorNamingHistogram3x3) {
        this.colorNamingHistogram3x3 = colorNamingHistogram3x3;
    }
    public List<Double> getColorNamingHistogram3x3() {
        return this.colorNamingHistogram3x3;
    }

    public List<Double> getColorStructureDescriptor() {
        return colorStructureDescriptor;
    }

    public void setColorStructureDescriptor(List<Double> colorStructureDescriptor) {
        this.colorStructureDescriptor = colorStructureDescriptor;
    }

    public List<Double> getGrayLevelRunLengthMatrix() {
        return grayLevelRunLengthMatrix;
    }

    public void setGrayLevelRunLengthMatrix(List<Double> grayLevelRunLengthMatrix) {
        this.grayLevelRunLengthMatrix = grayLevelRunLengthMatrix;
    }

    public void setGrayLevelRunLengthMatrix3x3(List<Double> grayLevelRunLengthMatrix3x3) {
        this.grayLevelRunLengthMatrix3x3 = grayLevelRunLengthMatrix3x3;
    }
    public List<Double> getGrayLevelRunLengthMatrix3x3() {
        return this.grayLevelRunLengthMatrix3x3;
    }

    public List<Double> getHistogramOfOrientedGradients() {
        return histogramOfOrientedGradients;
    }

    public void setHistogramOfOrientedGradients(List<Double> histogramOfOrientedGradients) {
        this.histogramOfOrientedGradients = histogramOfOrientedGradients;
    }

    public List<Double> getLocallyBinaryPatternsOnGS() {
        return locallyBinaryPatternsOnGS;
    }

    public void setLocallyBinaryPatternsOnGS(List<Double> locallyBinaryPatternsOnGS) {
        this.locallyBinaryPatternsOnGS = locallyBinaryPatternsOnGS;
    }

  public void setLocallyBinaryPatternsOnGS3x3(List<Double> locallyBinaryPatternsOnGS3x3) {
        this.locallyBinaryPatternsOnGS3x3 = locallyBinaryPatternsOnGS3x3;
    }
    public List<Double> getLocallyBinaryPatternsOnGS3x3() {
        return this.locallyBinaryPatternsOnGS3x3;
    }
}
