package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        // TODO: what if args is empty?
        File headCommit = new File(Repository.HEAD_AREA + "/head.bin");
        Commit currentCommit = null;
        if (headCommit.exists()) {
            currentCommit = Utils.readObject(headCommit, Commit.class);
        }
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
//                    Repository.makeSetup();
//                    Repository.makeStageArea();
//                    Repository.makeCommitArea();
//                    Repository.makeHeadArea();
                    String commitMessage = "This is the first commit!";
                    Commit firstCommit = new Commit(commitMessage, null, null);
                    firstCommit.writeCommit(Repository.HEAD_AREA, "head");
                    break;
                case "add":
                    if (!Repository.STAGE_AREA.exists()) {
                        throw new GitletException("STAGE_AREA doesn't exists, please execute 'git init' first.");
                    }
                    String secondArg = args[1];
                    if (secondArg == null) {
                        throw new GitletException("Please enter filename.");
                    } else {
                        File addFile = new File(Repository.CWD + "/" + secondArg);
                        if (!addFile.exists()) {
                            throw new GitletException(addFile + " does not exist.");
                        } else {
                            String[] parts = secondArg.split("/");
                            String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                            int lastIndex = realFileName.lastIndexOf('.');
                            String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
                            File createFile = new File(Repository.STAGE_AREA + "/" + fileNameWithoutExtension + ".bin");
                            Blobs blobFile = new Blobs(Repository.CWD + secondArg);
                            List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                            Blobs[] blobArray = Blobs.returnBlobsArray(fileNames);
                            Blobs isExisted = blobFile.equals(blobArray);
                            if (isExisted == null) {    // 文件不存在于暂存区
                                Utils.writeObject(createFile, blobFile);
                            } else {    // 文件存在于暂存区
                                //删除此文件
                                createFile.delete();
                            }
                        }
                    }
                    break;
                case "commit":
                    if (!Repository.STAGE_AREA.exists()) {
                        throw new GitletException("STAGE_AREA doesn't exists, please execute 'git init' first.");
                    }
                    if (!Repository.COMMIT_AREA.exists()) {
                        throw new GitletException("COMMIT_AREA doesn't exists, please execute 'git init' first.");
                    }
                    if (args.length < 2) {
                        throw new GitletException("Please enter message.");
                    } else {
                        secondArg = args[1];
                        boolean equalWithCurrent = true;
                        List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                        if (fileNames.size() == 0) {
                            System.out.println("The Staging area is clean. Will not do any commits.");
                        } else { // 对比新文件和父亲commit指向的blobs是否发生了变化，如果有变化则替换
                            Blobs[] blobArray = Blobs.returnBlobsArray(fileNames);
                            Blobs[] previousBlobArray = currentCommit.getBlobArray();
                            if (previousBlobArray == null) {
                                previousBlobArray = blobArray;
                                equalWithCurrent = false;
                            } else {
                                for (int i = 0; i < blobArray.length; i++) {
                                    boolean equalName = false;
                                    for (int j = 0; j < previousBlobArray.length; j++) {
                                        if (blobArray[i].getBlobName().equals(previousBlobArray[j].getBlobName())) {
                                            if (!blobArray[i].getBlobID().equals(previousBlobArray[j].getBlobID())) {
                                                previousBlobArray[j] = blobArray[i];
                                                equalWithCurrent = false;
                                            }
                                            equalName = true;
                                        }
                                    }
                                    if (!equalName) { //当没有同名文件时，新增文件到commit中
                                        Blobs[] tempBlobArray = new Blobs[previousBlobArray.length + 1];
                                        for (int k = 0; k < previousBlobArray.length; k++) {
                                            tempBlobArray[k] = previousBlobArray[k];
                                        }
                                        tempBlobArray[previousBlobArray.length] = blobArray[i];
                                        previousBlobArray = tempBlobArray;
                                        equalWithCurrent = false;
                                    }
                                }
                            }
                            if (!equalWithCurrent) {
                                Commit newCommit = new Commit(secondArg, previousBlobArray, currentCommit);
                                newCommit.writeCommit(Repository.COMMIT_AREA, newCommit.getCommitID()); // 将commit写入COMMIT_AREA
                                headCommit.delete();
                                newCommit.writeCommit(Repository.HEAD_AREA, "head");// 头指针指向最新的commit
                            } else {
                                Commit.clearStageArea(fileNames);
                                throw new GitletException("The commit is same with previous.");
                            }
                            // 清空缓存区
                            Commit.clearStageArea(fileNames);
                        }
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
}
