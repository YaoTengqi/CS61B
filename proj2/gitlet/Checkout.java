package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Checkout {
    public static boolean checkoutFile(Commit currentCommit, String fileName) throws IOException {
        // 1. java gitlet.Main checkout -- [file name]
        List<Blobs> previousBlobArray = currentCommit.getBlobArray();
        String checkoutFileName = Repository.CWD + fileName;
        boolean fileExists = false;
        if (previousBlobArray == null) {
            throw new GitletException("File does not exist in that commit.");
        } else {
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
            if (!fileExists) { //文件不存在
                throw new GitletException("File does not exist in that commit.");
            }
        }
        return fileExists;
    }

    public static void checkoutCommitFile(Commit currentCommit, String fileName, String commitID) throws IOException {
        // 2. java gitlet.Main checkout [commit id] -- [file name]
        List<Commit> commitList = Commit.returnCommitList(currentCommit);
        boolean commitExists = false;
        for (int i = 0; i < commitList.size(); i++) {
            Commit commit = commitList.get(i);
            String shortCommitID = commit.getCommitID().substring(0, 6);
            String shortCheckoutID = commitID.substring(0, 6);
            if (shortCommitID.equals(shortCheckoutID)) {
                commitExists = true;
                checkoutFile(commit, fileName);
            }
        }
        if (!commitExists) {
            throw new GitletException("No commit with that id exists.");
        }
    }
}
