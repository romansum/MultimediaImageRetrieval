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

    private static final int NUMBER_OF_OUTPUT_IMAGES = 50;
    private static SimilarityCalculation similarityCalculation;
    private static Relevance relevanceScorer;
    private static Diversity diversityScorer;
    private static String readGroundTruths = "false";

    public static void main(String[] args) {
        readGroundTruths =  PropConfig.accessPropertyFile("readGroundTruths");
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Application started.");

        Input reader = new Input();
        reader.read(readGroundTruths);

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Data was read successfully.");

        similarityCalculation = new SimilarityCalculation();
        similarityCalculation.setWeights();

        relevanceScorer = new Relevance();
        relevanceScorer.setSimilarityCalculation(similarityCalculation);

        diversityScorer = new Diversity();
        diversityScorer.setSimilarityCalculation(similarityCalculation);

        for (Map.Entry<String, Location> locationEntry : reader.getLocations().entrySet()) {
            relevanceScorer.calculateRelevanceScores(locationEntry.getValue());
            diversityScorer.calculateDiversityScores(locationEntry.getValue());
        }

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Images were ranked successfully.");

        Output writer = new Output();
        File outputFile = new File(PropConfig.accessPropertyFile("outputFile"));
        writer.writeOutput(outputFile, reader.getLocations(), Integer.parseInt(PropConfig.accessPropertyFile("OutputImages")), "TestRun",readGroundTruths);

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Application finished successfully.");
    }

}
