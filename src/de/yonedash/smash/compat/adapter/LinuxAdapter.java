package de.yonedash.smash.compat.adapter;

import de.yonedash.smash.Instance;
import de.yonedash.smash.resource.Texture;

public class LinuxAdapter implements Adapter {
    @Override
    public void setIcon(Instance instance, Texture texture) {
        instance.display.setIconImage(texture.getImage());
    }

    @Override
    public String getApplicationDataPath() {
        return System.getProperty("user.home");
    }

}
