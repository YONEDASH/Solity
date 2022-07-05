package de.yonedash.smash.resource;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

// This class stores fonts
public class FontLexicon {

    public Font compassPro, equipmentPro, futilePro;

    public FontLexicon() {

    }

    public void load() {
        this.compassPro = registerFont("/fonts/CompassPro.ttf");
        this.equipmentPro = registerFont("/fonts/EquipmentPro.ttf");
        this.futilePro = registerFont("/fonts/FutilePro.ttf");
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
