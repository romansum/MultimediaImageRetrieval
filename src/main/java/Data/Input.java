package Data;

import Images.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import Images.TermCollection;
/**
 * Created by rsume on 05.01.2018.
 */
public class Input {

    public Input() {
        this.locations = new HashMap<>();
        this.locationsTermCollection = new HashMap();
        this.imagesTermCollection = new HashMap();
    }
    // List of existion locations
    private Map<String, Location> locations;
    private Map<String, TermCollection> locationsTermCollection;
    private Map<String, TermCollection> imagesTermCollection;

    public Map<String, Location> getLocations() {
        return locations;
    }

    /**
     * reads the locations from the input data and all the attributes of a location
     * @param readGroundTruth if true it reads also the ground truth for locations
     */
    private void readLocations(String readGroundTruth) {
        String filePath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("LocationPath");
        File file = new File(filePath);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document document = dBuilder.parse(file);
            NodeList nodeList = document.getElementsByTagName("topic");
            //loop through all locations
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    Location location = new Location();
                    location.setNumber(Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent()));
                    location.setTitle(element.getElementsByTagName("title").item(0).getTextContent());
                    location.setLatitude(Double.parseDouble(element.getElementsByTagName("latitude").item(0).getTextContent()));
                    location.setLongitude(Double.parseDouble(element.getElementsByTagName("longitude").item(0).getTextContent()));
                    location.setWikiUrl(element.getElementsByTagName("wiki").item(0).getTextContent());
                    TermCollection textualDescriptors = this.locationsTermCollection.get(location.getTitle());
                    location.setTermCollection(textualDescriptors);
                    if(readGroundTruth.equals("true")) {
                        location.setClusters(loadGroundTruthClusters(PropConfig.accessPropertyFile("BasePath") + "gt/dGT/" + location.getTitle() + " dclusterGT.txt"));
                    }
                    this.locations.put(location.getTitle(), location);
                }
            }
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads all existing terms for a location or a image
     * @param path Path to the file where the information is stored
     * @param location if true the terms are read for a location otherwise for an image
     */
    private void readTextualFeatures(String path, boolean location) {
        String filePath = path;
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                String locationTitle = values[0];
                TermCollection textualDescriptors = new TermCollection();
                int startIndex = 1;
                while (!(values[startIndex].startsWith("\"") && values[startIndex].endsWith("\""))) {
                    startIndex++;
                }
                for (int i = startIndex; i < values.length; i += 4) {
                    try {
                        String term = values[i].replace("\"", "");
                        double tf_idf = Double.parseDouble(values[i + 3]);
                        textualDescriptors.getTerms().put(term, new Term(term,tf_idf));
                    } catch (Exception ex) {
                        Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(location) {
                    this.locationsTermCollection.put(locationTitle, textualDescriptors);
                } else {
                    this.imagesTermCollection.put(locationTitle, textualDescriptors);
                }
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads the images and the attributes of the images
     * @param readGroundTruths if true the ground truths for the images are also read
     */
    private void readImages(String readGroundTruths) {
        String directoryPath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("XMLPath");
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            if (file.isFile() && (!file.isHidden())) {
                String fileName = file.getName().replaceFirst("[.][^.]+$", "");
                String locationTitle = fileName;
                Location location = this.locations.get(locationTitle);
                Hashtable<String, Integer> groundTruthRelevance=null;
                Hashtable<String, Integer> groundTruthCluster=null;
                if(readGroundTruths.equals("true")) {
                    groundTruthRelevance = loadGroundTruths(PropConfig.accessPropertyFile("BasePath") + "gt/rGT/" + location.getTitle() + " rGT.txt");
                    groundTruthCluster = loadGroundTruths(PropConfig.accessPropertyFile("BasePath") + "gt/dGT/" + location.getTitle() + " dGT.txt");
                }
                if (location != null) {
                    try {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        Document document = dBuilder.parse(file);
                        document.getDocumentElement().normalize(); // recommended call
                        NodeList nodeList = document.getElementsByTagName("photo");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Node node = nodeList.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;

                                Image image = new Image();
                                image.setId(element.getAttribute("id"));
                                image.setTitle(element.getAttribute("title"));
                                image.setDescription(element.getAttribute("description"));
                                image.setTags(element.getAttribute("tags"));
                                image.setRank(Integer.parseInt(element.getAttribute("rank")));
                                image.setNumberOfComments(Integer.parseInt(element.getAttribute("nbComments")));
                                image.setNumberOfViews(Integer.parseInt(element.getAttribute("views")));
                                image.setLatitude(Double.parseDouble(element.getAttribute("latitude")));
                                image.setLongitude(Double.parseDouble(element.getAttribute("longitude")));

                                TermCollection textualDescriptors = this.imagesTermCollection.get(image.getId());
                                image.setTermCollection(textualDescriptors);
                                if(readGroundTruths.equals("true")) {
                                    if (groundTruthRelevance.get(image.getId()) != null)
                                        image.setRelevant_GT(groundTruthRelevance.get(image.getId()) == 1);

                                    if (groundTruthCluster.get(image.getId()) != null)
                                        image.setClusterId_GT(groundTruthCluster.get(image.getId()));
                                }
                                location.getImages().put(image.getId(), image);
                            }
                        }
                    } catch (SAXException | ParserConfigurationException | IOException ex) {
                        Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * Reads the visual descriptors for an image from the input files
     */
    private void readImageVisualDescriptors() {
        String directoryPath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("VisualDescriptorsPath");
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            if (file.isFile() && (!file.isHidden())) {
                String fileName = file.getName().replaceFirst("[.][^.]+$", "");
                String locationTitle = fileName.replaceFirst("[ ][^ ]+$", "");
                String visualDescriptorCode = fileName.replace((locationTitle + " "), "");
                Location location = this.locations.get(locationTitle);
                if (location != null) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String valuesText = line.replaceFirst("^(.*?[,])", "");
                            String imageId = line.replace(valuesText, "");
                            imageId = imageId.substring(0, imageId.length() - 1);

                            Image image = location.getImages().get(imageId);
                            if (image != null) {
                                String[] values = valuesText.split(",");
                                VisualContent visualContent = image.getVisualContent();
                                setVisualDescriptor(visualContent,visualDescriptorCode,values);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * Reads the visual descriptors for a wikipedia image from the input files
     */
    private void readWikiVisualDescriptors() {
        String directoryPath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("WikiVisualDescriptorsPath");
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            if (file.isFile() && (!file.isHidden())) {
                String fileName = file.getName().replaceFirst("[.][^.]+$", "");
                String locationTitle = fileName.replaceFirst("[ ][^ ]+$", "");
                String visualDescriptorCode = fileName.replace((locationTitle + " "), "");
                Location location = this.locations.get(locationTitle);
                if (location != null) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String valuesText = line.replaceFirst("^(.*[(].*[)][^,]*[,])", "");
                            String wikiImageId = line.replace(valuesText, "");
                            wikiImageId = wikiImageId.substring(0, wikiImageId.length() - 1);

                            WikipediaImg wikipediaImg = location.getWikipediaImages().get(wikiImageId);
                            if (wikipediaImg == null) {
                                wikipediaImg = new WikipediaImg();
                                wikipediaImg.setLocation(location);

                                wikipediaImg.setId(wikiImageId);

                                location.getWikipediaImages().put(wikipediaImg.getId(), wikipediaImg);
                            }
                            if (wikipediaImg != null) {
                                String[] values = valuesText.split(",");
                                VisualContent visualContent = wikipediaImg.getVisualContent();
                                setVisualDescriptor(visualContent, visualDescriptorCode, values);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * Reads the ground truths relevances and the cluster membership, is used to calculate the precision and cluster recall for the devset
     * @param path Path to the ground truth
     * @return hashtable with the ground truth relevance
     */
    public static Hashtable<String, Integer> loadGroundTruths(String path) {
        Hashtable<String, Integer> groundtruth = new Hashtable<String, Integer>();
        String[] parts;

        File file = new File(path);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                parts = line.split(",");
                if(parts.length != 2)
                    System.out.println("Error");
                else
                    groundtruth.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (Exception ex) {
            System.out.println("Error Data");
        }
        return groundtruth;
    }

    /**
     * Reads all existing clusters of the ground truth, is used to calculate the cluster recall for the devset
     * @param path Path to the ground truth
     * @return list with the existing clusters in the ground truth
     */
    public static List<Integer> loadGroundTruthClusters(String path) {
        List<Integer> clusters = new ArrayList<Integer>();
        String[] parts;
        int iLine = 0;
        File file = new File(path);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                iLine++;
                parts = line.split(",");
                if(parts.length != 2)
                    System.out.println("Error");
                else
                    clusters.add(Integer.parseInt(parts[0]));
            }
        } catch (Exception ex) {
            System.out.println("Error");
        }

        return clusters;
    }

    /**
     * Method which gathers the information for the different visual features
     * @param visualContent visual content of the actual image
     * @param VisualDesc code for each visual feature
     * @param values vectors for each feature
     */
    public void setVisualDescriptor (VisualContent visualContent, String VisualDesc, String[] values) {
            List<Double> descriptorValues = new ArrayList<>();
            for (int i = 0; i < values.length; i++) {
                descriptorValues.add(Double.parseDouble(values[i]));
            }
            switch (VisualDesc) {
                case "CM":
                    visualContent.setColorMomentsOnHSV(descriptorValues);
                    break;
                case "CN":
                    visualContent.setColorNamingHistogram(descriptorValues);
                    break;
                case "CSD":
                    visualContent.setColorStructureDescriptor(descriptorValues);
                    break;
                case "GLRLM":
                    visualContent.setGrayLevelRunLengthMatrix(descriptorValues);
                    break;
                case "HOG":
                    visualContent.setHistogramOfOrientedGradients(descriptorValues);
                    break;
                case "LBP":
                    visualContent.setLocallyBinaryPatternsOnGS(descriptorValues);
                    break;
                case "CM3x3":
                    visualContent.setColorMomentsOnHSV3x3(descriptorValues);
                    break;
                case "CN3x3":
                    visualContent.setColorNamingHistogram3x3(descriptorValues);
                    break;
                case "GLRLM3x3":
                    visualContent.setGrayLevelRunLengthMatrix3x3(descriptorValues);
                    break;
                case "LBP3x3":
                    visualContent.setLocallyBinaryPatternsOnGS3x3(descriptorValues);
                    break;
                default:
                    break;
            }
    }

    /**
     * Method which starts the input of the data
     * @param readGroundTruths if true the ground truths are also read
     */
    public void read(String readGroundTruths) {
        this.readTextualFeatures(PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("LocationTextualDescriptorPath"),true);
        this.readLocations(readGroundTruths);
        this.readTextualFeatures(PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("ImageTextualDescriptorPath"),false);
        this.readImages(readGroundTruths);
        this.readImageVisualDescriptors();
        this.readWikiVisualDescriptors();
    }
}
