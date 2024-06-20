package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.util.*;

/**
 * 专门处理merge操作的类，包括各种相关的操作函数
 */
public class Merge {

    private static boolean printConflict = false;

    /**
     * 找到两个branch的最近祖先节点
     *
     * @param master
     * @param other
     * @return
     */
    public static Commit findSplitAncestor(mergeCommit master, mergeCommit other) {
        Commit ancestor = null;
        Set<String> masterIDSet = new HashSet<>();
        Commit secondMaster = master.getSecondParent();
        Commit secondOther = other.getSecondParent();
        //获取master的commitID集合
        while (master != null) {
            masterIDSet.add(master.getCommitID());
            Commit tempCommit = master.getParent();
            if (tempCommit != null) {
                try {
                    master = new mergeCommit(tempCommit.getBranch(), tempCommit.getMessage(), tempCommit.getTime(), tempCommit.getBlobArray(), tempCommit.getParent(), null);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                master = null;
            }
        }
        while (secondMaster != null) {
            masterIDSet.add(secondMaster.getCommitID());
            Commit tempCommit = secondMaster.getParent();
            if (tempCommit != null) {
                try {
                    secondMaster = new mergeCommit(tempCommit.getBranch(), tempCommit.getMessage(), tempCommit.getTime(), tempCommit.getBlobArray(), tempCommit.getParent(), null);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                secondMaster = null;
            }
        }

        //遍历查找
        while (other != null) {
            String otherID = other.getCommitID();
            if (masterIDSet.contains(otherID)) {
                ancestor = other;
                break;
            }
            Commit tempCommit = other.getParent();
            if (tempCommit != null) {
                try {
                    other = new mergeCommit(tempCommit.getBranch(), tempCommit.getMessage(), tempCommit.getTime(), tempCommit.getBlobArray(), tempCommit.getParent(), null);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                other = null;
            }
        }
        //遍历查找
        while (secondOther != null && ancestor != null) {
            String otherID = secondOther.getCommitID();
            if (masterIDSet.contains(otherID)) {
                ancestor = secondOther;
                break;
            }
            Commit tempCommit = secondOther.getParent();
            if (tempCommit != null) {
                try {
                    secondOther = new mergeCommit(tempCommit.getBranch(), tempCommit.getMessage(), tempCommit.getTime(), tempCommit.getBlobArray(), tempCommit.getParent(), null);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            } else {
                secondOther = null;
            }
        }
        return ancestor;
    }

    /**
     * 找到两个commit的同名BlobList同时得到不同名的BlobList，分别为currentDifferentBlobList以及otherDifferentBlobList
     *
     * @param currentCommit
     * @param otherCommit
     * @return sameNameBlobName
     */
    public static Set<String> findSameBlob(Commit currentCommit, Commit otherCommit) {
        Set<String> sameNameBlobName = new HashSet<>();
        Set<String> currentBlobNameSet = new HashSet<>();
        //获取current的BlobName集合
        List<Blobs> currentBlobsList = currentCommit.getBlobArray();
        for (Blobs currentBlobs : currentBlobsList) {
            currentBlobNameSet.add(currentBlobs.getBlobName());
        }
        List<Blobs> otherBlobsList = otherCommit.getBlobArray();
        for (Blobs otherBlobs : otherBlobsList) {
            String otherBlobsName = otherBlobs.getBlobName();
            if (currentBlobNameSet.contains(otherBlobsName)) {    //出现同名Blobs时
                sameNameBlobName.add(otherBlobs.getBlobName());
            }
        }
        return sameNameBlobName;
    }

    /**
     * 对同名Blob进行处理，并处理后的Blob写入mergeBlobList中
     *
     * @param sameBlobList
     * @param ancestor
     */
    static List<Blobs> sameBlobListTraversal(Set<String> sameBlobList, Commit ancestor, Commit currentCommit, Commit otherCommit) throws IOException {
        List<Blobs> mergeBlobList = new ArrayList<>();
        List<Blobs> ancestorBlobList = ancestor.getBlobArray();
        List<Blobs> currentBlobList = currentCommit.getBlobArray();
        List<Blobs> otherBlobList = otherCommit.getBlobArray();
        Set<String> ancestorBlobID = new HashSet<>();
        Set<String> ancestorBlobName = new HashSet<>();
        //将祖先commit的blobList转为hashSet
        if (ancestorBlobList != null) {
            for (Blobs ancestorBlob : ancestorBlobList) {
                ancestorBlobID.add(ancestorBlob.getBlobID());
                ancestorBlobName.add(ancestorBlob.getBlobName());
            }
            //以currentBlob为出发点
            for (Blobs currentBlob : currentBlobList) {
                String currentName = currentBlob.getBlobName();
                if (sameBlobList.contains(currentName)) {   //是否为同名文件
                    //如过是同名文件则与ancestor中的Blobs进行对比
                    if (ancestorBlobName.contains(currentName)) {   //祖先commit原来有此文件
                        //查看blob是否发生改变
                        if (ancestorBlobID.contains(currentBlob.getBlobID())) {
                            //在master中未发生改变，则可以直接写入mergeList中(因为无论在other中是否改变都写入other就可以)
                            Blobs otherBlob = findSameNameInOther(currentName, otherBlobList);
                            if (otherBlob != null) {
                                // 1. & 2.
                                mergeBlobList.add(otherBlob);
                            }
                        } else {  //在master中发生改变
                            Blobs otherBlob = findSameNameInOther(currentName, otherBlobList);
                            if (otherBlob != null && ancestorBlobID.contains(otherBlob.getBlobID())) {
                                //在other中没有发生改变，直接写入master中发生改变的blob即可
                                // 3.
                                mergeBlobList.add(currentBlob);
                            } else {
                                // 4.
                                //在other中发生改变，产生了冲突(conflict)
                                byte[] newContent = resolveConflict(currentBlob, otherBlob);
                                Blobs newBlob = new Blobs(currentName, newContent);
                                mergeBlobList.add(newBlob);
                            }
                        }
                    } else {                                          //祖先commit原来没有此文件
                        Blobs otherBlob = findSameNameInOther(currentName, otherBlobList);
                        if (currentBlob.getBlobID().equals(otherBlob.getBlobID())) {
                            //内容相同，随便写入其中一个到mergeList
                            // 5.
                            mergeBlobList.add(currentBlob);
                        } else {
                            // 6.
                            //内容不同，产生冲突
                            byte[] newContent = resolveConflict(currentBlob, otherBlob);
                            Blobs newBlob = new Blobs(currentName, newContent);
                            mergeBlobList.add(newBlob);
                        }
                    }
                } else { //不是同名文件
                    //查看祖先commit中是否包含此blob
                    if (!ancestorBlobName.contains(currentName)) {
                        // 7.
                        //不包含此blob但不是同名blob说明是在master中新加的，可直接写入此blob
                        mergeBlobList.add(currentBlob);
                    }
//                else{
//                    8.
//                    //包含此blob但不是同名blob，说明在other中此名blob被删除，则mergeList中也应该被删除
//                }
                }
            }

            //以otherBlob为出发点
            if (otherBlobList != null) {
                for (Blobs otherBlob : otherBlobList) {
                    String otherName = otherBlob.getBlobName();
                    if (!sameBlobList.contains(otherName)) {
                        //同名文件已在"以currentBlob为出发点"中处理完，现在只需处理otherBlob的非同名Blobs
                        if (!ancestorBlobName.contains(otherName)) {
                            // 9.
                            //不包含此blob但不是同名blob说明是在other中新加的，可直接写入此blob
                            mergeBlobList.add(otherBlob);
                        }
//                else{
//                    10.
//                    //包含此blob但不是同名blob，说明在master中此名blob被删除，则mergeList中也应该被删除
//                }
                    }
                }
            }
        } else {
            mergeBlobList.addAll(currentBlobList);
            mergeBlobList.addAll(otherBlobList);
        }

        //解决特殊冲突
        List<Blobs> changeDeleteBlobList = resolveChangeDeleteFile(ancestorBlobList, currentBlobList, otherBlobList);

        mergeBlobList.addAll(changeDeleteBlobList);

        return mergeBlobList;
    }

    /**
     * 通过文件名找到other branch中相同文件名的blob，为了与ancestor以及master中的内容进行比较
     *
     * @param blobName
     * @param otherBlobList
     * @return
     */
    static Blobs findSameNameInOther(String blobName, List<Blobs> otherBlobList) {
        for (Blobs otherBlob : otherBlobList) {
            if (blobName.equals(otherBlob.getBlobName())) {
                return otherBlob;
            }
        }
        return null;
    }

    /**
     * 解决文件冲突的问题，即将当前branch的内容写在前面，而把另一branch的内容写在后面
     *
     * @param currentBlob
     * @param otherBlob
     * @return
     */
    static byte[] resolveConflict(Blobs currentBlob, Blobs otherBlob) throws IOException {
        byte[] currentContent = null;
        if (currentBlob != null) {
            currentContent = currentBlob.getContent();
        } else {
            currentContent = "".getBytes();
        }
        byte[] otherContent = null;
        if (otherBlob != null) {
            otherContent = otherBlob.getContent();
        } else {
            otherContent = "".getBytes();
        }
        String headString = "<<<<<<< HEAD\n";
        String divideLine = "=======\n";
        String endLine = ">>>>>>>\n";
        String fileContent = headString + new String(currentContent) + divideLine + new String(otherContent) + endLine;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        bos.write(headString.getBytes());
//        bos.write(currentContent);
//        bos.write(divideLine.getBytes());
//        bos.write(otherContent);
//        bos.write(endLine.getBytes());
        bos.write(fileContent.getBytes());
        byte[] newContent = bos.toByteArray();
        if (!printConflict) {
            System.out.println("Encountered a merge conflict.");
            printConflict = true;
        }
        return newContent;
    }

    /**
     * 当在祖先commit中存在的blob在任意一个branch中不复存在时将其删除(如果在WORK_AREA中存在，也要删除工作区文件)
     *
     * @param ancestor
     * @param mergeBlobList
     * @return deleteBlobs
     */
    static List<Blobs> findDeleteBlobs(Commit ancestor, List<Blobs> mergeBlobList) {
        List<Blobs> deleteBlobs = new ArrayList<>();

        List<Blobs> ancestorBlobList = ancestor.getBlobArray();
        if (ancestorBlobList != null) {
            for (Blobs ancestorBlob : ancestorBlobList) {
                String ancestorPath = ancestorBlob.getBlobName();
                String[] ancestorParts = ancestorPath.split("/");
                String ancestorName = ancestorParts[ancestorParts.length - 1]; // 获取真正的文件名
                boolean blobExists = false;
                for (Blobs mergeBlob : mergeBlobList) {
                    String mergePath = ancestorBlob.getBlobName();
                    String[] mergeParts = mergePath.split("/");
                    String mergeName = mergeParts[mergeParts.length - 1]; // 获取真正的文件名
                    if (ancestorName.equals(mergeName)) {
                        blobExists = true;
                        break;
                    }
                }
                if (blobExists) {
                    deleteBlobs.add(ancestorBlob);
                }
            }
        }
        return deleteBlobs;
    }


    /**
     * //处理存在于祖先节点，并在一个branch中改变，另一个branch中删除的conflict情况
     *
     * @param ancestorBlobList
     * @param currentBlobList
     * @param otherBlobList
     * @return
     * @throws IOException
     */
    static List<Blobs> resolveChangeDeleteFile(List<Blobs> ancestorBlobList, List<Blobs> currentBlobList, List<Blobs> otherBlobList) throws IOException {
        Set<String> currentBlobID = new HashSet<>();
        Set<String> currentBlobName = new HashSet<>();
        Set<String> otherBlobID = new HashSet<>();
        Set<String> otherBlobName = new HashSet<>();
        List<Blobs> mergeBlobList = new ArrayList<>();
        for (Blobs currentBlob : currentBlobList) {
            currentBlobID.add(currentBlob.getBlobID());
            currentBlobName.add(currentBlob.getBlobName());
        }
        for (Blobs otherBlob : otherBlobList) {
            otherBlobID.add(otherBlob.getBlobID());
            otherBlobName.add(otherBlob.getBlobName());
        }

        if (ancestorBlobList != null) {
            for (Blobs ancestorBlob : ancestorBlobList) {
                String ancestorName = ancestorBlob.getBlobName();
                if (currentBlobName.contains(ancestorName) && !otherBlobName.contains(ancestorName)) {   //祖先commit原来有此文件，但是在other branch中被删除了
                    if (!currentBlobID.contains(ancestorBlob.getBlobID())) {
                        //在master中发生改变
                        for (Blobs currentBlob : currentBlobList) {
                            String currentName = currentBlob.getBlobName();
                            if (currentName.equals(ancestorName)) {
                                byte[] newContent = resolveConflict(currentBlob, null);
                                Blobs newBlob = new Blobs(ancestorName, newContent);
                                mergeBlobList.add(newBlob);
                                break;
                            }
                        }

                    }
                } else if (!currentBlobName.contains(ancestorName) && otherBlobName.contains(ancestorName)) {  //祖先commit原来有此文件，master branch中被删除了
                    if (!otherBlobID.contains(ancestorBlob.getBlobID())) {
                        //在other中发生改变
                        for (Blobs otherBlob : otherBlobList) {
                            String otherName = otherBlob.getBlobName();
                            if (otherName.equals(ancestorName)) {
                                byte[] newContent = resolveConflict(null, otherBlob);
                                Blobs newBlob = new Blobs(ancestorName, newContent);
                                mergeBlobList.add(newBlob);
                                break;
                            }
                        }

                    }
                }
            }
        }
        return mergeBlobList;
    }
}
