package com.hq.mobydroid.device;

import java.beans.PropertyChangeEvent;
import javax.swing.Icon;
import javax.swing.SwingWorker;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 * @param <T>
 * @param <V>
 *
 */
public abstract class TaskWorker<T extends Object, V extends Object> extends SwingWorker {

    protected final MobydroidDevice device;
    private boolean mark;

    public TaskWorker(MobydroidDevice device) {
        // set the device and listner
        this.device = device;
        // add a PropertyChangeEvent Listener
        this.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            this.device.notifyTaskPropertyChangeEvent(evt);
        });
    }

    public abstract Status getStatus();

    public abstract String getName();

    public abstract String getMessage();

    public abstract Icon getIcon();

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public boolean isMarked() {
        return mark;
    }

    public static enum Status {
        PENDING, STARTED, DONE, CANCELLED, FAILED;

        @Override
        public String toString() {
            switch (this) {
                case PENDING:
                    return "Pending";
                case STARTED:
                    return "Running";
                case DONE:
                    return "Finished";
                case CANCELLED:
                    return "Cancelled";
                case FAILED:
                    return "Failed";
                default:
                    return null;
            }
        }
    };

}
