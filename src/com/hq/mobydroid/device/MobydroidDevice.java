package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import com.hq.jadb.JadbDevice;
import com.hq.jadb.JadbDeviceManager;
import com.hq.mobydroid.MobyDroid;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.stream.Stream;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class MobydroidDevice extends JadbDeviceManager {

    private final List<TaskWorker> tasks;
    private final Thread tasksRunner;
    private volatile boolean tasksRunnerStatus;

    public MobydroidDevice(JadbDevice device) {
        super(device);
        this.tasks = new ArrayList<>();

        // start tasks monitor timer
        tasksRunnerStatus = true;
        tasksRunner = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (tasksRunnerStatus) {
                        runNextTask();
                    }
                    try {
                        sleep(1);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
    }

    /**
     * Start running tasks.
     */
    public void startTasksRunner() {
        tasksRunnerStatus = true;
        tasksRunner.start();
    }

    /**
     * Stop running tasks.
     */
    public void stopTasksRunner() {
        tasksRunnerStatus = false;
        tasksRunner.interrupt();
    }

    /**
     * Get tasks runner status.
     *
     * @return
     */
    public boolean getTasksRunnerStatus() {
        return tasksRunnerStatus;
    }

    /**
     * Notify when a new PropertyChangeEvent fired.
     *
     * @param evt
     */
    public void notifyTaskPropertyChangeEvent(PropertyChangeEvent evt) {
        MobyDroid.notifyTaskPropertyChangeEvent();
    }

    /**
     *
     * @return
     */
    public List<TaskWorker> getTasks() {
        return tasks;
    }

    /**
     * Calculate running and pending tasks number.
     *
     * @return
     */
    public int getRunningTasksNumber() {
        return (int) tasks.stream().filter((taskWorker) -> (taskWorker.getStatus() == TaskWorker.Status.STARTED || taskWorker.getStatus() == TaskWorker.Status.PENDING)).count();
    }

    /**
     * Calculate running tasks total progress.
     *
     * @return
     */
    public float getTasksProgress() {
        Stream<TaskWorker> stream = tasks.stream().filter((taskWorker) -> (taskWorker.getStatus() == TaskWorker.Status.PENDING || taskWorker.getStatus() == TaskWorker.Status.STARTED));
        OptionalDouble average = stream.mapToDouble(taskWorker -> taskWorker.getProgress()).average();

        try {
            return (float) average.getAsDouble();
        } catch (NoSuchElementException ex) {
            return 0;
        }

    }

    /**
     * Clear finished, failed and cancelled tasks list.
     */
    public void clearDeadTasks() {
        tasks.stream().filter((taskWorker) -> (taskWorker.getStatus() == TaskWorker.Status.DONE || taskWorker.getStatus() == TaskWorker.Status.FAILED || taskWorker.getStatus() == TaskWorker.Status.CANCELLED)).forEachOrdered((taskWorker) -> {
            tasks.remove(taskWorker);
        });
    }

    /**
     * Run next task in queue.
     */
    private void runNextTask() {
        int running = (int) tasks.stream().filter((taskWorker) -> (taskWorker.getStatus() == TaskWorker.Status.STARTED)).count();
        if (running == 0) {
            for (TaskWorker taskWorker : tasks) {
                if (taskWorker.getStatus() == TaskWorker.Status.PENDING) {
                    taskWorker.execute();
                    break;
                }
            }
        }
    }

    /**
     * Fetch installed packages list.
     *
     * @param taskListener
     */
    public void runPackagesListTask(TaskListener taskListener) {
        tasks.add(new TaskPackageMangerList(this, taskListener));
    }

    /**
     * Install package.
     *
     * @param pkg
     */
    public void runPackageInstallTask(ApkgInstaller pkg) {
        tasks.add(new TaskPackageMangerInstall(this, pkg));
    }

    /**
     * Uninstall package.
     *
     * @param pkg
     */
    public void runPackageUninstallTask(Apkg pkg) {
        tasks.add(new TaskPackageMangerUninstall(this, pkg));
    }

    /**
     * Backup package.
     *
     * @param pkgs
     */
    public void runPackageBackupTask(List<Apkg> pkgs) {
        tasks.add(new TaskPackageBackup(this, pkgs));
    }

    /**
     * Restore package.
     *
     * @param abFile
     */
    public void runPackageRestoreTask(String abFile) {
        tasks.add(new TaskPackageRestore(this, abFile));
    }

    /**
     * Pull apk file from package.
     *
     * @param pkg
     */
    public void runPackagePullTask(Apkg pkg) {
        tasks.add(new TaskPackagePull(this, pkg));
    }
}
