package com.hq.mobydroid.device;

import com.hq.jadb.engine.JadbException;
import com.hq.jadb.manager.JadbDevicePackages;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.Icon;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class TaskPackageMangerList extends TaskWorker<Void, ApkgManager> {

    private final TaskListener taskListener;
    private static final Icon icon = MaterialIcons.buildIcon(MaterialIcons.ANDROID, 24, MaterialColor.AMBERA_100);
    private boolean enabled = false;
    private boolean disabled = false;
    private boolean system = false;
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
    public TaskPackageMangerList(MobydroidDevice device, boolean enabled, boolean disabled, boolean system, TaskListener taskListener) {
        super(device);
        this.taskListener = taskListener;
        this.enabled = enabled;
        this.disabled = disabled;
        this.system = system;
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
        Map<String, String> user_enabled = new HashMap();
        Map<String, String> user_disabled = new HashMap();
        Map<String, String> system_enabled = new HashMap();
        Map<String, String> system_disabled = new HashMap();
        try {
            // enabled third party apps
            if (this.enabled) {
                user_enabled.putAll(new JadbDevicePackages(device, true, true, false, false).getPackages());
            }
            // disabled third party apps
            if (this.disabled) {
                user_disabled.putAll(new JadbDevicePackages(device, true, false, true, false).getPackages());
            }
            // enabled system apps
            if (this.system && this.enabled) {
                system_enabled.putAll(new JadbDevicePackages(device, false, true, false, true).getPackages());
            }
            // disabled system apps
            if (this.system && this.disabled) {
                system_disabled.putAll(new JadbDevicePackages(device, false, false, true, true).getPackages());
            }
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
        int pkgs_size = user_enabled.size() + user_disabled.size() + system_enabled.size() + system_disabled.size();
        // enabled third party apps
        for (Map.Entry<String, String> pkg : user_enabled.entrySet()) {
            publish(new ApkgManager(device.getPackageDetails(pkg.getKey(), pkg.getValue()), false, true, false)); // get package details and publish it
            setProgress(10 + ((counter++) * (100 - 10) / pkgs_size)); // set progress
        }
        // disabled third party apps
        for (Map.Entry<String, String> pkg : user_disabled.entrySet()) {
            publish(new ApkgManager(device.getPackageDetails(pkg.getKey(), pkg.getValue()), false, false, false)); // get package details and publish it
            setProgress(10 + ((counter++) * (100 - 10) / pkgs_size)); // set progress
        }
        // enabled system apps
        for (Map.Entry<String, String> pkg : system_enabled.entrySet()) {
            publish(new ApkgManager(device.getPackageDetails(pkg.getKey(), pkg.getValue()), false, true, true)); // get package details and publish it
            setProgress(10 + ((counter++) * (100 - 10) / pkgs_size)); // set progress
        }
        // disabled system apps
        for (Map.Entry<String, String> pkg : system_disabled.entrySet()) {
            publish(new ApkgManager(device.getPackageDetails(pkg.getKey(), pkg.getValue()), false, false, true)); // get package details and publish it
            setProgress(10 + ((counter++) * (100 - 10) / pkgs_size)); // set progress
        }

        // set status to done
        setProgress(100);
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
