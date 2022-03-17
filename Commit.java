package gitlet;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;

/** Class taking care of commit objects.
 * @author Nhu Vu
 */
public class Commit implements Serializable {

    /** A commit object.
     *
     * @param parent for Parent commit.
     * @param message for Commit message.
     * @param blob for commit blob pointer.
     *
     */
    public Commit(String parent, String message,
                  HashMap<String, String> blob) {
        this._parent = parent;
        this._msg = message;
        this._blobPointer = blob;

        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyLocalizedPattern("EEE "
                + "MMM d HH:mm:ss yyyy Z");
        this._time = formatter.format(new Date());

        byte[] myFile = Utils.serialize(this);
        this.curr = Utils.sha1(myFile);
    }

    /** Returns the current commit's hashcode. */
    public String getCommitHash() {
        return this.curr;
    }

    /** Returns the current commit's timestamp. */
    public String timeStamp() {
        return this._time;
    }

    /** Returns the current commit's parent hash. */
    public String parentHash() {
        return this._parent;
    }

    /** Returns the commit's message. */
    public String commitMsg() {
        return this._msg;
    }

    /** Returns the blobs associated with this commit. */
    public HashMap<String, String> getBlob() {
        return this._blobPointer;
    }

    /** Parent commit. */
    private String _parent;

    /** Time of commit. */
    private String _time;

    /** Message connected to commit. */
    private String _msg;

    /** My blob. */
    private HashMap<String, String> _blobPointer;

    /** My hashcode. */
    private String curr;
}
