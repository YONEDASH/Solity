package de.yonedash.smash;

import de.yonedash.smash.entity.Entity;
import de.yonedash.smash.entity.EntityFog;

public class FogThread extends Thread{

    private final Instance instance;

    public FogThread(Instance instance) {
        setName("Fog Machine");
        this.instance = instance;
    }

    @Override
    public void run() {
        while (Constants.FOG_ENABLED) {
            World world = instance.world;

            if (world != null) {
                for (Entity entity : world.entitiesLoaded) {
                    if (entity instanceof EntityFog fog)
                            fog.generateFog(instance.scene);
                }
            }

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
