package de.yonedash.solity.config;

public class KeyBind {

    private Device device;
    private int code;

    private boolean locked;

    public KeyBind(String string) {
        String[] args = string.split("!");
        update(Device.valueOf(args[0]), Integer.parseInt(args[1]));
    }

    public KeyBind(Device device, int code) {
        update(device, code);
    }

    public void update(Device device, int code) {
        this.device = device;
        this.code = code;
    }

    public Device getDevice() {
        return device;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return device.name() + "!" + code;
    }

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean equals(Device device, int code) {
        return this.code == code && this.device == device;
    }

    public enum Device {
        KEYBOARD, MOUSE
    }

}
