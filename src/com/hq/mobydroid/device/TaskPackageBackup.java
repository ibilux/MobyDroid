package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import com.hq.jadb.engine.JadbException;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageBackup extends TaskWorker {

    private static final Icon icon = MaterialIcons.buildIcon(MaterialIcons.ANDROID, 24, MaterialColor.AMBERA_100);
    private Status status;
    private String message;
    private final List<Apkg> pkgs;

    /*
     * pm install: installs a package to the system.  Options:
     * -l: install the package with FORWARD_LOCK.
     * -r: reinstall an exisiting app, keeping its data.
     * -t: allow test .apks to be installed.
     * -i: specify the installer package name.
     * -s: install package on sdcard.
     * -f: install package on internal flash.
     * -d: allow version code downgrade.
     * -g: grant all runtime permissions
     */
    public TaskPackageBackup(MobydroidDevice device, List<Apkg> pkgs) {
        super(device);
        this.pkgs = pkgs;
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
            device.backup(pkgs, false);
        } catch (IOException | JadbException ex) {
            // set status to failed
            status = Status.FAILED;
            message = ex.getMessage();
            Log.log(Level.SEVERE, "TaskPackageBackup", ex);
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
        return "Buckup";
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
