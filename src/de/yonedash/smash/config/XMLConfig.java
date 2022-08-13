package de.yonedash.smash.config;

import java.io.*;
import java.util.Properties;

public abstract class XMLConfig {

    protected final Properties properties;

    protected final File file;

    public XMLConfig(File file) {
        this.properties = new Properties();
        this.file = file;
    }

    // Loads & initializes the config this function NEEDS to be called
    public void load() {
        try {
            this.loadFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            System.out.println("Initializing XMLConfig " + this.file.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.init();

        if (changesMade) {
            try {
                save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected abstract void init();

    private boolean changesMade = false;

    // Adds a certain property to properties if it does not already have it saved
    public void add(String key, Object defaultValue) {
        if (this.properties.getProperty(key) == null) {
            this.changesMade = true;
            this.properties.setProperty(key, String.valueOf(defaultValue));
        }
    }

    // Loads file into properties
    protected void loadFile() throws IOException {
        // Skip loading if file does not exist
        if (!this.file.exists())
            return;

        // Load from file
        InputStream inputStream = new FileInputStream(this.file);
        this.properties.loadFromXML(inputStream);
        inputStream.close();

        // Debug
        System.out.println("Loaded XMLConfig " + file.getCanonicalPath());
    }

    // Saves properties to file
    public void save() throws IOException {
        // If there are no parent folders for this file, make them
        if (!this.file.getParentFile().exists())
            this.file.getParentFile().mkdirs();

        // Save to file as XML
        FileOutputStream outputStream = new FileOutputStream(this.file);
        this.properties.storeToXML(outputStream, null);
        outputStream.flush();
        outputStream.close();

        // Reset changes made boolean
        changesMade = false;

        // Debug
        System.out.println("Saved XMLConfig " + file.getCanonicalPath());
    }

    // Set property
    public void set(String key, Object value) {
        this.properties.setProperty(key, String.valueOf(value));
    }

    // Return properties
    public Object get(String key) {
        return this.properties.get(key);
    }

    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(this.getString(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(this.getString(key));
    }

    public float getFloat(String key) {
        return Float.parseFloat(this.getString(key));
    }

    public long getLong(String key) {
        return Long.parseLong(this.getString(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.getString(key));
    }

}
