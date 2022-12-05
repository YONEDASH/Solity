package de.yonedash.solity.compat.adapter;

import de.yonedash.solity.Instance;
import de.yonedash.solity.resource.Texture;

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
