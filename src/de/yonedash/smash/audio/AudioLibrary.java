package de.yonedash.smash.audio;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

public class AudioLibrary {

    private boolean isLoaded;

    public Music themeMusic;
    public Sound selectSound, clickSound, errorSound, dashSound, hitSound,typeSound;

    public void load() {
        if (this.isLoaded)
            return;

        this.themeMusic = loadMusic("/assets/audio/music/theme.wav");

        this.selectSound = loadSound("/assets/audio/sound/select.wav");
        this.clickSound = loadSound("/assets/audio/sound/click.wav");
        this.errorSound = loadSound("/assets/audio/sound/error.wav");
        this.dashSound = loadSound("/assets/audio/sound/dash.wav");
        this.hitSound = loadSound("/assets/audio/sound/hit.wav");
        this.typeSound = loadSound("/assets/audio/sound/type.wav");

        this.isLoaded = true;
    }

    private Sound loadSound(String path) {
        return TinySound.loadSound(path);
    }

    private Music loadMusic(String path) {
        return TinySound.loadMusic(path);
    }

}
