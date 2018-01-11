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

    private double ImageGPSWeight;
    private double ImageRankWeight;
    private double ImageRelevanceWeight;
    private double ImageVisualSimilarityWeight;
    private double ImageTextualSimilarityWeight;

    private double LocationNameWeight;
    private double LocationGPSWeight;
    private double LocationRankWeight;
    private double LocationPropertiesWeight;
    private double LocationVisualSimilarityWeight;
    private double LocationTextualSimilarityWeight;

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

    private double calculateImagePropertiesScore(Image image) {
        //double score = 0.0;
        double numberOfViews = 0;
        double numberOfComments = 0;
        if (image.getNumberOfViews() > 0) {
            numberOfViews = 1-1.0/image.getNumberOfViews();
        }
        if (image.getNumberOfComments() > 0) {
            numberOfComments = 1-1.0/image.getNumberOfViews();
        }
        double score = (numberOfViews+numberOfComments)/2;
        score = numberOfViews;
        return score;
    }

    /**
     * Calculation of the similarity of two GPS positions with the  Haversine formula
     * @param startLat
     * @param endLat
     * @param startLong
     * @param endLong
     * @return
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

        return Math.min(1.0, similarity);
    }

    private double calculateVisualSimilarity(VisualContent visualContent1, VisualContent visualContent2) {
        double cmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getColorMomentsOnHSV(), visualContent2.getColorMomentsOnHSV()));
        double cnSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getColorNamingHistogram(), visualContent2.getColorNamingHistogram()));
        double csdSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getColorStructureDescriptor(), visualContent2.getColorStructureDescriptor()));
        double glrlmSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getGrayLevelRunLengthMatrix(), visualContent2.getGrayLevelRunLengthMatrix()));
        double hogSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getHistogramOfOrientedGradients(), visualContent2.getHistogramOfOrientedGradients()));
        double lbpSimilarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getLocallyBinaryPatternsOnGS(), visualContent2.getLocallyBinaryPatternsOnGS()));
        double cm3x3Similarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getColorMomentsOnHSV3x3(), visualContent2.getColorMomentsOnHSV3x3()));
        double cn3x3Similarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getColorNamingHistogram3x3(), visualContent2.getColorNamingHistogram3x3()));
        double glrlm3x3Similarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getGrayLevelRunLengthMatrix3x3(), visualContent2.getGrayLevelRunLengthMatrix3x3()));
        double lbp3x3Similarity = 1.0 / (1 + this.calculateManhattanDistance(visualContent1.getLocallyBinaryPatternsOnGS3x3(), visualContent2.getLocallyBinaryPatternsOnGS3x3()));


        double similarity = CMWeight*cmSimilarity + CNWeight*cnSimilarity + CSDWeight*csdSimilarity
                + GLRLMWeight*glrlmSimilarity + HOGWeight*hogSimilarity + LBPWeight*lbpSimilarity
                + CM3x3Weight*cm3x3Similarity + CN3x3Weight*cn3x3Similarity
                + GLRLM3x3Weight*glrlm3x3Similarity + LBP3x3Weight*lbp3x3Similarity;
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


    public double calculateSimilarityForImages(Image image01, Image image02) {
        double positionSimilarity = this.calculateGPSPositionSimilarity(image01.getLatitude(),image02.getLatitude(),image01.getLongitude(), image02.getLongitude());
        double rankSimilarity = 1.0 / (double) (1 + Math.abs(image02.getRank() - image01.getRank()));
        double relevanceSimilarity = 1.0 / (double) (1 + Math.abs(image02.getRelevanceScore() - image01.getRelevanceScore()));
        double visualSimilarity = this.calculateVisualSimilarity(image01.getVisualContent(), image02.getVisualContent());
        double textualSimilarity = this.calculateTextualSimilarity_Union(image01.getTextualDescriptors(), image02.getTextualDescriptors());

        double similarity = (ImageGPSWeight * positionSimilarity) + ImageRankWeight *rankSimilarity + ImageRelevanceWeight *relevanceSimilarity +
                ImageVisualSimilarityWeight *visualSimilarity + ImageTextualSimilarityWeight *textualSimilarity;
        return Math.min(1.0, similarity);
    }

    public double calculateSimilarityForImages(Image image, WikipediaImg wikipediaImg) {
        return this.calculateVisualSimilarity(image.getVisualContent(), wikipediaImg.getVisualContent());
    }

    public double calculateSimilarityForWikiImages(Location location, Image image) {
        double visualSimilarity = 0.0;
        for (Map.Entry<String, WikipediaImg> imageEntry : location.getWikipediaImages().entrySet()) {
            double tempSimilarity = this.calculateSimilarityForImages(image, imageEntry.getValue());
            if (tempSimilarity > visualSimilarity) {
                visualSimilarity = tempSimilarity;
            }
        }


        double nameTitleSimilarity = this.calculateWordOverlapSimilarity(location.getName(), image.getTitle());
        double nameTagsSimilarity = this.calculateWordOverlapSimilarity(location.getName(), image.getTags());
        double nameSimilarity = (nameTitleSimilarity > nameTagsSimilarity) ? nameTitleSimilarity : nameTagsSimilarity;

        double positionSimilarity = this.calculateGPSPositionSimilarity(location.getLatitude(),image.getLatitude(),location.getLongitude(), image.getLongitude());

        double rankSimilarity = 1.0/image.getRank();

        double imagePropertiesSimilarity = this.calculateImagePropertiesScore(image);
        double textualSimilarity = this.calculateTextualSimilarity_Intersect(location.getTermCollection(), image.getTextualDescriptors());
        double similarity = LocationNameWeight*nameSimilarity + LocationGPSWeight * positionSimilarity + LocationRankWeight*rankSimilarity
                +LocationPropertiesWeight*imagePropertiesSimilarity + LocationVisualSimilarityWeight *visualSimilarity
                + LocationTextualSimilarityWeight *textualSimilarity;
        return Math.min(1.0, similarity);
    }

    public static double calculateCosineSimilarity(List<Double> vector1, List<Double> vector2) {
        double sumAB = 0;
        double sumA = 0;
        double sumB = 0;
        for (int i = 0; i < vector1.size(); i++) {
            sumAB = sumAB + vector1.get(i) * vector2.get(i);
            sumA = sumA + vector1.get(i)*vector1.get(i);
            sumB = sumB + vector2.get(i)*vector2.get(i);
        }
        double similarity = sumAB / (Math.sqrt(sumA) * Math.sqrt(sumB));
        return similarity;
    }
    public static double calculateManhattanDistance(List<Double> vector01, List<Double> vector02) {
        double distance = 0;

        //if (vector01.size() == vector02.size()) {
            for (int i = 0; i < vector01.size(); i++) {
                distance += Math.abs(vector02.get(i) - vector01.get(i));
            }
        /*} else {
            distance = Double.MAX_VALUE;
        }*/

        return distance;
    }
    public static double calculateWordOverlapSimilarity(String textA, String textB) {
        String[] tokensArray01 = textA.split("\\W+");
        String[] tokensArray02 = textB.split("\\W+");

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
        ImageVisualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageVisualSimilarityWeight"));
        ImageTextualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("ImageTextualSimilarityWeight"));

        double sum = ImageGPSWeight + ImageRankWeight + ImageRelevanceWeight + ImageVisualSimilarityWeight + ImageTextualSimilarityWeight;

        if (sum == 0) {
            sum = 1;
        }

        ImageGPSWeight = ImageGPSWeight / sum;
        ImageRankWeight = ImageRankWeight / sum;
        ImageRelevanceWeight = ImageRelevanceWeight / sum;
        ImageVisualSimilarityWeight = ImageVisualSimilarityWeight / sum;
        ImageTextualSimilarityWeight = ImageTextualSimilarityWeight / sum;


        LocationNameWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationNameWeight"));
        LocationGPSWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationGPSWeight"));
        LocationRankWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationRankWeight"));
        LocationPropertiesWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationPropertiesWeight"));
        LocationVisualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationVisualSimilarityWeight"));
        LocationTextualSimilarityWeight = Double.parseDouble(PropConfig.accessPropertyFile("LocationTextualSimilarityWeight"));
        sum = LocationNameWeight + LocationGPSWeight + LocationRankWeight + LocationPropertiesWeight
                + LocationVisualSimilarityWeight + LocationTextualSimilarityWeight;

        if (sum == 0) {
            sum = 1;
        }

        LocationNameWeight = LocationNameWeight / sum;
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
