package de.yonedash.smash.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public final class ResourceFinder {

    public static InputStream openInputStream(String path) {
        // Get folder path
        File file = new File(new File("").getAbsolutePath() + "/" + path);
        try {
            // Try folder path
            System.out.println("FILE fileinputstream " + path);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // If folder path did not work, try getting the resource from inside the
            InputStream stream = ResourceFinder.class.getResourceAsStream(path);

            // If stream does not exist
            if (stream == null) {
                try {
                    System.out.println("PATH fileinputstream " + path);
                    // Return FileInputStream
                    return new FileInputStream(path);
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            System.out.println("inputstream " + path);
            return stream;
        }
    }


}
