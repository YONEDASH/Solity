package de.yonedash.smash.resource;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

// This class stores fonts
public class FontLexicon {

    public Font compassPro, equipmentPro, futilePro, arial;

    public FontLexicon() {

    }

    private boolean isLoaded;

    public void load() {
        if (isLoaded)
            return;

        this.arial = new Font("Arial", Font.PLAIN, 0);
        this.compassPro = registerFont("/assets/fonts/CompassPro.ttf");
        this.equipmentPro = registerFont("/assets/fonts/EquipmentPro.ttf");
        this.futilePro = registerFont("/assets/fonts/FutilePro.ttf");

        this.isLoaded = true;
    }

    // Register a font from resource path
    private Font registerFont(String path) {
        Font font = null;

        // Find file
        InputStream inputStream = ResourceFinder.openInputStream(path);

        // Throw exception if file not found
        if (inputStream == null)
            throw new RuntimeException("Font " + path + " not found");

        // Load as font
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
                    font = Font.createFont(Font.TRUETYPE_FONT, inputStream));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Close stream
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return font;
    }

}
