package de.yonedash.smash.graphics;

import de.yonedash.smash.Constants;
import de.yonedash.smash.Direction;
import de.yonedash.smash.Instance;
import de.yonedash.smash.entity.Entity;
import de.yonedash.smash.entity.EntityAnt;
import de.yonedash.smash.entity.EntityPlayer;
import de.yonedash.smash.resource.ResourceFinder;
import de.yonedash.smash.resource.Texture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static de.yonedash.smash.Direction.*;

public class TextureAtlas {

    private final Instance instance;
    protected final CopyOnWriteArrayList<Texture> texturesLoaded;
    private final HashMap<Class<? extends Entity>, TextureBundle> textureBundles;
    private final HashMap<Texture, Texture> particleMap;

    public final int walkSubId = 0x0000, idleSubId = 0x1111;

    public final Texture invalid;

    // Window iCON
    public Texture windowIcon;

    // UI
    public Texture uiBackground, uiCrossHair, uiArrow, uiHeartEmpty, uiHeartFull, uiDashEmpty, uiDashFull;

    // ING
    public Texture fork, coin, potionGreen, potionRed, potionYellow, animSlash, animAfterDeath, animHit;

    // Projectile
    public Texture projCanonBall;

    public Texture forest;

    public TextureAtlas(Instance instance) {
        this.instance = instance;

        this.texturesLoaded = new CopyOnWriteArrayList<>();
        this.textureBundles = new HashMap<>();
        this.particleMap = new HashMap<>();

        this.invalid = createMissingTexture();
    }

    public void load() {
        // Generate & set window icon
        this.windowIcon = generateWindowIcon();
        this.instance.adapter.setIcon(instance, windowIcon);

        this.fork = loadTexture("/Fork.png");
        this.forest = loadTexture("/forest.png");

        // Load UI
        this.uiBackground = loadTexture("/assets/ui/menuBackground.png");
        this.uiCrossHair = loadTexture("/assets/ui/crosshair.png");
        this.uiArrow = loadTexture("/assets/ui/arrow.png");
        this.uiHeartEmpty = loadTexture("/assets/ui/heartEmpty.png");
        this.uiHeartFull = loadTexture("/assets/ui/heartFull.png");
        this.uiDashEmpty = loadTexture("/assets/ui/dashEmpty.png");
        this.uiDashFull = loadTexture("/assets/ui/dashFull.png");
        this.coin = loadTexture(8.0, loadTexture("/assets/gameplay/coin/coin_anim_f0.png"), loadTexture("/assets/gameplay/coin/coin_anim_f1.png"), loadTexture("/assets/gameplay/coin/coin_anim_f2.png"), loadTexture("/assets/gameplay/coin/coin_anim_f3.png"));

        // Load projectiles
        this.projCanonBall = loadTexture("/projectile/CanonBall.png", 5.0, 0, 0, 16, 16, EAST, 5);

        // Load player assets
        TextureBundle playerBundle = getBundleOrCreate(EntityPlayer.class);

        Texture ninjaWalk = loadTexture("/entities/knight/Walk.png");
        playerBundle.store(0x003 + walkSubId, loadTexture(ninjaWalk,
                10.0, 0, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x001 + walkSubId, loadTexture(ninjaWalk,
                10.0, 16, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x004 + walkSubId, loadTexture(ninjaWalk,
                10.0, 32, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x002 + walkSubId, loadTexture(ninjaWalk,
                10.0, 48, 0, 16, 16, SOUTH, 4));
        ninjaWalk.flush();

        Texture ninjaIdle = loadTexture("/entities/knight/Idle.png");
        playerBundle.store(0x003 + idleSubId, loadTexture(ninjaIdle,
                1.0, 0, 0, 16, 16, SOUTH, 1));
        playerBundle.store(0x001 + idleSubId, loadTexture(ninjaIdle,
                1.0, 16, 0, 16, 16, SOUTH, 1));
        playerBundle.store(0x004 + idleSubId, loadTexture(ninjaIdle,
                1.0, 32, 0, 16, 16, SOUTH, 1));
        playerBundle.store(0x002 + idleSubId, loadTexture(ninjaIdle,
                1.0, 48, 0, 16, 16, SOUTH, 1));
        ninjaIdle.flush();

        Texture antIdle = loadTexture("/SoldierAntIdleSide.png", 4.0, 0, 0, 16, 16, EAST, 4);
        TextureBundle antBundle = getBundleOrCreate(EntityAnt.class);
        antBundle.store(0x003 + idleSubId, antIdle);
        antBundle.store(0x001 + idleSubId, antIdle);
        antBundle.store(0x004 + idleSubId, antIdle);
        antBundle.store(0x002 + idleSubId, antIdle);

        // Load effects
        this.animSlash = loadTexture("/slash_effect_anim_spritesheet.png",
                10.0, 0, 0, 16, 16, EAST, 3);

        this.animAfterDeath = loadTexture("/enemy_afterdead_explosion_anim_spritesheet.png",
                10.0, 0, 0, 16, 16, EAST, 4);

        this.animHit = loadTexture("/hit_effect_anim_spritesheet.png",
                10.0, 0, 0, 8, 8, EAST, 3);

        // Load potions
        this.potionGreen = loadTexture("/assets/gameplay/potion/potion_green.png");
        this.potionRed = loadTexture("/assets/gameplay/potion/potion_red.png");
        this.potionYellow = loadTexture("/assets/gameplay/potion/potion_yellow.png");
    }

    private Texture generateWindowIcon() {
        int size = 256;
        int arc = size / 2;
        int border = size / 32;
        double scale = 0.8;
        BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        Color backgroundColor = new Color(72, 72, 72);
        g2d.setPaint(new GradientPaint(size / 2f, 0, backgroundColor, size / 2f, size, backgroundColor.brighter()));
        g2d.fillRoundRect((int) (size * (1.0 - scale) * 0.5), (int) (size * (1.0 - scale) * 0.5), (int) (size * scale), (int) (size * scale), (int) (arc * scale), (int) (arc * scale));

        Color borderColor = new Color(72, 72, 72);
        g2d.setStroke(new BasicStroke((int) (border * scale)));
        g2d.setPaint(new GradientPaint(size / 2f, 0, borderColor.brighter(), size / 2f, size, borderColor));
        g2d.drawRoundRect((int) (size * (1.0 - scale) * 0.5), (int) (size * (1.0 - scale) * 0.5), (int) (size * scale), (int) (size * scale), (int) (arc * scale), (int) (arc * scale));

        String text = "S";
        g2d.setFont(instance.lexicon.futilePro.deriveFont((float) (size * 1 * scale)));

        g2d.setColor(Color.BLACK);
        FontRenderContext context = g2d.getFontRenderContext();
        GlyphVector vector = g2d.getFont().createGlyphVector(context, text);
        Rectangle2D r2d = vector.getVisualBounds();
        int textHeight = (int) r2d.getHeight();
        int textWidth = (int) r2d.getWidth();
        g2d.drawString(text, size / 2 - textWidth / 2, size / 2 + textHeight / 2);

        g2d.setFont(instance.lexicon.futilePro.deriveFont((float) (size * 0.81 * scale)));

        g2d.setColor(Constants.MAP_BACKGROUND_COLOR);
        vector = g2d.getFont().createGlyphVector(context, text);
        r2d = vector.getVisualBounds();
        textHeight = (int) r2d.getHeight();
        textWidth = (int) r2d.getWidth();
        g2d.drawString(text, size / 2 - textWidth / 2, size / 2 + textHeight / 2);

        return new TextureStatic(this, bufferedImage);
    }

    public void update(double dt) {
        this.texturesLoaded.forEach(texture -> {
            if (texture instanceof TextureAnimated textureAnimated)
                textureAnimated.update(dt);
        });
    }

    public void flush() {
        this.texturesLoaded.forEach(Texture::flush);
        this.texturesLoaded.clear();
        this.textureBundles.clear();
    }

    public TextureBundle getBundle(Class<? extends Entity> owner) {
        return this.textureBundles.get(owner);
    }

    public Texture getTexture(Class<? extends Entity> owner, int id) {
        TextureBundle bundle = getBundle(owner);

        Texture texture;
        if (bundle != null && (texture = bundle.pull(id)) != null)
            return texture;

        if (owner.getSuperclass() != null && owner.getSuperclass().isAssignableFrom(Entity.class))
            return getTexture((Class<? extends Entity>) owner.getSuperclass(), id);

        return this.invalid;
    }

    // Checks whether bundle is already created for certain entity, if not create a new one and return it
    private TextureBundle getBundleOrCreate(Class<? extends Entity> owner) {
        TextureBundle bundle = getBundle(owner);

        if (bundle == null)
            this.textureBundles.put(owner, bundle = new TextureBundle());

        return bundle;
    }

    public void storeParticle(Texture texture, Texture particle) {
        this.particleMap.put(texture, particle);
    }

    public Texture getParticle(Texture texture) {
        return this.particleMap.get(texture);
    }

    public Texture loadTexture(String path) {
        BufferedImage bufferedImage = loadBufferedImage(path);

        if (bufferedImage == null)
            System.err.println("Texture " + path + " not found");

        Texture result = bufferedImage == null ? createMissingTexture() : new TextureStatic(this, bufferedImage);
        this.texturesLoaded.add(result);
        return result;
    }

    public Texture loadTexture(String path, double playbackSpeed, int x, int y, int width, int height, Direction direction, int size) {
        Texture texture = loadTexture(path);
        Texture result = loadTexture(texture, playbackSpeed, x, y, width, height, direction, size);
        texture.flush();
        return result;
    }

    public Texture loadTexture(double playbackSpeed, Texture... textures) {
        Texture result = new TextureAnimated(this, playbackSpeed, textures);
        this.texturesLoaded.add(result);
        return result;
    }

    public Texture loadTexture(Texture texture, double playbackSpeed, int x, int y, int width, int height, Direction direction, int size) {
        BufferedImage bufferedImage = texture.getBufferedImage();

        if (bufferedImage == null) {
            Texture result = createMissingTexture();
            this.texturesLoaded.add(result);
            return result;
        }

        BufferedImage region;
        switch (direction) {
            case NORTH -> region = bufferedImage.getSubimage(x,  y - size * height, width, size * height);
            case EAST -> region = bufferedImage.getSubimage(x,  y, size * width, height);
            case SOUTH -> region = bufferedImage.getSubimage(x,  y, width, size * height);
            case WEST -> region = bufferedImage.getSubimage(x - width * size,  y, size * width, height);
            default ->  throw new IllegalArgumentException("Invalid direction");
        }

        Texture result = region == null ? createMissingTexture()
                : new TextureAnimated(this, region, playbackSpeed, direction, width, height);
        this.texturesLoaded.add(result);
        return result;
    }


    public Texture loadTexture(Texture texture, int x, int y, int width, int height) {
        BufferedImage bufferedImage = texture.getBufferedImage();

        if (bufferedImage == null) {
            Texture result = createMissingTexture();
            this.texturesLoaded.add(result);
            return result;
        }

        BufferedImage region = bufferedImage.getSubimage(x,  y, width, height);;
        Texture result = region == null ? createMissingTexture()
                : new TextureStatic(this, region);
        this.texturesLoaded.add(result);
        return result;
    }

    private BufferedImage loadBufferedImage(String path) {
        // Check if path is invalid, do not even try to load image
        if (path == null || path.isEmpty() || path.equals("null"))
            return null;

        // If path won't work anyways (because '/' prefix is needed), throw exception
        if (!path.startsWith("/"))
            throw new IllegalArgumentException("Path has to start with a '/'");

        try {
            // Load image
            InputStream in = ResourceFinder.openInputStream(path);
            // If input stream does not exist return null
            if (in == null)
                return null;

            // Convert input stream to image
            BufferedImage result = ImageIO.read(in);

            // Close input stream
            in.close();

            return result;
        } catch (IOException e) {
            e.printStackTrace();

            // If load has failed, return null
            return null;
        }
    }
    private Texture createMissingTexture() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, 1, 1);
        g.fillRect(1, 1, 2, 2);
        g.dispose();

        return new TextureStatic(this, image);
    }

}
