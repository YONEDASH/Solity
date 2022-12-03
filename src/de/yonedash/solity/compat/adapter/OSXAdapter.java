package de.yonedash.solity.compat.adapter;

import de.yonedash.solity.Instance;
import de.yonedash.solity.resource.Texture;

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
