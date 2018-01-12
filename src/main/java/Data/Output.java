package Data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import Images.Image;
import Images.Location;
/**
 * Created by rsume on 05.01.2018.
 */
public class Output {
    /**
     * Method which constructs the result file
     * @param file Output file
     * @param locations list of locations for which the result file should be created
     * @param numberOfImagesToPrint number of top images which should be stored for every location
     * @param readGroundTruths if true the precision, cluster recall and harmonic mean are calculated
     */
    public void writeOutput(File file, Map<String, Location> locations, int numberOfImagesToPrint, String readGroundTruths) {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter writer = new PrintWriter(file);
            List<Location> orderedLocations = new ArrayList<>(locations.values());
            orderedLocations.sort((location1, location2) -> Integer.valueOf(location1.getNumber()).compareTo(Integer.valueOf(location2.getNumber())));
            if(readGroundTruths.equals("true")) {
                System.out.println("Precision10 --------- Precision50 --------- ClusterRecall10 --------- ClusterRecall50");
            }
            double precision10 = 0;
            double precision50 = 0;
            double clusterRecall10 = 0;
            double clusterRecall50 = 0;
            //loop through all locations
            for (int i = 0; i < orderedLocations.size(); i++) {
                Location location = orderedLocations.get(i);
                List<Image> orderedImages = location.getTopImages(numberOfImagesToPrint);
                //calculate precision and cluster recall if necessary
                if(readGroundTruths.equals("true")) {
                    precision10 = precision10 + getPrecisionAt(10, orderedImages);
                    precision50 = precision10 + getPrecisionAt(50, orderedImages);
                    clusterRecall10 = clusterRecall10 + getClusterRecallAt(10, location.getClusters(), orderedImages);
                    clusterRecall50 = clusterRecall50 + getClusterRecallAt(50, location.getClusters(), orderedImages);
                }
                //loop through every image of the top images and save it to the result file
                for (int j = 0; j < orderedImages.size(); j++) {
                    Image image = orderedImages.get(j);
                    writer.println(location.getNumber()
                            + " "
                            + 0
                            + " "
                            + image.getId()
                            + " "
                            + j
                            + " "
                            + image.getDiversityScore()
                            + " "
                            + "RUN1");
                }
            }
            if(readGroundTruths.equals("true")) {
                System.out.print(precision10 / orderedLocations.size());
                System.out.print("-------");
                System.out.print(precision50 / orderedLocations.size());
                System.out.print("-------");
                System.out.print(clusterRecall10 / orderedLocations.size());
                System.out.print("-------");
                System.out.println(clusterRecall50 / orderedLocations.size());

                System.out.println("Harmonic Mean 10 ------- Harmonic Mean 50");
                List values10 = new ArrayList();
                List values50 = new ArrayList();
                values10.add(precision10 / orderedLocations.size());
                values50.add(precision50 / orderedLocations.size());
                values10.add(clusterRecall10 / orderedLocations.size());
                values50.add(clusterRecall50 / orderedLocations.size());
                System.out.print(this.harmonicMean(values10));
                System.out.println("  -------  "+this.harmonicMean(values50));
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Calculates the precision of the retrieved images
     * @param k number of top results which should be taken into account
     * @param images list of images
     * @return precision
     */
    public double getPrecisionAt(int k, List<Image> images) {
        double trueCount = 0, count = 0;
        if(k >= images.size())
            k = images.size();
        for(Image i : images) {
            if(i.isRelevant_GT())
                trueCount++;
            count++;
            if(count >= k)
                break;
        }
        return trueCount / k;
    }

    /**
     * Method which calculates the number of different cluster which occur in the top images compared to all the clusters in the ground truth
     * @param k number of top results which should be taken into account
     * @param clusters List of existing clusters in the ground truth
     * @param images list of images
     * @return cluster recall
     */
    public double getClusterRecallAt(int k, List<Integer> clusters, List<Image> images) {
        int count = 0;
        TreeSet<Integer> recalledClusters = new TreeSet<Integer>();
        if(k >= clusters.size())
            k = clusters.size();
        for(Image i : images) {
            recalledClusters.add(i.getClusterId_GT());
            count++;
            if (count >= k)
                break;
        }
        return (double)recalledClusters.size() / k;
    }

    /**
     * Calculates the harmonic mean between the precision and the cluster recall at a specific number of images
     * @param values list of the precision and cluster recall
     * @return harmonic mean
     */
    public double harmonicMean (List<Double> values) {
        double Harmonic_Mean;
        double divisor = 0;
        for(int i =0;i<values.size();i++) {
            divisor = divisor + 1/values.get(i);
        }
        Harmonic_Mean = values.size()/divisor;
        return Harmonic_Mean;
    }
}
