package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Nhu Vu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Gitlet r = new Gitlet();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (args[0]) {
        case "init":
            initHelper(r, args);
            break;
        case "add":
            addHelper(r, args);
            break;
        case "commit":
            commitHelper(r, args);
            break;
        case "log":
            if (validInput(1, args)) {
                r.log();
            }
            break;
        case "checkout":
            checkoutHelper(r, args);
            break;
        case "status":
            if (validInput(1, args)) {
                r.status();
            }
            break;
        case "rm":
            removeHelper(r, args);
            break;
        case "branch":
            branchHelper(r, args);
            break;
        case "find":
            findHelper(r, args);
            break;
        case "global-log":
            if (validInput(1, args)) {
                r.globalLog();
            }
            break;
        case "rm-branch":
            rmbHelper(r, args);
            break;
        case "reset":
            resetHelper(r, args);
            break;
        case "merge":
            mergeHelper(r, args);
            break;
        default:
            System.out.println("No command "
                    + "with that name exists.");
        }
        return;
    }


    /** Helper method.
     *
     * @param repo for this gitlet repo.
     * @param args for inputs.
     */
    public static void initHelper(Gitlet repo, String... args) {
        if (validInput(1, args)) {
            repo.init();
        }
    }

    /** Helper method.
     *
     * @param repo for this gitlet repo.
     * @param args for inputs.
     */
    public static void checkoutHelper(Gitlet repo, String... args) {
        if (args.length != 2
                && args.length != 3 && args.length != 4) {
            System.out.println("Incorrect operands.");
        } else if ((args.length == 3 && !args[1].equals("--"))
                || (args.length == 4
                && !args[2].equals("--"))) {
            System.out.println("Incorrect operands.");
        } else {
            repo.checkout(args);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void mergeHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.merge(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void branchHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.branch(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void rmbHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.removeBranch(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void commitHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.commit(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void removeHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.remove(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void addHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.add(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void findHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.find(args[1]);
        }
    }

    /** Helper method.
     *
     * @param r for this gitlet directory.
     * @param args for my inputs.
     */
    public static void resetHelper(Gitlet r, String... args) {
        if (validInput(2, args)) {
            r.reset(args[1]);
        }
    }

    /** Helper method to check for valid input.
     *
     * @param length for input length.
     * @param args for my inputs.
     * @return boolean indicating valid input.
     */
    public static boolean validInput(int length, String... args) {
        if (Gitlet.GITLET_REPO.exists() && !args[0].equals("init")) {
            if (args.length == length) {
                return true;
            } else {
                System.out.println("Incorrect operands.");
                return false;
            }
        } else if (args[0].equals("init")) {
            if (args.length == length) {
                return true;
            } else {
                System.out.println("Incorrect operands.");
                return false;
            }
        } else {
            System.out.println("Not in an "
                    + "initialized Gitlet directory.");
            return false;
        }
    }

}
