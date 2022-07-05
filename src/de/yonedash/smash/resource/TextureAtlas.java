package de.yonedash.smash.resource;

import de.yonedash.smash.Direction;
import de.yonedash.smash.Instance;
import de.yonedash.smash.entity.Entity;
import de.yonedash.smash.entity.EntityAnt;
import de.yonedash.smash.entity.EntityPlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static de.yonedash.smash.Direction.*;

public class TextureAtlas {

    private final Instance instance;
    protected final ArrayList<Texture> texturesLoaded;
    private final HashMap<Class<? extends Entity>, TextureBundle> textureBundles;
    private final HashMap<Texture, Texture> particleMap;

    public final int walkSubId = 0x0000, idleSubId = 0x1111;

    public final Texture invalid;
    public Texture crosshair, fork, slash, afterdeath, hit;

    public Texture forest;

    public TextureAtlas(Instance instance) {
        this.instance = instance;

        this.texturesLoaded = new ArrayList<>();
        this.textureBundles = new HashMap<>();
        this.particleMap = new HashMap<>();

        this.invalid = createMissingTexture();
    }

    public void load() {
        this.crosshair = loadTexture("/ui/crosshair.png");
        this.fork = loadTexture("/Fork.png");
        this.forest = loadTexture("/forest.png");

        // Load player assets
        TextureBundle playerBundle = getBundleOrCreate(EntityPlayer.class);

        Texture ninjaWalk = loadTexture("/Walk.png");
        playerBundle.store(0x003 + walkSubId, loadTexture(ninjaWalk,
                10.0, 0, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x001 + walkSubId, loadTexture(ninjaWalk,
                10.0, 16, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x004 + walkSubId, loadTexture(ninjaWalk,
                10.0, 32, 0, 16, 16, SOUTH, 4));
        playerBundle.store(0x002 + walkSubId, loadTexture(ninjaWalk,
                10.0, 48, 0, 16, 16, SOUTH, 4));
        ninjaWalk.flush();

        Texture ninjaIdle = loadTexture("/Idle.png");
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
        this.slash = loadTexture("/slash_effect_anim_spritesheet.png",
                10.0, 0, 0, 16, 16, EAST, 3);

        this.afterdeath = loadTexture("/enemy_afterdead_explosion_anim_spritesheet.png",
                10.0, 0, 0, 16, 16, EAST, 4);

        this.hit = loadTexture("/hit_effect_anim_spritesheet.png",
                10.0, 0, 0, 8, 8, EAST, 3);
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
