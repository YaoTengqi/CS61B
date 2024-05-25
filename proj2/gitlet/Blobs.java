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
            this.blobID = Utils.sha1(this.content);
        }
    }

    /**
     * 给定文件对象，读取文件中的内容到byte[]后返回
     *
     * @param blob_file
     * @return
     * @throws IOException
     */
//    private byte[] readFileToBytes(File blob_file) throws IOException {
//        byte[] temp_bytes = new byte[(int) blob_file.length()];
//        FileInputStream fis = new FileInputStream(blob_file);
//        fis.read(temp_bytes);
//        fis.close();
//        return temp_bytes;
//    }

    /**
     * 对比Blobs是否相同
     *
     * @param blobArray
     * @return
     */
    public Blobs equals(Blobs[] blobArray) {
        for (int i = 0; i < blobArray.length; i++) {
            Blobs blob = blobArray[i];
            String blobID = blob.getBlobID();
            if (blobID.equals(this.blobID)) {
                return blob;
            } else {
                continue;
            }
        }
        return null;
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

    public static boolean deleteStageFile(String fileName, String command) throws IOException {
        boolean returnFlag = false;
        List<Blobs> blobsList = null;
        String[] parts = fileName.split("/");
        String realFileName = parts[parts.length - 1]; // 获取真正的文件名
        int lastIndex = realFileName.lastIndexOf('.');
        String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
        File createFile = new File(Repository.STAGE_AREA + "/" + fileNameWithoutExtension + ".bin");
//        File workStageFile = new File(Repository.CWD + fileName);
        Blobs blobFile = new Blobs(Repository.CWD + fileName);
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
