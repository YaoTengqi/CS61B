package gitlet;

// TODO: any imports you need here

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date; // TODO: You'll likely use this in this class

/**
 * Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Commit {
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
    private Commit parent = null;      //本次提交的父亲commit


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
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        String parentCommitID = (parent != null) ? parent.getCommitID() : "";
        String blobIDs = this.getBlobsID();
        String commitInfo = parentCommitID + blobIDs;
        byte[] hashBytes = digest.digest(commitInfo.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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

    public String getCommitID() {
        return this.commitID;
    }
}
