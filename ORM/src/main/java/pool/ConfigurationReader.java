package pool;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ConfigurationReader {
    private static Map<String,String> map = new HashMap<>();
    static {
        try {
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()){
                String key = (String)en.nextElement();
                String value = properties.getProperty(key);
                map.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getStringValue(String key){
        return map.get(key);
    }
    public static int getIntValue(String key){
        if(key == null){
            key = "5";
        }
        return Integer.parseInt(map.get(key));
    }
}
