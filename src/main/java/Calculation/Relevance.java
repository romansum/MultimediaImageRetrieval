package Calculation;

import java.util.Map;
import Images.Image;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class Relevance {

    public Relevance() {
        this.similarityCalculation = new SimilarityCalculation();
    }

    private SimilarityCalculation similarityCalculation;

    /**
     * Setter method to set the Similarity Calculation which is used by the Relevance calculation
     * @param similarityCalculation
     */
    public void setSimilarityCalculation(SimilarityCalculation similarityCalculation) {
        this.similarityCalculation = similarityCalculation;
    }

    /**
     * Method to calculate the relevance of all the images for one location
     * @param location location for which the relevance should be calculated
     */
    public void calculateRelevanceScores(Location location) {
        //Calculates for every image the similarity to the location and the wikipedia images
        for (Map.Entry<String, Image> imageEntry : location.getImages().entrySet()) {
            imageEntry.getValue().setRelevanceScore(this.similarityCalculation.calculateSimilarityForLocation(location, imageEntry.getValue()));
        }
    }
}
