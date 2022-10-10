package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.progression.saves.SaveGame;
import de.yonedash.smash.progression.story.MainStory;
import de.yonedash.smash.scene.components.Button;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SceneChooseDifficulty extends SceneMenu {

    private final Button cancelButton;
    private final HashMap<Button, Difficulty> difficultyButtons;

    public SceneChooseDifficulty(Instance instance) {
        super(instance);

        this.components.add(this.cancelButton = new DifficultyMenuButton(this, "main.cancel"));

        this.difficultyButtons = new HashMap<>();
        for (Difficulty difficulty : Difficulty.values()) {
            this.difficultyButtons.put(new DifficultyMenuButton(this, "difficulty." + difficulty.name), difficulty);
        }
        this.components.addAll(difficultyButtons.keySet().stream().toList());
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

        ArrayList<Button> sortedDifficultyButtons = new ArrayList<>(difficultyButtons.keySet().stream().toList());
        sortedDifficultyButtons.sort(Comparator.comparingDouble(o -> difficultyButtons.get(o).value));
        ArrayList<Button> buttons = new ArrayList<>(sortedDifficultyButtons);
        buttons.add(0, cancelButton);

        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);

            if (!difficultyButtons.containsKey(button)) i--;
            button.setBounds(width / 3.0 - width / 3.0 * 0.9, height / 2.0 + super.scaleToDisplay(buttons.size() * buttonHeight + buttons.size() * buttonSpace) - super.scaleToDisplay(100.0 + buttonHeight * i + buttonSpace * i + buttonHeight), width / 3.0 * 0.8, super.scaleToDisplay(buttonHeight));
            if (!difficultyButtons.containsKey(button)) i++;

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
        } else if (component instanceof  Button btn) {
           Difficulty difficulty = difficultyButtons.get(btn);
           if (difficulty != null) {
               File file = getSaveGameFile(UUID.randomUUID() + "_" + System.currentTimeMillis());
               SaveGame saveGame = new SaveGame(file);
               saveGame.set("difficulty", difficulty);
               try {
                   saveGame.save();
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
               this.instance.scene = new SceneLoadWorld(this.instance, new MainStory(), saveGame);
           }
        }
    }

    private File getSaveGameFile(String name) {
        return new File(instance.gameRoot + "/saves", name + ".save");
    }

    static class DifficultyMenuButton extends Button {
        public DifficultyMenuButton(Scene scene, String key) {
            super(scene, key);
            setTextAlign(Align.LEFT, Align.CENTER);
            setBackgroundVisible(false);

            for (Difficulty difficulty : Difficulty.values()) {
                if (key.contains(difficulty.name)) {
                    double maxValue = 0, minValue = 99;

                    for (Difficulty difficulty1 : Difficulty.values()) {
                        double v = difficulty1.value;
                        if (v < minValue)
                            minValue = v;
                        if (v > maxValue)
                            maxValue = v;
                    }

                    double v = difficulty.value;
                    double weightedDifficulty = (v - minValue) / (maxValue - minValue);

                    Color easy = new Color(101, 162, 151);
                    Color hard = new Color(198, 40, 98);

                    int dtRed = hard.getRed() - easy.getRed();
                    int dtGreen = hard.getGreen() - easy.getGreen();
                    int dtBlue = hard.getBlue() - easy.getBlue();

                    int red = (int) Math.max(Math.min(easy.getRed() + (dtRed * weightedDifficulty), 255), 0);
                    int green = (int) Math.max(Math.min(easy.getGreen() + (dtGreen * weightedDifficulty), 255), 0);
                    int blue = (int) Math.max(Math.min(easy.getBlue() + (dtBlue * weightedDifficulty), 255), 0);

                    setTextColor(new Color(red, green, blue));

                    return;
                }
            }
            setTextColor(new Color(163, 163, 167));
        }
    }

}