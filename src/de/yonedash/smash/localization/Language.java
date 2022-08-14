package de.yonedash.smash.localization;

import java.util.Locale;

public enum Language {

    en_US("English"), de_DE("Deutsch");

    private final String nativeName;
    private LangProvider provider;

    Language(String nativeName) {
        this.nativeName = nativeName;
    }

    public String getNativeName() {
        return nativeName;
    }

    // free up ram
    public void flush() {
        // Set provider to null and let JVM handle the rest
        this.provider = null;
    }

    public LangProvider getProvider() {
        if (provider == null)
            return provider = new LangProvider("/assets/language/" + name() + ".lang", this);
        return provider;
    }

    public static Language systemDefault() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        for (Language lang : Language.values()) {
            if (lang.name().split("_")[0].equalsIgnoreCase(language))
                return lang;
        }
        System.out.println("System Language could not be recognized. Defaulting to " + Language.en_US.name());
        return Language.en_US;
    }


}
