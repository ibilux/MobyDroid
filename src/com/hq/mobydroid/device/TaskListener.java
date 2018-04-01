package com.hq.mobydroid.device;

import java.util.List;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 * @param <T>
 */
public interface TaskListener<T extends Object> {

    public void onStart();

    public void onProcess(List<T> list);

    public void onDone();
}
