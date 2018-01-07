package Data;

import Images.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import Images.TermCollection;
/**
 * Created by rsume on 05.01.2018.
 */
public class DataReader {

    public DataReader() {
        this.locations = new HashMap<>();
        this.locationsTextualDescriptors = new HashMap();
        this.imagesTextualDescriptors = new HashMap();
    }

    private Map<String, Location> locations;
    private Map<String, TermCollection> locationsTextualDescriptors;
    private Map<String, TermCollection> imagesTextualDescriptors;

    public Map<String, Location> getLocations() {
        return locations;
    }

    private void readLocations(boolean testset) {
        String filePath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("LocationPath");
        File file = new File(filePath);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document document = dBuilder.parse(file);
            document.getDocumentElement().normalize(); // recommended call

            NodeList nodeList = document.getElementsByTagName("topic");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    Location location = new Location();

                    // number
                    try {
                        location.setNumber(Integer.parseInt(element.getElementsByTagName("number").item(0).getTextContent()));
                    } catch (DOMException | NumberFormatException ex) {
                    }

                    // title
                    try {
                        location.setTitle(element.getElementsByTagName("title").item(0).getTextContent());
                    } catch (Exception ex) {
                    }

                    // position
                    try {
                        location.setLatitude(Double.parseDouble(element.getElementsByTagName("latitude").item(0).getTextContent()));
                        location.setLongitude(Double.parseDouble(element.getElementsByTagName("longitude").item(0).getTextContent()));
                    } catch (DOMException | NumberFormatException ex) {
                    }

                    // wiki
                    try {
                        location.setWikiUrl(element.getElementsByTagName("wiki").item(0).getTextContent());
                    } catch (Exception ex) {
                    }

                    // textual descriptors
                    TermCollection textualDescriptors = this.locationsTextualDescriptors.get(location.getTitle());
                    if (textualDescriptors != null) {
                        location.setTextualDescriptors(textualDescriptors);
                    }
                    location.setClusters(loadGroundTruthClusters(PropConfig.accessPropertyFile("BasePath") + "gt/dGT/" + location.getTitle() + " dclusterGT.txt"));
                    this.locations.put(location.getTitle(), location);

                }
            }
        } catch (SAXException | ParserConfigurationException | IOException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readLocationTextualFeatures(boolean testset) {
        String filePath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("LocationTextualDescriptorPath");
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");

                String locationTitle = values[0];
                TermCollection textualDescriptors = new TermCollection();

                // skip the location name, moving to the first term
                int startIndex = 1;
                while (!(values[startIndex].startsWith("\"") && values[startIndex].endsWith("\""))) {
                    startIndex++;
                }

                for (int i = startIndex; i < values.length; i += 4) {
                    try {
                        String term = values[i].replace("\"", "");
                        int tf = Integer.parseInt(values[i + 1]);
                        int df = Integer.parseInt(values[i + 2]);
                        double tf_idf = Double.parseDouble(values[i + 3]);

                        textualDescriptors.getTerms().put(term, new Term(term, tf, df, tf_idf));
                    } catch (Exception ex) {
                        Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                this.locationsTextualDescriptors.put(locationTitle, textualDescriptors);
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readImageTextualFeatures(boolean testset) {
        String filePath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("ImageTextualDescriptorPath");
        File file = new File(filePath);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");

                String imageId = values[0];
                TermCollection textualDescriptors = new TermCollection();

                for (int i = 1; i < values.length; i += 4) {
                    try {
                        String term = values[i].replace("\"", "");
                        int tf = Integer.parseInt(values[i + 1]);
                        int df = Integer.parseInt(values[i + 2]);
                        double tf_idf = Double.parseDouble(values[i + 3]);

                        textualDescriptors.getTerms().put(term, new Term(term, tf, df, tf_idf));
                    } catch (Exception ex) {
                        Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                this.imagesTextualDescriptors.put(imageId, textualDescriptors);
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readImages(boolean testset) {
        String directoryPath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("XMLPath");
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            if (file.isFile() && (!file.isHidden())) {
                String fileName = file.getName().replaceFirst("[.][^.]+$", "");
                String locationTitle = fileName;
                Location location = this.locations.get(locationTitle);
                Hashtable<String, Integer> groundTruthRelevance = loadGroundTruthAllocation(PropConfig.accessPropertyFile("BasePath") + "gt/rGT/" + location.getTitle() + " rGT.txt");
                Hashtable<String, Integer> groundTruthCluster = loadGroundTruthAllocation(PropConfig.accessPropertyFile("BasePath") + "gt/dGT/" + location.getTitle() + " dGT.txt");

                if (location != null) {
                    try {
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                        Document document = dBuilder.parse(file);
                        document.getDocumentElement().normalize(); // recommended call

                        // Exp. 2013-06-04 02:45:20
                        DateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH);

                        NodeList nodeList = document.getElementsByTagName("photo");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Node node = nodeList.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;

                                Image image = new Image();
                                image.setLocation(location);

                                // id
                                try {
                                    image.setId(element.getAttribute("id"));
                                } catch (Exception ex) {
                                }

                                // title
                                try {
                                    image.setTitle(element.getAttribute("title"));
                                } catch (Exception ex) {
                                }

                                // description
                                try {
                                    image.setDescription(element.getAttribute("description"));
                                } catch (Exception ex) {
                                }

                                // tags
                                try {
                                    image.setTags(element.getAttribute("tags"));
                                } catch (Exception ex) {
                                }

                                // url
                                try {
                                    image.setUrl(element.getAttribute("url_b"));
                                } catch (Exception ex) {
                                }
                                // username
                                try {
                                    image.setUsername(element.getAttribute("username"));
                                } catch (Exception ex) {
                                }

                                // rank
                                try {
                                    image.setRank(Integer.parseInt(element.getAttribute("rank")));
                                } catch (Exception ex) {
                                }

                                // numberOfComments
                                try {
                                    image.setNumberOfComments(Integer.parseInt(element.getAttribute("nbComments")));
                                } catch (Exception ex) {
                                }

                                // numberOfViews
                                try {
                                    image.setNumberOfViews(Integer.parseInt(element.getAttribute("views")));
                                } catch (Exception ex) {
                                }

                                // license
                                try {
                                    image.setLicense(Integer.parseInt(element.getAttribute("license")));
                                } catch (Exception ex) {
                                }

                                // position
                                try {
                                    //image.setPosition(new GPSPosition(Double.parseDouble(element.getAttribute("latitude")), Double.parseDouble(element.getAttribute("longitude"))));
                                    image.setLatitude(Double.parseDouble(element.getAttribute("latitude")));
                                    image.setLongitude(Double.parseDouble(element.getAttribute("longitude")));

                                } catch (Exception ex) {
                                }

                                // dateTaken
                                try {
                                    image.setDateTaken(format.parse(element.getAttribute("date_taken")));
                                } catch (Exception ex) {
                                }

                                // textual descriptors
                                TermCollection textualDescriptors = this.imagesTextualDescriptors.get(image.getId());
                                if (textualDescriptors != null) {
                                    image.setTextualDescriptors(textualDescriptors);
                                }
                                if(groundTruthRelevance.get(image.getId()) != null)
                                    image.setRelevant_GT(groundTruthRelevance.get(image.getId())==1);

                                if(groundTruthCluster.get(image.getId()) != null)
                                    image.setClusterId_GT(groundTruthCluster.get(image.getId()));

                                location.getImages().put(image.getId(), image);
                            }
                        }
                    } catch (SAXException | ParserConfigurationException | IOException ex) {
                        Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void readVisualDescriptors(boolean testset, boolean includeSpatialPyramidRepresentation) {
        String directoryPath = PropConfig.accessPropertyFile("BasePath")+PropConfig.accessPropertyFile("VisualDescriptorsPath");
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();

        for (File file : fileList) {
            if (file.isFile() && (!file.isHidden())) {
                String fileName = file.getName().replaceFirst("[.][^.]+$", "");
                String locationTitle = fileName.replaceFirst("[ ][^ ]+$", "");
                String visualDescriptorCode = fileName.replace((locationTitle + " "), "");
                //String visualDescriptorCode = fileName.replaceFirst(".+[ ]", "");

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
                                List<Double> descriptorValues = new ArrayList<>();
                                for (int i = 0; i < values.length; i++) {
                                    descriptorValues.add(Double.parseDouble(values[i]));
                                }

                                VisualDescriptors visualDescriptors = image.getVisualDescriptors();
                                switch (visualDescriptorCode) {
                                    case "CM":
                                        visualDescriptors.setColorMomentsOnHSV(descriptorValues);
                                        break;
                                    case "CN":
                                        visualDescriptors.setColorNamingHistogram(descriptorValues);
                                        break;
                                    case "CSD":
                                        visualDescriptors.setColorStructureDescriptor(descriptorValues);
                                        break;
                                    case "GLRLM":
                                        visualDescriptors.setGrayLevelRunLengthMatrix(descriptorValues);
                                        break;
                                    case "HOG":
                                        visualDescriptors.setHistogramOfOrientedGradients(descriptorValues);
                                        break;
                                    case "LBP":
                                        visualDescriptors.setLocallyBinaryPatternsOnGS(descriptorValues);
                                        break;

                                    case "CM3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setColorMomentsOnHSV3x3(descriptorValues);
                                        }
                                        break;
                                    case "CN3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setColorNamingHistogram3x3(descriptorValues);
                                        }
                                        break;
                                    case "GLRLM3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setGrayLevelRunLengthMatrix3x3(descriptorValues);
                                        }
                                        break;
                                    case "LBP3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setLocallyBinaryPatternsOnGS3x3(descriptorValues);
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void readWikiVisualDescriptors(boolean testset, boolean includeSpatialPyramidRepresentation) {
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
                            // floating point numbers: [-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?
                            // this process is just to avoid of spcial cases,
                            // where a comma is presented in the imageId, Exp. "altes_museum(Frankl, A.)"
                            // Older expression: ^(.*[(].*[)]\\d*[,])
                            // The older expression had a problem on a case on the test set "la_sainte_chapelle(Didier B).jog"
                            String valuesText = line.replaceFirst("^(.*[(].*[)][^,]*[,])", "");
                            String wikiImageId = line.replace(valuesText, "");
                            wikiImageId = wikiImageId.substring(0, wikiImageId.length() - 1);

                            WikiImage wikiImage = location.getWikiImages().get(wikiImageId);

                            // some ids use non-english characters 
                            // which differ in the descriptors file from the file name of the image
                            // so the wiki images are read from the descriptors file only
                            // as we are not interested in the image file itself
                            // Exp. "acropolis_athens/acropolis_athens(Ricardo AndrÇ Frantz).jpg"
                            // It's "acropolis_athens(Ricardo Andr� Frantz)" in "acropolis_athens CM.csv"
                            if (wikiImage == null) {
                                wikiImage = new WikiImage();
                                wikiImage.setLocation(location);

                                wikiImage.setId(wikiImageId);

                                location.getWikiImages().put(wikiImage.getId(), wikiImage);
                            }

                            if (wikiImage != null) {
                                String[] values = valuesText.split(",");
                                List<Double> descriptorValues = new ArrayList<>();
                                for (int i = 0; i < values.length; i++) {
                                    descriptorValues.add(Double.parseDouble(values[i]));
                                }

                                VisualDescriptors visualDescriptors = wikiImage.getVisualDescriptors();
                                switch (visualDescriptorCode) {
                                    case "CM":
                                        visualDescriptors.setColorMomentsOnHSV(descriptorValues);
                                        break;
                                    case "CN":
                                        visualDescriptors.setColorNamingHistogram(descriptorValues);
                                        break;
                                    case "CSD":
                                        visualDescriptors.setColorStructureDescriptor(descriptorValues);
                                        break;
                                    case "GLRLM":
                                        visualDescriptors.setGrayLevelRunLengthMatrix(descriptorValues);
                                        break;
                                    case "HOG":
                                        visualDescriptors.setHistogramOfOrientedGradients(descriptorValues);
                                        break;
                                    case "LBP":
                                        visualDescriptors.setLocallyBinaryPatternsOnGS(descriptorValues);
                                        break;

                                    case "CM3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setColorMomentsOnHSV3x3(descriptorValues);
                                        }
                                        break;
                                    case "CN3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setColorNamingHistogram3x3(descriptorValues);
                                        }
                                        break;
                                    case "GLRLM3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setGrayLevelRunLengthMatrix3x3(descriptorValues);
                                        }
                                        break;
                                    case "LBP3x3":
                                        if (includeSpatialPyramidRepresentation) {
                                            visualDescriptors.setLocallyBinaryPatternsOnGS3x3(descriptorValues);
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    public static Hashtable<String, Integer> loadGroundTruthAllocation(String path) {

        Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
        String[] parts;
        int iLine = 0;

        File file = new File(path);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {

                iLine++;
                parts = line.split(",");
                if(parts.length != 2)
                    System.out.println("Error: invalid count of elments in line " + iLine + " (file: " + path + ")");
                else
                    ht.put(parts[0], Integer.parseInt(parts[1]));
            }
        } catch (Exception ex) {
            System.out.println("Error: invalid data in: " + path);
        }

        return ht;
    }

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
                    System.out.println("Error: invalid count of elments in line " + iLine + " (file: " + path + ")");
                else
                    clusters.add(Integer.parseInt(parts[0]));
            }
        } catch (Exception ex) {
            System.out.println("Error: invalid data in: " + path);
        }

        return clusters;
    }

    public void read(boolean testset) {
        this.readLocationTextualFeatures(testset);
        this.readLocations(testset);
        this.readImageTextualFeatures(testset);
        this.readImageTextualFeatures(testset);
        this.readImages(testset);
        this.readVisualDescriptors(testset, false);
        this.readWikiVisualDescriptors(testset, false);
    }
}
