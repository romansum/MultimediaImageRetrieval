package Images;

/**
 * Created by rsume on 05.01.2018.
 */
public class Image {

    public Image() {
        this.rank = Integer.MAX_VALUE;
        this.numberOfComments = 0;
        this.numberOfViews = 0;
        this.relevant_GT = false;
        this.clusterId_GT = -1;
        this.relevanceScore = 0;
        this.diversityScore = 0;
        this.termCollection = new TermCollection();
        this.visualContent = new VisualContent();
    }
    //All the attributes of an image
    private String id;
    private String title;
    private String description;
    private String tags;
    private int rank;
    private int numberOfComments;
    private int numberOfViews;
    private double latitude;
    private double longitude;
    private TermCollection termCollection;
    private VisualContent visualContent;
    private boolean relevant_GT;
    private int clusterId_GT;

    private double relevanceScore;
    private double diversityScore;
    //Getter and Setter methods for the attributes
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public int getNumberOfViews() {
        return numberOfViews;
    }

    public void setNumberOfViews(int numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

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

    public TermCollection getTermCollection() {
        return termCollection;
    }

    public void setTermCollection(TermCollection termCollection) {
        this.termCollection = termCollection;
    }

    public VisualContent getVisualContent() {
        return visualContent;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public double getDiversityScore() {
        return diversityScore;
    }

    public void setDiversityScore(double diversityScore) {
        this.diversityScore = diversityScore;
    }

    public boolean isRelevant_GT() {
        return relevant_GT;
    }

    public void setRelevant_GT(boolean relevant_GT) {
        this.relevant_GT = relevant_GT;
    }

    public int getClusterId_GT() {
        return clusterId_GT;
    }

    public void setClusterId_GT(int clusterId_GT) {
        this.clusterId_GT = clusterId_GT;
    }

}
