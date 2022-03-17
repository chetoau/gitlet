package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Staging area.
 * @author Nhu Vu
 */
public class Stage implements Serializable {

    /** Staging area for adding and removing changes to working dir. */
    public Stage() {
        _added = new HashMap<String, String>();
        _removed = new ArrayList<>();
    }

    /** Add file to added_files in staging area.
     *
     * @param name for file name.
     * @param hashcode for file hash id.
     */
    public void add(String name, String hashcode) {
        _added.put(name, hashcode);
    }

    /** Add file to removed_files in staging area.
     *
     * @param name for file name.
     */
    public void remove(String name) {
        _removed.add(name);
    }

    /** Return the HashMap containing added files.
     *
     * @return my added stage.
     */
    public HashMap<String, String> getAdded() {
        return _added;
    }

    /** Return ArrayList of removed files.
     *
     * @return my removed stage.
     */
    public ArrayList<String> getRemoved() {
        return _removed;
    }

    /** Meant for committing, clear the staging area. */
    public void clearStage() {
        _added = new HashMap<>();
        _removed = new ArrayList<>();
    }

    /** Data structure to keep track of my added files in staging area. */
    private HashMap<String, String> _added;

    /** Data structure to keep track of my removed files. */
    private ArrayList<String> _removed;

}
