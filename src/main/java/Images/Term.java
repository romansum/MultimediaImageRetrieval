package Images;
/**
 * Created by rsume on 05.01.2018.
 */
public class Term {

    public Term(String value, int TF, int DF, double TF_IDF) {
        this.value = value;
        this.TF = TF;
        this.DF = DF;
        this.TF_IDF = TF_IDF;
    }

    private String value;
    private int TF;
    private int DF;
    private double TF_IDF;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getTF() {
        return TF;
    }

    public void setTF(int TF) {
        this.TF = TF;
    }

    public int getDF() {
        return DF;
    }

    public void setDF(int DF) {
        this.DF = DF;
    }

    public double getTF_IDF() {
        return TF_IDF;
    }

    public void setTF_IDF(double TF_IDF) {
        this.TF_IDF = TF_IDF;
    }

}
