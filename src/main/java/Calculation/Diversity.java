
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
    //Number of most relevant images which are considered for the diversity calculation
    private int ConsideredImages;
    private SimilarityCalculation similarityCalculation;

    public Diversity() {
        ConsideredImages = Integer.parseInt(PropConfig.accessPropertyFile("ConsideredImages"));
        this.similarityCalculation = new SimilarityCalculation();
    }
    public void setSimilarityCalculation(SimilarityCalculation similarityCalculation) {
        this.similarityCalculation = similarityCalculation;
    }

    /**
     * Creates a similarity matrix in which the similarity between each image is stored
     * @param img list of images for which the matrix should be created
     * @return matrix of similarity scores
     */
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

    /**
     * Method which calculates the relevant images based on the relevance and diversity
     * @param location location for which the calulation should be executed
     */
    public void calculateDiversityScores(Location location) {
        //Get the images for the location
        List<Image> relevantImages = new ArrayList<Image>(location.getImages().values());
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(10,10);
        //Sort the images by the relevance score
        relevantImages.sort((image1, image2) -> Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore())));
        //Only consider the first x imagesa
        relevantImages = relevantImages.subList(0, this.ConsideredImages);
        //Calculate the similarities between the images
        Map<String, Map<String, Double>> similarityMatrix = this.getSimilarityMatrix(relevantImages);
        List<Image> diverseImages = new ArrayList<>();
        for (int i = 0; i < this.ConsideredImages; i++) {
            //Get the first image of the list and remove it
            Image usedImage = relevantImages.remove(0);
            //Add it to the list of images which are relevant and distinct
            diverseImages.add(usedImage);
            for (Image image : relevantImages) {
                double similarity = similarityMatrix.get(usedImage.getId()).get(image.getId());
                //Set the distance of each image to the actual image
                double distance = (1.0 / similarity);
                //Add the distance to the diversity score
                image.setDiversityScore(image.getDiversityScore() + distance);
            }
            //Reverse sort the images based on the diversity score
            relevantImages.sort((image1, image2) -> Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore())));
            //diverseImages.add(relevantImages.get(i));
        }
        for (int i = 0; i < diverseImages.size(); i++) {
            diverseImages.get(i).setDiversityScore(diverseImages.size() - i);
        }
    }

}
