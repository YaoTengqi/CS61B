package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;

/**
 * Represents a gitlet commit object.
 * 每个commit代表一次提交，有一个独一无二的ID，提交信息，提交时间，对blobs的引用以及父亲commit
 * does at a high level.
 *
 * @author ytq
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String commitID;    //每个commit都有一个由SHA-1生成的ID
    private String message;     //每次提交都会有一个message来描述本次提交
    private Date time;          //提交的时间
    private Blobs[] blobArray;  //本次提交所包含的blob，存储在此队列中
    private Commit parent;      //本次提交的父亲commit

    /* TODO: fill in the rest of this class. */
    public Commit(String message, Blobs[] blobArray, Commit parent) throws NoSuchAlgorithmException {
        this.message = message;
        Date epochTime = new Date(0L);
        this.time = epochTime;
        this.blobArray = blobArray;
        this.parent = parent;
        this.commitID = calculateID(parent, blobArray);
    }

    private String calculateID(Commit parent, Blobs[] blobArray) throws NoSuchAlgorithmException {
        String parentCommitID = (parent != null) ? parent.getCommitID() : "";
        String blobIDs = this.getBlobsID();
        String commitInfo = parentCommitID + blobIDs + message + time;
        return Utils.sha1(commitInfo);
    }

    private String getBlobsID() {
        if (blobArray == null) {
            return null;
        }
        StringBuilder blobIDs = new StringBuilder();
        for (Blobs blob : blobArray) {
            if (blob != null) {
                blobIDs.append(blob.getBlobID());
            }
        }
        return blobIDs.toString();
    }

    /**
     * 将此commit写入COMMIT_AREA中
     */
    public void writeCommit(File AREA, String commitName) throws IOException {
        File newCommit = Utils.join(AREA, commitName + ".bin");
        if (!newCommit.exists()) {
            newCommit.createNewFile();
            Utils.writeObject(newCommit, this);
        } else {
            throw new GitletException("This commit already exists.");
        }
    }

    public static void clearStageArea(List<String> fileNames) {
        for (String fileName : fileNames) {
            File deletedFile = Utils.join(Repository.STAGE_AREA, fileName);
            deletedFile.delete();
        }
    }

    public String getCommitID() {
        return this.commitID;
    }

    public Blobs[] getBlobArray() {
        return blobArray;
    }
}
