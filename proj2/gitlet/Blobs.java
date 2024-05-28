package gitlet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The saved contents of files.
 * Since Gitlet saves many versions of files,
 * a single file might correspond to multiple blobs:
 * each being tracked in a different commit.
 */
public class Blobs implements Serializable {
    private String blobID;
    private String blobName;
    private byte[] content;

    public String getBlobID() {
        return this.blobID;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getBlobName() {
        return blobName;
    }

    /**
     * 根据给定的文件名生成blob对象，先存储文件数据，再跟进文件数据计算出相应的SHA-1哈希ID
     *
     * @param fileName
     * @throws IOException
     */
    public Blobs(String fileName) throws IOException {
        File blob_file = new File(fileName);
        if (!blob_file.exists()) {    // 不存在时提示
            System.out.println(fileName + " doesn't exist, please check the file!");
            throw new FileNotFoundException();
        } else { // 存在时获取文件内容
            this.content = Utils.readContents(blob_file);
            this.blobName = fileName;
            this.blobID = Utils.sha1(this.blobName, this.content);
        }
    }

    /**
     * 对比两个BlobsList是否一致，即检查上一个commit的BlobsList与当前要添加的blob是否一致
     *
     * @param previousBlobList
     * @param currentBlob
     * @return
     */
    public static int trackFiles(List<Blobs> previousBlobList, Blobs currentBlob) {
        int returnFlag = 0;
        if (previousBlobList == null) {
            return 0;   // blobs不存在或发生改变
        }
        for (int i = 0; i < previousBlobList.size(); i++) {
            String previousBlobName = previousBlobList.get(i).getBlobName();
            String blobName = currentBlob.getBlobName();
            String previousBlobID = previousBlobList.get(i).getBlobID();
            String blobID = currentBlob.getBlobID();
            if (previousBlobName.equals(blobName)) {
                returnFlag = 1; // blobs存在且发生改变
                if (blobID.equals(previousBlobID)) {
                    returnFlag = 2;    // blobs存在且并未改变
                }
            }
        }
        return returnFlag;   // blobs不存在或发生改变
    }

    public static void addBlobs(Commit currentCommit, String blobFileName) throws IOException {
        List<Blobs> previousBlobList = currentCommit.getBlobArray();
        Blobs currentBlob = new Blobs(blobFileName);
        int blobChangeFlag = trackFiles(previousBlobList, currentBlob);
        if (blobChangeFlag != 2) {
            deleteStageFile(currentBlob.getBlobName(), "add", currentBlob);
        } else {
            throw new GitletException("This file is tracked and not changed.");
        }
    }


    public static List<Blobs> returnBlobsList(List<String> fileNames, File workStage) {
        List<Blobs> blobsList = new ArrayList<Blobs>();
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            File file = new File(workStage + "/" + fileName);
            Blobs tempBlob = Utils.readObject(file, Blobs.class);
            blobsList.add(tempBlob);
        }
        return blobsList;
    }

    /**
     * 删除或者添加STAGE_AREA中的blob
     *
     * @param fileName
     * @param command
     * @param blobFile
     * @return
     * @throws IOException
     */
    public static boolean deleteStageFile(String fileName, String command, Blobs blobFile) throws IOException {
        boolean returnFlag = false;
        List<Blobs> blobsList = new ArrayList<>();
        String[] parts = fileName.split("/");
        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
        int lastIndex = realFileName.lastIndexOf('.');
        String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
        File createFile = new File(Repository.STAGE_AREA + "/" + fileNameWithoutExtension + ".bin");
//        File workStageFile = new File(Repository.CWD + fileName);
//        Blobs blobFile = new Blobs(Repository.CWD + fileName);
        List<String> fileNames = Utils.plainFilenamesIn(Repository.STAGE_AREA);
        if (command.equals("add")) {
            blobsList = Blobs.returnBlobsList(fileNames, Repository.STAGE_AREA);
        } else if (command.equals("rm")) {
            blobsList = Blobs.returnBlobsList(fileNames, Repository.REMOVAL_AREA);
        }
        Blobs isExisted = null;
        for (Blobs blob : blobsList) {
            if (blobFile.equals(blob)) {
                isExisted = blob;
                break;
            }
        }
        if (isExisted == null && command.equals("add")) {    // 文件不存在于暂存区
            Utils.writeObject(createFile, blobFile);
        } else {    // 文件存在于暂存区
            //删除此文件
            createFile.delete();
            returnFlag = true;
        }
        return returnFlag;
    }

}
