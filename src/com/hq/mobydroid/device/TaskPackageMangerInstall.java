package com.hq.mobydroid.device;

import com.hq.jadb.engine.JadbException;
import com.hq.mobydroid.Log;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageMangerInstall extends TaskWorker {

    private final ApkgInstaller pkg;
    private Status status;
    private String message;

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
    public TaskPackageMangerInstall(MobydroidDevice device, ApkgInstaller pkg) {
        super(device);
        this.pkg = pkg;
        this.status = Status.PENDING;
        this.message = "To be installed";
    }

    @Override
    protected Object doInBackground() {
        // start
        status = Status.STARTED;

        // set progress to 0
        setProgress(0);

        // install package and update progress
        try {
            device.install(new File(pkg.getPath()), pkg.isReinstallable(), pkg.isOnSdcard(), pkg.isDowngradable(), true);
        } catch (IOException | JadbException ex) {
            // set status to failed
            status = Status.FAILED;
            message = ex.getMessage();
            Log.log(Level.SEVERE, "TaskPackageMangerInstall", ex);
            return null;
        }

        // set status to done
        status = Status.DONE;
        message = "Installed successfully";
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
