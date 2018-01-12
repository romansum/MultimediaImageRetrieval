package Calculation;

import java.util.*;
import Data.PropConfig;
import Images.*;
/**
 * Created by rsume on 05.01.2018.
 */
public class SimilarityCalculation {

    public SimilarityCalculation() {
    }

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    /**
     * Weights for the images
     */
    private double ImageGPSWeight;
    private double ImageRelevanceWeight;
    private double ImageVisualSimilarityWeight;
    private double ImageTextualSimilarityWeight;
    /**
     * Weights for the location descriptors and wikpedia images
     */
    private double LocationGPSWeight;
    private double LocationRankWeight;
    private double LocationPropertiesWeight;
    private double LocationVisualSimilarityWeight;
    private double LocationTextualSimilarityWeight;
    /**
     * Weight for each single visual descriptor for the images and wikipedia images
     */
    private double CMWeight;
    private double CNWeight;
    private double CSDWeight;
    private double GLRLMWeight;
    private double HOGWeight;
    private double LBPWeight;
    private double CM3x3Weight;
    private double CN3x3Weight;
    private double GLRLM3x3Weight;
    private double LBP3x3Weight;

    /**
     * Calculates the score for an image depending on the number of views and number of comments
     * @param image image for which the score is calculated
     * @return the score as double value
     */
    private double calculateImagePropertiesScore(Image image) {
        double numberOfViews = 0;
        double numberOfComments = 0;
        if (image.getNumberOfViews() > 0) {
            numberOfViews = 1-1.0/image.getNumberOfViews();
        }
        double score = numberOfViews;
        return score;
    }

    /**
     * Calculation of the similarity of two GPS positions with the  Haversine formula
     * @param startLat latitude of point A
     * @param endLat latitude of point B
     * @param startLong longitude of point A
     * @param endLong longitude of point B
     * @return distance between the two points
     */
    private double calculateGPSPositionSimilarity(double startLat, double endLat, double startLong, double endLong) {
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        double similarity = 1.0 / (1 + distance);
        return similarity;
    }

    /**
     * Calculates distance between each visual feature
     * @param visualContent1 first vector
     * @param visualContent2 second vector
     * @return similarity of vectors
     */
    private double calculateVisualSimilarity(VisualContent visualContent1, VisualContent visualContent2) {
        double cmSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getColorMomentsOnHSV(), visualContent2.getColorMomentsOnHSV()));
        double cnSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getColorNamingHistogram(), visualContent2.getColorNamingHistogram()));
        double csdSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getColorStructureDescriptor(), visualContent2.getColorStructureDescriptor()));
        double glrlmSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getGrayLevelRunLengthMatrix(), visualContent2.getGrayLevelRunLengthMatrix()));
        double hogSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getHistogramOfOrientedGradients(), visualContent2.getHistogramOfOrientedGradients()));
        double lbpSimilarity = 1.0 / (1 + this.calculateDistance(visualContent1.getLocallyBinaryPatternsOnGS(), visualContent2.getLocallyBinaryPatternsOnGS()));
        double cm3x3Similarity = 1.0 / (1 + this.calculateDistance(visualContent1.getColorMomentsOnHSV3x3(), visualContent2.getColorMomentsOnHSV3x3()));
        double cn3x3Similarity = 1.0 / (1 + this.calculateDistance(visualContent1.getColorNamingHistogram3x3(), visualContent2.getColorNamingHistogram3x3()));
        double glrlm3x3Similarity = 1.0 / (1 + this.calculateDistance(visualContent1.getGrayLevelRunLengthMatrix3x3(), visualContent2.getGrayLevelRunLengthMatrix3x3()));
        double lbp3x3Similarity = 1.0 / (1 + this.calculateDistance(visualContent1.getLocallyBinaryPatternsOnGS3x3(), visualContent2.getLocallyBinaryPatternsOnGS3x3()));


        double similarity = CMWeight*cmSimilarity + CNWeight*cnSimilarity + CSDWeight*csdSimilarity
                + GLRLMWeight*glrlmSimilarity + HOGWeight*hogSimilarity + LBPWeight*lbpSimilarity
                + CM3x3Weight*cm3x3Similarity + CN3x3Weight*cn3x3Similarity
                + GLRLM3x3Weight*glrlm3x3Similarity + LBP3x3Weight*lbp3x3Similarity;
        return  similarity;
    }

    /**
     * Calculates the similarity between two collections of terms through the shared terms
     * @param termCollectionA collection A
     * @param termCollectionB collection B
     * @return similarity of the two collections based on common terms
     */
    private double calculateTextualSimilarity(TermCollection termCollectionA, TermCollection termCollectionB) {
        Set<String> commonTerms = new HashSet<>(termCollectionA.getTerms().keySet());
        commonTerms.retainAll(termCollectionB.getTerms().keySet());
        List<Double> list1 = new ArrayList();
        List<Double> list2 = new ArrayList();
        for (String term : commonTerms) {
            list1.add(termCollectionA.getTerms().get(term).getTF_IDF());
            list2.add(termCollectionB.getTerms().get(term).getTF_IDF());
        }
        double similarity = (1-1.0 / (1 + this.calculateDistance(list1,list2)));
        return similarity;
    }

    /**
     * Calculates the relevance of a image for a location
     * @param location location for which the image relevance should be calculated
     * @param image image for which the relevance should be calculated
     * @return relevance score
     */
    public double calculateSimilarityForLocation(Location location, Image image) {
        double visualSimilarity = 0.0;
        //Loop through the wikipedia images of the location and calculate the visual similarity
        for (Map.Entry<String, WikipediaImg> imageEntry : location.getWikipediaImages().entrySet()) {
            double sim = this.calculateVisualSimilarity(image.getVisualContent(), imageEntry.getValue().getVisualContent());
            //Store the similarity of the most similar wikipedia image
            if (sim > visualSimilarity) {
                visualSimilarity = sim;
            }
        }
        //Get similarity between GPS position of location and image
        double positionSimilarity = this.calculateGPSPositionSimilarity(location.getLatitude(),image.getLatitude(),location.getLongitude(), image.getLongitude());
        //Get rank score of image
        double rankScore = 1.0/image.getRank();
        //Get image property score
        double imagePropertiesScore = this.calculateImagePropertiesScore(image);
        //Get textual similarity between the location text and the image text
        double textualSimilarity = this.calculateTextualSimilarity(location.getTermCollection(), image.getTermCollection());
        double relevanceScore = LocationGPSWeight * positionSimilarity + LocationRankWeight*rankScore
                +LocationPropertiesWeight*imagePropertiesScore + LocationVisualSimilarityWeight *visualSimilarity
                + LocationTextualSimilarityWeight *textualSimilarity;
        return relevanceScore;
    }

    /**
     * Calculates the similarity between two images for the calculation of diverse images
     * @param image1 first image
     * @param image2 second image
     * @return similarity between images
     */
    public double calculateSimilarityForImages(Image image1, Image image2) {
        //Similarity of GPS position
        double positionSimilarity = this.calculateGPSPositionSimilarity(image1.getLatitude(),image2.getLatitude(),image1.getLongitude(), image2.getLongitude());
        //Similarity of relevance score
        double relevanceSimilarity = 1.0 / (double) (1 + Math.abs(image2.getRelevanceScore() - image1.getRelevanceScore()));
        //visual similarity
        double visualSimilarity = this.calculateVisualSimilarity(image1.getVisualContent(), image2.getVisualContent());
        //textual similarity of the two images
        double textualSimilarity = this.calculateTextualSimilarity(image1.getTermCollection(), image2.getTermCollection());
        double similarity = (ImageGPSWeight * positionSimilarity) + ImageRelevanceWeight *relevanceSimilarity +
                ImageVisualSimilarityWeight *visualSimilarity + ImageTextualSimilarityWeight *textualSimilarity;
        return similarity;
    }

    /**
     * Calculates the distance of two vectors
     * @param vector1
     * @param vector2
     * @return distance of vectors
     */
    public static double calculateDistance(List<Double> vector1, List<Double> vector2) {
        double distance = 0;
            for (int i = 0; i < vector1.size(); i++) {
                distance += Math.abs(vector2.get(i) - vector1.get(i));
            }
        return distance;
    }

    /**
     * Sets the weights for the different features which are retrieved from the properties file
     */
    public void setWeights () {
        ImageGPSWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageGPSWeight"));
        ImageRelevanceWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageRelevanceWeight"));
        ImageVisualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageVisualSimilarityWeight"));
        ImageTextualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageTextualSimilarityWeight"));

        double sum = ImageGPSWeight + ImageRelevanceWeight + ImageVisualSimilarityWeight + ImageTextualSimilarityWeight;

        if (sum == 0) {
            sum = 1;
        }

        ImageGPSWeight = ImageGPSWeight / sum;
        ImageRelevanceWeight = ImageRelevanceWeight / sum;
        ImageVisualSimilarityWeight = ImageVisualSimilarityWeight / sum;
        ImageTextualSimilarityWeight = ImageTextualSimilarityWeight / sum;


        LocationGPSWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationGPSWeight"));
        LocationRankWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationRankWeight"));
        LocationPropertiesWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationPropertiesWeight"));
        LocationVisualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationVisualSimilarityWeight"));
        LocationTextualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationTextualSimilarityWeight"));
        sum = LocationGPSWeight + LocationRankWeight + LocationPropertiesWeight
                + LocationVisualSimilarityWeight + LocationTextualSimilarityWeight;

        if (sum == 0) {
            sum = 1;
        }
        LocationGPSWeight = LocationGPSWeight / sum;
        LocationRankWeight = LocationRankWeight / sum;
        LocationPropertiesWeight = LocationPropertiesWeight / sum;
        LocationVisualSimilarityWeight = LocationVisualSimilarityWeight / sum;
        LocationTextualSimilarityWeight = LocationTextualSimilarityWeight / sum;

        CMWeight = Double.parseDouble(PropConfig.accessPropertyFile("CMWeight"));
        CNWeight = Double.parseDouble(PropConfig.accessPropertyFile("CNWeight"));
        CSDWeight = Double.parseDouble(PropConfig.accessPropertyFile("CSDWeight"));
        GLRLMWeight = Double.parseDouble(PropConfig.accessPropertyFile("GLRLMWeight"));
        HOGWeight = Double.parseDouble(PropConfig.accessPropertyFile("HOGWeight"));
        LBPWeight = Double.parseDouble(PropConfig.accessPropertyFile("LBPWeight"));
        CM3x3Weight = Double.parseDouble(PropConfig.accessPropertyFile("CM3x3Weight"));
        CN3x3Weight = Double.parseDouble(PropConfig.accessPropertyFile("CN3x3Weight"));
        GLRLM3x3Weight = Double.parseDouble(PropConfig.accessPropertyFile("GLRLM3x3Weight"));
        LBP3x3Weight = Double.parseDouble(PropConfig.accessPropertyFile("LBP3x3Weight"));

        sum = CMWeight + CNWeight + CSDWeight + GLRLMWeight + HOGWeight + LBPWeight
                +CM3x3Weight+CN3x3Weight+GLRLM3x3Weight+LBP3x3Weight;

        if (sum == 0) {
            sum = 1;
        }

        CMWeight = CMWeight / sum;
        CNWeight = CNWeight / sum;
        CSDWeight = CSDWeight / sum;
        GLRLMWeight = GLRLMWeight / sum;
        HOGWeight = HOGWeight / sum;
        LBPWeight = LBPWeight / sum;
        CM3x3Weight = CM3x3Weight / sum;
        CN3x3Weight = CN3x3Weight / sum;
        GLRLM3x3Weight = GLRLM3x3Weight / sum;
        LBP3x3Weight = LBP3x3Weight / sum;
    }



}
