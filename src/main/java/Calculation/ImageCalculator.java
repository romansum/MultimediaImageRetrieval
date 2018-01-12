package Calculation;

import java.io.File;
import Data.Input;
import Data.Output;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import Data.PropConfig;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class ImageCalculator {

    private static SimilarityCalculation similarityCalculation;
    private static Relevance relevanceScorer;
    private static Diversity diversityScorer;
    //Variable which defines if existing ground truths should be used to calculate precision and cluster recall
    private static String readGroundTruths = "false";

    /**
     * Main class for the calculation of the relevant and distinct images for a location
     * @param args
     */
    public static void main(String[] args) {
        readGroundTruths =  PropConfig.accessPropertyFile("readGroundTruths");
        Input reader = new Input();
        reader.read(readGroundTruths);
        similarityCalculation = new SimilarityCalculation();
        //Set weights for the descriptors from the properties file
        similarityCalculation.setWeights();
        relevanceScorer = new Relevance();
        relevanceScorer.setSimilarityCalculation(similarityCalculation);
        diversityScorer = new Diversity();
        diversityScorer.setSimilarityCalculation(similarityCalculation);
        //Calculate the relevance and diversity for each location
        for (Map.Entry<String, Location> locationEntry : reader.getLocations().entrySet()) {
            relevanceScorer.calculateRelevanceScores(locationEntry.getValue());
            diversityScorer.calculateDiversityScores(locationEntry.getValue());
        }
        Output writer = new Output();
        //Write the resulting images in the result file
        File outputFile = new File(PropConfig.accessPropertyFile("outputFile"));
        writer.writeOutput(outputFile, reader.getLocations(), Integer.parseInt(PropConfig.accessPropertyFile("OutputImages")), "TestRun",readGroundTruths);
        System.out.println("Application finished successfully.");
    }

}
