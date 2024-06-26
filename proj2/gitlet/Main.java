package gitlet;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author YaoTengqi
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        File headCommit = new File(Repository.HEAD_AREA + "/head.bin");
        Commit currentCommit = null;
        mergeCommit mergeCurrentCommit = null;
        List<String> stageFileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
        List<String> workStageFileNames = Utils.plainFilenamesIn(Repository.WORK_STAGE);
        List<String> removeFileNames = Utils.plainFilenamesIn(Repository.REMOVAL_AREA);
        boolean isUntracked = false;
        boolean fileExists = false;
        if (headCommit.exists()) {
            currentCommit = Utils.readObject(headCommit, Commit.class);
            if (currentCommit instanceof mergeCommit) {
                mergeCurrentCommit = Utils.readObject(headCommit, mergeCommit.class);
            } else {
                try {
                    mergeCurrentCommit = new mergeCommit(currentCommit.getBranch(), currentCommit.getMessage(), currentCommit.getTime(), currentCommit.getBlobArray(), currentCommit.getParent(), null);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (!headCommit.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
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
//                            throw new GitletException(addFile + " does not exist.");
                            System.out.println("File does not exist.");
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
                    if (args.length < 2 || args[1].equals("")) {
                        System.out.println("Please enter a commit message.");
                    } else if (stageFileNames.size() == 0 && removeFileNames.size() == 0) {
                        System.out.println("No changes added to the commit.");
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
                        boolean removalEqualWithCurrent = Commit.updateBlobArray(newCommit, previousBlobArray, removeFileNames, "REMOVAL_AREA");
                        boolean stageEqualWithCurrent = Commit.updateBlobArray(newCommit, newCommit.getBlobArray(), stageFileNames, "STAGE_AREA");
                        // 更新blobArray后需要更新ID
                        try {
                            newCommit = new Commit(newCommit.getBranch(), newCommit.getMessage(), newCommit.getTime(), newCommit.getBlobArray(), newCommit.getParent());
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
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
                        String fileNameWithoutExtension = getNoExtensionName(secondArg);
                        File stageRemoveFile = Utils.join(Repository.STAGE_AREA, fileNameWithoutExtension + ".bin");
                        File removeFile = Utils.join(Repository.REMOVAL_AREA, fileNameWithoutExtension + ".bin");
                        // 如果STAGE_AREA中有对应的文件则将其删去
                        int rmFlag = -1;
                        boolean existInStage = false;
                        for (int i = 0; i < stageBlobsList.size(); i++) {
                            Blobs blob = stageBlobsList.get(i);
                            if (blob.getBlobName().equals(String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg)))) {
                                // 该文件被commit过，标记为删除，在下一次commit时删除
                                stageRemoveFile.delete();
                            }
                            existInStage = true;
                        }
                        // 如果currentCommit中有对应的文件则将其放入REMOVE_AREA中下一次删去
                        Blobs removeBlob = new Blobs(String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg)));
                        String removeID = removeBlob.getBlobID();
                        if (removeID == null) {
                            //文件不存在于WORK_STAGE，因此要在上一次commit中进行查找
                            List<Blobs> currentBlobs = currentCommit.getBlobArray();
                            for (Blobs blob : currentBlobs) {
                                String blobName = blob.getBlobName();
                                String removePath = String.valueOf(Utils.join(Repository.WORK_STAGE, secondArg));
                                if (blobName.equals(removePath)) {
                                    removeBlob = blob;
                                    rmFlag = 3; // 存在于上一次commit但不存在于当前WORK_STAGE
                                }
                            }
                        } else {
                            rmFlag = Blobs.trackFiles(currentCommit.getBlobArray(), removeBlob);
                        }
                        if (rmFlag == 0) {
                            // 不存在于上一次commit不存在于当前WORK_STAGE
                            if (!existInStage) {
                                System.out.println("No reason to remove the file.");
                            }
                        } else if (rmFlag == 1 || rmFlag == 2) {
                            Utils.writeObject(removeFile, removeBlob);
                            File thisFile = Utils.join(Repository.WORK_STAGE, secondArg);
                            if (thisFile.exists()) {  // 在工作目录下删除文件
                                thisFile.delete();
                            }
                        } else if (rmFlag == 3) {
                            Utils.writeObject(removeFile, removeBlob);  //添加到REMOVAL_AREA中
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
                    List<String> globalLogFiles = Utils.plainFilenamesIn(Repository.COMMIT_AREA);
                    for (String globalFileName : globalLogFiles) {
                        File commitFile = new File(String.valueOf(Utils.join(Repository.COMMIT_AREA, globalFileName)));
                        Commit globalLogCommit = Utils.readObject(commitFile, Commit.class);
                        System.out.println("===");
                        System.out.println("commit " + globalLogCommit.getCommitID());
                        System.out.println("Date: " + globalLogCommit.getTime());
                        System.out.println(globalLogCommit.getMessage());
                        System.out.println();
                    }
                    //打印initial commit
                    printInitialCommit();
                    break;
                case "find":
                    //要从全部的commit中寻找，不能从头结点开始找
                    globalLogFiles = Utils.plainFilenamesIn(Repository.COMMIT_AREA);
//                    List<Commit> findCommitList = Commit.returnCommitList(currentCommit);
                    boolean findFlag = false;
                    if (args.length < 2) {
                        throw new GitletException("Please enter removed file name.");
                    } else {
                        secondArg = args[1];
                        // 首先查找initial commit
                        if ("initial commit".contains(secondArg)) {
                            System.out.println("d87aa6d88d9b64a08e646e9763ca97e9d2728ef2");
                            findFlag = true;
                        } else {
                            for (String globalFileName : globalLogFiles) {
                                File commitFile = new File(String.valueOf(Utils.join(Repository.COMMIT_AREA, globalFileName)));
                                Commit globalLogCommit = Utils.readObject(commitFile, Commit.class);
                                String message = globalLogCommit.getMessage();
                                if (message.contains(secondArg)) {
                                    System.out.println(globalLogCommit.getCommitID());
                                    findFlag = true;
                                }
                            }
                        }
                    }
                    if (!findFlag) {
                        System.out.println("Found no commit with that message.");
                    }
                    break;
                case "status":
                    List<String> branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                    List<String> untrackedFileNames = new ArrayList<>();
                    Set<String> removeFiles = new HashSet<>();
                    System.out.println("=== Branches ===");
                    System.out.println("*" + currentCommit.getBranch());    //首先输出当前commit的名称，并带上*作为标识
                    for (String branchFileName : branchFileNames) {
                        File stageFile = new File(String.valueOf(Utils.join(Repository.HEAD_AREA, branchFileName)));
                        Commit branchCommit = Utils.readObject(stageFile, Commit.class);
                        String branchName = branchCommit.getBranch(); // 获取真正的文件名
                        if (!branchName.equals(currentCommit.getBranch())) {
                            System.out.println(branchName);
                        }
                    }
                    System.out.println();
                    System.out.println("=== Staged Files ===");
                    for (String stageFileName : stageFileNames) {
                        File stageFile = new File(String.valueOf(Utils.join(Repository.STAGE_AREA, stageFileName)));
                        Blobs blob = Utils.readObject(stageFile, Blobs.class);
                        String realFileName = getRealName(blob.getBlobName());
                        System.out.println(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Removed Files ===");
                    for (String removeFileName : removeFileNames) {
                        File removeFile = new File(String.valueOf(Utils.join(Repository.REMOVAL_AREA, removeFileName)));
                        Blobs blob = Utils.readObject(removeFile, Blobs.class);
                        String realFileName = getRealName(blob.getBlobName());
                        System.out.println(realFileName);
                        removeFiles.add(realFileName);
                    }
                    System.out.println();
                    System.out.println("=== Modifications Not Staged For Commit ===");
                    // 查看发生改变(modified)的文件
                    for (String workStageFile : workStageFileNames) {
                        String fileName = String.valueOf(Utils.join(Repository.WORK_STAGE, workStageFile));
                        Blobs blob = new Blobs(fileName);
                        int modifyFlag = Blobs.trackFiles(currentCommit.getBlobArray(), blob);
                        if (modifyFlag == 2 || modifyFlag == 0) {
                            int lastIndex = workStageFile.lastIndexOf('.');
                            String fileNameWithoutExtension = workStageFile.substring(0, lastIndex);
                            File workStageBin = Utils.join(Repository.STAGE_AREA, fileNameWithoutExtension + ".bin");
                            if (workStageBin.exists()) {
                                Blobs stageBlob = Utils.readObject(workStageBin, Blobs.class);
                                if (!stageBlob.getBlobID().equals(blob.getBlobID())) {
                                    System.out.println(workStageFile + "(modified)");
                                }
                            } else if (modifyFlag == 0) {
                                untrackedFileNames.add(workStageFile);
                            }
                        } else if (modifyFlag == 1) {
                            System.out.println(workStageFile + "(modified)");
                        }
                    }
                    //查看被删除的文件(currentCommit的blobArray中有但workStage中没有的)
                    List<Blobs> currentBlobs = currentCommit.getBlobArray();
                    workStageFileNames = Utils.plainFilenamesIn(Repository.WORK_STAGE);
                    if (currentBlobs != null) {
                        for (Blobs blobs : currentBlobs) {
                            boolean fileExist = false;
                            String blobName = blobs.getBlobName();
                            String realFileName = getRealName(blobName);
                            for (String workStageFile : workStageFileNames) {
                                if (realFileName.equals(workStageFile)) {
                                    fileExist = true;
                                    break;
                                }
                            }
                            if (!fileExist && !removeFiles.contains(realFileName)) {
                                System.out.println(realFileName + "(deleted)");
                            }
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
                                System.out.println("No need to checkout the current branch.");
                            } else {

                                // 3.1 检查untracked file
                                for (String workFile : workStageFileNames) {
                                    try {
                                        isUntracked = Checkout.checkUntracked(currentCommit, workFile);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
                                boolean branchExist = false;
                                if (!isUntracked) {
                                    // 3.2 找到要切换的branch名称
                                    for (String branchFileName : branchFileNames) {
                                        if (branchFileName.equals(secondArg + ".bin")) {
                                            branchExist = true;
                                            File branchFile = new File(String.valueOf(Utils.join(Repository.HEAD_AREA, branchFileName)));
                                            Commit branchCommit = Utils.readObject(branchFile, Commit.class);
                                            // 将当前branch写回HEAD_AREA中保留此branch
                                            File currentBranchFile = new File(String.valueOf(Utils.join(Repository.HEAD_AREA, currentCommit.getBranch() + ".bin")));
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
                                        System.out.println("No such branch exists.");
                                    } else {
                                        // 3.3 切换文件版本
                                        for (String workFile : workStageFileNames) {
                                            try {
                                                fileExists = Checkout.checkoutFile(currentCommit, workFile, true);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            if (!fileExists) {
                                                // 当workStage中的文件在other branch中文件不存在时将其删除
                                                File removeWorkFile = new File(String.valueOf(Utils.join(Repository.WORK_STAGE, workFile)));
                                                removeWorkFile.delete();
                                            }

                                        }
                                        // 3.4 添加当前WORK_AREA不存在但存在于other branch的文件
                                        List<Blobs> currentBlobList = currentCommit.getBlobArray();
                                        if (currentBlobList != null) {
                                            for (Blobs currentBlob : currentBlobList) {
                                                String currentName = currentBlob.getBlobName();
                                                String realFileName = getRealName(currentName);
                                                if (!workStageFileNames.contains(realFileName)) {
                                                    try {
                                                        Checkout.checkoutFile(currentCommit, realFileName, true);
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                            }
                                        }
                                        // 3.5 清空缓存区
                                        Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                                        Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                                    }
                                } else {
                                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                                }
                            }
                        } else if (args.length == 3) {
                            // 1. java gitlet.Main checkout -- [file name]
                            String fileName = args[2];
                            try {
                                boolean checkFlag = false;
                                Checkout.checkoutFile(currentCommit, fileName, checkFlag);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (args.length == 4) {
                            // 2. java gitlet.Main checkout [commit id] -- [file name]
                            String commitID = args[1];
                            String operands = args[2];
                            String fileName = args[3];
                            boolean resetFlag = false;
                            if (!operands.equals("--")) {
                                System.out.println("Incorrect operands.");
                            } else {
                                try {
                                    Checkout.checkoutCommitFile(currentCommit, fileName, commitID, resetFlag);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
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
                        File branchFile = Checkout.findBranch(currentCommit, branchName);
                        if (branchFile != null && branchFile.exists()) {
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
                        // 检查untracked file
                        for (String workFile : workStageFileNames) {
                            try {
                                isUntracked = Checkout.checkUntracked(mergeCurrentCommit, workFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (!isUntracked) {
                            //切换头指针
                            headCommit.delete();
                            try {
                                currentCommit.writeCommit(Repository.HEAD_AREA, "head");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            stageFileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
                            removeFileNames = Utils.plainFilenamesIn(Repository.REMOVAL_AREA);
                            Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                            Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                        } else {
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        }
                        // 覆盖当前工作区的文件
                        List<Blobs> coverBlobs = currentCommit.getBlobArray();
                        coverWorkStageFile(coverBlobs);
                    }
                    break;
                case "merge":
                    if (args.length < 2) {
                        throw new GitletException("Please enter branch's name.");
                    } else {
                        String branchName = args[1];
                        if (currentCommit.getBranch().equals(branchName)) {
                            System.out.println("Cannot merge a branch with itself.");
                        } else if (stageFileNames.size() != 0) {
                            System.out.println("You have uncommitted changes.");
                        } else {
                            File branchFile = Checkout.findBranch(mergeCurrentCommit, branchName);

                            if (branchFile != null) {
                                // 检查untracked file
                                for (String workFile : workStageFileNames) {
                                    try {
                                        isUntracked = Checkout.checkUntracked(mergeCurrentCommit, workFile);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if (!isUntracked) {
                                    Commit otherCommit = Utils.readObject(branchFile, Commit.class);
                                    mergeCommit mergeOtherCommit = null;
                                    if (otherCommit instanceof mergeCommit) {
                                        mergeOtherCommit = Utils.readObject(branchFile, mergeCommit.class);
                                    } else {
                                        try {
                                            mergeOtherCommit = new mergeCommit(otherCommit.getBranch(), otherCommit.getMessage(), otherCommit.getTime(), otherCommit.getBlobArray(), otherCommit.getParent(), null);
                                        } catch (NoSuchAlgorithmException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    List<Blobs> mergeBlobList = null;
                                    List<Blobs> deleteBlobList = null;
                                    String mergeMessage = "Merged " + otherCommit.getBranch() + " into " + currentCommit.getBranch() + ".";
                                    dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
                                    currentTime = new Date();
                                    String formatDate = dateFormat.format(currentTime);
                                    mergeCommit newCommit = null;
                                    boolean ancestorOfCurrent = false;
                                    // 1. 寻找祖先节点
                                    Commit ancestor = Merge.findSplitAncestor(mergeCurrentCommit, mergeOtherCommit);
                                    // 2. 判断特殊情况
                                    if (ancestor.getCommitID().equals(mergeOtherCommit.getCommitID())) {
                                        System.out.println("Given branch is an ancestor of the current branch.");
                                        ancestorOfCurrent = true;
                                    } else {
                                        // 3. 寻找同名文件
                                        Set<String> sameNameBlobName = Merge.findSameBlob(currentCommit, otherCommit);
                                        try {
                                            // 4. 算出合并后的blobList
                                            mergeBlobList = Merge.sameBlobListTraversal(sameNameBlobName, ancestor, currentCommit, otherCommit);
                                            // 5. 算出要删除的blobList
                                            deleteBlobList = Merge.findDeleteBlobs(ancestor, mergeBlobList);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        // Merged [given branch name] into [current branch name].
                                        try {
                                            newCommit = new mergeCommit(currentCommit.getBranch(), mergeMessage, formatDate, mergeBlobList, currentCommit, otherCommit);
                                        } catch (NoSuchAlgorithmException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    if (!ancestorOfCurrent) {
                                        // 7. 写新的commit并更改头指针
                                        try {
                                            newCommit.writeCommit(Repository.COMMIT_AREA, newCommit.getCommitID()); // 将commit写入COMMIT_AREA
                                            headCommit.delete();
                                            newCommit.writeCommit(Repository.HEAD_AREA, "head");// 头指针指向最新的commit
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        // 8. 在工作区删除deleteBlobsList中的文件
                                        if (deleteBlobList != null) {
                                            for (Blobs deleteBlob : deleteBlobList) {
                                                String deleteName = deleteBlob.getBlobName();
                                                File deleteFile = new File(deleteName);
                                                deleteFile.delete();
                                            }
                                        }
                                        // 9. 在工作区添加mergeList中的文件
                                        coverWorkStageFile(mergeBlobList);
                                        // 10. 清空缓存区
                                        Commit.clearStageArea(stageFileNames, Repository.STAGE_AREA);
                                        Commit.clearStageArea(removeFileNames, Repository.REMOVAL_AREA);
                                        if (ancestor.getCommitID().equals(mergeCurrentCommit.getCommitID())) {
                                            System.out.println("Current branch fast-forwarded.");
                                        }
                                    }
                                } else {
                                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                                }
                            }
                        }
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }

    public static void printInitialCommit() {
        //打印initial commit
        System.out.println("===");
        System.out.println("commit " + "d87aa6d88d9b64a08e646e9763ca97e9d2728ef2");
        System.out.println("Date: " + "Wed Dec 31 18:00:00 1969 -0600");
        System.out.println("initial commit");
        System.out.println();
    }

    /**
     * reset或者merge时将新的file覆盖当前workStage的同名File
     */
    public static void coverWorkStageFile(List<Blobs> coverBlobs) {
        if (coverBlobs != null) {
            for (Blobs coverBlob : coverBlobs) {
                String resetName = coverBlob.getBlobName();
                File addFile = new File(resetName);
                if (addFile.exists()) {
                    addFile.delete();
                }
                String content = new String(coverBlob.getContent());
                Utils.writeContents(addFile, content);
            }
        }
    }

    /**
     * 返回真正的文件名(去除绝对路径)
     *
     * @param path
     * @return
     */
    public static String getRealName(String path) {
        String[] parts = path.split("/");
        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
        return realFileName;
    }

    public static String getNoExtensionName(String path) {
        String realFileName = getRealName(path);
        int lastIndex = realFileName.lastIndexOf('.');
        String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
        return fileNameWithoutExtension;
    }

}


