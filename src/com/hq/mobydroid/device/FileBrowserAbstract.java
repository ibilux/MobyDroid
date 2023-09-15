package com.hq.mobydroid.device;

import com.hq.jadb.MyFile;
import java.util.List;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public abstract class FileBrowserAbstract {

    private final FileBrowserListener fileBrowserListener;
    private List<MyFile> src;
    private MyFile dst;
    protected String path;

    FileBrowserAbstract(FileBrowserListener fileBrowserListener) {
        this.fileBrowserListener = fileBrowserListener;
        path = "";
    }

    public abstract List<MyFile> goTo(String path);

    public abstract List<MyFile> list(String path);

    public abstract String getParent();

    public abstract String resolvePath(String name);

    public abstract boolean rename(String src, String dst);

    public abstract boolean delete(List<String> dst);

    public abstract boolean mkdir(String dst);

    public abstract boolean mkfile(String dst);

    public abstract boolean copy(String src, String dst);

    public String getPath() {
        return path;
    }

    public void onCopy(List<MyFile> src) {
        this.src = src;
        fileBrowserListener.onCopy(this);
    }

    public void onPaste() {
        this.dst = new MyFile(path, (0x1 << 14), 0, 0);
        fileBrowserListener.onPaste(this);
    }

    public List<MyFile> getSrc() {
        return src;
    }

    public void setDst(String path) {
        this.dst = new MyFile(path, (0x1 << 14), 0, 0);
    }

    public MyFile getDst() {
        return dst;
    }
}
