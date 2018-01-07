package Calculation;

import java.util.*;
import Data.PropConfig;
import Images.*;
/**
 * Created by rsume on 05.01.2018.
 */
public class ImageSimilarity {

    public ImageSimilarity() {
    }
    private double ImageGPSWeight;
    private double ImageRankWeight;
    private double ImageRelevanceWeight;
    private double ImageVisualDescriptorsWeight;
    private double ImageTextualDescriptorsWeight;

    private double LocationNameWeight;
    private double LocationGPSWeight;
    private double LocationRankWeight;
    private double LocationPropertiesWeight;
    private double LocationVisualDescriptorsWeight;
    private double LocationTextualDescriptorsWeight;

    private double CMWeight;
    private double CNWeight;
    private double CSDWeight;
    private double GLRLMWeight;
    private double HOGWeight;
    private double LBPWeight;

    private double calculateImagePropertiesScore(Image image) {
        double score = 0.0;

        double numberOfViewsScore = (image.getNumberOfViews() > 0) ? (1.0 / image.getNumberOfViews()) : (0.0);
        double numberOfCommentsScore = (image.getNumberOfComments() > 0) ? (1.0 / image.getNumberOfComments()) : (0.0);

        score = (1.0 * numberOfViewsScore)
                + (0.0 * numberOfCommentsScore);

        return Math.min(1.0, score);
    }

    private double calculateGPSPositionSimilarity(double latitude1, double latitude2, double longitude1, double longitude2) {
        double similarity = 0.0;
            double earthRadius = 6371000; // in meters
            double latitudeDifference = Math.toRadians(latitude2 - latitude1);
            double LongitudeDifference = Math.toRadians(longitude2 - longitude1);
            double a = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                    + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2))
                    * Math.sin(LongitudeDifference / 2) * Math.sin(LongitudeDifference / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadius * c;
            similarity = 1.0 / (1 + distance);

        return Math.min(1.0, similarity);
    }

    private double calculateVisualSimilarity(VisualDescriptors visualDescriptors01, VisualDescriptors visualDescriptors02) {
        double similarity = 0.0;

        double cmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getColorMomentsOnHSV(), visualDescriptors02.getColorMomentsOnHSV()));
        double cnSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getColorNamingHistogram(), visualDescriptors02.getColorNamingHistogram()));
        double csdSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getColorStructureDescriptor(), visualDescriptors02.getColorStructureDescriptor()));
        double glrlmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getGrayLevelRunLengthMatrix(), visualDescriptors02.getGrayLevelRunLengthMatrix()));
        double hogSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getHistogramOfOrientedGradients(), visualDescriptors02.getHistogramOfOrientedGradients()));
        double lbpSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors01.getLocallyBinaryPatternsOnGS(), visualDescriptors02.getLocallyBinaryPatternsOnGS()));

        similarity = CMWeight*cmSimilarity + CNWeight*cnSimilarity + CSDWeight*csdSimilarity
                + GLRLMWeight*glrlmSimilarity + HOGWeight*hogSimilarity + LBPWeight*lbpSimilarity;
        return Math.min(1.0, similarity);
    }


    private double calculateTextualSimilarity_Intersect(TermCollection textualDescriptors01, TermCollection textualDescriptors02) {
        double similarity = 0.0;

        // get shared terms between the two images (intersect)
        Set<String> sharedTerms = new HashSet<>(textualDescriptors01.getTerms().keySet());
        sharedTerms.retainAll(textualDescriptors02.getTerms().keySet());

        // get TF_IDF scores for the shared instances
        List<Double> tf_idfList01 = new ArrayList<>();
        List<Double> tf_idfList02 = new ArrayList<>();
        for (String term : sharedTerms) {
            tf_idfList01.add(textualDescriptors01.getTerms().get(term).getTF_IDF());
            tf_idfList02.add(textualDescriptors02.getTerms().get(term).getTF_IDF());
        }
        similarity = 1.0 / (1 + this.calculateManhattanDistance(tf_idfList01,tf_idfList02));

        return Math.min(1.0, similarity);
    }

    private double calculateTextualSimilarity_Union(TermCollection textualDescriptors01, TermCollection textualDescriptors02) {
        double similarity = 0.0;

        // get all terms in the two images (union)
        Set<String> allTerms = new HashSet<>(textualDescriptors01.getTerms().keySet());
        allTerms.addAll(textualDescriptors02.getTerms().keySet());

        // get TF_IDF scores for all instances
        List<Double> tf_idfList01 = new ArrayList<>();
        List<Double> tf_idfList02 = new ArrayList<>();
        for (String term : allTerms) {
            Term term01 = textualDescriptors01.getTerms().get(term);
            if (term01 != null) {
                tf_idfList01.add(term01.getTF_IDF());
            } else {
                tf_idfList01.add(0.0);
            }

            Term term02 = textualDescriptors02.getTerms().get(term);
            if (term02 != null) {
                tf_idfList02.add(term02.getTF_IDF());
            } else {
                tf_idfList02.add(0.0);
            }
        }

        similarity = this.calculateCosineSimilarity(tf_idfList01, tf_idfList02);

        return Math.min(1.0, similarity);
    }


    public double calculateSimilarity(Image image01, Image image02) {
        double similarity = 0.0;

        double positionSimilarity = this.calculateGPSPositionSimilarity(image01.getLatitude(),image02.getLatitude(),image01.getLongitude(), image02.getLongitude());
        double rankSimilarity = 1.0 / (double) (1 + Math.abs(image02.getRank() - image01.getRank()));
        double relevanceSimilarity = 1.0 / (double) (1 + Math.abs(image02.getRelevanceScore() - image01.getRelevanceScore()));
        double visualSimilarity = this.calculateVisualSimilarity(image01.getVisualDescriptors(), image02.getVisualDescriptors());
        double textualSimilarity = this.calculateTextualSimilarity_Union(image01.getTextualDescriptors(), image02.getTextualDescriptors());

        similarity = (ImageGPSWeight * positionSimilarity) + ImageRankWeight *rankSimilarity + ImageRelevanceWeight *relevanceSimilarity +
                ImageVisualDescriptorsWeight *visualSimilarity + ImageTextualDescriptorsWeight *textualSimilarity;
        return Math.min(1.0, similarity);
    }

    public double calculateSimilarity(Image image, WikiImage wikiImage) {
        return this.calculateVisualSimilarity(image.getVisualDescriptors(), wikiImage.getVisualDescriptors());
    }

    public double calculateSimilarity(Location location, Image image) {
        double similarity = 0.0;

        double visualSimilarity = 0.0;
        for (Map.Entry<String, WikiImage> imageEntry : location.getWikiImages().entrySet()) {
            double tempSimilarity = this.calculateSimilarity(image, imageEntry.getValue());
            if (tempSimilarity > visualSimilarity) {
                visualSimilarity = tempSimilarity;
            }
        }

        double nameTitleSimilarity = this.calculateWordOverlapSimilarity(location.getName(), image.getTitle());
        double nameTagsSimilarity = this.calculateWordOverlapSimilarity(location.getName(), image.getTags());
        double nameSimilarity = (nameTitleSimilarity > nameTagsSimilarity) ? nameTitleSimilarity : nameTagsSimilarity;

        double positionSimilarity = this.calculateGPSPositionSimilarity(location.getLatitude(),image.getLatitude(),location.getLongitude(), image.getLongitude());

        double rankSimilarity = (image.getRank() > 0) ? (1.0 / image.getRank()) : 0.0;
        double imagePropertiesSimilarity = this.calculateImagePropertiesScore(image);
        //double textualSimilarity = this.calculateTextualSimilarity_Intersect(location.getTextualDescriptors(), image.getTextualDescriptors());
        double textualSimilarity = this.calculateTextualSimilarity_Intersect(location.getTextualDescriptors(), image.getTextualDescriptors());
        similarity = LocationNameWeight*nameSimilarity + LocationGPSWeight * positionSimilarity + LocationRankWeight*rankSimilarity
                +LocationPropertiesWeight*imagePropertiesSimilarity + LocationVisualDescriptorsWeight*visualSimilarity
                + LocationTextualDescriptorsWeight*textualSimilarity;
        return Math.min(1.0, similarity);
    }

    public static double calculateCosineSimilarity(List<Double> vector01, List<Double> vector02) {
        double similarity = 0.0;
        double dotProduct = 0.0;
        double norm01 = 0.0;
        double norm02 = 0.0;

        if (vector01.size() == vector02.size()) {
            if (!vector01.isEmpty()) {
                for (int i = 0; i < vector01.size(); i++) {
                    dotProduct += vector01.get(i) * vector02.get(i);
                    norm01 += Math.pow(vector01.get(i), 2);
                    norm02 += Math.pow(vector02.get(i), 2);
                }

                similarity = dotProduct / (Math.sqrt(norm01) * Math.sqrt(norm02));
            }
        }
        return similarity;
    }
    public static double calculateManhattanDistance(List<Double> vector01, List<Double> vector02) {
        double distance = 0.0;

        if (vector01.size() == vector02.size()) {
            for (int i = 0; i < vector01.size(); i++) {
                distance += Math.abs(vector02.get(i) - vector01.get(i));
            }
        } else {
            distance = Double.MAX_VALUE;
        }

        return distance;
    }
    public static double calculateWordOverlapSimilarity(String text01, String text02) {
        double similarity = 0.0;

        String[] tokensArray01 = text01.split("\\W+");
        String[] tokensArray02 = text02.split("\\W+");

        Set<String> tokensSet01 = new HashSet<>(Arrays.asList(tokensArray01));
        Set<String> tokensSet02 = new HashSet<>(Arrays.asList(tokensArray02));
        Set<String> tokensSet = new HashSet<>();
        tokensSet.addAll(tokensSet01);
        tokensSet.addAll(tokensSet02);

        int commonTokens = (tokensSet01.size() + tokensSet02.size()) - tokensSet.size();
        similarity = (double) commonTokens / (double) Math.min(tokensSet01.size(), tokensSet02.size());

        return similarity;
    }
    public void setWeights () {
        ImageGPSWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageGPSWeight"));
        ImageRankWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageRankWeight"));
        ImageRelevanceWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageRelevanceWeight"));
        ImageVisualDescriptorsWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageVisualDescriptorsWeight"));
        ImageTextualDescriptorsWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageTextualDescriptorsWeight"));

        double sum = ImageGPSWeight + ImageRankWeight + ImageRelevanceWeight + ImageVisualDescriptorsWeight + ImageTextualDescriptorsWeight;

        if (sum == 0) {
            sum = 1;
        }

        ImageGPSWeight = ImageGPSWeight / sum;
        ImageRankWeight = ImageRankWeight / sum;
        ImageRelevanceWeight = ImageRelevanceWeight / sum;
        ImageVisualDescriptorsWeight = ImageVisualDescriptorsWeight / sum;
        ImageTextualDescriptorsWeight = ImageTextualDescriptorsWeight / sum;


        LocationNameWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationNameWeight"));
        LocationGPSWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationGPSWeight"));
        LocationRankWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationRankWeight"));
        LocationPropertiesWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationPropertiesWeight"));
        LocationVisualDescriptorsWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationVisualDescriptorsWeight"));
        LocationTextualDescriptorsWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationTextualDescriptorsWeight"));
        sum = LocationNameWeight + LocationGPSWeight + LocationRankWeight + LocationPropertiesWeight
                + LocationVisualDescriptorsWeight + LocationTextualDescriptorsWeight;

        if (sum == 0) {
            sum = 1;
        }

        LocationNameWeight = LocationNameWeight / sum;
        LocationGPSWeight = LocationGPSWeight / sum;
        LocationRankWeight = LocationRankWeight / sum;
        LocationPropertiesWeight = LocationPropertiesWeight / sum;
        LocationVisualDescriptorsWeight = LocationVisualDescriptorsWeight / sum;
        LocationTextualDescriptorsWeight = LocationTextualDescriptorsWeight / sum;

        CMWeight = Double.parseDouble(PropConfig.accessPropertyFile("CMWeight"));
        CNWeight = Double.parseDouble(PropConfig.accessPropertyFile("CNWeight"));
        CSDWeight = Double.parseDouble(PropConfig.accessPropertyFile("CSDWeight"));
        GLRLMWeight = Double.parseDouble(PropConfig.accessPropertyFile("GLRLMWeight"));
        HOGWeight = Double.parseDouble(PropConfig.accessPropertyFile("HOGWeight"));
        LBPWeight = Double.parseDouble(PropConfig.accessPropertyFile("LBPWeight"));

        sum = CMWeight + CNWeight + CSDWeight + GLRLMWeight + HOGWeight + LBPWeight;

        if (sum == 0) {
            sum = 1;
        }

        CMWeight = CMWeight / sum;
        CNWeight = CNWeight / sum;
        CSDWeight = CSDWeight / sum;
        GLRLMWeight = GLRLMWeight / sum;
        HOGWeight = HOGWeight / sum;
        LBPWeight = LBPWeight / sum;
    }
}
