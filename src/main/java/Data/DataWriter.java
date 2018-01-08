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
public class DataWriter {

    public void writeOutput(File file, Map<String, Location> locations, int numberOfImagesToPrint, String runId) {
        try {
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            PrintWriter writer = new PrintWriter(file);

            // order locations according to their query_id (number)
            List<Location> orderedLocations = new ArrayList<>(locations.values());
            orderedLocations.sort(new Comparator<Location>() {
                @Override
                public int compare(Location location1, Location location2) {
                    return Integer.valueOf(location1.getNumber()).compareTo(Integer.valueOf(location2.getNumber()));
                }
            });
            System.out.println("Precision10 --------- Precision50 --------- ClusterRecall10 --------- ClusterRecall50");
            double precision10 = 0;
            double precision50 = 0;
            double clusterRecall10 = 0;
            double clusterRecall50 = 0;
            for (int i = 0; i < orderedLocations.size(); i++) {
                Location location = orderedLocations.get(i);
                //System.out.println(location.getTitle()+ " "+ location.getNumber());

                List<Image> orderedImages = location.getTopImages(numberOfImagesToPrint);
                precision10 = precision10+ getPrecisionAt(10,orderedImages);
                precision50 = precision10+ getPrecisionAt(50,orderedImages);
                clusterRecall10 = clusterRecall10 + getClusterRecallAt(10,location.getClusters(),orderedImages);
                clusterRecall50 = clusterRecall50 + getClusterRecallAt(50,location.getClusters(),orderedImages);
                //System.out.println(getPrecisionAt(5,orderedImages));
                for (int j = 0; j < orderedImages.size(); j++) {
                    Image image = orderedImages.get(j);
                    //if(location.getTitle().equals("cabrillo")) {
                       // System.out.print(image.getId()+ "   ");
                        //System.out.print(image.getClusterId_GT()+ "   ");

                    //}
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
                //System.out.println();
            }
            System.out.print(precision10/orderedLocations.size());
            System.out.print("-------");
            System.out.print(precision50/orderedLocations.size());
            System.out.print("-------");
            System.out.print(clusterRecall10/orderedLocations.size());
            System.out.print("-------");
            System.out.println(clusterRecall50/orderedLocations.size());

            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double getPrecisionAt(int k, List<Image> images) {

        double trueCount = 0, count = 0;

        if(k >= images.size())
            k = images.size();

        for(Image i : images) {
            //System.out.print(i.isRelevant_GT());
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
        //System.out.println(recalledClusters);
        //System.out.println(recalledClusters.size());
        //System.out.println(k);

        return (double)recalledClusters.size() / k;
    }
}
