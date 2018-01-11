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

    public void writeOutput(File file, Map<String, Location> locations, int numberOfImagesToPrint, String runId, String readGroundTruths) {
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
            for (int i = 0; i < orderedLocations.size(); i++) {
                Location location = orderedLocations.get(i);
                List<Image> orderedImages = location.getTopImages(numberOfImagesToPrint);
                if(readGroundTruths.equals("true")) {
                    precision10 = precision10 + getPrecisionAt(10, orderedImages);
                    precision50 = precision10 + getPrecisionAt(50, orderedImages);
                    clusterRecall10 = clusterRecall10 + getClusterRecallAt(10, location.getClusters(), orderedImages);
                    clusterRecall50 = clusterRecall50 + getClusterRecallAt(50, location.getClusters(), orderedImages);
                }
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
                            + runId);
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
