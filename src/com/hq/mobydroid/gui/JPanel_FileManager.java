package com.hq.mobydroid.gui;

import com.hq.jadb.engine.JadbException;
import com.hq.jadb.MyFile;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.FileBrowserAbstract;
import com.hq.mobydroid.device.FileBrowserListener;
import com.hq.mobydroid.device.FileBrowserLocal;
import com.hq.mobydroid.device.FileBrowserDevice;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JPanel_FileManager extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final JPanel_FileBrowser jPanel_FileBrowser_Local;
    private final JPanel_FileBrowser jPanel_FileBrowser_Device;
    private final FileBrowserLocal fileBrowserLocal;
    private final FileBrowserDevice fileBrowserDevice;
    private final FileBrowserListener fileBrowserListener;
    private final JDialog_Working workingDialog;
    private FileBrowserAbstract copyFrom;
    private FileBrowserAbstract copyTo;
    // *************************************************************

    /**
     * Creates new form JPanel_ManageApps
     */
    public JPanel_FileManager() {
        // initialize components
        initComponents();

        // initialize working dialog
        workingDialog = new JDialog_Working((JFrame) this.getParent(), true);
        workingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
            }
        });

        // copy paste file browser listener
        fileBrowserListener = new FileBrowserListener() {
            @Override
            public void onCopy(FileBrowserAbstract src) {
                copyFrom = src;
            }

            @Override
            public void onPaste(FileBrowserAbstract dst) {
                copyTo = dst;
                // paste
                seeknPaste();
            }
        };

        // initialize file browsers for both local and device
        fileBrowserLocal = new FileBrowserLocal(fileBrowserListener);
        fileBrowserDevice = new FileBrowserDevice(fileBrowserListener);

        // initialize JPanels
        jPanel_FileBrowser_Local = new JPanel_FileBrowser(fileBrowserLocal);
        jPanel_FileBrowser_Device = new JPanel_FileBrowser(fileBrowserDevice);

        // show JPanel_FileBrowser for local host
        jPanel_Local.setVisible(false);
        jPanel_Local.removeAll();
        jPanel_Local.setLayout(new java.awt.BorderLayout());
        jPanel_Local.add(jPanel_FileBrowser_Local);
        jPanel_Local.setVisible(true);

        // show JPanel_FileBrowser for device
        jPanel_Device.setVisible(false);
        jPanel_Device.removeAll();
        jPanel_Device.setLayout(new java.awt.BorderLayout());
        jPanel_Device.add(jPanel_FileBrowser_Device);
        jPanel_Device.setVisible(true);
    }

    /**
     * Update files list.
     */
    public void updateFilesList() {
        jPanel_FileBrowser_Local.refreshHandle();
        jPanel_FileBrowser_Device.refreshHandle();
    }

    /**
     * search and copy files in sub folders.
     *
     */
    private void seeknPaste() {
        SwingWorker copyWorker = new SwingWorker<Void, File>() {
            @Override
            public Void doInBackground() {
                // disable user interface
                jPanel_FileBrowser_Local.disableUI();
                jPanel_FileBrowser_Device.disableUI();

                // copy > paste
                if (copyFrom instanceof FileBrowserLocal) { // copy from local
                    if (copyTo instanceof FileBrowserLocal) { // to local
                        // only for expert
                        if (Boolean.valueOf(Settings.get("Express_Settings"))) {
                            startnCopy((srcFile, dstFile) -> {
                                copyTo.copy(srcFile, dstFile);
                            });
                        }
                    } else if (copyTo instanceof FileBrowserDevice) { // to device
                        startnCopy((srcFile, dstFile) -> {
                            try {
                                MobyDroid.getDevice().push(srcFile, new MyFile(dstFile, 16895, 0, (new Date()).getTime()));
                            } catch (IOException | JadbException ex) {
                                Log.log(Level.SEVERE, "OnPastePush", ex);
                            }
                        });
                    }
                } else if (copyFrom instanceof FileBrowserDevice) { // copy from device
                    if (copyTo instanceof FileBrowserDevice) { // to device
                        // only for expert
                        if (Boolean.valueOf(Settings.get("Express_Settings"))) {
                            copyFrom.getSrc().forEach(src -> {
                                copyTo.copy(src.getPath(), copyTo.getDst().getPath());
                            });
                        }
                    } else if (copyTo instanceof FileBrowserLocal) { // to local
                        startnCopy((srcFile, dstFile) -> {
                            try {
                                MobyDroid.getDevice().pull(srcFile, dstFile);
                            } catch (IOException | JadbException ex) {
                                Log.log(Level.SEVERE, "OnPastePull", ex);
                            }
                        });
                    }
                }

                return null;
            }

            @Override

            protected void done() {
                // enable user interface
                jPanel_FileBrowser_Local.enableUI();
                jPanel_FileBrowser_Device.enableUI();
                // hide working dialog and turn off the wait cursor
                workingDialog.setVisible(false);
                setCursor(null);
            }
        };
        copyWorker.execute();

        // show working dialog and turn on the wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        workingDialog.setVisible(true);
        copyWorker.cancel(true);
    }

    private interface SwingWorkerPublisher {

        void copy(String src, String dst);
    }

    private void startnCopy(SwingWorkerPublisher swingWorkerPublisher) {
        // copy > paste
        copyFrom.getSrc().forEach(srcFile -> {
            String dstFile = copyTo.getDst().resolve(srcFile.getName());
            if (srcFile.isDirectory()) {
                // seek n search
                seeknCopy(srcFile.getPath(), dstFile, swingWorkerPublisher);
            } else {
                // if it's a file then copy it
                swingWorkerPublisher.copy(srcFile.getPath(), dstFile);
                //System.out.println(srcFile.getPath() + "\t\t > " + dstFile);
            }
        });
    }

    private void seeknCopy(String src, String dst, SwingWorkerPublisher swingWorkerPublisher) {
        // create destination directory
        copyTo.mkdir(dst);

        // list all the directory contents
        List<MyFile> list = copyFrom.list(src);
        list.forEach(srcFile -> {
            // construct the dst file structure
            String dstFile = Paths.get(dst, srcFile.getName()).toString();
            if (srcFile.isDirectory()) {
                // recursive copy
                seeknCopy(srcFile.getPath(), dstFile, swingWorkerPublisher);
            } else {
                // copy file
                swingWorkerPublisher.copy(srcFile.getPath(), dstFile);
                //System.out.println(srcFile.getPath() + "\t\t > " + dstFile);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_Local = new javax.swing.JPanel();
        jPanel_Device = new javax.swing.JPanel();
        jLabel_Local = new javax.swing.JLabel();
        jLabel_Device = new javax.swing.JLabel();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Manager : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        jPanel_Local.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Local.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Local.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        javax.swing.GroupLayout jPanel_LocalLayout = new javax.swing.GroupLayout(jPanel_Local);
        jPanel_Local.setLayout(jPanel_LocalLayout);
        jPanel_LocalLayout.setHorizontalGroup(
            jPanel_LocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel_LocalLayout.setVerticalGroup(
            jPanel_LocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel_Device.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Device.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Device.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        javax.swing.GroupLayout jPanel_DeviceLayout = new javax.swing.GroupLayout(jPanel_Device);
        jPanel_Device.setLayout(jPanel_DeviceLayout);
        jPanel_DeviceLayout.setHorizontalGroup(
            jPanel_DeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel_DeviceLayout.setVerticalGroup(
            jPanel_DeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 233, Short.MAX_VALUE)
        );

        jLabel_Local.setForeground(new java.awt.Color(97, 97, 97));
        jLabel_Local.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Local.setText("Local");

        jLabel_Device.setForeground(new java.awt.Color(97, 97, 97));
        jLabel_Device.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Device.setText("Device");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Local, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addComponent(jPanel_Local, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Device, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_Device, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Device)
                    .addComponent(jLabel_Local, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Local, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_Device, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_Device;
    private javax.swing.JLabel jLabel_Local;
    private javax.swing.JPanel jPanel_Device;
    private javax.swing.JPanel jPanel_Local;
    // End of variables declaration//GEN-END:variables
}
