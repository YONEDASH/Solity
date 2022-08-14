package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.progression.story.ActualStory;
import de.yonedash.smash.progression.story.TutorialStory;
import de.yonedash.smash.scene.components.Button;
import de.yonedash.smash.scene.components.Component;

import java.awt.*;
import java.io.File;

public class SceneInWorldPaused extends SceneMenu {

    private Button continueButton, loadCheckpoint, optionsButton, quitButton;

    protected final Scene before;

    public SceneInWorldPaused(Instance instance, SceneInWorld before) {
        super(instance);

        this.before = before;

        this.components.add(this.continueButton = new SceneMainMenu.MainMenuButton(this, "ingame.continue"));
        this.components.add(this.loadCheckpoint = new SceneMainMenu.MainMenuButton(this, "ingame.loadcheckpoint"));
        this.components.add(this.optionsButton = new SceneMainMenu.MainMenuButton(this, "ingame.options"));
        this.components.add(this.quitButton = new SceneMainMenu.MainMenuButton(this, "ingame.quit"));

        if (instance.world.story instanceof TutorialStory)
            this.loadCheckpoint.setEnabled(false);
    }

    private int page, buttonsPerPage;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Display display = this.instance.display;
        int width = display.getWidth(), height = display.getHeight();

        if (before instanceof SceneMainMenu)
            drawBackground(g2d, dt);
        else if (before instanceof SceneInWorld siw)
            siw.update(g2d, 0);

        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillRect(0, 0, width / 3, height);

        double lineThickness = 9.0;
        Color lineColor = Color.WHITE;

        g2d.setColor(Constants.MAP_BACKGROUND_COLOR);
        g2d.setFont(this.instance.lexicon.futilePro.deriveFont((float) super.scaleToDisplay(Math.min(250.0, width / 3.0 * 0.8))));
        this.fontRenderer.drawStringAccurately(g2d, "Solity", width / 3 / 2, height / 8, Align.CENTER, Align.CENTER, true);

        double buttonHeight = 75.0, buttonSpace = 10.0;

        Button[] buttons = new Button[] { this.quitButton, this.optionsButton, this.loadCheckpoint, this.continueButton };

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

        this.updateComponents(g2d, dt);
    }

    @Override
    public void fireComponent(Component component) {
        super.fireComponent(component);

        if (component == continueButton) {
            instance.scene = before;
        } else if (component == loadCheckpoint) {
            loadSaveGame(getSaveGameFile(instance.world.saveGame.getName()));
        } else if (component == quitButton) {
            instance.scene = new SceneLoadMainMenu(instance);
        } else if (component == optionsButton) {
            optionsButton.selected = 0;
            instance.scene = new SceneOptions(instance, this);
        }
    }


    private void loadSaveGame(File file) {
        this.instance.scene = new SceneLoadWorld(this.instance, new ActualStory(), new SaveGame(file));
    }

    private File getSaveGameFile(String name) {
        return new File(instance.gameRoot + "/saves", name + ".save");
    }
}