package gitlet;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Staging implements Serializable {
    private TreeMap<String, String> additionArea;
    private TreeMap<String, String> removalArea;

    public Staging() {
        /** Staging area for addition. */
        this.additionArea = new TreeMap<String, String>();
        /** Staging area for removal. */
        this.removalArea = new TreeMap<String, String>();
    }


    /** Helper function to read staging area, modify it, then write changes. */
    public static void addHelper(String fileName, String blob) {
        if (!ADDAREA.exists()) {
            writeObject(ADDAREA, new Staging());
        }
        if (getAddStagingID(fileName).equals(sha1(readContents(join(CWD, fileName))))) {
            System.exit(0);
        }
        Staging trackedFiles = readObject(ADDAREA, Staging.class);
        trackedFiles.additionArea.put(fileName, sha1(blob));
        writeObject(join(STAGING_DIR, "addArea"), trackedFiles);
    }

    public static void removeHelper(String fileName) {
        if (!SUBAREA.exists()) {
            writeObject(SUBAREA, new Staging());
        }
        Staging removedFiles = readObject(SUBAREA, Staging.class);
        removedFiles.removalArea.put(fileName, "");
        writeObject(join(STAGING_DIR, "subArea"), removedFiles);
    }

    public static String getAddStagingID(String fileName) {
        TreeMap<String, String> accessedFile = readObject(ADDAREA, Staging.class).additionArea;
        if (accessedFile.get(fileName) == null) {
            return "";
        }
        return accessedFile.get(fileName);
    }

    public TreeMap<String, String> getAddArea() {
        return this.additionArea;
    }

    public TreeMap<String, String> getSubArea() {
        return this.removalArea;
    }
}
