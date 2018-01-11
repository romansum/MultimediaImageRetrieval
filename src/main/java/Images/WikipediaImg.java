package Images;
/**
 * Created by rsume on 05.01.2018.
 */
public class WikipediaImg {

    public WikipediaImg() {
        this.visualContent = new VisualContent();
    }

    private String id;

    private VisualContent visualContent;

    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VisualContent getVisualContent() {
        return visualContent;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
