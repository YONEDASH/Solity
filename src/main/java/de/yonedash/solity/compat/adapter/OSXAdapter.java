package de.yonedash.solity.compat.adapter;

import de.yonedash.solity.Instance;
import de.yonedash.solity.resource.Texture;

import java.awt.*;
public class OSXAdapter implements Adapter {

    public OSXAdapter() {
        // Set this property so app name is displayed properly on MacOS
        System.setProperty("apple.awt.application.name", "Solity");
    }

    @Override
    public void setIcon(Instance instance, Texture texture) {
        Taskbar.getTaskbar().setIconImage(texture.getImage());
    }

    @Override
    public String getApplicationDataPath() {
        return System.getProperty("user.home") + "/Library/Application Support/";
    }

}
