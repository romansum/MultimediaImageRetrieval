
package Calculation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Data.PropConfig;
import Images.Image;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class Diversity {

    private int ConsideredImages;
    private ImageSimilarity imageSimilarity;

    public Diversity() {
        ConsideredImages = Integer.parseInt(PropConfig.accessPropertyFile("ConsideredImages"));
        this.imageSimilarity = new ImageSimilarity();
    }


    
    public void setImageSimilarity(ImageSimilarity imageSimilarity) {
        this.imageSimilarity = imageSimilarity;
    }

    private List<Image> getRelevantImages(Location location, int count) {
        List<Image> result = new ArrayList<Image>(location.getImages().values());

        result.sort(new Comparator<Image>() {
            @Override
            public int compare(Image image1, Image image2) {
                return Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore()));
            }
        });
        result = result.subList(0, Math.min(count, result.size()));
        return result;
    }

    private Map<String, Map<String, Double>> getSimilarityMatrix(List<Image> img) {
        Map<String, Map<String, Double>> result = new HashMap<>();

        for (Image image1 : img) {
            result.put(image1.getId(), new HashMap<>());
            for (Image image2 : img) {
                result.get(image1.getId()).put(image2.getId(), this.imageSimilarity.calculateSimilarityForDiversity(image1, image2));
            }
        }
        return result;
    }

    public void calculateDiversityScores(Location location) {
        List<Image> relevantImages = this.getRelevantImages(location, this.ConsideredImages);
        Map<String, Map<String, Double>> similarityMatrix = this.getSimilarityMatrix(relevantImages);
        int size = relevantImages.size();
        List<Image> diverseImages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Image firstImage = relevantImages.remove(0);
            diverseImages.add(firstImage);

            for (Image image : relevantImages) {
                double similarity = similarityMatrix.get(firstImage.getId()).get(image.getId());
                double distance = 0;
                if(similarity > 0) {
                    distance = (1.0 / similarity);
                } else {
                    distance = Double.MAX_VALUE;
                }
                image.setDiversityScore(image.getDiversityScore() + distance);
            }

            relevantImages.sort(new Comparator<Image>() {
                @Override
                public int compare(Image image1, Image image2) {
                    return Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore()));
                }
            });
        }
        for (int i = 0; i < diverseImages.size(); i++) {
            diverseImages.get(i).setDiversityScore(diverseImages.size() - i);
        }
    }

}
