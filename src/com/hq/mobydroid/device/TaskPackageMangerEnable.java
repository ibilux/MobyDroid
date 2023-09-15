package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import com.hq.jadb.engine.JadbException;
import com.hq.mobydroid.Log;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageMangerEnable extends TaskWorker {

    private final Apkg pkg;
    private Status status;
    private String message;

    /*
     * pm enable: remove a package to the system.  Options:
     * '-k': keep the data and cache directories
     */
    public TaskPackageMangerEnable(MobydroidDevice device, Apkg pkg) {
        super(device);
        this.pkg = pkg;
        this.status = Status.PENDING;
        this.message = "To be enabled";
    }

    @Override
    protected Object doInBackground() {
        // start
        status = Status.STARTED;

        // clear packages list and set progress to 0
        setProgress(0);

        // enable package and update progress
        try {
            device.enable(pkg);
        } catch (IOException | JadbException ex) {
            // set status to failed
            status = Status.FAILED;
            message = ex.getMessage();
            Log.log(Level.SEVERE, "TaskPackageMangerEnable", ex);
            return null;
        }

        // set status to done
        status = Status.DONE;
        message = "Enabled successfully";
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
        return pkg.getLabel();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Icon getIcon() {
        return pkg.getIcon();
    }

}
