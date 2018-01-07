
package Calculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Images.Image;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class Diversity {

    private static final int MAX_NUMBER_OF_IMAGES_TO_CONSIDER = 110;
    
    public Diversity(int numberOfConsideredRelevantImages) {
        this.numberOfConsideredRelevantImages = numberOfConsideredRelevantImages;
        this.imageSimilarity = new ImageSimilarity();
    }
    
    public Diversity() {
        this(MAX_NUMBER_OF_IMAGES_TO_CONSIDER);
    }

    private int numberOfConsideredRelevantImages;
    private ImageSimilarity imageSimilarity;

    
    public void setImageSimilarity(ImageSimilarity imageSimilarity) {
        this.imageSimilarity = imageSimilarity;
    }

    private List<Image> getTopRelevantImages(Location location, int count) {
        List<Image> result = new ArrayList<>(location.getImages().values());

        result.sort(new Comparator<Image>() {

            @Override
            public int compare(Image image1, Image image2) {
                return Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore()));
            }
        });

        result = result.subList(0, Math.min(count, result.size()));

        return result;
    }

    private Map<String, Map<String, Double>> getSimilarityMatrix(List<Image> images) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        for (Image image01 : images) {
            result.put(image01.getId(), new HashMap<>());
            for (Image image02 : images) {
                result.get(image01.getId()).put(image02.getId(), this.imageSimilarity.calculateSimilarity(image01, image02));
            }
        }

        return result;
    }

    public void calculateDiversityScores(Location location) {
        location.reinitDiversityScores();
        List<Image> topRelevantImages = this.getTopRelevantImages(location, this.numberOfConsideredRelevantImages);
        Map<String, Map<String, Double>> similarityMatrix = this.getSimilarityMatrix(topRelevantImages);
        int size = topRelevantImages.size();
        List<Image> diverseImages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Image topImage = topRelevantImages.remove(0);
            diverseImages.add(topImage);

            for (Image image : topRelevantImages) {
                double similarity = similarityMatrix.get(topImage.getId()).get(image.getId());
                double distance = (similarity > 0) ? (1.0 / similarity) : (Double.MAX_VALUE);
                image.setDiversityScore(image.getDiversityScore() + distance);
            }

            topRelevantImages.sort(new Comparator<Image>() {

                @Override
                public int compare(Image image1, Image image2) {
                    // two then one to get the reverse order (descending)
                    return Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore()));
                }
            });
        }

        for (int i = 0; i < diverseImages.size(); i++) {
            diverseImages.get(i).setDiversityScore(diverseImages.size() - i);
        }
    }

}
