package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Checkout {
    static boolean commitExistsLock = false;

    /**
     * 检查fileName是否为未被追踪的文件
     *
     * @param currentCommit
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean checkUntracked(Commit currentCommit, String fileName) throws IOException {
        List<String> workStageFileNames = Utils.plainFilenamesIn(Repository.WORK_STAGE);
        List<String> untrackedFileNames = new ArrayList<>();
        for (String workStageFile : workStageFileNames) {
            Blobs blob = new Blobs(Repository.WORK_STAGE + "/" + workStageFile);
            int modifyFlag = Blobs.trackFiles(currentCommit.getBlobArray(), blob);
            if (modifyFlag == 0) {
                untrackedFileNames.add(workStageFile);
            }
        }
        for (String untrackedFileName : untrackedFileNames) {
            if (fileName.equals(untrackedFileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 回退到当前current的file版本
     *
     * @param currentCommit
     * @param fileName
     * @return
     * @throws IOException
     */
    public static boolean checkoutFile(Commit currentCommit, String fileName, boolean resetFlag) throws IOException {
        // 1. java gitlet.Main checkout -- [file name]
        List<Blobs> previousBlobArray = currentCommit.getBlobArray();
        String checkoutFileName = String.valueOf(Utils.join(Repository.WORK_STAGE, fileName));
        boolean fileExists = false;
        if (previousBlobArray == null && !resetFlag) {
            System.out.println("File does not exist in that commit.");
        } else if (previousBlobArray != null) {
            for (Blobs blob : previousBlobArray) {
                if (checkoutFileName.equals(blob.getBlobName())) {
                    fileExists = true;
                    //操作 local stage的文件
                    File workStageFile = new File(checkoutFileName);
                    if (workStageFile.exists()) {
                        workStageFile.delete();
                    }
                    workStageFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(workStageFile);
                    fos.write(blob.getContent());
                }
            }
            if (!fileExists && !resetFlag) { //文件不存在
                System.out.println("File does not exist in that commit.");
            }
        }
        return fileExists;
    }

    /**
     * 回退到特定commit版本(通过commitID查询)的指定file
     *
     * @param currentCommit 当前的commit
     * @param fileName      要切换的文件名
     * @param commitID      要回退的commit版本ID，用于寻找相应的commit
     * @throws IOException
     */
    public static Commit checkoutCommitFile(Commit currentCommit, String fileName, String commitID, boolean resetFlag) throws IOException {
        // 2. java gitlet.Main checkout [commit id] -- [file name]
        // 需要从全部的commit中寻找，不能从当前commit中找
//        List<Commit> commitList = Commit.returnCommitList(currentCommit);
        List<String> commitNames = Utils.plainFilenamesIn(Repository.COMMIT_AREA);
        List<Commit> commitList = new ArrayList<>();
        for (String commitName : commitNames) {
            File commitFile = new File(String.valueOf(Utils.join(Repository.COMMIT_AREA, commitName)));
            Commit newCommit = Utils.readObject(commitFile, Commit.class);
            commitList.add(newCommit);
        }

        boolean commitExists = false;
        boolean fileExists = false;
        for (int i = 0; i < commitList.size(); i++) {
            Commit commit = commitList.get(i);
            String shortCommitID = commit.getCommitID().substring(0, 6);
            String shortCheckoutID = commitID.substring(0, 6);
            if (shortCommitID.equals(shortCheckoutID)) {
                commitExists = true;
                //检查是否为UntrackedFile
                boolean isUntracked = checkUntracked(currentCommit, fileName);
                if (isUntracked && !resetFlag) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                }
                fileExists = checkoutFile(commit, fileName, resetFlag);
                if (resetFlag) {
                    currentCommit = commit; //如果为reset指令则切换头指针
                }
                break;
            }
        }
        if (!commitExists && !commitExistsLock) {
            System.out.println("No commit with that id exists.");
            commitExistsLock = true;
        }
        if (!fileExists) {
            File workFile = new File(Repository.WORK_STAGE + fileName);
            workFile.delete();
        }
        return currentCommit;
    }

    /**
     * 完成reset操作：退回到特殊commit版本的文件内容，并删除未追踪的文件
     *
     * @param currentCommit
     * @param commitID
     * @throws IOException
     */
    public static Commit resetCommitFile(Commit currentCommit, String commitID) throws IOException {
        List<String> workFileNames = Utils.plainFilenamesIn(Repository.WORK_STAGE);
        boolean resetFlag = true;
        for (String workFileName : workFileNames) {
            currentCommit = checkoutCommitFile(currentCommit, "/" + workFileName, commitID, resetFlag);
        }
        return currentCommit;
    }

    /**
     * 查找该branch名的分支是否存在
     *
     * @param currentCommit
     * @param branchName
     * @return branchFile
     */
    public static File findBranch(Commit currentCommit, String branchName) {
        boolean branchExist = false;
        File branchFile = null;
        if (currentCommit.getBranch().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return null;
        }
        List<String> branchFileNames = Utils.plainFilenamesIn(Repository.HEAD_AREA);
        for (String branchFileName : branchFileNames) {
            if (branchFileName.equals(branchName + ".bin")) {
                branchFile = new File(Repository.HEAD_AREA + "/" + branchName + ".bin");
//                branchFile.delete();
                branchExist = true;
                break;
            }
        }
        if (!branchExist) {
            System.out.println("A branch with that name does not exist.");
        }
        return branchFile;
    }
}
