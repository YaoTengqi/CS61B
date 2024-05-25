package gitlet;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


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
                        List<Blobs> previousBlobArray = currentCommit.getBlobArray();
                        List<Blobs> currentCommitBlobArray = new ArrayList<>();
                        Commit newCommit = new Commit(secondArg, currentCommitBlobArray, currentCommit);
                        // TODO: 处理previousBlobArray的数据问题
                        boolean stageEqualWithCurrent = Commit.updateBlobArray(newCommit, previousBlobArray, stageFileNames, "STAGE_AREA");
                        boolean removalEqualWithCurrent = Commit.updateBlobArray(newCommit, newCommit.getBlobArray(), removeFileNames, "REMOVAL_AREA");
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
                        List<Blobs> blobsList = Blobs.returnBlobsList(fileNames, Repository.STAGE_AREA);
                        boolean rmFlag = false;
                        for (int i = 0; i < blobsList.size(); i++) {
                            Blobs blob = blobsList.get(i);
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
                    List<Commit> logCommitList = Commit.returnCommitList(currentCommit);
                    for (int i = 0; i < logCommitList.size(); i++) {
                        Commit logCommit = logCommitList.get(i);
                        System.out.println("===");
                        System.out.println("commit " + logCommit.getCommitID());
                        System.out.println("Date: " + logCommit.getTime());
                        System.out.println(logCommit.getMessage());
                        System.out.println();
                    }
                    break;
                case "global-log":
                    List<Commit> globalLogCommitList = Commit.returnCommitList(currentCommit);
                    for (int i = 0; i < globalLogCommitList.size(); i++) {
                        Commit globalLogCommit = globalLogCommitList.get(i);
                        System.out.println("===");
                        System.out.println("commit " + globalLogCommit.getCommitID());
                        System.out.println("Date: " + globalLogCommit.getTime());
                        System.out.println(globalLogCommit.getMessage());
                        List<Blobs> previousBlobArray = globalLogCommit.getBlobArray();
                        if (previousBlobArray != null) {
                            for (Blobs blob : previousBlobArray) {
                                System.out.println("Blobs: " + blob.getBlobID() + " " + blob.getBlobName());
                            }
                        }
                        System.out.println();
                    }
                    break;
                case "find":
                    List<Commit> findCommitList = Commit.returnCommitList(currentCommit);
                    boolean findFlag = false;
                    if (args.length < 2) {
                        throw new GitletException("Please enter removed file name.");
                    } else {
                        secondArg = args[1];
                        for (int i = 0; i < findCommitList.size(); i++) {
                            Commit findLogCommit = findCommitList.get(i);
                            String message = findLogCommit.getMessage();
                            if (message.contains(secondArg)) {
                                System.out.println(findLogCommit.getCommitID());
                                findFlag = true;
                            }
                        }
                    }
                    if (!findFlag) {
                        throw new GitletException("Found no commit with that message.");
                    }
                    break;
                case "status":
                    List<String> stageFileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                    List<String> removeFileNames = Utils.plainFilenamesIn(Repository.REMOVAL_AREA);
                    System.out.println("=== Branches ===");
                    System.out.println();
                    System.out.println("=== Staged Files ===");
                    for (String stageFileName : stageFileNames) {
//                        System.out.println(stageFileName);
                        File stageFile = new File(Repository.STAGE_AREA + "/" + stageFileName);
                        Blobs blob = Utils.readObject(stageFile, Blobs.class);
                        String[] parts = blob.getBlobName().split("/");
                        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                        System.out.println(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Removed Files ===");
                    for (String removeFileName : removeFileNames) {
                        File removeFile = new File(Repository.REMOVAL_AREA + "/" + removeFileName);
                        Blobs blob = Utils.readObject(removeFile, Blobs.class);
                        String[] parts = blob.getBlobName().split("/");
                        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                        System.out.println(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Modifications Not Staged For Commit ===");
                    System.out.println();
                    System.out.println("=== Untracked Files ===");
                    System.out.println();
                    break;
                case "checkout":
                    if (args.length < 2) {
                        throw new GitletException("Please enter correctly.");
                    } else {
                        if (args.length == 2) {
                            secondArg = args[1];
                            // 1. java gitlet.Main checkout -- [file name]
                            Checkout.checkoutFile(currentCommit, secondArg);
                        } else if (args.length == 3) {
                            // 2. java gitlet.Main checkout [commit id] -- [file name]
                            String commitID = args[1];
                            String fileName = args[2];
                            Checkout.checkoutCommitFile(currentCommit, fileName, commitID);
                        }
                        // 3. java gitlet.Main checkout [branch name]
                    }
                    break;
                default:

                    System.out.println("No command with that name exists.");
            }
        }
    }
}
