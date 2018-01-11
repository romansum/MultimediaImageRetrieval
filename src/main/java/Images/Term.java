package Images;
/**
 * Created by rsume on 05.01.2018.
 */
public class Term {

    public Term(String value, double TF_IDF) {
        this.value = value;
        this.TF_IDF = TF_IDF;
    }
    private String value;
    private double TF_IDF;
    public double getTF_IDF() {
        return TF_IDF;
    }
    public void setTF_IDF(double TF_IDF) {
        this.TF_IDF = TF_IDF;
    }
}
