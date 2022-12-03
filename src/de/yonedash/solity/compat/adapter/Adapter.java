package de.yonedash.solity.compat.adapter;

import de.yonedash.solity.Instance;
import de.yonedash.solity.resource.Texture;


public interface Adapter {
    void setIcon(Instance instance, Texture texture);

    String getApplicationDataPath();

}
