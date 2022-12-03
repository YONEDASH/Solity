package de.yonedash.solity.compat.adapter;

import de.yonedash.solity.Instance;
import de.yonedash.solity.resource.Texture;

public class WindowsAdapter implements Adapter {

    @Override
    public void setIcon(Instance instance, Texture texture) {
        instance.display.setIconImage(texture.getBufferedImage());
    }

    @Override
    public String getApplicationDataPath() {
        return System.getenv("AppData");
    }

}
