package Images;

import java.util.*;
/**
 * Created by rsume on 05.01.2018.
 */
public class Location {

    public Location() {
        this.images = new HashMap<>();
        this.wikipediaImages = new HashMap<>();
        this.TermCollection = new TermCollection();
    }

    private int number;
    private String title;
    private String wikiUrl;
    private double latitude;
    private double longitude;
    private Map<String, Image> images;
    private Map<String, WikipediaImg> wikipediaImages;
    private List<Integer> clusters;

    private TermCollection TermCollection;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getClusters() { return clusters; }

    public void setClusters(List<Integer> clusters) { this.clusters = clusters; }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getWikiUrl() {
        return wikiUrl;
    }

    public void setWikiUrl(String wikiUrl) {
        this.wikiUrl = wikiUrl;
    }

    public Map<String, Image> getImages() {
        return images;
    }

    public void setImages(Map<String, Image> images) {
        this.images = images;
    }

    public Map<String, WikipediaImg> getWikipediaImages() {
        return wikipediaImages;
    }

    public void setWikipediaImages(Map<String, WikipediaImg> wikipediaImages) {
        this.wikipediaImages = wikipediaImages;
    }

    public TermCollection getTermCollection() {
        return TermCollection;
    }

    public void setTermCollection(TermCollection termCollection) {
        this.TermCollection = termCollection;
    }

    public String getName() {
        return this.title.replace("_", " ");
    }

    public List<Image> getTopImages(int count) {
        List<Image> orderedImages = new ArrayList<>(this.getImages().values());
        orderedImages.sort((image1, image2) -> {
            int diversityComparator = Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore()));
            if (diversityComparator == 0) {
                return Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore()));
            } else {
                return diversityComparator;
            }
        });
        return orderedImages;
    }

}
