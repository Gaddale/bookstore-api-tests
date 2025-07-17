package com.bookapp.api.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static Properties properties;
    private static final String CONFIG_FILE_NAME = "config.properties";

    static {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find " + CONFIG_FILE_NAME + " on the classpath.");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load " + CONFIG_FILE_NAME, ex);
        }
    }

    public static String getProperty(String key) {
        String property = properties.getProperty(key);
        if (property == null || property.isEmpty()) {
            throw new RuntimeException("Property '" + key + "' not found or is empty in " + CONFIG_FILE_NAME);
        }
        return property;
    }

    // Optional: Method to get properties as Integer
    public static Integer getIntegerProperty(String key) {
        String property = getProperty(key);
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Property '" + key + "' in " + CONFIG_FILE_NAME + " is not a valid integer.", e);
        }
    }
}