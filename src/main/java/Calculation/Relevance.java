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

    public void setSimilarityCalculation(SimilarityCalculation similarityCalculation) {
        this.similarityCalculation = similarityCalculation;
    }

    public void calculateRelevanceScores(Location location) {
        for (Map.Entry<String, Image> imageEntry : location.getImages().entrySet()) {
            double x = this.similarityCalculation.calculateSimilarityForWikiImages(location, imageEntry.getValue());
            imageEntry.getValue().setRelevanceScore(this.similarityCalculation.calculateSimilarityForWikiImages(location, imageEntry.getValue()));
        }
    }
}
