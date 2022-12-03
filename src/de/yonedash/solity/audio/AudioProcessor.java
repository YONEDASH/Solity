package de.yonedash.solity.audio;

import de.yonedash.solity.Instance;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;

/**
 * created by Til M. on 06.12.21 in Krypton
 */
public class AudioProcessor {

    private Music currentMusic;
    private Thread queueThread;
    private double masterVolume, soundVolume, musicVolume;

    private AudioProcessor(double masterVolume, double soundVolume, double musicVolume) {
        this.masterVolume = masterVolume;
        setSoundVolume(soundVolume);
        setMusicVolume(musicVolume);
    }

    private Instance instance;

    public AudioProcessor(Instance instance) {
        this(0, 0, 0);
        this.instance = instance;
    }

    public void loadVolumesFromConfig() {
        setMasterVolume(instance.gameConfig.getDouble("volumeMaster"));
        setSoundVolume(instance.gameConfig.getDouble("volumeSound"));
        setMusicVolume(instance.gameConfig.getDouble("volumeMusic"));
    }

    public void setSoundVolume(double soundVolume) {
        this.soundVolume = Math.min(2.0, Math.max(0.0, soundVolume));
    }

    public void setMusicVolume(double musicVolume) {
        this.musicVolume = Math.min(2.0, Math.max(0.0, musicVolume));

        if (currentMusic != null)
            currentMusic.setVolume(this.musicVolume * this.masterVolume);
    }

    public void setMasterVolume(double masterVolume) {
        this.masterVolume = masterVolume;
        setMusicVolume(musicVolume);
        setSoundVolume(soundVolume);
    }

    public double getSoundVolume() {
        return soundVolume;
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    public void play(Sound sound) {
        play(sound, 1.0);
    }

    public void play(Sound sound, double volume) {
        sound.play(masterVolume * soundVolume * Math.min(1.0, Math.max(0.0, volume)));
    }

    public void stop(Sound sound) {
        sound.stop();
    }

    public void force(Music music) {
        if (currentMusic != null) {
            currentMusic.stop();
        }

        currentMusic = music;
        music.setVolume(masterVolume * musicVolume);
        music.play(true);
    }

    public void softForce(Music music) {
        if (currentMusic != null) {
            if (currentMusic.loop()) {
                queue(music);
            } else {
                force(music);
            }
        } else {
            force(music);
        }
    }

    public void stopMusic() {
        if (currentMusic == null)
            return;

        currentMusic.stop();
        currentMusic = null;
    }

    public void queue(final Music music) {
        if (music == currentMusic)
            return;

        if (currentMusic != null) {
            if (queueThread != null) {
                try {
                    queueThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            currentMusic.setLoop(false);

            // todo fix freeze?
            queueThread = new Thread(() -> {
                while (currentMusic.playing()) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                force(music);
                queueThread = null;
            });
            queueThread.start();
        } else {
            force(music);
        }
    }

}