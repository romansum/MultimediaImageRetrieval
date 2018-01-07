package Images;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by rsume on 05.01.2018.
 */
public class TermCollection {

    public TermCollection() {
        this.terms = new HashMap<>();
    }

    private Map<String, Term> terms;

    public Map<String, Term> getTerms() {
        return terms;
    }



}
