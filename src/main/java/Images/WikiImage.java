package Images;
/**
 * Created by rsume on 05.01.2018.
 */
public class WikiImage {

    public WikiImage() {
        this.visualDescriptors = new VisualDescriptors();
    }

    private String id;

    private VisualDescriptors visualDescriptors;

    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VisualDescriptors getVisualDescriptors() {
        return visualDescriptors;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
