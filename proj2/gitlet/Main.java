package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

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
        Commit currentCommit;
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    // TODO: handle the `init` command
                    Repository.makeSetup();
                    Repository.makeStageArea();
                    Repository.makeCommitArea();
                    String commitMessage = "This is the first commit!";
                    Blobs[] blobArray = {null};
                    Commit firstCommit = new Commit(commitMessage, blobArray, null);
                    currentCommit = firstCommit;
                    break;
                case "add":
                    // TODO: handle the `add [filename]` command
                    if (!Repository.STAGE_AREA.exists()) {
                        throw new GitletException("STAGE_AREA doesn't exists, please execute 'git init' first.");
                    }
                    String secondArg = args[1];
                    if (secondArg == null) {
                        throw new GitletException("Please enter filename.");
                    } else {
                        File addFile = new File(Repository.CWD + secondArg);
                        if (!addFile.exists()) {
                            throw new GitletException(addFile + "does not exist.");
                        } else {
                            String[] parts = secondArg.split("/");
                            String realFileName = parts[parts.length - 1]; // 获取真正的文件名
                            int lastIndex = realFileName.lastIndexOf('.');
                            String fileNameWithoutExtension = realFileName.substring(0, lastIndex);
                            File createFile = new File(Repository.STAGE_AREA + "/" + fileNameWithoutExtension + ".bin");
                            Blobs blobFile = new Blobs(Repository.CWD + secondArg);
                            Utils.writeObject(createFile, blobFile);
                        }
                    }
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
}
