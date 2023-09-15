package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import javax.swing.Icon;

/**
 * A Class to hold android package manager.
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class ApkgManager extends Apkg {

    private boolean mark;
    private final boolean enabled;
    private final boolean system;

    public ApkgManager(Apkg pkg, boolean mark, boolean enabled, boolean system) {
        this(pkg.getPackage(), pkg.getVersion(), pkg.getLabel(), pkg.getIcon(), pkg.getPath(), pkg.getSize(), pkg.getInstallTime(), enabled, system, mark);
    }

    public ApkgManager(String packageStr, String versionStr, String labelStr, Icon icon, String pathStr, long size, long installTime, boolean enabled, boolean system, boolean mark) {
        super(packageStr, versionStr, labelStr, icon, pathStr, size, installTime);
        this.mark = mark;
        this.enabled = enabled;
        this.system = system;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public boolean isMarked() {
        return mark;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isSystem() {
        return this.system;
    }
}
