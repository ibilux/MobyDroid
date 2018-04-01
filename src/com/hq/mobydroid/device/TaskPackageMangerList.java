package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import java.io.IOException;
import com.hq.jadb.engine.JadbException;
import com.hq.jadb.manager.JadbDevicePackages;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageMangerList extends TaskWorker<Void, Apkg> {

    private final TaskListener taskListener;
    private static final Icon icon = MaterialIcons.buildIcon(MaterialIcons.ANDROID, 24, MaterialColor.AMBERA_100);
    private Status status;
    private String message;


    /*
     * adb shell pm list packages
     * adb shell pm list packages -f See their associated file.
     * adb shell pm list packages -d Filter to only show disabled packages.
     * adb shell pm list packages -e Filter to only show enabled packages.
     * adb shell pm list packages -s Filter to only show system packages.
     * adb shell pm list packages -3 Filter to only show third party packages.
     * adb shell pm list packages -i See the installer for the packages.
     * adb shell pm list packages -u Also include uninstalled packages.
     * adb shell pm list packages --user <USER_ID> The user space to query.
     */
    public TaskPackageMangerList(MobydroidDevice device, TaskListener taskListener) {
        super(device);
        this.taskListener = taskListener;
        this.status = Status.PENDING;
        this.message = "fetch installed packages from the device";
    }

    @Override
    protected Object doInBackground() {
        // start
        status = Status.STARTED;
        taskListener.onStart();

        // set progress to 0
        setProgress(0);

        // get packages list
        Map<String, String> pkgs;
        try {
            pkgs = new JadbDevicePackages(device).getPackages();
        } catch (IOException | JadbException ex) {
            // set status to failed
            status = Status.FAILED;
            message = ex.getMessage();
            Log.log(Level.SEVERE, "TaskPackageMangerList", ex);
            return null;
        }

        //set progress to 10
        setProgress(10);

        // start getting packages details and update progress
        int counter = 0;
        for (Map.Entry<String, String> pkg : pkgs.entrySet()) {
            // get package details and publish it
            publish(device.getPackageDetails(pkg.getKey(), pkg.getValue()));
            // set progress
            setProgress(10 + ((counter++) * (100 - 10) / pkgs.size()));
        }

        // set status to done
        status = Status.DONE;
        message = "Done";
        return null;
    }

    @Override
    protected void process(List list) {
        // process
        taskListener.onProcess(list);
    }

    @Override
    protected void done() {
        // set progress to 100
        setProgress(100);
        // done: report to listner
        taskListener.onDone();
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getName() {
        return "Packages list";
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
