package gitlet;
import java.io.File;
import java.util.TreeMap;
import static gitlet.Commit.commitCommand;
import static gitlet.Utils.*;
import java.util.*;
import java.text.SimpleDateFormat;

/* Represents a gitlet repository.
 *  The repository contains the bulk of the methods to execute in this program.
 *
 *  @author Matthew Mojica
 */
public class Repository {
    /*
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Subdirectory in .gitlet to store commits. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commits");
    /** Subdirectory in .gitlet to store blobs. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** Subdirectory in .gitlet to act as staging area. */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");
    /** Subdirectory in .gitlet to keep track of branches. */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    /** Creates master branch and stores latest commit. */
    public static final File MASTERBRANCH = join(BRANCHES_DIR, "master");
    /** Subdirectory in .gitlet to keep tracked of pointers to head pointer and active branch. */
    public static final File HEAD_DIR = join(GITLET_DIR, "HEAD");
    /** Creates head pointer to a commit object in the current branch. */
    public static final File HEADPOINTER = join(HEAD_DIR, "HEAD");
    /** Records the current active branch name. */
    public static final File HEADNAME = join(HEAD_DIR, "headName.txt");
    /** File where staging for addition occurs. */
    public static final File ADDAREA = join(STAGING_DIR, "addArea");
    /** File where staging for removal occurs. */
    public static final File SUBAREA = join(STAGING_DIR, "subArea");


    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            System.out.print("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        /** Create persistence in project. */
        GITLET_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BLOBS_DIR.mkdir();
        STAGING_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        HEAD_DIR.mkdir();
        writeObject(ADDAREA, new Staging());
        writeObject(SUBAREA, new Staging());


        /** Create initial commit and write it to commit folder.  */
        Commit initialCommit = new Commit();
        File initialCommitFile = join(COMMIT_DIR, sha1(serialize(initialCommit)));
        writeObject(initialCommitFile, initialCommit);

        /** Update master branch and head pointer with initial commit. */
        writeObject(MASTERBRANCH, initialCommit);
        writeObject(HEADPOINTER, initialCommit);
        writeContents(HEADNAME, "master");
    }

    public static void addCommand(String fileName) {
        File addedFile = join(CWD, fileName);
        if (!addedFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        /** Accesses current commit's tracked files. */
        Commit headCommit = readObject(HEADPOINTER, Commit.class);
        TreeMap<String, String> currentTrackedFiles = headCommit.getTrackedFiles();

        /** Accesses files that are currently staged to be added. */
        TreeMap<String, String> stagedForAddition = readObject(ADDAREA, Staging.class).getAddArea();

        if (sha1(readContents(addedFile)).equals(currentTrackedFiles.get(fileName))) {
            if (stagedForAddition.containsKey(fileName)) {
                stagedForAddition.remove(fileName);
            }
            stagingRemovalHelper(fileName);
            System.exit(0);
        }

        /** Accesses files that are currently staged to be removed. */
        stagingRemovalHelper(fileName);

        /** Read contents of file, then write blob to BLOBS_DIR. */
        String blob = readContentsAsString(addedFile);
        writeContents(join(BLOBS_DIR, sha1(blob)), blob);

        /** Add file name and blob pairing to staging TreeMap. */
        Staging.addHelper(fileName, blob);
    }

    private static void stagingRemovalHelper(String fileName) {
        Staging filesToRemove = readObject(SUBAREA, Staging.class);
        if (SUBAREA.exists()) {
            TreeMap<String, String> stagedForRemoval = filesToRemove.getSubArea();
            if (stagedForRemoval.containsKey(fileName)) {
                stagedForRemoval.remove(fileName);
            }
            writeObject(SUBAREA, filesToRemove);
        }
    }

    public static void removeCommand(String fileName) {
        File removedFile = join(CWD, fileName);

        /** Accesses files that are currently staged to be added. */
        Staging stagedForAddition = readObject(ADDAREA, Staging.class);

        /** Accesses current commit's tracked files. */
        Commit headCommit = readObject(HEADPOINTER, Commit.class);
        TreeMap<String, String> currentTrackedFiles = headCommit.getTrackedFiles();

        if ((!currentTrackedFiles.containsKey(fileName)
                && !stagedForAddition.getAddArea().containsKey(fileName))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (stagedForAddition.getAddArea().containsKey(fileName)) {
            stagedForAddition.getAddArea().remove(fileName);
        }
        if (currentTrackedFiles.containsKey(fileName)) {
            Staging.removeHelper(fileName);
            restrictedDelete(fileName);
        }
        writeObject(ADDAREA, stagedForAddition);
    }

    public static void logCommand() {
        Commit currentCommit = readObject(HEADPOINTER, Commit.class);
        while (currentCommit != null) {

            /** Writes log in terminal */
            logHelper(currentCommit);

            /** If currentCommit is initial commit, terminates program before null errors occur. */
            if (currentCommit.getParentID() == null) {
                return;
            }
            currentCommit = readObject(join(COMMIT_DIR, currentCommit.getParentID()), Commit.class);
        }
    }

    private static void logHelper(Commit currentCommit) {


        System.out.println("===");
        System.out.println("commit " + sha1(serialize(currentCommit)));
        if (currentCommit.getMerged()) {
            System.out.println("Merge: " + currentCommit.getParentID().substring(0, 7) + " "
                    + currentCommit.getParentID2().substring(0, 7));
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("EEE MMM d kk:mm:ss yyyy Z");
        String dateString = timeFormat.format(currentCommit.getTimestamp());
        System.out.println("Date: " + dateString);
        System.out.println(currentCommit.getMessage());
        System.out.println("");
    }

    public static void branchCommand(String branchName) {
        File newBranch = join(BRANCHES_DIR, branchName);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            writeObject(newBranch, readObject(HEADPOINTER, Commit.class));
        }
    }

    public static void checkoutCommand1(String fileName) {
        Commit headCommit = readObject(HEADPOINTER, Commit.class);
        TreeMap<String, String> headCommitFiles = headCommit.getTrackedFiles();
        if (!headCommitFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File cwdVersion = join(CWD, fileName);
        File blobContents = join(BLOBS_DIR, headCommitFiles.get(fileName));
        writeContents(cwdVersion, readContentsAsString(blobContents));
    }

    public static void checkoutCommand2(String commitID, String fileName) {
        List<String> workingFiles = plainFilenamesIn(COMMIT_DIR);
        for (String i : workingFiles) {
            if (i.startsWith(commitID)) {
                commitID = i;
            }
        }
        File givenCommit = join(COMMIT_DIR, commitID);
        if (!givenCommit.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit givenCommitObject = readObject(givenCommit, Commit.class);
        TreeMap<String, String> givenCommitFiles = givenCommitObject.getTrackedFiles();
        if (!givenCommitFiles.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File cwdVersion = join(CWD, fileName);
        File checkoutBlob = join(BLOBS_DIR, givenCommitFiles.get(fileName));
        writeContents(cwdVersion, readContentsAsString(checkoutBlob));
    }

    public static void checkoutCommand3(String branchName) {
        File givenBranch = join(BRANCHES_DIR, branchName);
        if (!givenBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(readContentsAsString(HEADNAME))) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String[] currentFiles = CWD.list();
        Set<String> currentFilesSet = readObject(HEADPOINTER, Commit.class).
                getTrackedFiles().keySet();

        /** Check whether any files in the CWD will be overwritten. */
        checkUntracked(currentFiles, givenBranch);

        /** Deletes files in CWD that are tracked by current commit, but not by given commit. */
        for (String i : currentFiles) {
            if (readObject(HEADPOINTER, Commit.class).getTrackedFiles().containsKey(i)
                && !readObject(givenBranch, Commit.class).getTrackedFiles().containsKey(i)) {
                restrictedDelete(join(CWD, i));
            }
        }

        /** Write the files from the given commit to the CWD. */
        TreeMap<String, String> givenTrackedFiles = readObject(givenBranch, Commit.class)
                .getTrackedFiles();
        for (String i : givenTrackedFiles.keySet()) {
            File moveToCWD = join(CWD, i);
            writeContents(moveToCWD, readContentsAsString(join(BLOBS_DIR,
                    givenTrackedFiles.get(i))));
        }

        /** Empty staging area */
        readObject(ADDAREA, Staging.class).getAddArea().clear();
        readObject(SUBAREA, Staging.class).getSubArea().clear();

        /** Changed branch head. */
        writeContents(HEADNAME, branchName);
        writeObject(HEADPOINTER, readObject(join(BRANCHES_DIR, branchName), Commit.class));
    }

    private static void checkUntracked(String[] workingFiles, File givenBranch) {
        TreeMap<String, String> givenCommitFiles = readObject(givenBranch, Commit.class).
                getTrackedFiles();
        for (String i : workingFiles) {
            if (!readObject(HEADPOINTER, Commit.class).getTrackedFiles().containsKey(i)
                    && !readObject(ADDAREA, Staging.class).getAddArea().containsKey(i)
                    && givenCommitFiles.containsKey(i)
                    && !sha1(readContentsAsString(join(CWD, i))).equals(givenCommitFiles.get(i))) {
                System.out.println("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public static void rmBranchCommand(String branchName) {
        File givenBranch = join(BRANCHES_DIR, branchName);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(readContentsAsString(HEADNAME))) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        givenBranch.delete();
    }

    public static void resetCommand(String commitID) {
        File givenCommit = join(COMMIT_DIR, commitID);
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        String[] workingFiles = CWD.list();
        Set<String> currentFiles = readObject(HEADPOINTER, Commit.class).getTrackedFiles().keySet();
        if (!commitList.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Set<String> givenFiles = readObject(givenCommit, Commit.class).getTrackedFiles().keySet();
        checkUntracked(workingFiles, givenCommit);
        for (String i : givenFiles) {
            checkoutCommand2(commitID, i);
        }

        /** Switch branches. */
        writeObject(HEADPOINTER, readObject(givenCommit, Commit.class));
        writeObject(join(BRANCHES_DIR, readContentsAsString(HEADNAME)),
                readObject(givenCommit, Commit.class));

        /** Clear staging area. */
        Staging addArea = readObject(ADDAREA, Staging.class);
        addArea.getAddArea().clear();
        writeObject(ADDAREA, addArea);
        Staging subArea = readObject(SUBAREA, Staging.class);
        subArea.getSubArea().clear();
        writeObject(SUBAREA, subArea);
    }

    public static void statusCommand() {
        /** Print out branches with head pointer. */
        System.out.println("=== Branches ===");
        List<String> allBranches = plainFilenamesIn(BRANCHES_DIR);
        for (String i : allBranches) {
            if (i.equals(readContentsAsString(HEADNAME))) {
                System.out.print("*");
            }
            System.out.println(i);
        }
        System.out.println("");

        /** Print out staged files. */
        System.out.println("=== Staged Files ===");
        Set<String> filesToAdd = readObject(ADDAREA, Staging.class).getAddArea().keySet();
        List<String> addFilesList = new ArrayList<String>(filesToAdd);
        Collections.sort(addFilesList);
        for (String i : addFilesList) {
            System.out.println(i);
        }
        System.out.println("");

        /** Print out removed files. */
        System.out.println("=== Removed Files ===");
        Set<String> filesToSub = readObject(SUBAREA, Staging.class).getSubArea().keySet();
        List<String> subFilesList = new ArrayList<String>(filesToSub);
        Collections.sort(subFilesList);
        for (String i : subFilesList) {
            System.out.println(i);
        }
        System.out.println("");

        /** Print out unimplemented areas. */
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        System.out.println("");
    }

    public static void findCommand(String message) {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        boolean commitExists = false;
        for (String i : commitList) {
            if (readObject(join(COMMIT_DIR, i), Commit.class).getMessage().equals(message)) {
                System.out.println(i);
                commitExists = true;
            }
        }
        if (!commitExists) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void globalLogCommand() {
        List<String> commitList = plainFilenamesIn(COMMIT_DIR);
        for (String i : commitList) {
            logHelper(readObject(join(COMMIT_DIR, i), Commit.class));
        }
    }

    public static void mergeCommand(String otherBranch) {
        File givenBranch = join(BRANCHES_DIR, otherBranch);
        String[] workingFiles = CWD.list();
        TreeMap<String, String> currentHeadFiles = readObject(join(BRANCHES_DIR,
                readContentsAsString(HEADNAME)), Commit.class).getTrackedFiles();
        /** Check error cases. */
        mergeErrors(givenBranch, otherBranch);
        checkUntracked(workingFiles, givenBranch);
        /** Find split point and check if it is current branch. */
        Commit splitPoint = findSplitPoint(readContentsAsString(HEADNAME), otherBranch);
        /** Get file names tracked by all three branches and place them in one set. */
        Commit otherHeadCommit = readObject(join(BRANCHES_DIR, otherBranch), Commit.class);
        TreeMap<String, String> otherHeadFiles = otherHeadCommit.getTrackedFiles();
        TreeMap<String, String> splitPointFiles = splitPoint.getTrackedFiles();
        Set<String> filesInOther = otherHeadFiles.keySet();
        Set<String> filesInHead = currentHeadFiles.keySet();
        Set<String> filesInSplit = splitPoint.getTrackedFiles().keySet();
        Set<String> allFiles = gatherFiles(filesInOther, filesInHead, filesInSplit);
        Staging additionArea = readObject(ADDAREA, Staging.class);
        Boolean mergeOccurred = false;
        for (String i : allFiles) {
            if (!filesInSplit.contains(i)) {
                if (!filesInHead.contains(i) && filesInOther.contains(i)) {
                    additionArea.getAddArea().put(i, otherHeadFiles.get(i));
                    checkoutCommand2(sha1(serialize(readObject(join(BRANCHES_DIR, otherBranch),
                            Commit.class))), i);
                } else if (filesInHead.contains(i) && !filesInOther.contains(i)) {
                    continue;
                }
            } else {
                if (filesInOther.contains(i)) {
                    if (filesInHead.contains(i)) {
                        if (!currentHeadFiles.get(i).equals(splitPointFiles.get(i))
                                && otherHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                            continue;
                        }
                        if (currentHeadFiles.get(i).equals(splitPointFiles.get(i))
                                && !otherHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                            additionArea.getAddArea().put(i, otherHeadFiles.get(i));
                            checkoutCommand2(sha1(serialize(readObject(join(BRANCHES_DIR,
                                    otherBranch), Commit.class))), i);
                        }
                        if (!currentHeadFiles.get(i).equals(splitPointFiles.get(i))
                                && !otherHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                            if (currentHeadFiles.get(i).equals(otherHeadFiles.get(i))) {
                                continue;
                            } else {
                                mergeOccurred = executeMergeConflict(i, currentHeadFiles,
                                        additionArea, otherHeadFiles);
                            }
                        } else if (otherHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                            continue;
                        }
                    }
                } else if (filesInHead.contains(i)
                        && currentHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                    removeCommand(i);
                } else if (filesInHead.contains(i)
                        && !currentHeadFiles.get(i).equals(splitPointFiles.get(i))) {
                    mergeOccurred = executeMergeConflict(i, currentHeadFiles, additionArea,
                            otherHeadFiles);
                }
            }
        }
        endSequence(mergeOccurred, otherBranch, otherHeadCommit, additionArea);
    }

    private static Boolean executeMergeConflict(String i, TreeMap<String, String> currentHeadFiles,
                                    Staging additionArea, TreeMap<String, String> otherHeadFiles) {
        additionArea.getAddArea().put(i, sha1(createMergeConflict(currentHeadFiles
                .get(i), otherHeadFiles.get(i))));
        writeContents(join(CWD, i), createMergeConflict(
                currentHeadFiles.get(i), otherHeadFiles.get(i)));
        return true;
    }

    private static void endSequence(Boolean mergeOccurred, String otherBranch,
                                    Commit otherHeadCommit, Staging additionArea) {
        if (mergeOccurred) {
            System.out.println("Encountered a merge conflict.");
        }
        writeObject(ADDAREA, additionArea);
        String commitString = "Merged " + otherBranch + " into " + readContentsAsString(HEADNAME)
                + ".";
        commitCommand(commitString, true, sha1(serialize(otherHeadCommit)));
    }

    private static Set<String> gatherFiles(Set<String> filesInOther,
                                           Set<String> filesInSplit, Set<String> filesInHead) {
        Set<String> allFiles = new HashSet<String>();
        allFiles.addAll(filesInOther);
        allFiles.addAll(filesInSplit);
        allFiles.addAll(filesInHead);
        return allFiles;
    }

    private static void mergeErrors(File givenBranch, String otherBranch) {
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (readContentsAsString(HEADNAME).equals(otherBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (!readObject(ADDAREA, Staging.class).getAddArea().isEmpty()
                || !readObject(SUBAREA, Staging.class).getSubArea().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static Commit findSplitPoint(String currentBranch, String otherBranch) {
        LinkedList<String> currentBranchAncestors = bfsHelper(sha1(serialize(
                readObject(join(BRANCHES_DIR, currentBranch), Commit.class))));
        if (currentBranchAncestors.contains(sha1(serialize(readObject(
                join(BRANCHES_DIR, otherBranch), Commit.class))))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        LinkedList<String> otherBranchAncestors = bfsHelper(sha1(serialize(readObject(join(
                BRANCHES_DIR, otherBranch), Commit.class))));
        if (otherBranchAncestors.contains(sha1(serialize(readObject(HEADPOINTER, Commit.class))))) {
            checkoutCommand3(otherBranch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        currentBranchAncestors.retainAll(otherBranchAncestors);
        String lastCommonAncestor = currentBranchAncestors.getFirst();
        return readObject(join(COMMIT_DIR, lastCommonAncestor), Commit.class);
    }

    private static LinkedList<String> bfsHelper(String commitID) {
        HashSet<String> marked = new HashSet<String>();
        LinkedList<String> queue = new LinkedList<String>();
        LinkedList<String> ancestorList = new LinkedList<String>();
        queue.addLast(commitID);
        marked.add(commitID);
        while (!marked.isEmpty()) {
            String node = queue.removeFirst();
            Commit nodeCommit = readObject(join(COMMIT_DIR, node), Commit.class);

            /** Work for parent 1. */
            String parentID1 = nodeCommit.getParentID();
            if (parentID1 == null) {
                return ancestorList;
            }
            queue.addLast(parentID1);
            marked.add(parentID1);
            ancestorList.addLast(parentID1);

            /** Work for parent 2. */
            if (nodeCommit.getParentID2() != null) {
                String parentID2 = nodeCommit.getParentID2();
                queue.addLast(parentID2);
                marked.add(parentID2);
                ancestorList.addLast(parentID2);
            }
        }
        return ancestorList;
    }

    private static String createMergeConflict(String currentFile, String otherFile) {
        String currentString = "";
        String otherString = "";
        if (currentFile != null) {
            currentString = readContentsAsString(join(BLOBS_DIR, currentFile));
        }
        if (otherFile != null) {
            otherString = readContentsAsString(join(BLOBS_DIR, otherFile));
        }

        String conflictString = "<<<<<<< HEAD\n" + currentString + "=======\n" + otherString
                + ">>>>>>>\n";
        writeContents(join(BLOBS_DIR, sha1(conflictString)), conflictString);
        return conflictString;
    }
}
