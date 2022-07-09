package de.yonedash.smash.graphics;

import de.yonedash.smash.Chunk;
import de.yonedash.smash.Constants;
import de.yonedash.smash.Instance;
import de.yonedash.smash.World;
import de.yonedash.smash.entity.DisplayEntity;
import de.yonedash.smash.entity.Entity;

import java.util.ArrayList;

public class GraphicsThread extends Thread{

    private final Instance instance;

    public GraphicsThread(Instance instance) {
        setName("Graphics Thread");
        this.instance = instance;
    }

    @Override
    public void run() {
        while (Constants.FOG_ENABLED || Constants.LIGHTING_ENABLED) {
            World world = instance.world;

            if (world != null) {
                ArrayList<DisplayEntity> litUpEntities = new ArrayList<>();

                for (Chunk chunk : world.chunksLoaded) {

                    // Update Fog
                    chunk.getEntityFog().updateFog(instance.scene);
                }

                // Save entities with a light source
                for (Entity entity : world.entitiesLoaded) {
                    if (entity.doesEmitLight())
                        litUpEntities.add(entity);
                }

                if (litUpEntities.isEmpty())
                    continue;

                // Update Lighting
                world.chunksLoaded.forEach(chunk -> chunk.getEntityLighting().updateLighting(instance.scene, litUpEntities));
            }

            // Wait for Game Loop to keep going
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
