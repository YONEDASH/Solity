package de.yonedash.smash.config;

import java.io.File;
import java.io.IOException;

public class SaveGameTemporary extends SaveGame {

    public SaveGameTemporary() {
        super(new File("tempSaveGame"));
    }

    @Override
    public void save() throws IOException {

    }
}
