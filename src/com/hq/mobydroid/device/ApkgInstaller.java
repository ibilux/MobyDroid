package com.hq.mobydroid.device;

import com.hq.apktool.Apkg;
import javax.swing.Icon;

/**
 * A Class to hold android package installer.
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class ApkgInstaller extends Apkg {

    private boolean onsdcard;
    private boolean reinstall;
    private boolean downgrade;

    public ApkgInstaller(Apkg pkg, boolean onsdcard, boolean reinstall, boolean downgrade) {
        this(pkg.getPackage(), pkg.getVersion(), pkg.getLabel(), pkg.getIcon(), pkg.getPath(), pkg.getSize(), pkg.getInstallTime(), onsdcard, reinstall, downgrade);
    }

    public ApkgInstaller(String packageStr, String versionStr, String labelStr, Icon icon, String pathStr, long size, long installTime, boolean onsdcard, boolean reinstall, boolean downgrade) {
        super(packageStr, versionStr, labelStr, icon, pathStr, size, installTime);
        this.onsdcard = onsdcard;
        this.reinstall = reinstall;
        this.downgrade = downgrade;
    }

    public void setOnSdcard(boolean onsdcard) {
        this.onsdcard = onsdcard;
    }

    public void setReinstall(boolean reinstall) {
        this.reinstall = reinstall;
    }

    public void setDowngrade(boolean downgrade) {
        this.downgrade = downgrade;
    }

    public boolean isOnSdcard() {
        return onsdcard;
    }

    public boolean isReinstallable() {
        return reinstall;
    }

    public boolean isDowngradable() {
        return downgrade;
    }

}
