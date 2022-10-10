package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.progression.story.MainStory;
import de.yonedash.smash.scene.components.Button;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class SceneChooseSave extends SceneMenu {

    private final Button cancelButton;
    private final ArrayList<SaveButton> saveGameButtons;
    private final DeleteButton deleteButton1, deleteButton2;

    public SceneChooseSave(Instance instance) {
        super(instance);

        this.components.add(this.cancelButton = new SceneChooseDifficulty.DifficultyMenuButton(this, "main.cancel"));

        this.components.add(this.deleteButton1 = new DeleteButton(this, "main.delete"));
        this.components.add(this.deleteButton2 = new DeleteButton(this, "main.delete"));

        deleteButton1.setVisible(false);
        deleteButton2.setVisible(false);

        this.saveGameButtons = new ArrayList<>();
        File saveDir = new File(instance.gameRoot, "/saves");
        File[] files = saveDir.listFiles();
        if (files != null) {
            for (File file : saveDir.listFiles()) {
                if (file.getName().endsWith(".save")) {
                    SaveGame saveGame = new SaveGame(file);
                    saveGame.load();
                    this.saveGameButtons.add(new SaveButton(this, saveGame));
                }
            }
        }

        this.components.addAll(saveGameButtons);
    }

    @Override
    public void devicePressed(KeyBind.Device device, int code) {
        super.devicePressed(device, code);


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
        double saveGameHeight = 200.0;

        ArrayList<SaveButton> sortedDifficultyButtons = new ArrayList<>(saveGameButtons);
        sortedDifficultyButtons.sort(Comparator.comparingDouble(o -> o.saveGame.getLong("lastAccess")));
        ArrayList<Button> buttons = new ArrayList<>(sortedDifficultyButtons);
        buttons.add(0, cancelButton);

        deleteButton1.setVisible(false);
        deleteButton2.setVisible(false);

        int delButtons = 0;
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);

            //if (!saveGameButtons.contains(button)) i--;
            button.setBounds(width / 3.0 - width / 3.0 * 0.9, height / 2.0 + super.scaleToDisplay(buttons.size() * (buttonHeight + saveGameHeight) + buttons.size() * buttonSpace) - super.scaleToDisplay(100.0 + (buttonHeight + saveGameHeight) * i + buttonSpace * i + (buttonHeight + saveGameHeight) + saveGameHeight / 2), width / 3.0 * 0.8, super.scaleToDisplay(buttonHeight));
            //if (!saveGameButtons.contains(button)) i++;

            if (saveGameButtons.contains(button) && button instanceof SaveButton saveButton) {
                delButtons++;
                BoundingBox bounds = button.getBounds();
                double delWidth = bounds.size.y * 2.5;
                DeleteButton target = delButtons == 1 ? deleteButton1 : delButtons == 2 ? deleteButton2 : null;
                if (target != null) {
                    target.sg = saveButton.saveGame;
                    target.setVisible(true);
                    target.setBounds(bounds.position.x + bounds.size.x - delWidth, bounds.position.y, delWidth, bounds.size.y);
                }

                g2d.setColor(new Color(163, 163, 167));
                g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) super.scaleToDisplay(60.0)));

                fontRenderer.drawStringAccurately(g2d, localize("difficulty." + Difficulty.valueOf(saveButton.saveGame.getString("difficulty")).name), width / 3 / 2, (int) (button.getBounds().position.y + button.getBounds().size.y + super.scaleToDisplay(lineThickness * 2.0) + super.scaleToDisplay(40.0)), Align.CENTER, Align.CENTER, true);
                fontRenderer.drawStringAccurately(g2d, saveButton.saveGame.getCheckpointStep() + ", " + saveButton.saveGame.getCoins(), width / 3 / 2, (int) (button.getBounds().position.y + button.getBounds().size.y + super.scaleToDisplay(lineThickness * 2.0) + super.scaleToDisplay(100.0)), Align.CENTER, Align.CENTER, true);

            }

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
    public void fireComponent(de.yonedash.smash.scene.components.Component component) {
        if (component == cancelButton) {
            this.instance.scene = new SceneMainMenu(this.instance);
        } else if (component instanceof DeleteButton db) {
            File file = getSaveGameFile(db.sg.getName());
            file.renameTo(new File(file.getParentFile(), file.getName() + ".deleted"));
           //  file.delete();
            this.instance.scene = new SceneChooseSave(this.instance);
        } else if (component instanceof SaveButton sb && saveGameButtons.contains(sb)) {
            this.instance.scene = new SceneLoadWorld(this.instance, new MainStory(), sb.saveGame);
        }


    }

    private File getSaveGameFile(String name) {
        return new File(instance.gameRoot + "/saves", name + ".save");
    }

    static class SaveButton extends Button {

        final SaveGame saveGame;

        public SaveButton(Scene scene, SaveGame saveGame) {
            super(scene, saveGame.getName());
            this.saveGame = saveGame;
            Date date = new Date(saveGame.getLong("lastAccess"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            key = "dummy";
            setLocalizationObjects(sdf.format(date));
            setTextAlign(Align.LEFT, Align.CENTER);
            setBackgroundVisible(false);
            setTextColor(new Color(163, 163, 167));
        }
    }

    static class DeleteButton extends Button {

        SaveGame sg;

        public DeleteButton(Scene scene, String key) {
            super(scene, key);
            setTextAlign(Align.RIGHT, Align.CENTER);
            setBackgroundVisible(false);
            setTextColor(new Color(195, 46, 46));
        }
    }
}

