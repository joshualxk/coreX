package io.bigoldbro.corex.all;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Joshua on 2018/4/3.
 */
public class Config {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("config/io.bigoldbro.corex.properties"));
        for(String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.out.println(key + " => " + value);
        }
    }
}
