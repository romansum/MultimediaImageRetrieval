
package Calculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Data.PropConfig;
import Images.Image;
import Images.Location;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

/**
 * Created by rsume on 05.01.2018.
 */
public class Diversity {

    private int ConsideredImages;
    private SimilarityCalculation similarityCalculation;

    public Diversity() {
        ConsideredImages = Integer.parseInt(PropConfig.accessPropertyFile("ConsideredImages"));
        this.similarityCalculation = new SimilarityCalculation();
    }


    
    public void setSimilarityCalculation(SimilarityCalculation similarityCalculation) {
        this.similarityCalculation = similarityCalculation;
    }


    private Map<String, Map<String, Double>> getSimilarityMatrix(List<Image> img) {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (Image image1 : img) {
            result.put(image1.getId(), new HashMap<>());
            for (Image image2 : img) {
                result.get(image1.getId()).put(image2.getId(), this.similarityCalculation.calculateSimilarityForImages(image1, image2));
            }
        }
        return result;
    }

    public void calculateDiversityScores(Location location) {
        List<Image> relevantImages = new ArrayList<Image>(location.getImages().values());
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(10,10);

        relevantImages.sort((image1, image2) -> Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore())));
        relevantImages = relevantImages.subList(0, this.ConsideredImages);

        Map<String, Map<String, Double>> similarityMatrix = this.getSimilarityMatrix(relevantImages);
        List<Image> diverseImages = new ArrayList<>();
        for (int i = 0; i < this.ConsideredImages; i++) {
            Image usedImage = relevantImages.remove(0);
            diverseImages.add(usedImage);

            for (Image image : relevantImages) {
                double similarity = similarityMatrix.get(usedImage.getId()).get(image.getId());
                double distance = (1.0 / similarity);
                image.setDiversityScore(image.getDiversityScore() + distance);
            }

            relevantImages.sort((image1, image2) -> Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore())));
            //diverseImages.add(relevantImages.get(i));
        }
        for (int i = 0; i < diverseImages.size(); i++) {
            diverseImages.get(i).setDiversityScore(diverseImages.size() - i);
        }
    }

}
