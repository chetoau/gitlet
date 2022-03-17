package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/** Gitlet class, class containing all
 * functionalities of Gitlet.
 * @author Nhu Vu
 */
public class Gitlet implements Serializable {

    /** Current working directory initializing gitlet in. */
    static final File CWD = new File(".");

    /** Our initialized .gitlet folder. */
    static final File GITLET_REPO = Utils.join(CWD, ".gitlet");

    /** Folder keeping track of commits. */
    static final File COMMITS_DIR = Utils.join(GITLET_REPO, "commits");

    /** Directory for my gitlet branches. */
    static final File BRANCH_DIR = Utils.join(GITLET_REPO, "branches");

    /** Folder to keep track of my blobs. */
    static final File BLOBS_DIR = Utils.join(GITLET_REPO, "blobs");

    /** Directory for my staging area. */
    static final File STAGING_AREA = Utils.join(GITLET_REPO, "stage");


    /**Initializes gitlet repository for version-control system. */
    public void init() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            GITLET_REPO.mkdirs();
            COMMITS_DIR.mkdirs();
            BLOBS_DIR.mkdirs();
            BRANCH_DIR.mkdirs();
            STAGING_AREA.mkdirs();

            Commit c = new Commit(null, "initial commit", new HashMap<>());
            String path = c.getCommitHash() + ".txt";
            Utils.writeObject(Utils.join(COMMITS_DIR, path), c);


            File headFile = Utils.join(BRANCH_DIR, "HEAD.txt");
            Utils.writeContents(headFile, "master");


            File masterP = Utils.join(BRANCH_DIR, "master.txt");
            Utils.writeContents(masterP, c.getCommitHash());

            staging = new Stage();
            Utils.writeObject(Utils.join(STAGING_AREA, "stage.txt"), staging);
        } else {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
    }

    /** Add files and new changes to staging area.
     * @param name for file name.
     */
    public void add(String name) {
        Commit curr = recentCommit();
        File a = Utils.join(CWD, name);
        if (a.exists()) {
            staging = Utils.readObject(Utils.join(STAGING_AREA,
                    "stage.txt"), Stage.class);
            byte[] contents = Utils.readContents(a);
            String hash = Utils.sha1(contents);

            if (staging.getRemoved().contains(name)) {
                staging.getRemoved().remove(name);
                Utils.writeObject(Utils.join(STAGING_AREA,
                        "stage.txt"), staging);
            } else if (staging.getAdded().containsKey(name)
                    && staging.getAdded().get(name).equals(hash)) {
                staging.getAdded().remove(name, hash);
                Utils.writeObject(Utils.join(STAGING_AREA,
                        "stage.txt"), staging);
                System.out.println("File is up to date");
            } else if (hash.equals(curr.getBlob().get(name))) {
                Utils.writeObject(Utils.join(STAGING_AREA,
                        "stage.txt"), staging);
            } else {
                File newBlob = Utils.join(BLOBS_DIR,
                        hash + ".txt");
                Utils.writeContents(newBlob, contents);
                staging.getAdded().put(name, hash);
                Utils.writeObject(Utils.join(STAGING_AREA,
                        "stage.txt"), staging);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }

    /** Commit changes made to working directory.
     * @param message for commit message.
     */
    public void commit(String message) {
        staging = Utils.readObject(Utils.join(STAGING_AREA,
                "stage.txt"), Stage.class);
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        } else if (staging.getAdded().isEmpty()
                && staging.getRemoved().isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        } else {
            HashMap<String, String> addedList = staging.getAdded();
            ArrayList<String> removedList = staging.getRemoved();
            ArrayList<String> changes =
                    new ArrayList<>(addedList.keySet());
            Commit recent = recentCommit();
            HashMap<String, String> contents =
                    duplicateBlobs(recent);
            for (String item: changes) {
                contents.put(item, addedList.get(item));
            }
            for (String item: removedList) {
                contents.remove(item);
            }
            Commit toCommit = new Commit(recent.getCommitHash(),
                    message, contents);
            String head = Utils.readContentsAsString(
                    Utils.join(BRANCH_DIR, "HEAD.txt"));
            Utils.writeObject(Utils.join(COMMITS_DIR,
                    toCommit.getCommitHash() + ".txt"),
                    toCommit);
            Utils.writeContents(Utils.join(BRANCH_DIR,
                    head + ".txt"), toCommit.getCommitHash());
            staging.clearStage();
            Utils.writeObject(Utils.join(STAGING_AREA,
                    "stage.txt"), staging);
        }
    }



    /** Displays all past commits of CWD. */
    public void log() {
        Commit c = recentCommit();
        String result = "";
        while (c != null) {
            result += "===" + "\n";
            result += "commit " + c.getCommitHash() + "\n";
            result += "Date: " + c.timeStamp() + "\n";
            result += c.commitMsg() + "\n";
            result += "\n";
            if (c.parentHash() == null) {
                break;
            } else {
                String parent = c.parentHash();
                c = Utils.readObject(Utils.join(COMMITS_DIR,
                        parent + ".txt"), Commit.class);
            }
        }
        System.out.println(result);
    }

    /** Retrieve a past commit, changing head pointer.
     * @param args for checkout inputs.
     */
    public void checkout(String... args) {
        if (args.length == 3) {
            Commit lastCommit = recentCommit();
            String name = args[2];
            if (!lastCommit.getBlob().containsKey(args[2])) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String hash = lastCommit.getBlob().get(name);
            File getBlob = Utils.join(BLOBS_DIR, hash + ".txt");
            byte[] contents = Utils.readContents(getBlob);
            File currFile = Utils.join(CWD, name);
            Utils.writeContents(currFile, contents);
        }
        if (args.length == 4) {
            String hashID = args[1];
            String name = args[3];
            List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
            for (String realID: allCommits) {
                if (realID.contains(hashID)) {
                    hashID = realID.substring(0, realID.length() - 4);
                }
            }
            File target = Utils.join(COMMITS_DIR, hashID + ".txt");
            if (!target.exists()) {
                System.out.println("No commit with that id exists.");
                return;
            }
            Commit tarCommit = Utils.readObject(target, Commit.class);
            if (!tarCommit.getBlob().containsKey(name)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String blobHash = tarCommit.getBlob().get(name);
            File getBlob = Utils.join(BLOBS_DIR,
                    blobHash + ".txt");
            byte[] contents = Utils.readContents(getBlob);
            Utils.writeContents(Utils.join(CWD, name), contents);
        }
        if (args.length == 2) {
            checkoutHelper(args);
        }
    }

    /** Checkout helper because method is too long.
     * @param args for checkout input.
     */
    public void checkoutHelper(String... args) {
        String branchName = args[1];
        File branch = Utils.join(BRANCH_DIR,
                branchName + ".txt");
        String currBranch = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            return;
        } else if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        } else {
            String commitPath = Utils.readContentsAsString(branch);
            Commit myCommit = Utils.readObject(Utils.join(COMMITS_DIR,
                    commitPath + ".txt"), Commit.class);
            Commit currCommit = recentCommit();
            HashMap<String, String> myBlobs = myCommit.getBlob();
            HashMap<String, String> currBlobs = currCommit.getBlob();
            List<String> workingDir = Utils.plainFilenamesIn(CWD);
            ArrayList<String> files = new ArrayList<>();
            for (String name: workingDir) {
                if (name.endsWith(".txt")) {
                    files.add(name);
                }
            }
            for (String name: files) {
                if (myBlobs.containsKey(name)
                        && !currBlobs.containsKey(name)) {
                    System.out.println("There is an"
                            + " untracked file in the way;"
                            + " delete it, or add and commit it first.");
                    return;
                }
            }
            for (String name: files) {
                if (!myBlobs.containsKey(name)
                        && currBlobs.containsKey(name)) {
                    Utils.restrictedDelete(name);
                }
            }
            for (String name: myBlobs.keySet()) {
                String hash = myBlobs.get(name);
                File blob = Utils.join(BLOBS_DIR,
                        hash + ".txt");
                byte[] contents = Utils.readContents(blob);
                Utils.writeContents(Utils.join(CWD, name),
                        contents);
            }
            staging = Utils.readObject(Utils.join(STAGING_AREA,
                    "stage.txt"), Stage.class);
            staging.clearStage();
            Utils.writeObject(Utils.join(STAGING_AREA,
                    "stage.txt"), staging);
            Utils.writeContents(Utils.join(BRANCH_DIR,
                    "HEAD.txt"), branchName);
        }
    }

    /** Unstage a file staged for addition
     *  or stage a file for removal in the current commit.
     * @param name for file name.
     */
    public void remove(String name) {
        staging = Utils.readObject(Utils.join(STAGING_AREA,
                "stage.txt"), Stage.class);
        Commit headCommit = recentCommit();
        HashMap<String, String> inCommit = duplicateBlobs(headCommit);
        boolean tracked = false;
        for (String file: inCommit.keySet()) {
            if (file.equals(name)) {
                tracked = true;
                break;
            }
        }
        if (staging.getAdded().containsKey(name)) {
            staging.getAdded().remove(name);
            Utils.writeObject(Utils.join(STAGING_AREA,
                    "stage.txt"), staging);
        } else if (tracked) {
            staging.remove(name);
            Utils.restrictedDelete(name);
            if (staging.getAdded().containsKey(name)) {
                staging.getAdded().remove(name);
                Utils.writeObject(Utils.join(STAGING_AREA,
                        "stage.txt"), staging);
            }
            Utils.writeObject(Utils.join(STAGING_AREA,
                    "stage.txt"), staging);
        } else {
            System.out.println("No reason to"
                    + " remove the file.");
            return;
        }
    }

    /** Finds all commits with given message.
     * @param message for commit message.
     */
    public void find(String message) {
        List<String> commits = Utils.plainFilenamesIn(COMMITS_DIR);
        int count = 0;
        for (String commit: commits) {
            Commit c = Utils.readObject(Utils.join(COMMITS_DIR,
                    commit), Commit.class);
            if (c.commitMsg().equals(message)) {
                System.out.println(c.getCommitHash());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Prints out all information about all commits ever made. */
    public void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        String result = "";
        for (String name: allCommits) {
            Commit c = Utils.readObject(Utils.join(
                    COMMITS_DIR, name), Commit.class);
            result += "===" + "\n";
            result += "commit " + c.getCommitHash() + "\n";
            result += "Date: " + c.timeStamp() + "\n";
            result += c.commitMsg() + "\n";
            result += "\n";
        }
        System.out.println(result);
    }

    /** Prints out current status of repo, such as which
     * branches exist/files staged for addition, etc.
     */
    public void status() {
        staging = Utils.readObject(Utils.join(STAGING_AREA,
                "stage.txt"), Stage.class);
        String head = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        String result = "=== Branches ===" + "\n";
        List<String> branches = Utils.plainFilenamesIn(BRANCH_DIR);
        for (String branch: branches) {
            branch = branch.substring(0, branch.length() - 4);
            if (branch.equals(head)) {
                result += "*" + branch;
            } else if (branch.equals("HEAD")) {
                continue;
            } else {
                result += branch;
            }
            result += "\n";
        }
        result += "\n" + "=== Staged Files ===" + "\n";
        String[] addedNames =
                staging.getAdded().keySet().toArray(new String[0]);
        Arrays.sort(addedNames);
        for (String name: addedNames) {
            result += name;
            result += "\n";
        }
        result += "\n" + "=== Removed Files ===" + "\n";
        String[] removedNames = staging.getRemoved().toArray(new String[0]);
        for (String name: removedNames) {
            result += name;
            result += "\n";
        }
        result += "\n" + "=== Modifications Not Staged For Commit ===" + "\n";
        result += "\n" + "=== Untracked Files ===" + "\n";
        System.out.println(result);
    }

    /** Creates a new branch with given branch name.
     *
     * @param name for branch name.
     */
    public void branch(String name) {
        File newBranch = Utils.join(BRANCH_DIR, name + ".txt");
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        } else {
            Commit current = recentCommit();
            Utils.writeContents(newBranch, current.getCommitHash());
        }
    }

    /** Deletes poiner to branch with given name,
     *  leaving all commits under it alone.
     * @param name for branch name.
     */
    public void removeBranch(String name) {
        String currBranch = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        File branch = Utils.join(BRANCH_DIR, name + ".txt");
        if (!branch.delete()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (name.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }

    }

    /** Checks out files tracked by given commit ID,
     *  removes tracked files not present in that commit.
     * @param hash for commit hash.
     */
    public void reset(String hash) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        for (String realID: allCommits) {
            if (realID.contains(hash)) {
                hash = realID.substring(0, realID.length() - 4);
            }
        }
        File commitFile = Utils.join(COMMITS_DIR, hash + ".txt");
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit myCommit = Utils.readObject(commitFile, Commit.class);
        Commit currCommit = recentCommit();
        HashMap<String, String> myBlobs = myCommit.getBlob();
        HashMap<String, String> currBlobs = currCommit.getBlob();
        List<String> workingDir = Utils.plainFilenamesIn(CWD);
        ArrayList<String> files = new ArrayList<>();
        for (String name: workingDir) {
            if (name.endsWith(".txt")) {
                files.add(name);
            }
        }
        for (String name: files) {
            if (myBlobs.containsKey(name) && !currBlobs.containsKey(name)) {
                System.out.println("There is an untracked"
                        + " file in the way; delete it,"
                        + " or add and commit it first.");
                return;
            }
        }
        for (String name: files) {
            if (!myBlobs.containsKey(name) && currBlobs.containsKey(name)) {
                Utils.restrictedDelete(name);
            }
        }
        for (String name: myBlobs.keySet()) {
            String hashCode = myBlobs.get(name);
            File blob = Utils.join(BLOBS_DIR, hashCode + ".txt");
            byte[] contents = Utils.readContents(blob);
            Utils.writeContents(Utils.join(CWD, name), contents);
        }
        staging = Utils.readObject(Utils.join(STAGING_AREA,
                "stage.txt"), Stage.class);
        staging.clearStage();
        Utils.writeObject(Utils.join(STAGING_AREA,
                "stage.txt"), staging);
        String headPath = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        Utils.writeContents(Utils.join(BRANCH_DIR,
                headPath + ".txt"), hash);
    }

    /** Merges files from the given branch to the current branch.
     *
     * @param name for branch name.
     */
    public void merge(String name) {
        if (!mergeErrors(name)) {
            return;
        } else if (!mergeUntracked(name)) {
            return;
        } else {
            String branchHash = Utils.readContentsAsString(
                    Utils.join(BRANCH_DIR, name + ".txt"));
            Commit branchP = Utils.readObject(Utils.join(COMMITS_DIR,
                    branchHash + ".txt"), Commit.class);
            Commit currCommit = recentCommit();
            HashMap<String, Commit> mergeTree = mergeTreeBuilder();
            Commit split = null;
            while (branchP != null && Utils.readObject(Utils.join(
                            COMMITS_DIR, branchP.getCommitHash() + ".txt"),
                    Commit.class) != null) {
                if (mergeTree.containsKey(branchP.getCommitHash())) {
                    split = mergeTree.get(branchP.getCommitHash());
                    break;
                }
                branchP = Utils.readObject(Utils.join(COMMITS_DIR,
                        branchP.parentHash() + ".txt"), Commit.class);
            }
            if (mergeTree.containsKey(branchHash)) {
                System.out.println("Given branch is an ancestor"
                        + " of the current branch.");
                return;
            } else if (split.getCommitHash().equals(
                    currCommit.getCommitHash())) {
                String head = Utils.readContentsAsString(
                        Utils.join(BRANCH_DIR, "HEAD.txt"));
                Utils.writeContents(Utils.join(BRANCH_DIR,
                        head + ".txt"));
                System.out.println("Current branch fast-forwarded.");
                return;
            }
            mergeHelp(branchHash, split, branchP);
            if (conflictDet(currCommit, branchP, split, branchHash)) {
                System.out.println("Encountered a merge conflict.");
                return;
            } else {
                String head = Utils.readContentsAsString(
                        Utils.join(BRANCH_DIR, "HEAD.txt"));
                commit("Merged " + name + " into " + head + ".");
            }
        }
    }

    /**Helper method to determine conflict or not.
     *
     * @param curr for current commit.
     * @param branch for branch commit.
     * @param split for split point commit.
     * @param hash for branch hash.
     * @return boolean indicating if there's merge conflict.
     */
    public boolean conflictDet(Commit curr, Commit branch,
                               Commit split, String hash) {
        boolean conflict = false;
        for (String file: curr.getBlob().keySet()) {
            String bComContents = branch.getBlob().get(file);
            String currContents = curr.getBlob().get(file);
            if (split.getBlob().containsKey(file)
                    && branch.getBlob().containsKey(file)) {
                String splitContents = split.getBlob().get(file);
                if (!splitContents.equals(bComContents)
                        || !currContents.equals(bComContents)
                        || !splitContents.equals(currContents)) {
                    writeConflict2(currContents, bComContents, file);
                    conflict = true;
                }
                if (splitContents.equals(currContents)
                        && !splitContents.equals(bComContents)) {
                    checkout("checkout", hash, "--", file);
                    add(file);
                }
            } else if (split.getBlob().containsKey(file)
                    && !branch.getBlob().containsKey(file)
                    && !curr.getBlob().get(file).equals(
                    split.getBlob().get(file))) {
                writeConflict1(currContents, bComContents, file);
                conflict = true;
            }
        }
        return conflict;
    }

    /** Helper for merge.
     *
     * @param hash for branch hash.
     * @param splitP for split point commit.
     * @param branchCom for branch commit.
     */
    public void mergeHelp(String hash, Commit splitP, Commit branchCom) {
        Commit currCommit = recentCommit();
        ArrayList<String> sFiles = new ArrayList<>(
                splitP.getBlob().keySet());
        ArrayList<String> bFiles = new ArrayList<>(
                branchCom.getBlob().keySet());
        for (String file: bFiles) {
            if (!sFiles.contains(file)) {
                checkout("checkout", hash, "--", file);
                add(file);
            }
        }
        for (String file: bFiles) {
            if (currCommit.getBlob().containsKey(file)) {
                String sHash = splitP.getBlob().get(file);
                String cHash = currCommit.getBlob().get(file);
                if (sHash.equals(cHash) && !bFiles.contains(file)) {
                    remove(file);
                }
            }
        }
    }

    /** Helper method to write contents of case 1.
     *
     * @param currContents contents of current commit.
     * @param bContents contents of intended branch commit.
     * @param file file to be modified.
     */
    public void writeConflict1(String currContents,
                               String bContents, String file) {
        File workFile = Utils.join(CWD, file);
        String header = "<<<<<<< HEAD" + "\n";
        String bars = "=======" + "\n";
        String c = Utils.readContentsAsString(Utils.join(
                BLOBS_DIR, currContents + ".txt"));
        String b = Utils.readContentsAsString(Utils.join(
                BLOBS_DIR, bContents + ".txt"));
        String arrows = ">>>>>>>\n";
        String result = header + c + "\n" + bars + b + "\n" + arrows;
        Utils.writeContents(workFile, result);
    }

    /** Helper method to write contents of case 2.
     *
     * @param currContents contents of current commit.
     * @param bContents contents of intended branch commit.
     * @param file file to be modified.
     */
    public void writeConflict2(String currContents,
                               String bContents, String file) {
        File workFile = Utils.join(CWD, file);
        String header = "<<<<<<< HEAD" + "\n";
        String bars = "=======";
        String c = Utils.readContentsAsString(Utils.join(
                BLOBS_DIR, currContents + ".txt"));
        String b = Utils.readContentsAsString(Utils.join(
                BLOBS_DIR, bContents + ".txt"));
        String arrows = ">>>>>>>\n";
        String result = header + c + bars + "\n" + b + arrows;
        Utils.writeContents(workFile, result);
    }

    /** Helper method to build commit tree and find split point.
     *
     * @return a split point of two branches.
     */
    public HashMap<String, Commit> mergeTreeBuilder() {
        HashMap<String, Commit> mergeTree = new HashMap<String, Commit>();
        Commit currP = recentCommit();
        while (currP != null && Utils.readObject(Utils.join(COMMITS_DIR,
                currP.getCommitHash() + ".txt"), Commit.class)
                != null) {
            Commit currCommit = Utils.readObject(Utils.join
                    (COMMITS_DIR, currP.getCommitHash() + ".txt"),
                    Commit.class);
            mergeTree.put(currP.getCommitHash(), currCommit);
            if (currP.parentHash() != null) {
                currP = Utils.readObject(Utils.join(COMMITS_DIR,
                        currP.parentHash() + ".txt"), Commit.class);
            } else {
                break;
            }
        }
        return mergeTree;
    }

    /** Helper method to throw errors in merge.
     *
     * @param name for branch name.
     * @return a boolean detecting error.
     */
    public boolean mergeErrors(String name) {
        String currBranch = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        File thisBranch = Utils.join(BRANCH_DIR, name + ".txt");
        if (name.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return false;
        } else if (!thisBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return false;
        } else if (thisBranch.exists() && !name.equals(currBranch)) {
            staging = Utils.readObject(Utils.join
                    (STAGING_AREA, "stage.txt"), Stage.class);
            if (!staging.getAdded().isEmpty()
                    || !staging.getRemoved().isEmpty()) {
                System.out.println("You have uncommitted changes.");
                return false;
            }
        }
        return true;
    }

    /** Print error for when there are untracked files.
     *
     * @param name for branch name.
     * @return boolean indicating if there are untracked files.
     */
    public boolean mergeUntracked(String name) {
        boolean isTracked = true;
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
        ArrayList<String> normFiles = new ArrayList<>();
        for (String file: cwdFiles) {
            if (file.endsWith(".txt")) {
                normFiles.add(file);
            }
        }
        String myCommitHash = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, name + ".txt"));
        Commit branchCommit = Utils.readObject(Utils.join(COMMITS_DIR,
                myCommitHash + ".txt"), Commit.class);
        Commit currCommit = recentCommit();
        HashMap<String, String> currBlobs = currCommit.getBlob();
        HashMap<String, String> branchBlobs = branchCommit.getBlob();
        for (String file: normFiles) {
            if (!currBlobs.containsKey(file)
                    && branchBlobs.containsKey(file)) {
                System.out.println("There is an untracked file in the "
                        + "way; delete it, or add and commit it first.");
                isTracked = false;
            }
        }
        return isTracked;
    }

    /** Helper method meant for retrieving most recent commit.
     *
     * @return my most recent commit.
     */
    public Commit recentCommit() {
        String head = Utils.readContentsAsString(
                Utils.join(BRANCH_DIR, "HEAD.txt"));
        File myCommit = Utils.join(BRANCH_DIR,
                head + ".txt");
        String commitHash = Utils.readContentsAsString(myCommit);
        return Utils.readObject(Utils.join(COMMITS_DIR,
                commitHash + ".txt"), Commit.class);
    }

    /** Helper method meant to make a copy of blob maps.
     *
     * @param c for this commit.
     * @return a copy of this commit's blob pointer.
     */
    public HashMap<String, String> duplicateBlobs(Commit c) {
        HashMap<String, String> myBlobs = c.getBlob();
        return (HashMap<String, String>) myBlobs.clone();
    }

    /** Stage object for addition and removal. */
    private static Stage staging;

}
