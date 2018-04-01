package com.hq.mobydroid.device;

import com.hq.jadb.engine.JadbException;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageRestore extends TaskWorker {

    private static final Icon icon = MaterialIcons.buildIcon(MaterialIcons.ANDROID, 24, MaterialColor.AMBERA_100);
    private Status status;
    private String message;
    private final String abFile;

    public TaskPackageRestore(MobydroidDevice device, String abfile) {
        super(device);
        this.abFile = abfile;
        this.status = Status.PENDING;
        this.message = "To be backed up";
    }

    @Override
    protected Object doInBackground() {
        // start
        status = Status.STARTED;

        // set progress to 0
        setProgress(0);

        // backup package and update progress
        try {
            device.restore(abFile);
        } catch (IOException | JadbException ex) {
            // set status to failed
            status = Status.FAILED;
            message = ex.getMessage();
            Log.log(Level.SEVERE, "TaskPackageRestore", ex);
            return null;
        }

        // set status to done
        status = Status.DONE;
        message = "Backed up successfully";
        return null;
    }

    @Override
    protected void done() {
        // set progress to 100
        setProgress(100);
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getName() {
        return "Restore";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

}
