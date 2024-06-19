package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String commitID;    //每个commit都有一个由SHA-1生成的ID

    private String branch;
    private String message;     //每次提交都会有一个message来描述本次提交

    private String time;          //提交的时间

    private List<Blobs> blobArray;  //本次提交所包含的blob，存储在此队列中
    private Commit parent;      //本次提交的父亲commit


    public Commit(String branchName, String message, String commitTime, List<Blobs> blobArray, Commit parent) throws NoSuchAlgorithmException {
        this.message = message;
        this.time = commitTime;
        this.blobArray = blobArray;
        this.parent = parent;
        this.commitID = calculateID();
        this.branch = branchName;
    }

    public void setBlobArray(List<Blobs> blobArray) {
        this.blobArray = blobArray;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public Commit getParent() {
        return parent;
    }

    public String getBranch() {
        return branch;
    }

    private String calculateID() {
        String blobIDs = this.getBlobsID();
        String commitInfo = blobIDs + message + time;
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

    public String getCommitID() {
        return this.commitID;
    }

    public List<Blobs> getBlobArray() {
        return blobArray;
    }

    /**
     * 将此commit写入COMMIT_AREA中
     */
    public void writeCommit(File area, String commitName) throws IOException {
        File newCommit = Utils.join(area, commitName + ".bin");
        if (!newCommit.exists()) {
            newCommit.createNewFile();
            Utils.writeObject(newCommit, this);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    /**
     * 删除STAGE_AREA中的blob
     *
     * @param fileNames
     */
    public static void clearStageArea(List<String> fileNames, File clearStage) {
        for (String fileName : fileNames) {
            File deletedFile = Utils.join(clearStage, fileName);
            deletedFile.delete();
        }
    }

    /**
     * 更新Commit的BlobArray
     *
     * @param updateCommit
     * @param previousBlobArray
     * @param fileNames
     * @param command
     * @return
     */
    public static boolean updateBlobArray(Commit updateCommit, List<Blobs> previousBlobArray, List<String> fileNames, String command) {
        boolean equalWithCurrent = true;
        List<Blobs> blobArray = new ArrayList<>();
        List<Blobs> tempBlobArray = new ArrayList<>();
        if (fileNames.size() == 0 && command.equals("STAGE_AREA")) {
//            System.out.println("The Staging area is clean. Will not do any commits.");
            tempBlobArray.addAll(previousBlobArray);
        } else { // 对比新文件和父亲commit指向的blobs是否发生了变化，如果有变化则替换
            if (command.equals("STAGE_AREA")) {
                blobArray = Blobs.returnBlobsList(fileNames, Repository.STAGE_AREA);
            } else if (command.equals("REMOVAL_AREA")) {
                blobArray = Blobs.returnBlobsList(fileNames, Repository.REMOVAL_AREA);
            }
            if (previousBlobArray == null) {
                if (command.equals("STAGE_AREA")) {
                    tempBlobArray.addAll(blobArray);
                }
                equalWithCurrent = false;
            } else {
                tempBlobArray.addAll(previousBlobArray);
                int equalName = 0;
                for (int i = 0; i < previousBlobArray.size(); i++) {
                    for (int j = 0; j < blobArray.size(); j++) {
                        if (blobArray.get(j).getBlobName().equals(previousBlobArray.get(i).getBlobName())) {   // 出现同名文件时
                            String currentID = blobArray.get(j).getBlobID();
                            String previousID = previousBlobArray.get(i).getBlobID();
                            if (!currentID.equals(previousID) && command.equals("STAGE_AREA")) { //当操作缓存区stage_area时
                                tempBlobArray.remove(i);
                                tempBlobArray.add(i, blobArray.get(j));
                                equalWithCurrent = false;
                            }
                            if (command.equals("REMOVAL_AREA")) { //当操作删除区removal_area时，将该文件从blobArray中删除
                                int removeIndex = i - (previousBlobArray.size() - tempBlobArray.size()); // 计算相对索引
                                tempBlobArray.remove(removeIndex);
                                equalWithCurrent = false;
                            }
                            equalName = equalName + 1;
                        }
                    }
                }
                if (equalName != blobArray.size() && command.equals("STAGE_AREA")) { //当没有同名文件且操作暂存区时，新增文件到commit中
                    tempBlobArray.addAll(blobArray);
                    equalWithCurrent = false;
                } else if (equalName != blobArray.size() && command.equals("REMOVAL_AREA")) {
                    equalWithCurrent = true;
                }
            }
        }
        updateCommit.setBlobArray(tempBlobArray);
        return equalWithCurrent;
    }

    /**
     * 根据父亲指针循环获取Commit得到commitList
     *
     * @param currentCommit
     * @return
     */
    public static List<Commit> returnCommitList(Commit currentCommit) {
        List<Commit> commitList = new ArrayList<Commit>();
        Commit commit = currentCommit;
        int commitIdx = 0;
        while (commit != null) {
            commitList.add(commit);
            commitIdx = commitIdx + 1;
            commit = commit.getParent();
        }
        return commitList;
    }

    /**
     * 复制 headCommit 并更改其branchName完成新建一个branch的功能
     *
     * @param branchName
     */
    public Commit newBranch(String branchName) {
        this.branch = branchName;
        return this;
    }
}
