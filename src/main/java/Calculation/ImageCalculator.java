package Calculation;

import java.io.File;
import Data.DataReader;
import Data.DataWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class ImageCalculator {

    private static final int NUMBER_OF_OUTPUT_IMAGES = 50;
    private static ImageSimilarity imageSimilarity;
    private static Relevance relevanceScorer;
    private static Diversity diversityScorer;

    public static void main(String[] args) {

        Date date;
        DateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Application started.");

        DataReader reader = new DataReader();
        reader.read(false);

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Data was read successfully.");

        imageSimilarity = new ImageSimilarity();
        imageSimilarity.setWeights();

        relevanceScorer = new Relevance();
        relevanceScorer.setImageSimilarity(imageSimilarity);

        diversityScorer = new Diversity();
        diversityScorer.setImageSimilarity(imageSimilarity);

        for (Map.Entry<String, Location> locationEntry : reader.getLocations().entrySet()) {
            relevanceScorer.calculateRelevanceScores(locationEntry.getValue());
            diversityScorer.calculateDiversityScores(locationEntry.getValue());
        }

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Images were ranked successfully.");

        DataWriter writer = new DataWriter();
        File outputfile = new File("C:/Users/rsume/Downloads/div-2014/run.csv");
        writer.writeOutput(outputfile, reader.getLocations(), NUMBER_OF_OUTPUT_IMAGES, "RUN_ID");

        date = new Date();
        System.out.println(dateFormat.format(date) + ", Application finished successfully.");
    }

}
