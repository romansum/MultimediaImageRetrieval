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

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

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
        //double score = 0.0;
        double numberOfViews = 0;
        if (image.getNumberOfViews() > 0) {
            numberOfViews = 1-1.0/image.getNumberOfViews();
        }
        //System.out.println("NUMBER OF VIEWS   " + image.getNumberOfViews());
        //System.out.println("SCORE    "+numberOfViews);
        //double numberOfViewsScore = (image.getNumberOfViews() > 0) ? (1.0 / image.getNumberOfViews()) : (0.0);
        //double numberOfCommentsScore = (image.getNumberOfComments() > 0) ? (1.0 / image.getNumberOfComments()) : (0.0);

        double score = (0.1 * numberOfViews);
        score = numberOfViews;
        //score = 0;
         //       + (0.0 * numberOfCommentsScore);

        return Math.min(1.0, score);
    }

    private double calculateGPSPositionSimilarity(double startLat, double endLat, double startLong, double endLong) {
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        double similarity = 1.0 / (1 + distance);

        return Math.min(1.0, similarity);
    }

    private double calculateVisualSimilarity(VisualDescriptors visualDescriptors1, VisualDescriptors visualDescriptors2) {
        double cmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getColorMomentsOnHSV(), visualDescriptors2.getColorMomentsOnHSV()));
        double cnSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getColorNamingHistogram(), visualDescriptors2.getColorNamingHistogram()));
        double csdSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getColorStructureDescriptor(), visualDescriptors2.getColorStructureDescriptor()));
        double glrlmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getGrayLevelRunLengthMatrix(), visualDescriptors2.getGrayLevelRunLengthMatrix()));
        double hogSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getHistogramOfOrientedGradients(), visualDescriptors2.getHistogramOfOrientedGradients()));
        double lbpSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualDescriptors1.getLocallyBinaryPatternsOnGS(), visualDescriptors2.getLocallyBinaryPatternsOnGS()));

        double similarity = CMWeight*cmSimilarity + CNWeight*cnSimilarity + CSDWeight*csdSimilarity
                + GLRLMWeight*glrlmSimilarity + HOGWeight*hogSimilarity + LBPWeight*lbpSimilarity;
        return Math.min(1.0, similarity);
    }


    private double calculateTextualSimilarity_Intersect(TermCollection textualDescriptors01, TermCollection textualDescriptors02) {
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
        //System.out.println(this.calculateManhattanDistance(tf_idfList01,tf_idfList02));
        double similarity = (1-1.0 / (1 + this.calculateManhattanDistance(tf_idfList01,tf_idfList02)));
        return Math.min(1.0, similarity);
    }

    private double calculateTextualSimilarity_Union(TermCollection textualDescriptors01, TermCollection textualDescriptors02) {
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

        double similarity = this.calculateCosineSimilarity(tf_idfList01, tf_idfList02);

        return Math.min(1.0, similarity);
    }


    public double calculateSimilarityForDiversity(Image image01, Image image02) {
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

    public double calculateSimilarityForDiversity(Image image, WikiImage wikiImage) {
        return this.calculateVisualSimilarity(image.getVisualDescriptors(), wikiImage.getVisualDescriptors());
    }

    public double calculateSimilarityForRelevance(Location location, Image image) {
        double visualSimilarity = 0.0;
        for (Map.Entry<String, WikiImage> imageEntry : location.getWikiImages().entrySet()) {
            double tempSimilarity = this.calculateSimilarityForDiversity(image, imageEntry.getValue());
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
        double textualSimilarity = this.calculateTextualSimilarity_Intersect(location.getTextualDescriptors(), image.getTextualDescriptors());
        double similarity = LocationNameWeight*nameSimilarity + LocationGPSWeight * positionSimilarity + LocationRankWeight*rankSimilarity
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
        String[] tokensArray01 = text01.split("\\W+");
        String[] tokensArray02 = text02.split("\\W+");

        Set<String> tokensSet01 = new HashSet<>(Arrays.asList(tokensArray01));
        Set<String> tokensSet02 = new HashSet<>(Arrays.asList(tokensArray02));
        Set<String> tokensSet = new HashSet<>();
        tokensSet.addAll(tokensSet01);
        tokensSet.addAll(tokensSet02);

        int commonTokens = (tokensSet01.size() + tokensSet02.size()) - tokensSet.size();
        double similarity = (double) commonTokens / (double) Math.min(tokensSet01.size(), tokensSet02.size());

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
