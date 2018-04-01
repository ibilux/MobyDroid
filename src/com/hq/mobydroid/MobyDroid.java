package com.hq.mobydroid;

import com.hq.jadb.Jadb;
import com.hq.mobydroid.device.MobydroidDevice;
import com.hq.mobydroid.gui.JFrame_Main;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 *
 */
public class MobyDroid {

    // ****************** Static global variables ******************
    private static Jadb jadb;
    private static JFrame_Main jFrame_Main;
    private static MobydroidDevice mDevice;
    // *************************************************************

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // initialize 
        init();

        // initialize logger
        Log.init();

        // load settings
        Settings.load();

        // start adb
        Jadb.launchAdbServer();

        // Set the Metal look and feel
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            Log.log(Level.SEVERE, "SetLookAndFeel", ex);
        }
        //</editor-fold>

        // initialize global variables
        jadb = new Jadb();
        jFrame_Main = new JFrame_Main();

        // Create and display the form
        java.awt.EventQueue.invokeLater(() -> {
            jFrame_Main.setVisible(true);
        });
    }

    private static void init() {
        // Makre sure that files and directories exists
        try {
            File file;
            file = new File(MobydroidStatic.MOBY_DATA_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(MobydroidStatic.MOBY_HOME_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(MobydroidStatic.SETTINGS_PATH);
            if (!file.exists()) {
                Settings.save();
            }
        } catch (Exception ex) {
            Log.log(Level.SEVERE, "init", ex);
        }
    }

    public static Jadb getJadb() {
        return jadb;
    }

    public static void setDevice(MobydroidDevice device) {
        mDevice = device;
    }

    public static MobydroidDevice getDevice() {
        return mDevice;
    }

    public static void notifyTaskPropertyChangeEvent() {
        jFrame_Main.notifyTaskPropertyChangeEvent();
    }

    public static void setProgressBarValue(int value) {
        jFrame_Main.setProgressBarValue(value);
    }

    public static void setProgressBarString(String string) {
        jFrame_Main.setProgressBarString(string);
    }
}
