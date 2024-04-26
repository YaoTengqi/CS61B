package gitlet;

import java.security.NoSuchAlgorithmException;

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
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    // TODO: handle the `init` command
                    Repository.makeSetup();
                    String commitMessage = "This is the first commit!";
                    Blobs[] blobArray = {null};
                    Commit firstCommit = new Commit(commitMessage, blobArray, null);
                    break;
                case "add":
                    // TODO: handle the `add [filename]` command
                    break;
                default:
                    System.out.println("No command with that name exists.");
            }
        }
    }
}
