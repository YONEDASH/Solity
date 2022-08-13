package de.yonedash.smash.compat.adapter;

import de.yonedash.smash.Instance;
import de.yonedash.smash.resource.Texture;

import java.awt.*;
public class OSXAdapter implements Adapter {

    @Override
    public void setIcon(Instance instance, Texture texture) {
        Taskbar.getTaskbar().setIconImage(texture.getImage());
    }

    @Override
    public String getApplicationDataPath() {
        return System.getProperty("user.home") + "/Library/Application Support/";
    }

}
