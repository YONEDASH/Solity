package de.yonedash.solity.scene;

import de.yonedash.solity.*;
import de.yonedash.solity.config.KeyBind;
import de.yonedash.solity.progression.saves.SaveGame;
import de.yonedash.solity.progression.saves.SaveGameTemporary;
import de.yonedash.solity.progression.story.MainStory;
import de.yonedash.solity.progression.story.TutorialStory;
import de.yonedash.solity.scene.components.Button;
import de.yonedash.solity.scene.components.Component;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

public class SceneMainMenu extends SceneMenu {

    private final Button continueButton, loadButton, startNewButton, optionsButton, quitButton, tutorialButton;

    private final boolean hasPlayedAlready;

    public SceneMainMenu(Instance instance) {
        super(instance);

        this.components.add(this.continueButton = new MainMenuButton(this, "main.continue"));
        this.components.add(this.loadButton = new MainMenuButton(this, "main.loadjourney"));
        this.components.add(this.startNewButton = new MainMenuButton(this, "main.startnewjourney"));
        this.components.add(this.optionsButton = new MainMenuButton(this, "main.options"));
        this.components.add(this.quitButton = new MainMenuButton(this, "main.quit"));
        this.components.add(this.tutorialButton = new MainMenuButton(this, "main.starttutorial"));

        // Play main theme
        this.instance.audioProcessor.softForce(instance.library.themeMusic);

        // Set has played state
        String lastPlayed = this.instance.launchData.getString("saveGameLastPlayed");

        File saveDir = new File(instance.gameRoot, "/saves");
        File[] files = saveDir.listFiles();
        if (files != null) {
            int saveGameCount = 0;
            for (File file : saveDir.listFiles()) {
                if (file.getName().endsWith(".save")) {
                    saveGameCount++;
                }
            }
            if (saveGameCount >= 2)
                startNewButton.setEnabled(false);

           hasPlayedAlready = saveGameCount > 0;
        } else {
            hasPlayedAlready = !lastPlayed.equals("null") && getSaveGameFile(lastPlayed).exists();
        }
    }

    @Override
    public void devicePressed(KeyBind.Device device, int code) {
        super.devicePressed(device, code);

        if (device == KeyBind.Device.KEYBOARD && code == KeyEvent.VK_C) {
            instance.scene = new SceneCreditRoll(instance, new SceneLoadMainMenu(instance));
        }
    }

    @Override
    public void update(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        drawBackground(g2d, dt);

        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRect(0, 0, width / 3, height);

        double lineThickness = 9.0;
        Color lineColor = Color.WHITE;

        g2d.setColor(Constants.MAP_BACKGROUND_COLOR);
        g2d.setFont(this.instance.lexicon.futilePro.deriveFont((float) super.scaleToDisplay(Math.min(250.0, width / 3.0 * 0.8))));
        this.fontRenderer.drawStringAccurately(g2d, "Solity", width / 3 / 2, height / 8, Align.CENTER, Align.CENTER, true);

        double buttonHeight = 75.0, buttonSpace = 10.0;

        Button[] buttons = hasPlayedAlready ? new Button[] { this.quitButton, this.optionsButton, this.tutorialButton, this.startNewButton, this.loadButton, this.continueButton } : new Button[] { this.quitButton, this.optionsButton, this.startNewButton, this.tutorialButton };

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.setBounds(width / 3.0 - width / 3.0 * 0.9, height / 2.0 + super.scaleToDisplay(buttons.length * buttonHeight + buttons.length * buttonSpace) - super.scaleToDisplay(100.0 + buttonHeight * i + buttonSpace * i + buttonHeight), width / 3.0 * 0.8, super.scaleToDisplay(buttonHeight));

            g2d.setColor(lineColor.darker());
            g2d.fillRect(super.scaleToDisplay(lineThickness * 2.0), (int) (button.getBounds().position.y + button.getBounds().size.y), width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, super.scaleToDisplay(lineThickness * 0.5));

        }

        // Draw surrounding white rect
        g2d.setStroke(new BasicStroke(super.scaleToDisplay(lineThickness)));

        g2d.setColor(lineColor);
        g2d.drawRect(super.scaleToDisplay(lineThickness * 2.0), super.scaleToDisplay(lineThickness * 2.0), width / 3 - (super.scaleToDisplay(lineThickness * 2.0) * 2), height - (super.scaleToDisplay(lineThickness * 2.0) * 2));

        g2d.setColor(Color.WHITE);
        g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) scaleToDisplay(40.0)));
        fontRenderer.drawString(g2d, "Version 1.0.3, Chapter 1", width - 3, 3, Align.RIGHT, Align.TOP, false);
        fontRenderer.drawString(g2d, "(c) 2021/2022 Til M.", width - 3, 3 + (int) fontRenderer.bounds(g2d, "V,C1").y + 5, Align.RIGHT, Align.TOP, false);

        this.updateComponents(g2d, dt);
    }

    @Override
    public void fireComponent(Component component) {
        if (component == continueButton) {
            String saveGameName = this.instance.launchData.getString("saveGameLastPlayed");
            File file = getSaveGameFile(saveGameName);

            // Check if SaveGame actually exists
            if (saveGameName == null || saveGameName.equals("null"))
                throw new RuntimeException("Last played SaveGame is null");
            if (!file.exists())
                throw new RuntimeException("Last played SaveGame does not exist");

            // Load it
            loadSaveGame(file);
        } else if (component == optionsButton) {
            this.instance.scene = new SceneOptions(this.instance, this);
        } else if (component == tutorialButton) {
            this.instance.scene = new SceneLoadWorld(this.instance, new TutorialStory(), new SaveGameTemporary());
        } else if (component == startNewButton) {
           // loadSaveGame(getSaveGameFile(UUID.randomUUID() + "_" + System.currentTimeMillis()));
            this.instance.scene = new SceneChooseDifficulty(this.instance);
        } else if (component == loadButton) {
            this.instance.scene = new SceneChooseSave(this.instance);
        }else if (component == quitButton) {
           this.instance.stop(0);
       }
    }

    private void loadSaveGame(File file) {
        this.instance.scene = new SceneLoadWorld(this.instance, new MainStory(), new SaveGame(file));
    }

    private File getSaveGameFile(String name) {
        return new File(instance.gameRoot + "/saves", name + ".save");
    }

    static class MainMenuButton extends Button {
        public MainMenuButton(Scene scene, String key) {
            super(scene, key);
            setTextAlign(Align.LEFT, Align.CENTER);
            setBackgroundVisible(false);
            setTextColor(new Color(163, 163, 167));
        }
    }

}