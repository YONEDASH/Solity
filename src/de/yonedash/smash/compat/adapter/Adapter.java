package de.yonedash.smash.compat.adapter;

import de.yonedash.smash.Instance;
import de.yonedash.smash.resource.Texture;


public interface Adapter {
    void setIcon(Instance instance, Texture texture);

    String getApplicationDataPath();

}
