package de.yonedash.smash.scene;

import de.yonedash.smash.*;
import de.yonedash.smash.config.*;
import de.yonedash.smash.graphics.EntityFog;
import de.yonedash.smash.launch.LaunchConfig;
import de.yonedash.smash.launch.RenderPipeline;
import de.yonedash.smash.localization.BindLocalizer;
import de.yonedash.smash.localization.Language;
import de.yonedash.smash.scene.components.Button;
import de.yonedash.smash.scene.components.Component;
import de.yonedash.smash.scene.components.LocalizedComponent;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;

public class SceneOptions extends SceneMenu {

    private static boolean fullRestartNeeded;

    private Button leftButton, rightButton, backButton, cancelButton, applyButton;

    private ArrayList<OptionComponent> optionComponents;
    private ArrayList<KeyBindButton> inputButtons;

    private final Scene before;

    public SceneOptions(Instance instance, Scene before) {
        super(instance);

        this.before = before;

        this.components.add(this.leftButton = new NavButton(this, "options.left"));
        this.components.add(this.rightButton = new NavButton(this, "options.right"));
        this.components.add(this.backButton = new NavButton(this, "options.back"));
        this.components.add(this.cancelButton = new NavButton(this, "options.cancel"));
        this.components.add(this.applyButton = new NavButton(this, "options.apply"));

        this.optionComponents = new ArrayList<>();
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "language"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "volumeMaster"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "volumeMusic"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "volumeSound"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "volumeTyping"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "fullscreen"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "fpsLimit"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "vsync"));
        this.optionComponents.add(new OptionComponent(this, instance.gameConfig, "lowPowerMode"));
        this.optionComponents.add(new OptionComponent(this, instance.graphicsConfig, "preset"));
        this.optionComponents.add(new OptionComponent(this, instance.launchData, "renderPipeline"));

        ArrayList<OptionComponent> sortedInput = new ArrayList<>();
        for (String key : instance.inputConfig.keys()) {
            sortedInput.add(new OptionComponent(this, instance.inputConfig, key));
        }
        sortedInput.sort(Comparator.comparing(o -> o.key));
        this.optionComponents.addAll(sortedInput);

        this.components.addAll(this.optionComponents);

        applyButton.setEnabled(false);
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
        g2d.setFont(this.instance.lexicon.futilePro.deriveFont((float) super.scaleToDisplay(Math.min(140.0, width / 3.0 * 0.5))));
        this.fontRenderer.drawStringAccurately(g2d, "Options", width / 3 / 2, height / 12, Align.CENTER, Align.CENTER, true);

        g2d.setColor(lineColor.darker());
        g2d.fillRect(super.scaleToDisplay(lineThickness * 2.0), height / 6, width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, super.scaleToDisplay(lineThickness * 0.5));

        // Buttons
        double buttonHeight = 100.0, buttonSpace = 10.0;

        // Nav Buttons
        Button[] navigateButtons1 = new Button[] { this.backButton, this.cancelButton, this.applyButton };
        double navButtonWidth = width / 3.0 * 0.8 / navigateButtons1.length;

        for (int i = 0; i < navigateButtons1.length; i++) {
            Button button = navigateButtons1[i];
            button.setBounds(buttonSpace / 2 + (navButtonWidth + scaleToDisplay(buttonSpace)) * i + width / 3.0 / 2 - (navButtonWidth + scaleToDisplay(buttonSpace)) * navigateButtons1.length / 2, height - scaleToDisplay(100.0) - scaleToDisplay(buttonHeight), navButtonWidth, scaleToDisplay(buttonHeight));
        }

        Button[] navigateButtons2 = new Button[] { this.leftButton, this.rightButton };
        double navButtonWidth2 = width / 3.0 * 0.8 / navigateButtons2.length;

        for (int i = 0; i < navigateButtons2.length; i++) {
            Button button = navigateButtons2[i];
            button.setBounds(buttonSpace / 2 + (navButtonWidth2 + scaleToDisplay(buttonSpace)) * i + width / 3.0 / 2 - (navButtonWidth2 + scaleToDisplay(buttonSpace)) * navigateButtons2.length / 2, height - scaleToDisplay(100.0) - scaleToDisplay(buttonHeight * 2 + buttonSpace), navButtonWidth2, scaleToDisplay(buttonHeight));
        }

        Button navButton = navigateButtons2[0];

        // Option Buttons

        // Clip
        Rectangle2D clip = new Rectangle2D.Double(super.scaleToDisplay(lineThickness * 2.0), height / 6 + super.scaleToDisplay(lineThickness * 0.5), width / 3.0 - super.scaleToDisplay(lineThickness * 2.0) * 2, navButton.getBounds().position.y - super.scaleToDisplay(lineThickness) - height / 6);
        g2d.setClip(clip);

        int optionStartY = height / 6 + super.scaleToDisplay(lineThickness * 0.5);

        buttonsPerPage = (int) (clip.getHeight() / (super.scaleToDisplay(buttonHeight + buttonSpace)));

        optionComponents.forEach(optionComponent -> optionComponent.setVisible(false));
        for (int i = page * buttonsPerPage; i < optionComponents.size() && i < (page + 1) * buttonsPerPage; i++) {
            Component component = optionComponents.get(i);
            component.setBounds(super.scaleToDisplay(lineThickness * 2.0), optionStartY + scaleToDisplay((i - page * buttonsPerPage) * (buttonHeight + buttonSpace)), width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, scaleToDisplay(buttonHeight));
            component.setVisible(true);
        }

        int pages = (int) Math.ceil(optionComponents.size() / (double) buttonsPerPage);

        if (optionComponents.stream().noneMatch(optionComponent -> optionComponent.newBindMode)) {
            leftButton.setEnabled(page != 0);
            rightButton.setEnabled(page < pages - 1);
        }

        optionComponents.forEach(optionComponent -> optionComponent.update(g2d, dt));

        if (page >= pages)
            page = Math.max(0, pages - 1);

        g2d.setClip(null);

        // Draw Nav

        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRect(super.scaleToDisplay(lineThickness * 2.0), (int) (navButton.getBounds().position.y - super.scaleToDisplay(lineThickness)), width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, super.scaleToDisplay(lineThickness * 0.5) + (int) navButton.getBounds().size.y * 2 + super.scaleToDisplay(buttonSpace) + super.scaleToDisplay(lineThickness * 2));

        g2d.setColor(lineColor.darker());
        g2d.fillRect(super.scaleToDisplay(lineThickness * 2.0), (int) (navButton.getBounds().position.y + navButton.getBounds().size.y * 2 + super.scaleToDisplay(buttonSpace) + super.scaleToDisplay(lineThickness)), width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, super.scaleToDisplay(lineThickness * 0.5));
        g2d.fillRect(super.scaleToDisplay(lineThickness * 2.0), (int) (navButton.getBounds().position.y - super.scaleToDisplay(lineThickness)), width / 3 - super.scaleToDisplay(lineThickness * 2.0) * 2, super.scaleToDisplay(lineThickness * 0.5));

        Arrays.stream(navigateButtons1).forEach(btn -> btn.update(g2d, dt));
        Arrays.stream(navigateButtons2).forEach(btn -> btn.update(g2d, dt));

        // Draw surrounding white rect
        g2d.setStroke(new BasicStroke(super.scaleToDisplay(lineThickness)));

        g2d.setColor(lineColor);
        g2d.drawRect(super.scaleToDisplay(lineThickness * 2.0), super.scaleToDisplay(lineThickness * 2.0), width / 3 - (super.scaleToDisplay(lineThickness * 2.0) * 2), height - (super.scaleToDisplay(lineThickness * 2.0) * 2));

        // Draw restart notice
        if (fullRestartNeeded) {
            g2d.setColor(new Color(255, 131, 131));
            g2d.setFont(instance.lexicon.futilePro.deriveFont((float) super.scaleToDisplay(35.0)));
            fontRenderer.drawString(g2d, localize("options.restartNeeded"), width / 3 / 2, (int) clip.getY() - super.scaleToDisplay(lineThickness * 2), Align.CENTER, Align.BOTTOM, true);
        }
    }

    @Override
    public void fireComponent(Component component) {
        super.fireComponent(component);

        if (component == cancelButton || component == backButton) {
            instance.scene = before;
        }

        if (component == applyButton) {
            ArrayList<INIConfig> configs = new ArrayList<>();
            for (OptionComponent optionComponent : optionComponents) {
                if (optionComponent.isModified) {
                    String valueBefore = optionComponent.config.getString(optionComponent.key);
                    optionComponent.setInConfig();
                    if (optionComponent.config.getString(optionComponent.key).equals(valueBefore))
                        continue;
                    if (!configs.contains(optionComponent.config))
                        configs.add(optionComponent.config);

                    if (optionComponent.config instanceof GameConfig && optionComponent.key.equals("lowPowerMode")) {
                        fullRestartNeeded = true;
                    }
//                    if (optionComponent.config instanceof GraphicsConfig && optionComponent.key.equals("preset")) {
//                        fullRestartNeeded = true;
//                    }
                }
            }
            configs.forEach(config -> {
                try {
                    config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // Flush language
            instance.gameConfig.language.flush();

            // Reload configs
            configs.forEach(INIConfig::load);

            // If graphics changed, reset menu fog
            for (INIConfig config : configs) {
                if (config instanceof GraphicsConfig gc) {
                    SceneMenu.entityFog.createFog(gc);

                    if (before instanceof SceneInWorld siw) {
                        siw.reloadFog(gc);
                    }
                    break;
                }
            }

            // Set volumes
            instance.audioProcessor.setMasterVolume(instance.gameConfig.getDouble("volumeMaster"));
            instance.audioProcessor.setMusicVolume(instance.gameConfig.getDouble("volumeMusic"));
            instance.audioProcessor.setSoundVolume(instance.gameConfig.getDouble("volumeSound"));

            // Set display state
            instance.display.setFullscreen(0, instance.gameConfig.getBoolean("fullscreen"));

            // Set restart needed notice
            if (!fullRestartNeeded) {
                fullRestartNeeded = configs.stream().anyMatch(config -> config instanceof LaunchConfig);
            }

            applyButton.setEnabled(false);
            backButton.setEnabled(true);
        }

        if (component == leftButton) {
            page--;
        } else if (component == rightButton) {
            page++;
        }

        if (component instanceof EditButton eb) {
            eb.owner.fire(component);
        }

        if (component instanceof BindButton bb) {
            bb.owner.fire(component);
        }
    }

    static class NavButton extends Button {
        public NavButton(Scene scene, String key) {
            super(scene, key);
            setTextAlign(Align.CENTER, Align.CENTER);
            setBackgroundVisible(true);
            setTextColor(new Color(163, 163, 167));
        }
    }

    static class EditButton extends Button {
        OptionComponent owner;

        public EditButton(Scene scene, OptionComponent owner, String key) {
            super(scene, key);
            this.owner = owner;
            setTextAlign(Align.CENTER, Align.CENTER);
            setBackgroundVisible(false);
            setTextColor(new Color(163, 163, 167));
            setFontScale(2.25);
        }
    }

    static class BindButton extends Button {
        OptionComponent owner;

        public BindButton(Scene scene, OptionComponent owner, String key) {
            super(scene, key);
            this.owner = owner;
            setTextAlign(Align.CENTER, Align.CENTER);
            setBackgroundVisible(true);
            setTextColor(new Color(163, 163, 167));
            setFontScale(0);
        }

        @Override
        public void update(Graphics2D g2d, double dt) {
            updateColor(dt);
        }

        public Color getCurrentTextColor() {
            return currentTextColor;
        }

    }

    static class OptionComponent extends LocalizedComponent {
        private ArrayList<Button> children;
        private Button left, right;
        private BindButton bindBtn;
        private Type type;
        private INIConfig config;

        private int valueInt;
        private double valueDouble;
        private boolean valueBoolean;
        private KeyBind valueBind;

        private String key;

        public OptionComponent(Scene scene, INIConfig config, String key) {
            super(scene, key);
            this.key = key;
            this.children = new ArrayList<>();
            this.config = config;

            if (config instanceof InputConfig inputConfig) {
                valueBind = inputConfig.getBind(key);
                type = Type.BIND;
                populate(scene, config, key);
                return;
            }

            String value = config.getString(key);

            try {
                Integer.parseInt(value);
                this.type = Type.NATURAL;
                this.valueInt = config.getInt(key);
                if (key.equals("fpsLimit") && valueInt <= 0)
                    valueInt = Integer.MAX_VALUE;
            } catch(NumberFormatException e) {
                try {
                    Double.parseDouble(value);
                    this.type = Type.REAL;
                    this.valueDouble = config.getDouble(key);
                } catch(NumberFormatException e1) {
                if (value.equals("false") || value.equals("true")) {
                    this.type = Type.BOOL;
                    this.valueBoolean = config.getBoolean(key);
                } else {
                        this.type = Type.ENUM;
                        if (key.equals("language")) {
                            for (int i = 0; i < Language.values().length; i++) {
                                if (Language.values()[i].name().equals(value)) {
                                    this.valueInt = i;
                                }
                            }
                        } else if (key.equals("renderPipeline")) {
                            for (int i = 0; i < RenderPipeline.AVAILABLE.length; i++) {
                                if (RenderPipeline.AVAILABLE[i].name().equals(value)) {
                                    this.valueInt = i;
                                }
                            }
                        } else if (key.equals("preset")) {
                            for (int i = 0; i < GraphicsConfig.Preset.values().length; i++) {
                                if (GraphicsConfig.Preset.values()[i].name().equals(value)) {
                                    this.valueInt = i;
                                }
                            }
                        }
                    }
                }
            }

            populate(scene, config, key);
        }

        private void populate(Scene scene, INIConfig config, String key) {
            if (type != Type.BIND) {
                left = new EditButton(scene, this,"options.left");
                right = new EditButton(scene, this, "options.right");
                children.add(left);
                children.add(right);
            } else {
                bindBtn = new BindButton(scene, this, "wow youve somehow found an easteregg");
                children.add(bindBtn);
            }
        }

        @Override
        public void devicePressed(KeyBind.Device device, int code) {
            super.devicePressed(device, code);

            if (isVisible)
                children.forEach(children -> children.devicePressed(device, code));

            if (newBindMode && newBindWaitDelay > 100.0) {
                newBindMode = false;
                valueBind = new KeyBind(device, code);
                restoreEnableStates();
                isModified = true;
                if (scene instanceof SceneOptions sceneOptions) {
                    sceneOptions.applyButton.setEnabled(true);
                    sceneOptions.backButton.setEnabled(false);
                }
            }
        }

        @Override
        public void mouseMoved(int x, int y) {
            super.mouseMoved(x, y);

            if (isVisible)
                children.forEach(children -> children.mouseMoved(x, y));
        }

        private boolean isModified;

        private boolean newBindMode = false;
        private double newBindWaitDelay = 0;

        @Override
        public void update(Graphics2D g2d, double dt) {
            if (!isVisible) {
                if (newBindMode) {
                    restoreEnableStates();
                    newBindMode = false;
                }
                return;
            }

            if (newBindMode) {
                newBindWaitDelay += dt;
                if (newBindWaitDelay > 5000.0) {
                    restoreEnableStates();
                    newBindMode = false;
                }
            }

            Instance instance = scene.instance;
            FontRenderer fontRenderer = scene.fontRenderer;
            Vec2D center = bounds.center();
            double inset = 10.0;
            g2d.setColor(new Color(163, 163, 167));
            g2d.setFont(instance.lexicon.equipmentPro.deriveFont((float) this.scene.scaleToDisplay(55.0)));
            fontRenderer.drawStringAccurately(g2d, this.scene.localize("options." + this.key, this.localizationObjects), (int) this.bounds.position.x + this.scene.scaleToDisplay(10.0 + inset), (int) center.y, Align.LEFT, Align.CENTER, true);

            if (type != Type.BIND) {
                String value = "error";
                if (type == Type.ENUM) {
                    if (key.equals("language"))
                        value = Language.values()[valueInt].getNativeName();
                    else if (key.equals("renderPipeline"))
                        value = RenderPipeline.AVAILABLE[valueInt].name();
                    else if (key.equals("preset"))
                        value = scene.localize("options.graphics." + GraphicsConfig.Preset.values()[valueInt].name().toLowerCase(Locale.ROOT));

                } else if (type == Type.REAL) {
                    value = (int) Math.round(valueDouble * 100) + "%";
                } else if (type == Type.NATURAL) {
                    value = String.valueOf(valueInt);
                    if (key.equals("fpsLimit") && maxFps > 0 && valueInt > maxFps)
                        value = scene.localize("options.unlimitedFps");
                } else if (type == Type.BOOL) {
                    value = valueBoolean ? scene.localize("options.on") : scene.localize("options.off");
                }

                double[] valBounds = getBounds(key);

                if (scene instanceof SceneOptions os && os.optionComponents.stream().noneMatch(optionComponent -> optionComponent.newBindMode)) {
                    if (type == Type.NATURAL || type == Type.ENUM)
                        left.setEnabled(valueInt > valBounds[0]);
                    else if (type == Type.REAL)
                        left.setEnabled(valueDouble > valBounds[0]);
                    else if (type == Type.BOOL)
                        left.setEnabled(valueBoolean);

                    if (type == Type.NATURAL || type == Type.ENUM)
                        right.setEnabled(valueInt < valBounds[1]);
                    else if (type == Type.REAL)
                        right.setEnabled(valueDouble < valBounds[1]);
                    else if (type == Type.BOOL)
                        right.setEnabled(!valueBoolean);
                }

                // Estimated center position, for string, but should work fine too (in order to safe time im not creating a formula for this)
                BoundingBox valueBounds = fontRenderer.drawStringAccurately(g2d, value, this.scene.scaleToDisplay(10.0 + inset) + (int) (scene.getDisplay().getWidth() / 3 / 3 * 2.14), (int) center.y, Align.CENTER, Align.CENTER, true);

                double editSize = bounds.size.y * 0.7;

                if (left != null && right != null) {
                    left.setBounds(center.x, valueBounds.position.y + valueBounds.size.y / 2 - editSize / 2, editSize, editSize);
                    right.setBounds(scene.getDisplay().getWidth() / 3.0 - scene.scaleToDisplay(10.0 + inset) - editSize, valueBounds.position.y + valueBounds.size.y / 2 - editSize / 2, editSize, editSize);
                }
            } else {
                double bindHeight = this.bounds.size.y * 0.8;
                String override = !newBindMode ? null : System.currentTimeMillis() / 500 % 2 == 0 ? "?" : "";
                BoundingBox hintBounds = BindLocalizer.drawHint(g2d, scene, valueBind, this.scene.scaleToDisplay(10.0 + inset) + (int) (scene.getDisplay().getWidth() / 3 / 3 * 2.14), (int) (center.y - bindHeight / 2), (int) bindHeight, Align.CENTER, bindBtn.getCurrentTextColor(), override);
               bindBtn.setBounds(hintBounds);

            }

            children.forEach(children -> children.update(g2d, dt));
        }

        public void setInConfig() {
            if (isModified) {
                if (type == Type.ENUM && key.equals("language")) {
                    config.set(key, Language.values()[valueInt].name());
                } else if (type == Type.ENUM && key.equals("preset") && config instanceof GraphicsConfig gc) {
                    GraphicsConfig.Preset preset = GraphicsConfig.Preset.values()[valueInt];
                    config.set(key, preset.name());
                    preset.apply(gc);
                } else if (type == Type.ENUM && key.equals("renderPipeline")) {
                    config.set(key, RenderPipeline.AVAILABLE[valueInt].name());
                } else if (type == Type.NATURAL) {
                    if (key.equals("fpsLimit") && valueInt > maxFps)
                        config.set(key, 0);
                    else
                        config.set(key, valueInt);
                } else if (type == Type.REAL) {
                    config.set(key, Math.round(valueDouble * 100.0) / 100.0);
                } else if (type == Type.BOOL) {
                    config.set(key, valueBoolean);
                } else if (type == Type.BIND) {
                    config.set(key, valueBind);
                }
            }
        }

        private HashMap<Button, Boolean> enabledStates;

        private void restoreEnableStates() {
            enabledStates.keySet().forEach(btn -> btn.setEnabled(enabledStates.get(btn)));
            enabledStates.clear();
        }

        public void fire(Component component) {
            double dStepSize = 0.1;
            double iStepSize = 1;
            double[] bounds = getBounds(key);
            double min = bounds[0], max = bounds[1];

            if (key.equals("fpsLimit"))
                iStepSize = 5;

            if (component == bindBtn && !newBindMode) {
                newBindMode = true;
                newBindWaitDelay = 0;

                this.enabledStates = new HashMap<>();

                for (Component c : scene.components) {
                    if (c instanceof Button btn) {
                        enabledStates.put(btn, btn.isEnabled());
                        btn.setEnabled(false);
                    } else if (c instanceof OptionComponent oc) {
                        for (Component occ : oc.children) {
                            if (occ instanceof Button btn) {
                                enabledStates.put(btn, btn.isEnabled());
                                btn.setEnabled(false);
                            }
                        }
                    }
                }

                return;
            }

            if (component == left && left.isEnabled()) {
                if (this.type == Type.REAL) {
                    this.valueDouble -= dStepSize;
                    if (this.valueDouble < min)
                        this.valueDouble = 0;
                } else if (this.type == Type.NATURAL || this.type == Type.ENUM) {
                    if (this.valueInt > max)
                        this.valueInt = (int) max;
                    this.valueInt -= iStepSize;
                    if (this.valueInt < min)
                        this.valueInt = 0;
                } else if (this.type == Type.BOOL) {
                    valueBoolean = false;
                }
                isModified = true;
            } else if (component == right && right.isEnabled()) {
                if (this.type == Type.REAL) {
                    this.valueDouble += dStepSize;
                    if (this.valueDouble > max)
                        this.valueDouble = max;
                } else if (this.type == Type.NATURAL || this.type == Type.ENUM) {
                    this.valueInt += iStepSize;
                    if (this.valueInt > max)
                        this.valueInt = (int) max;
                } else if (this.type == Type.BOOL) {
                    valueBoolean = true;
                }
                isModified = true;
            }

            if (isModified && scene instanceof SceneOptions sceneOptions) {
                sceneOptions.applyButton.setEnabled(true);
                sceneOptions.backButton.setEnabled(false);
            }
        }

        private static int maxFps;

        private double[] getBounds(String key) {
            if (type == Type.ENUM && key.equals("language")) {
                return new double[] { 0, Language.values().length - 1 };
            } else if (type == Type.ENUM && key.equals("preset")) {
                return new double[] { 0, GraphicsConfig.Preset.values().length - 1 };
            } else if (type == Type.ENUM && key.equals("renderPipeline")) {
                return new double[] { 0, RenderPipeline.AVAILABLE.length - 1 };
            } else if (type == Type.NATURAL && key.equals("fpsLimit")) {
                if (maxFps == 0)
                    maxFps = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate();
                return new double[] { 15, maxFps + 5 };
            }
            return new double[] { 0, 1 };
        }

        enum Type {
            NATURAL, REAL, ENUM, BOOL, BIND
        }
    }

    static class KeyBindButton extends Button {

        public KeyBindButton(Scene scene, String key) {
            super(scene, key);
            setTextAlign(Align.CENTER, Align.CENTER);
            setBackgroundVisible(false);
            setTextColor(new Color(163, 163, 167));
        }
    }
}
