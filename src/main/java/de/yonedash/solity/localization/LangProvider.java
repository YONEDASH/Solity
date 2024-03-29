package de.yonedash.solity.localization;

import de.yonedash.solity.resource.ResourceFinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import java.text.MessageFormat;

public class LangProvider {

    private final String path;
    private final Language language;
    private final Properties properties;

    public LangProvider(String path, Language language) {
        this.path = path;
        this.language = language;
        this.properties = new Properties();
        this.load();
        if (language != Language.en_US)
            merge(Language.en_US.getProvider());
    }

    public void merge(LangProvider provider) {
        for (Object key : provider.properties.keySet()) {
            if (properties.get(key) == null) {
                properties.setProperty(String.valueOf(key), provider.properties.getProperty(String.valueOf(key)));
            }
        }
    }

    private void load() {
        try {
            InputStream is = ResourceFinder.openInputStream(path);
            if (is == null)
                return;
            properties.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String localize(String key, Object... args) {
        String value = properties.getProperty(key);
        return value != null ? (args == null ? value : MessageFormat.format(value, args)) : args == null ? key + "<<" + null : (args.length > 0 ? key + "<<" + Arrays.toString(args) : key);
    }

    public Language getLanguage() {
        return language;
    }

}