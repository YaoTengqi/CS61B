package gitlet;

import java.io.*;
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

    /**
     * 根据给定的文件名生成blob对象，先存储文件数据，再跟进文件数据计算出相应的SHA-1哈希ID
     *
     * @param fileName
     * @throws IOException
     */
    public Blobs(String fileName) throws IOException {
        File blob_file = new File(fileName);
        if (!blob_file.exists()) {    // 不存在时提示
            System.out.println(fileName + "doesn't exist, please check the file!");
            throw new FileNotFoundException();
        } else { // 存在时获取文件内容
            this.content = readFileToBytes(blob_file);
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
    private byte[] readFileToBytes(File blob_file) throws IOException {
        byte[] temp_bytes = new byte[(int) blob_file.length()];
        FileInputStream fis = new FileInputStream(blob_file);
        fis.read(temp_bytes);
        fis.close();
        return temp_bytes;
    }

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

    public static Blobs[] returnBlobsArray(List<String> fileNames) {
        Blobs[] blobArray = new Blobs[fileNames.size()];
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            File file = new File(Repository.STAGE_AREA + "/" + fileName);
            Blobs tempBlob = Utils.readObject(file, Blobs.class);
            blobArray[i] = tempBlob;
        }
        return blobArray;
    }


    public String getBlobID() {
        return this.blobID;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getBlobName() {
        return blobName;
    }
}
