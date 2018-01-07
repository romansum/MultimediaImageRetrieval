package Calculation;

import java.util.Map;

import Images.Image;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class Relevance {

    public Relevance() {
        this.imageSimilarity = new ImageSimilarity();
    }

    private ImageSimilarity imageSimilarity;

    public void setImageSimilarity(ImageSimilarity imageSimilarity) {
        this.imageSimilarity = imageSimilarity;
    }

    public void calculateRelevanceScores(Location location) {
        location.reinitRelevanceScores();

        for (Map.Entry<String, Image> imageEntry : location.getImages().entrySet()) {
            double x = this.imageSimilarity.calculateSimilarity(location, imageEntry.getValue());
            imageEntry.getValue().setRelevanceScore(this.imageSimilarity.calculateSimilarity(location, imageEntry.getValue()));
        }
    }
}
