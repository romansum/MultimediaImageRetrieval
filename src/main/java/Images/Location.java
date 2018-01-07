package Images;

import java.util.*;
/**
 * Created by rsume on 05.01.2018.
 */
public class Location {

    public Location() {
        this.images = new HashMap<>();
        this.wikiImages = new HashMap<>();
        this.textualDescriptors = new TermCollection();
    }

    private int number;
    private String title;
    private String wikiUrl;
    private double latitude;
    private double longitude;
    private Map<String, Image> images;
    private Map<String, WikiImage> wikiImages;
    private List<Integer> clusters;

    private TermCollection textualDescriptors;

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

    public Map<String, WikiImage> getWikiImages() {
        return wikiImages;
    }

    public void setWikiImages(Map<String, WikiImage> wikiImages) {
        this.wikiImages = wikiImages;
    }

    public TermCollection getTextualDescriptors() {
        return textualDescriptors;
    }

    public void setTextualDescriptors(TermCollection textualDescriptors) {
        this.textualDescriptors = textualDescriptors;
    }

    public String getName() {
        return this.title.replace("_", " ");
    }

    public List<String> getIndexTerms() {
        //List<String> result = new ArrayList<>(this.textualDescriptors.getTerms().keySet());
        List<String> result = new ArrayList<>(this.getTextualDescriptors().getTerms().keySet());
        result.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return result;
    }

    public void reinitRelevanceScores() {
        for (Map.Entry<String, Image> imageEntry : this.getImages().entrySet()) {
            imageEntry.getValue().setRelevanceScore(0);
        }
    }

    public void reinitDiversityScores() {
        for (Map.Entry<String, Image> imageEntry : this.getImages().entrySet()) {
            imageEntry.getValue().setDiversityScore(0);
        }
    }

    public List<Image> getTopImages(int count) {
        List<Image> orderedImages = new ArrayList<>(this.getImages().values());
        orderedImages.sort(new Comparator<Image>() {

            @Override
            public int compare(Image image1, Image image2) {
                // sort by diversityScore then by relevanceScore (descending)
                // two then one to get the reverse order (descending)
                int diversityComparator = Double.valueOf(image2.getDiversityScore()).compareTo(Double.valueOf(image1.getDiversityScore()));

                if (diversityComparator == 0) {
                    return Double.valueOf(image2.getRelevanceScore()).compareTo(Double.valueOf(image1.getRelevanceScore()));
                } else {
                    return diversityComparator;
                }
            }
        });

        return orderedImages.subList(0, Math.min(count, orderedImages.size()));
    }

}
