package de.yonedash.smash.compat.adapter;

import de.yonedash.smash.Instance;
import de.yonedash.smash.resource.Texture;

public class FallbackAdapter implements Adapter {

    @Override
    public void setIcon(Instance instance, Texture texture) {
        instance.display.setIconImage(texture.getBufferedImage());
    }

    @Override
    public String getApplicationDataPath() {
        return System.getProperty("user.dir");
    }

}

