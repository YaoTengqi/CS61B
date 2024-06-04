package gitlet;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class mergeCommit extends Commit {
    private Commit secondParent;

    public mergeCommit(String branchName, String message, String currentTime, List<Blobs> blobArray, Commit parent, Commit secondParent) throws NoSuchAlgorithmException {
        super(branchName, message, currentTime, blobArray, parent);
        this.secondParent = secondParent;
    }

}
