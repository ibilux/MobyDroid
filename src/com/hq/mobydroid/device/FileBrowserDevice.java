package com.hq.mobydroid.device;

import com.hq.jadb.MyFile;
import com.hq.jadb.engine.JadbException;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class FileBrowserDevice extends FileBrowserAbstract {

    public FileBrowserDevice(FileBrowserListener fileBrowserListener) {
        super(fileBrowserListener);
    }

    @Override
    public List<MyFile> goTo(String path) {
        if (path.isEmpty()) {
            this.path = "/sdcard";
        } else {
            this.path = Paths.get(path).toString().replace('\\', '/'); // work around path escape bug
        }

        return list(this.path);
    }

    @Override
    public List<MyFile> list(String path) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return new ArrayList<>();
        }
        // Fetch files list from the device
        try {
            return device.list(path);
        } catch (IOException | JadbException ex) {
            // "Failed to fetch files list"
            Log.log(Level.SEVERE, "FileBrowserDeviceList", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public String getParent() {
        Path parent = Paths.get(this.path).getParent();
        if (parent == null) {
            return "";
        }

        return parent.toString().replace('\\', '/'); // work around path escape bug
    }

    @Override
    public String resolvePath(String name) {
        return Paths.get(this.path).resolve(name).toString().replace('\\', '/'); // work around path escape bug
    }

    @Override
    public boolean rename(String src, String dst) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return false;
        }
        try {
            return device.rename(src, dst);
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "FileBrowserRenameDeviceFile", ex);
        }
        return false;
    }

    @Override
    public boolean delete(List<String> dst) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return false;
        }
        dst.forEach((file) -> {
            try {
                device.delete(file);
            } catch (IOException | JadbException ex) {
                Log.log(Level.SEVERE, "FileBrowserDeleteDeviceFile", ex);
            }
        });
        return true;
    }

    @Override
    public boolean mkdir(String dst) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return false;
        }
        try {
            return device.mkdir(dst);
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "FileBrowserMakeDeviceDir", ex);
        }
        return false;
    }

    @Override
    public boolean mkfile(String dst) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return false;
        }
        try {
            return device.mkfile(dst);
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "FileBrowserMakeDeviceFile", ex);
        }
        return false;
    }

    @Override
    public boolean copy(String src, String dst) {
        MobydroidDevice device = MobyDroid.getDevice();
        if (device == null) {
            return false;
        }
        try {
            return device.copy(src, dst);
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "FileBrowserCopyDeviceFile", ex);
        }
        return false;
    }
}
