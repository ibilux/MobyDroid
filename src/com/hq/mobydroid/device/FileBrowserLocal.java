package com.hq.mobydroid.device;

import com.hq.jadb.MyFile;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobydroidStatic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class FileBrowserLocal extends FileBrowserAbstract {

    public FileBrowserLocal(FileBrowserListener fileBrowserListener) {
        super(fileBrowserListener);
    }

    @Override
    public List<MyFile> goTo(String path) {
        if (path.isEmpty()) {
            this.path = Paths.get(MobydroidStatic.HOME_PATH).toString();
        } else {
            this.path = path;
        }

        return list(this.path);
    }

    @Override
    public List<MyFile> list(String path) {
        File folder = new File(path);
        List<MyFile> list = new ArrayList<>();
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                // for more details about file mode, see : https://github.com/ibilux/understanding-file-mode
                int filemode = (file.canRead() ? 4 : 0) + (file.canWrite() ? 2 : 0) + (file.canExecute() ? 1 : 0);
                if (file.isDirectory()) {
                    filemode += (0x1 << 14);
                } else {
                    filemode += (0x1 << 15);
                }
                list.add(new MyFile(file.getAbsolutePath(), filemode, file.length(), file.lastModified()));
            }
        }
        return list;
    }

    @Override
    public String getParent() {
        Path parent = Paths.get(this.path).getParent();
        if (parent == null) {
            return "";
        }
        return parent.toString();
    }

    @Override
    public String resolvePath(String name) {
        return Paths.get(this.path).resolve(name).toString();
    }

    @Override
    public boolean rename(String src, String dst) {
        return (new File(src)).renameTo(new File(dst));
    }

    @Override
    public boolean delete(List<String> dst) {
        dst.forEach((file) -> {
            (new File(file)).delete();
        });
        return true;
    }

    @Override
    public boolean mkdir(String dst) {
        return (new File(dst)).mkdir();
    }

    @Override
    public boolean mkfile(String dst) {
        try {
            return (new File(dst)).createNewFile();
        } catch (IOException ex) {
            Log.log(Level.SEVERE, "FileBrowserMakeLocalFile", ex);
        }
        return false;
    }

    @Override
    public boolean copy(String src, String dst) {
        //Use bytes stream to support all file types
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dst);) {
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException ex) {
            Log.log(Level.SEVERE, "FileBrowserCopyLocalFile", ex);
            return false;
        }
        return true;
        /*// using files
        try {
            Files.copy(new File(src).toPath(), new File(dst).toPath());
        } catch (IOException ex) {
        }*/
    }
}
