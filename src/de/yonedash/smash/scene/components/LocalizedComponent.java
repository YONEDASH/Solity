package de.yonedash.smash.scene.components;

import de.yonedash.smash.scene.Scene;

public abstract class LocalizedComponent extends Component {

    protected String key;
    protected Object[] localizationObjects;

    public LocalizedComponent(Scene scene, String key) {
        super(scene);
        this.key = key;
    }

    public void setLocalizationObjects(Object... localizationObjects) {
        this.localizationObjects = localizationObjects;
    }

}
