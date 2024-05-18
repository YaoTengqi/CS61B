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
//                    Repository.makeRemovalArea();
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
                        File addFile = new File(Repository.CWD + secondArg);
                        if (!addFile.exists()) {
                            throw new GitletException(addFile + " does not exist.");
                        } else {
//                            String[] parts = secondArg.split("/");
//                            String realFileName = parts[parts.length - 1]; // 获取真正的文件名
//                            int lastIndex = realFileName.lastIndexOf('.');
//                            String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
//                            File createFile = new File(Repository.STAGE_AREA + "/" + fileNameWithoutExtension + ".bin");
//                            Blobs blobFile = new Blobs(Repository.CWD + secondArg);
//                            List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
//                            Blobs[] blobArray = Blobs.returnBlobsArray(fileNames);
//                            Blobs isExisted = blobFile.equals(blobArray);
//                            if (isExisted == null) {    // 文件不存在于暂存区
//                                Utils.writeObject(createFile, blobFile);
//                            } else {    // 文件存在于暂存区
//                                //删除此文件
//                                createFile.delete();
//                            }
                            Blobs.deleteStageFile(secondArg, "add");
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

                        List<String> stageFileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                        List<String> removeFileNames = Utils.plainFilenamesIn(Repository.REMOVAL_AREA);
                        Blobs[] previousBlobArray = currentCommit.getBlobArray();
                        Commit newCommit = new Commit(secondArg, previousBlobArray, currentCommit);

                        // 缓冲区文件操作
//                        if (stageFileNames.size() == 0) {
//                            System.out.println("The Staging area is clean. Will not do any commits.");
//                        } else { // 对比新文件和父亲commit指向的blobs是否发生了变化，如果有变化则替换
//                            Blobs[] blobArray = Blobs.returnBlobsArray(stageFileNames);
//                            boolean equalWithCurrent = true;
//                            if (previousBlobArray == null) {
//                                previousBlobArray = blobArray;
//                                equalWithCurrent = false;
//                            } else {
//                                for (int i = 0; i < blobArray.length; i++) {
//                                    boolean equalName = false;
//                                    for (int j = 0; j < previousBlobArray.length; j++) {
//                                        if (blobArray[i].getBlobName().equals(previousBlobArray[j].getBlobName())) {
//                                            if (!blobArray[i].getBlobID().equals(previousBlobArray[j].getBlobID())) {
//                                                previousBlobArray[j] = blobArray[i];
//                                                equalWithCurrent = false;
//                                            }
//                                            equalName = true;
//                                        }
//                                    }
//                                    if (!equalName) { //当没有同名文件时，新增文件到commit中
//                                        Blobs[] tempBlobArray = new Blobs[previousBlobArray.length + 1];
//                                        for (int k = 0; k < previousBlobArray.length; k++) {
//                                            tempBlobArray[k] = previousBlobArray[k];
//                                        }
//                                        tempBlobArray[previousBlobArray.length] = blobArray[i];
//                                        previousBlobArray = tempBlobArray;
//                                        equalWithCurrent = false;
//                                    }
//                                }
//                            }
                        // TODO: 处理previousBlobArray的数据问题
                        boolean removalEqualWithCurrent = Commit.updateBlobArray(newCommit, previousBlobArray, removeFileNames, "REMOVAL_AREA");
                        boolean stageEqualWithCurrent = Commit.updateBlobArray(newCommit, previousBlobArray, stageFileNames, "STAGE_AREA");
                        if (!(stageEqualWithCurrent && removalEqualWithCurrent)) {
//                            Commit newCommit = new Commit(secondArg, previousBlobArray, currentCommit);
                            newCommit.writeCommit(Repository.COMMIT_AREA, newCommit.getCommitID()); // 将commit写入COMMIT_AREA
                            headCommit.delete();
                            newCommit.writeCommit(Repository.HEAD_AREA, "head");// 头指针指向最新的commit
                        } else {
                            Commit.clearStageArea(stageFileNames);
                            Commit.clearStageArea(removeFileNames);
                            throw new GitletException("The commit is same with previous.");
                        }
                        // 清空缓存区
                        Commit.clearStageArea(stageFileNames);
                        Commit.clearStageArea(removeFileNames);
                    }
                    break;
                case "rm":
                    // Unstage the file if it is currently staged for addition
                    if (args.length < 2) {
                        throw new GitletException("Please enter removed file name.");
                    } else {
                        secondArg = args[1];
                        List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                        Blobs[] blobArray = Blobs.returnBlobsArray(fileNames, Repository.STAGE_AREA);
                        boolean rmFlag = false;
                        for (Blobs blob : blobArray) {
                            if (blob.getBlobName().equals(Repository.CWD + secondArg)) {
                                // 该文件被commit过，标记为删除，在下一次commit时删除
                                String[] parts = secondArg.split("/");
                                String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                                int lastIndex = realFileName.lastIndexOf('.');
                                String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
                                File removeFile = Utils.join(Repository.REMOVAL_AREA, fileNameWithoutExtension + ".bin");
                                Utils.writeObject(removeFile, blob);
                                rmFlag = true;
                            }
                        }
                        if (rmFlag) { // rm fileName 的 file 即不在STAGE_AREA也不在headCommit中，将报错
                            Blobs.deleteStageFile(secondArg, "rm");
                        }
                        File thisFile = Utils.join(Repository.CWD, secondArg);
                        if (thisFile.exists() && rmFlag) {  // 在工作目录下删除文件
                            thisFile.delete();
                        } else {
                            throw new GitletException("No reason to remove the file.");
                        }
                    }
                    break;
                case "log":
                    Commit logCommit = currentCommit;
                    while (logCommit != null) {
                        System.out.println("===");
                        System.out.println("commit " + logCommit.getCommitID());
                        System.out.println("Date: " + logCommit.getTime());
                        System.out.println(logCommit.getMessage());
                        System.out.println();
                        logCommit = logCommit.getParent();
                    }
                    break;
                case "global-log":
                    logCommit = currentCommit;
                    while (logCommit != null) {
                        System.out.println("===");
                        System.out.println("commit " + logCommit.getCommitID());
                        System.out.println("Date: " + logCommit.getTime());
                        System.out.println(logCommit.getMessage());
                        Blobs[] previousBlobArray = logCommit.getBlobArray();
                        if (previousBlobArray != null) {
                            for (Blobs blob : previousBlobArray) {
                                System.out.println("Blobs: " + blob.getBlobID() + " " + blob.getBlobName());
                            }
                        }
                        System.out.println();
                        logCommit = logCommit.getParent();
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
}
