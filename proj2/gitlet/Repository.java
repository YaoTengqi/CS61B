package gitlet;

import java.io.File;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGE_AREA = join(GITLET_DIR, "stage_area");
    public static final File COMMIT_AREA = join(GITLET_DIR, "commit_area");
    public static final File HEAD_AREA = join(GITLET_DIR, "head_area");
    public static final File REMOVAL_AREA = join(GITLET_DIR, "removal_area");

    /* TODO: fill in the rest of this class. */
    public static void makeSetup() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            // TODO: execute the first commit
        } else {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
    }

    public static void makeStageArea() {
        if (!STAGE_AREA.exists()) {
            STAGE_AREA.mkdir();
        } else {
            throw new GitletException("A STAGE_AREA already exists in the current directory.");
        }
    }

    public static void makeCommitArea() {
        if (!COMMIT_AREA.exists()) {
            COMMIT_AREA.mkdir();
        } else {
            throw new GitletException("A COMMIT_AREA already exists in the current directory.");
        }
    }

    public static void makeHeadArea() {
        if (!HEAD_AREA.exists()) {
            HEAD_AREA.mkdir();
        } else {
            throw new GitletException("A HEAD_AREA already exists in the current directory.");
        }
    }

    public static void makeRemovalArea() {
        if (!REMOVAL_AREA.exists()) {
            REMOVAL_AREA.mkdir();
        } else {
            throw new GitletException("A REMOVAL_AREA already exists in the current directory.");
        }
    }
}
