/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.jadb.engine.JadbException;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.MobydroidStatic;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.MobydroidDevice;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JPanel_ScreenCapture extends javax.swing.JPanel {

    // ************************ My variable ************************
    private byte[] iconBytes;
    private int rotationAngle = 0;
    // *************************************************************

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_ScreenCapture() {
        // initialize components
        initComponents();

        // copy paste file browser listener
    }

    /**
     * Handle buttons events.
     */
    private void captureScreenHandle() {
        captureScreenshot();
    }

    private void saveScreenHandle() {
        saveScreenshot();
    }

    private void rotateLeftHandle() {
        rotateImage(-1);
    }

    private void rotateRightHandle() {
        rotateImage(+1);
    }

    /*
     * capture a screenshot
     */
    private void captureScreenshot() {
        MobydroidDevice mDevice = MobyDroid.getDevice();
        if (mDevice == null) {
            return;
        }

        try {
            // get Screenshot bytes
            iconBytes = mDevice.getScreenshot();
            // convert byte array to ImageIcon with WIDTH and HEIGTH
            drawImage();
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "CaptureScreenshot", ex);
        }
    }

    /*
     * save the screenshot
     */
    private void saveScreenshot() {
        if (iconBytes != null && iconBytes.length > 0) {
            JFileChooser fileChooser = new JFileChooser();
            File path = new File(Settings.get("ScreenCapture_SavePath"));
            if (!path.exists()) {
                path = new File(MobydroidStatic.MOBY_DATA_PATH);
            }
            fileChooser.setCurrentDirectory(path);
            fileChooser.setSelectedFile(new File("Screenshot_" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + ".png"));
            fileChooser.setDialogTitle("Save Screenshot");
            fileChooser.setFileFilter(new FileNameExtensionFilter("*.png", "png"));
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
                fos.write(iconBytes);
            } catch (IOException ex) {
                Log.log(Level.SEVERE, "SaveScreenshot", ex);
            }
            // save last directory to settings ..
            Settings.set("ScreenCapture_SavePath", fileChooser.getSelectedFile().getParent());
            Settings.save();
        }
    }

    /*
     * rotate image
     */
    private void rotateImage(int angle) {
        rotationAngle += angle;
        if (rotationAngle == 4) {
            rotationAngle = 0;
        }
        if (rotationAngle == -1) {
            rotationAngle = 3;
        }
        drawImage();
    }

    /*
     * draw and scale image to jlabel
     */
    private void drawImage() {
        // check first
        if (iconBytes == null || iconBytes.length == 0) {
            return;
        }

        // load bufered image
        BufferedImage bi;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(iconBytes));
        } catch (IOException ex) {
            return;
        }

        // rotate
        if (rotationAngle != 0) {
            AffineTransform tx = new AffineTransform();
            tx.rotate(Math.PI * rotationAngle / 2.0, bi.getWidth() / 2.0, bi.getHeight() / 2.0);
            if (rotationAngle == 1 || rotationAngle == 3) {
                double offset;
                if (rotationAngle == 1) {
                    offset = (bi.getWidth() - bi.getHeight()) / 2.0;
                } else {
                    offset = (bi.getHeight() - bi.getWidth()) / 2.0;
                }
                tx.translate(offset, offset);
            }
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            bi = op.filter(bi, null);
        }

        // get width and height
        int width = bi.getWidth();
        int height = bi.getHeight();

        // first check if we need to scale width
        if (bi.getWidth() > jLabel_ScreenShot.getWidth()) {
            //scale width to fit
            width = jLabel_ScreenShot.getWidth();
            //scale height to maintain aspect ratio
            height = (width * bi.getHeight()) / bi.getWidth();
        }
        // then check if we need to scale even with the new height
        if (height > jLabel_ScreenShot.getHeight()) {
            //scale height to fit instead
            height = jLabel_ScreenShot.getHeight();
            //scale width to maintain aspect ratio
            width = (height * bi.getWidth()) / bi.getHeight();
        }

        // set image
        jLabel_ScreenShot.setIcon(new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_DEFAULT)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel_ScreenShot = new javax.swing.JLabel();
        mButton_Capture = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Save = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_RotateLeft = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_RotateRight = new com.hq.mobydroid.gui.MaterialButtonIconV();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Screen Capture : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        jLabel_ScreenShot.setBackground(new java.awt.Color(153, 153, 153));
        jLabel_ScreenShot.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_ScreenShot.setOpaque(true);
        jLabel_ScreenShot.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                jLabel_ScreenShotAncestorResized(evt);
            }
        });

        mButton_Capture.setToolTipText("Capture");
        mButton_Capture.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                captureScreenHandle();
            }
        });
        mButton_Capture.setAlignmentX(0.0F);
        mButton_Capture.setAlignmentY(0.0F);
        mButton_Capture.setFocusable(true);
        mButton_Capture.setIcon(MaterialIcons.PHOTO_CAMERA);

        mButton_Save.setToolTipText("Save");
        mButton_Save.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                saveScreenHandle();
            }
        });
        mButton_Save.setAlignmentX(0.0F);
        mButton_Save.setAlignmentY(0.0F);
        mButton_Save.setFocusable(true);
        mButton_Save.setIcon(MaterialIcons.SAVE);

        mButton_RotateLeft.setToolTipText("Rotate Left");
        mButton_RotateLeft.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                rotateLeftHandle();
            }
        });
        mButton_RotateLeft.setAlignmentX(0.0F);
        mButton_RotateLeft.setAlignmentY(0.0F);
        mButton_RotateLeft.setFocusable(true);
        mButton_RotateLeft.setIcon(MaterialIcons.ROTATE_LEFT);

        mButton_RotateRight.setToolTipText("Rotate Right");
        mButton_RotateRight.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                rotateRightHandle();
            }
        });
        mButton_RotateRight.setAlignmentX(0.0F);
        mButton_RotateRight.setAlignmentY(0.0F);
        mButton_RotateRight.setFocusable(true);
        mButton_RotateRight.setIcon(MaterialIcons.ROTATE_RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(mButton_Capture, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_RotateLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_RotateRight, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel_ScreenShot, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mButton_Capture, mButton_RotateLeft, mButton_Save});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_ScreenShot, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(mButton_RotateLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mButton_Save, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mButton_Capture, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mButton_RotateRight, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {mButton_Capture, mButton_RotateLeft, mButton_Save});

    }// </editor-fold>//GEN-END:initComponents

    private void jLabel_ScreenShotAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_jLabel_ScreenShotAncestorResized
        drawImage();
    }//GEN-LAST:event_jLabel_ScreenShotAncestorResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_ScreenShot;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Capture;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_RotateLeft;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_RotateRight;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Save;
    // End of variables declaration//GEN-END:variables
}
