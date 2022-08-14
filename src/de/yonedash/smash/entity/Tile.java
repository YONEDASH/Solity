package de.yonedash.smash.entity;

import de.yonedash.smash.*;
import de.yonedash.smash.resource.Texture;
import de.yonedash.smash.graphics.TextureAtlas;
import de.yonedash.smash.scene.Scene;

import java.awt.*;

public class Tile extends LevelObject {

    private final Texture texture;
    private ParticleType particleType;
    private double time, timeNextParticleEmit;

    public Tile(BoundingBox boundingBox, int z, Texture texture) {
        super(boundingBox, z);
        this.texture = texture;
        this.particleType = null;
    }

    @Override
    public void draw(Scene scene, Graphics2D g2d, double dt) {
        double padding = (1 / scene.calculateDisplayScaleFactor()) - 1;
        g2d.drawImage(this.texture.getImage(),
                scene.scaleToDisplay(this.boundingBox.position.x - padding),
                scene.scaleToDisplay(this.boundingBox.position.y - padding),
                scene.scaleToDisplay(this.boundingBox.size.x + padding * 2),
                scene.scaleToDisplay(this.boundingBox.size.y + padding * 2),
                null
        );

        if (!Constants.PARTICLE_EMIT_IN_LOADED_CHUNKS)
            emitParticles(scene, dt);

    }

    public void emitParticles(Scene scene, double dt) {
        if (!Constants.PARTICLES_ENABLED)
            return;

        if (this.particleType != null && (this.time += dt) > this.timeNextParticleEmit) {
            rollNextEmitTime(scene.instance.graphicsConfig.particleEmitDelayFactor);
            this.time = 0;

            double random1 = random(scene), random2 = random(scene), random3 = (random1 + random2) / 2, random4 = random(scene);
            double centerOffsetFactor = 0.7;
            Vec2D particleSize = new Vec2D(random4 * random3 * Tile.TILE_SIZE * 0.0615 * 2.3, random4 * random3 * Tile.TILE_SIZE * 0.0615 * 2.3);
            World world = scene.instance.world;
            double rotation = (15.0 + random3 * 69.0) + (90.0 * (world.weatherProgress + 1.0) / 2.0);
            EntityParticle entityParticleLeaf = new EntityParticle(
                    new BoundingBox(this.boundingBox.center().clone().add(
                            new Vec2D(this.boundingBox.size.x * random1 * centerOffsetFactor , this.boundingBox.size.y * random2 * centerOffsetFactor)), particleSize),
                    scene.instance.atlas.getParticle(this.texture),
                    rotation, 0.075 * random3, this.particleType.timeAlive * ((random1 + random2 + random3) / 3.0),
                    this.z + (random1 > 0.5 ? 1 : -1), true);
            world.entitiesLoaded.add(entityParticleLeaf);
        }
    }

    private double random(Scene scene) {
        return (scene.instance.world.simplexNoise.eval(scene.instance.world.particleOffset++, scene.instance.world.particleOffset / 2) + 1.0) / 2.0;
    }

    public void setParticleType(TextureAtlas atlas, ParticleType particleType) {
        this.particleType = particleType;
        rollNextEmitTime(1.0);

        int tw = this.texture.getWidth(), th = this.texture.getHeight();
        int size = Math.min(8, Math.min(tw, th));
        Texture particle = atlas.loadTexture(this.texture, tw / 2 - size / 2, th / 2 - size / 2, size, size);
        atlas.storeParticle(this.texture, particle);
    }

    private void rollNextEmitTime(double factor) {
        this.timeNextParticleEmit = Math.random() * this.particleType.maxDelay * factor;
    }

}
