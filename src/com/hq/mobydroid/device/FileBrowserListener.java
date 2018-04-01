package com.hq.mobydroid.device;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public interface FileBrowserListener {

    public void onCopy(FileBrowserAbstract src);

    public void onPaste(FileBrowserAbstract dst);
}
