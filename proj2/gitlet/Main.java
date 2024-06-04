package gitlet;


import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


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
    public static void main(String[] args) {
        File headCommit = new File(Repository.HEAD_AREA + "/head.bin");
        Commit currentCommit = null;
        List<String> stageFileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
        List<String> removeFileNames = Utils.plainFilenamesIn(Repository.REMOVAL_AREA);
        if (headCommit.exists()) {
            currentCommit = Utils.readObject(headCommit, Commit.class);
        }
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    boolean init_flag = true;
                    init_flag = Repository.makeSetup();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
                    Date currentTime = new Date(0L);
                    String epochTime = dateFormat.format(currentTime);
                    if (init_flag) {
                        Repository.makeStageArea();
                        Repository.makeCommitArea();
                        Repository.makeHeadArea();
                        Repository.makeRemovalArea();
                        String masterBranch = "master";
                        String commitMessage = "initial commit";
                        Commit firstCommit = null;
                        try {
                            firstCommit = new Commit(masterBranch, commitMessage, epochTime, null, null);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            firstCommit.writeCommit(Repository.HEAD_AREA, "head");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "add":
                    if (!Repository.STAGE_AREA.exists()) {
                        throw new GitletException("STAGE_AREA doesn't exists, please execute 'git init' first.");
                    }
                    String secondArg = args[1];
                    if (secondArg == null) {
                        throw new GitletException("Please enter filename.");
                    } else {
                        String addFileName = String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg));
                        File addFile = new File(addFileName);
                        if (!addFile.exists()) {
                            throw new GitletException(addFile + " does not exist.");
                        } else {
                            try {
                                Blobs.addBlobs(currentCommit, addFileName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
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
                        List<Blobs> previousBlobArray = currentCommit.getBlobArray();
                        List<Blobs> currentCommitBlobArray = new ArrayList<>();
                        String currentBranch = currentCommit.getBranch();
                        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
                        currentTime = new Date();
                        String formatDate = dateFormat.format(currentTime);
                        Commit newCommit = null;
                        try {
                            newCommit = new Commit(currentBranch, secondArg, formatDate, currentCommitBlobArray, currentCommit);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        // TODO: 处理previousBlobArray的数据问题
                        boolean removalEqualWithCurrent = Commit.updateBlobArray(newCommit, previousBlobArray, removeFileNames, "REMOVAL_AREA");
                        boolean stageEqualWithCurrent = Commit.updateBlobArray(newCommit, newCommit.getBlobArray(), stageFileNames, "STAGE_AREA");
                        if (!(stageEqualWithCurrent && removalEqualWithCurrent)) {
//                            Commit newCommit = new Commit(secondArg, previousBlobArray, currentCommit);
                            try {
                                newCommit.writeCommit(Repository.COMMIT_AREA, newCommit.getCommitID()); // 将commit写入COMMIT_AREA
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            headCommit.delete();
                            try {
                                newCommit.writeCommit(Repository.HEAD_AREA, "head");// 头指针指向最新的commit
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                            Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                            throw new GitletException("The commit is same with previous.");
                        }
                        // 清空缓存区
                        Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                        Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                    }
                    break;
                case "rm":
                    // Unstage the file if it is currently staged for addition
                    if (args.length < 2) {
                        throw new GitletException("Please enter removed file name.");
                    } else {
                        secondArg = args[1];
                        List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                        List<Blobs> stageBlobsList = Blobs.returnBlobsList(fileNames, Repository.STAGE_AREA);
                        String[] parts = secondArg.split("/");
                        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                        int lastIndex = realFileName.lastIndexOf('.');
                        String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
                        File stageRemoveFile = Utils.join(Repository.STAGE_AREA, fileNameWithoutExtension + ".bin");
                        File removeFile = Utils.join(Repository.REMOVAL_AREA, fileNameWithoutExtension + ".bin");
                        // 如果STAGE_AREA中有对应的文件则将其删去
                        int rmFlag = -1;
                        for (int i = 0; i < stageBlobsList.size(); i++) {
                            Blobs blob = stageBlobsList.get(i);
                            if (blob.getBlobName().equals(String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg)))) {
                                // 该文件被commit过，标记为删除，在下一次commit时删除
                                stageRemoveFile.delete();
                            }
                        }
                        // 如果currentCommit中有对应的文件则将其放入REMOVE_AREA中下一次删去
                        Blobs removeBlob = null;
                        try {
                            removeBlob = new Blobs(String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        rmFlag = Blobs.trackFiles(currentCommit.getBlobArray(), removeBlob);
                        if (rmFlag == 1) {
                            Utils.writeObject(removeFile, removeBlob);
                            File thisFile = Utils.join(Repository.WORK_STAGE, secondArg);
                            if (thisFile.exists()) {  // 在工作目录下删除文件
                                thisFile.delete();
                            } else {
                                // rm fileName 的 file 即不在STAGE_AREA也不在headCommit中，将报错
                                throw new GitletException("No reason to remove the file.");
                            }
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
                    List<String> branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                    List<String> untrackedFileNames = new ArrayList<>();
                    System.out.println("=== Branches ===");
                    System.out.println("*" + currentCommit.getBranch());    //首先输出当前commit的名称，并带上*作为标识
                    for (String branchFileName : branchFileNames) {
                        File stageFile = new File(Repository.HEAD_AREA + branchFileName);
                        Commit branchCommit = Utils.readObject(stageFile, Commit.class);
                        String branchName = branchCommit.getBranch(); // 获取真正的文件名
                        if (!branchName.equals(currentCommit.getBranch())) {
                            System.out.println(branchName);
                        }
                    }
                    System.out.println();
                    System.out.println("=== Staged Files ===");
                    for (String stageFileName : stageFileNames) {
                        File stageFile = new File(Repository.STAGE_AREA + stageFileName);
                        Blobs blob = Utils.readObject(stageFile, Blobs.class);
                        String[] parts = blob.getBlobName().split("/");
                        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                        System.out.println(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Removed Files ===");
                    for (String removeFileName : removeFileNames) {
                        File removeFile = new File(Repository.REMOVAL_AREA + removeFileName);
                        Blobs blob = Utils.readObject(removeFile, Blobs.class);
                        String[] parts = blob.getBlobName().split("/");
                        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                        System.out.println(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Modifications Not Staged For Commit ===");
                    List<String> workStageFileNames = Utils.plainFilenamesIn(Repository.WORK_STAGE);
                    for (String workStageFile : workStageFileNames) {
                        Blobs blob = null;
                        try {
                            blob = new Blobs(Repository.WORK_STAGE + workStageFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        int modifyFlag = Blobs.trackFiles(currentCommit.getBlobArray(), blob);
                        if (modifyFlag == 2 || modifyFlag == 0) {
                            int lastIndex = workStageFile.lastIndexOf('.');
                            String fileNameWithoutExtension = workStageFile.substring(0, lastIndex);
                            File workStageBin = Utils.join(Repository.STAGE_AREA, fileNameWithoutExtension + ".bin");
                            if (workStageBin.exists()) {
                                Blobs stageBlob = Utils.readObject(workStageBin, Blobs.class);
                                if (!stageBlob.getBlobID().equals(blob.getBlobID())) {
                                    System.out.println(workStageFile);
                                }
                            } else if (modifyFlag == 0) {
                                untrackedFileNames.add(workStageFile);
                            }
                        } else if (modifyFlag == 1) {
                            System.out.println(workStageFile);
                        }
                    }
                    System.out.println();
                    System.out.println("=== Untracked Files ===");
                    for (String untrackedFile : untrackedFileNames) {
                        System.out.println(untrackedFile);
                    }
                    System.out.println();
                    break;
                case "checkout":
                    if (args.length < 2) {
                        throw new GitletException("Please enter correctly.");
                    } else {
                        if (args.length == 2) {
                            secondArg = args[1];
                            // 3. java gitlet.Main checkout [branch name]
                            if (currentCommit.getBranch().equals(secondArg)) {
                                throw new GitletException("No need to checkout the current branch.");
                            } else {
                                branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                                boolean branchExist = false;
                                for (String branchFileName : branchFileNames) {
                                    if (branchFileName.equals(secondArg + ".bin")) {
                                        branchExist = true;
                                        File branchFile = new File(Repository.HEAD_AREA + branchFileName);
                                        Commit branchCommit = Utils.readObject(branchFile, Commit.class);
                                        // 将当前branch写回HEAD_AREA中保留此branch
                                        File currentBranchFile = new File(Repository.HEAD_AREA + currentCommit.getBranch() + ".bin");
                                        if (currentBranchFile.exists()) {
                                            currentBranchFile.delete();
                                        }
                                        try {
                                            currentCommit.writeCommit(Repository.HEAD_AREA, currentCommit.getBranch());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        // 切换到新branch
                                        currentCommit = branchCommit;
                                        headCommit.delete();
                                        try {
                                            currentCommit.writeCommit(Repository.HEAD_AREA, "head");
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                                if (!branchExist) {
                                    throw new GitletException("No such branch exists.");
                                }
                            }
                        } else if (args.length == 3) {
                            // 1. java gitlet.Main checkout -- [file name]
                            String fileName = args[2];
                            try {
                                Checkout.checkoutFile(currentCommit, fileName);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (args.length == 4) {
                            // 2. java gitlet.Main checkout [commit id] -- [file name]
                            String commitID = args[1];
                            String fileName = args[3];
                            boolean resetFlag = false;
                            try {
                                Checkout.checkoutCommitFile(currentCommit, fileName, commitID, resetFlag);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                    break;
                case "branch":
                    if (args.length < 2) {
                        throw new GitletException("Please enter new branch's name.");
                    } else {
                        String branchName = args[1];
                        Commit newBranchHead = currentCommit.newBranch(branchName);
                        try {
                            newBranchHead.writeCommit(Repository.HEAD_AREA, branchName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "rm-branch":
                    if (args.length < 2) {
                        throw new GitletException("Please enter branch's name.");
                    } else {
                        String branchName = args[1];
                        branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                        File branchFile = Checkout.findBranch(currentCommit, branchName, branchFileNames);
                        if (branchFile.exists()) {
                            branchFile.delete();
                        }
                    }
                    break;
                case "reset":
                    if (args.length < 2) {
                        throw new GitletException("Please enter branch's name.");
                    } else {
                        String commitID = args[1];
                        try {
                            currentCommit = Checkout.resetCommitFile(currentCommit, commitID);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //切换头指针
                        headCommit.delete();
                        try {
                            currentCommit.writeCommit(Repository.HEAD_AREA, "head");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case "merge":
                    if (args.length < 2) {
                        throw new GitletException("Please enter branch's name.");
                    } else {
                        String branchName = args[1];
                        branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                        File branchFile = Checkout.findBranch(currentCommit, branchName, branchFileNames);
                        Commit otherCommit = Utils.readObject(branchFile, Commit.class);
                        Commit ancestor = Merge.findSplitAncestor(currentCommit, otherCommit);
                        Set<String> sameNameBlobName = Merge.findSameBlob(currentCommit, otherCommit);
                        List<Blobs> mergeBlobList = null;
                        try {
                            mergeBlobList = Merge.sameBlobListTraversal(sameNameBlobName, ancestor, currentCommit, otherCommit);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String mergeMessage = "Merged " + otherCommit.getBranch() + " into " + currentCommit.getBranch() + ".";
//                        Merged [given branch name] into [current branch name].
                        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
                        currentTime = new Date();
                        String formatDate = dateFormat.format(currentTime);
                        mergeCommit newCommit = null;
                        try {
                            newCommit = new mergeCommit(currentCommit.getBranch(), mergeMessage, formatDate, mergeBlobList, currentCommit.getParent(), otherCommit.getParent());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            newCommit.writeCommit(Repository.COMMIT_AREA, newCommit.getCommitID()); // 将commit写入COMMIT_AREA
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        headCommit.delete();
                        try {
                            newCommit.writeCommit(Repository.HEAD_AREA, "head");// 头指针指向最新的commit
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        // 清空缓存区
                        Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                        Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
}
