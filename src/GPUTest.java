
import de.yonedash.smash.Instance;

public class GPUTest {

    public static void main(String[] args) {
        // Enable GPU acceleration
        System.setProperty("sun.java2d.metal", "True");
        // System.setProperty("sun.java2d.opengl", "True");

        // Program starts here
        // Create new instance and run
        (new Instance()).run();
    }

}
