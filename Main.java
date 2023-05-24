package gitlet;
import static gitlet.Commit.commitCommand;
import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Matthew Mojica
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (!GITLET_DIR.exists() && !args[0].equals("init")) {
            System.out.println("Not in an initialized Gitlet directory");
            System.exit(0);
        } else {
            String firstArg = args[0];
            switch (firstArg) {
                case "init":
                    checkOperands(args,1);
                    initCommand();
                    break;
                case "add":
                    checkOperands(args,2);
                    addCommand(args[1]);
                    break;
                case "commit":
                    checkOperands(args,2);
                    commitCommand(args[1],false, null);
                    break;
                case "rm":
                    checkOperands(args,2);
                    removeCommand(args[1]);
                    break;
                case "log":
                    checkOperands(args,1);
                    logCommand();
                    break;
                case "global-log":
                    checkOperands(args,1);
                    globalLogCommand();
                    break;
                case "find":
                    checkOperands(args,2);
                    findCommand(args[1]);
                    break;
                case "rm-branch":
                    checkOperands(args,2);
                    rmBranchCommand(args[1]);
                    break;
                case "reset":
                    checkOperands(args,2);
                    resetCommand(args[1]);
                    break;
                case "status":
                    checkOperands(args,1);
                    statusCommand();
                    break;
                case "checkout":
                    if (args.length == 2) {
                        checkoutCommand3(args[1]);
                    } else if (args.length == 3 && args[1].equals("--")) {
                        checkoutCommand1(args[2]);
                    } else if (args.length == 4 && args[2].equals("--")) {
                        checkoutCommand2(args[1],args[3]);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    break;
                case "branch":
                    checkOperands(args,2);
                    branchCommand(args[1]);
                    break;
                case "merge":
                    checkOperands(args,2);
                    mergeCommand(args[1]);
                    break;
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
        }
    }

    public static void checkOperands(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
