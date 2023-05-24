package gitlet;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;
import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.Utils.sha1;

/*
 *  Represents a gitlet commit object.
 *  The Commit class takes a snapshot of specified tracked files as well as various metadata.
 *
 *  @author Matthew Mojica
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    /** The timestamp of this Commit. */
    private final Date timestamp;

    /** Tracks files and which blobs they point to. */
    private final TreeMap<String, String> trackedFiles;

    /** Tracks parent commit ID. */
    private final String parentID;

    /** Tracks second parent commit ID. */
    private final String parentID2;

    /** Indicates where commit was formed through merge. */
    private final Boolean merged;

    /** Makes the initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parentID = null;
        this.parentID2 = null;
        this.trackedFiles = new TreeMap<>();
        this.merged = false;
    }


    public Commit(String parentSha, String parentSha2, Date timeCreated,
                  String message, TreeMap<String, String> trackedFiles, Boolean merged) {
        this.message = message;
        this.timestamp = timeCreated;
        this.parentID = parentSha;
        this.parentID2 = parentSha2;
        this.trackedFiles = trackedFiles;
        this.merged = merged;
    }



    public static void commitCommand(String message, Boolean merge, String parent2) {
        if (readObject(ADDAREA, Staging.class).getAddArea().isEmpty()
                && readObject(SUBAREA, Staging.class).getSubArea().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        /** Create new commit object. */
        Commit newCommit;
        String parentSha = sha1(serialize(readObject(HEADPOINTER, Commit.class)));
        TreeMap<String, String> oldFiles = readObject(HEADPOINTER, Commit.class).trackedFiles;
        if (!merge) {
            Commit unmergeCommit = new Commit(parentSha, null, new Date(),
                                   message, oldFiles, false);
            newCommit = unmergeCommit;
        } else {
            Commit mergeCommit = new Commit(parentSha, parent2, new Date(),
                                 message, oldFiles, true);
            newCommit = mergeCommit;
        }

        /** Update tracked files in parent commit with files in staging area then clear addArea. */
        Staging addArea = readObject(ADDAREA, Staging.class);
        newCommit.trackedFiles.putAll(addArea.getAddArea());
        addArea.getAddArea().clear();
        writeObject(ADDAREA, addArea);

        /** Drop pointers to files that are located inside of the "Removed Files" staging area. */
        Staging removedFiles = readObject(SUBAREA, Staging.class);
        for (String i : removedFiles.getSubArea().keySet()) {
            newCommit.trackedFiles.remove(i);
        }
        removedFiles.getSubArea().clear();
        writeObject(SUBAREA, removedFiles);

        /** Update pointers with newest commit and write to commit directory. */
        writeObject(HEADPOINTER, newCommit);
        writeObject(join(BRANCHES_DIR, readContentsAsString(HEADNAME)), newCommit);
        writeObject(join(COMMIT_DIR, sha1(serialize(newCommit))), newCommit);
    }

    public String getMessage() {
        return this.message;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getParentID() {
        return this.parentID;
    }

    public String getParentID2() {
        return this.parentID2;
    }

    public TreeMap<String, String> getTrackedFiles() {
        return this.trackedFiles;
    }

    public Boolean getMerged() {
        return merged;
    }
}
