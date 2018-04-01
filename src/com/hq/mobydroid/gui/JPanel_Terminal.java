/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.jadb.engine.JadbException;
import com.hq.jadb.engine.JadbTransport;
import com.hq.jterm.JTerm;
import com.hq.jterm.JTermInputProcessor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.MobydroidStatic;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.MobydroidDevice;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JPanel_Terminal extends javax.swing.JPanel {

    // ************************ My variable ************************
    private JadbTransport jShellTransport;
    private final JTerm jTerm;
    // *************************************************************

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_Terminal() {
        // initialize components
        initComponents();

        // initialize JTerm
        JTermInputProcessor termProcessor = (String command, JTerm console) -> {
            if (jShellTransport == null) {
                updateShellTransport();
            }
            if (jShellTransport != null) {
                try {
                    console.remove(command.length());
                    jShellTransport.write(command);
                } catch (IOException ex) {
                    jShellTransport = null;
                    Log.log(Level.SEVERE, "ShellTransportWrite", ex);
                }
            }
        };

        jTerm = new JTerm(jTextPane_Jterm, termProcessor, Color.BLACK, Color.GREEN, new Font(Font.MONOSPACED, Font.BOLD, 12));
    }

    /**
     * Update shell transport status.
     */
    public void updateShellTransport() {
        // get device and shell transporter
        if (jShellTransport == null) {
            MobydroidDevice mDevice = MobyDroid.getDevice();
            if (mDevice == null) {
                return;
            }

            try {
                jShellTransport = mDevice.executeShell();
                new Thread(() -> {
                    byte[] buffer = new byte[8 * 1024];
                    int len;
                    while (jShellTransport != null) {
                        try {
                            while ((len = jShellTransport.read(buffer)) > 0) {
                                String string = new String(buffer, 0, len, Charset.forName("utf-8"));
                                jTerm.write(string, false);
                            }
                        } catch (IOException ex) {
                            len = -1;
                        }
                        if (len == -1) {
                            jShellTransport = null;
                            break;
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                        }
                    }
                    // exit falg
                    jTerm.write("Terminated ...", true);
                }).start();
            } catch (IOException | JadbException ex) {
                jShellTransport = null;
                Log.log(Level.SEVERE, "UpdateShellTransport", ex);
            }
        }
    }

    private void clearHandle() {
        jTerm.cls();
    }

    private void saveHandle() {
        saveTermText();
    }

    /*
     * Save Terminal Text.
     */
    private void saveTermText() {
        String jTermText = jTerm.read();
        if (jTermText != null && jTermText.length() > 0) {
            JFileChooser fileChooser = new JFileChooser();
            File path = new File(Settings.get("Terminal_SavePath"));
            if (!path.exists()) {
                path = new File(MobydroidStatic.MOBY_DATA_PATH);
            }
            fileChooser.setCurrentDirectory(path);
            fileChooser.setSelectedFile(new File("TerminalText_" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + ".txt"));
            fileChooser.setDialogTitle("Save Terminal Text");
            fileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
            File file;
            do {
                if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                file = fileChooser.getSelectedFile();
                if (!file.exists()) {
                    break;
                }
                if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Replace File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ResourceLoader.MaterialIcons_WARNING) == JOptionPane.YES_OPTION) {
                    break;
                }
            } while (true);

            // save file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(jTermText.getBytes());
            } catch (IOException ex) {
            }
            // save last directory to settings ..
            Settings.set("Terminal_SavePath", fileChooser.getSelectedFile().getParent());
            Settings.save();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mButton_Clear = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Save = new com.hq.mobydroid.gui.MaterialButtonIconV();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane_Jterm = new javax.swing.JTextPane();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Screen Capture : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        mButton_Clear.setToolTipText("Clear");
        mButton_Clear.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                clearHandle();
            }
        });
        mButton_Clear.setAlignmentX(0.0F);
        mButton_Clear.setAlignmentY(0.0F);
        mButton_Clear.setFocusable(true);
        mButton_Clear.setIcon(MaterialIcons.CLEAR_ALL);

        mButton_Save.setToolTipText("Save");
        mButton_Save.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                saveHandle();
            }
        });
        mButton_Save.setAlignmentX(0.0F);
        mButton_Save.setAlignmentY(0.0F);
        mButton_Save.setFocusable(true);
        mButton_Save.setIcon(MaterialIcons.SAVE);

        jScrollPane1.setViewportView(jTextPane_Jterm);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mButton_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mButton_Clear, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mButton_Clear, mButton_Save});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mButton_Clear, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {mButton_Clear, mButton_Save});

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextPane_Jterm;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Clear;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Save;
    // End of variables declaration//GEN-END:variables
}
