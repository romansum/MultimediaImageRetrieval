package Data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Created by rsume on 05.01.2018.
 */
public class PropConfig {
    private static InputStream inputStream;

    /**
     * Method which retrieves a specific property from the Config.properties file
     * @param property name of the property which should be retrieved
     * @return value of the property
     */
    public static String accessPropertyFile(String property) {
        String retrievedProp = "";
        // Try to access the Properties-File
        try {
            Properties prop = new Properties();
            String propFileName = "Config.properties";
            inputStream = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("Config.properties");

            if (inputStream != null) {
                prop.load(inputStream);
            } else {

                throw new FileNotFoundException("property file '"
                        + propFileName + "' not found in the classpath");

            }
            retrievedProp = MessageFormat.format((String) prop.get(property),
                    "A");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return retrievedProp;
    }
}
