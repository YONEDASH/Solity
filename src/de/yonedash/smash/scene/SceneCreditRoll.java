package de.yonedash.smash.scene;

import de.yonedash.smash.Align;
import de.yonedash.smash.Instance;
import de.yonedash.smash.Vec2D;
import de.yonedash.smash.config.KeyBind;
import de.yonedash.smash.resource.ResourceFinder;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SceneCreditRoll extends Scene {

    private static final String[] creditLines = loadCreditText();

    private static String[] loadCreditText() {
        InputStream is = ResourceFinder.openInputStream("/assets/credits.txt");
        Scanner scanner = new Scanner(is);

        scanner.useDelimiter(System.lineSeparator());

        ArrayList<String> lines = new ArrayList<>();
        while (scanner.hasNext())
           lines.add(scanner.next());

        scanner.close();
        try {
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lines.forEach(line -> System.out.println(line));

        return lines.toArray(new String[0]);
    }

    public SceneCreditRoll(Instance instance) {
        super(instance);
    }

    private double currentLine = 0;

    @Override
    public void update(Graphics2D g2d, double dt) {
        Font bold = instance.lexicon.futilePro;
        Font plain = instance.lexicon.compassPro;

        float boldSize = scaleToDisplay(130.0);
        float plainSize = scaleToDisplay(110.0);

        double lineSpaceRatio = 1.5;

        g2d.setColor(Color.WHITE);

        double creditHeight = 0;
        for (String line : creditLines) {
            g2d.setFont(line.startsWith("*") ? bold.deriveFont(boldSize) : plain.deriveFont(plainSize));
            Vec2D bounds = fontRenderer.bounds(g2d, line.isEmpty() ? "X" : line);

            creditHeight += bounds.y * lineSpaceRatio;
        }

        int width = instance.display.getWidth(), height = instance.display.getHeight();

        double linePosY = height - (currentLine * (creditHeight / creditLines.length));
        for (String line : creditLines) {
            if (linePosY > -boldSize && linePosY < height + boldSize) {
                g2d.setFont(line.startsWith("*") ? bold.deriveFont(boldSize) : plain.deriveFont(plainSize));
                fontRenderer.drawStringAccurately(g2d, line.startsWith("*") ? line.substring(1) : line, width / 2, (int) Math.ceil(linePosY), Align.CENTER, Align.CENTER, false);
            }

            linePosY += fontRenderer.bounds(g2d, line.isEmpty() ? "X" : line).y * lineSpaceRatio;

        }

        currentLine += time(0.001, dt);
    }

    @Override
    public void devicePressed(KeyBind.Device device, int code) {
        if (device == KeyBind.Device.KEYBOARD && code == KeyEvent.VK_R) {
            currentLine = 0;
        }
    }
}
