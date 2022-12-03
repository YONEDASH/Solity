package de.yonedash.solity.compat;

import de.yonedash.solity.compat.adapter.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public enum OS {

    OSX(OSXAdapter.class), WINDOWS(WindowsAdapter.class), LINUX(LinuxAdapter.class), UNKNOWN(FallbackAdapter.class);

    public static final OS LOCAL_MACHINE = getOS();

    private final Class<? extends Adapter> adapterClass;

    OS(Class<? extends Adapter> adapterClass) {
        this.adapterClass = adapterClass;
    }

    public Adapter newAdapter() {
        try {
            return this.adapterClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static OS getOS() {
        String name = System.getProperty("os.name").toUpperCase(Locale.ROOT);
        if (name.contains("WINDOWS")) {
            return OS.WINDOWS;
        } else if (name.contains("OS X")) {
            return OS.OSX;
        } else if (name.contains("LINUX")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

}
