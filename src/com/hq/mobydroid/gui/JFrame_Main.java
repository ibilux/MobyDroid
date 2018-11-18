package com.hq.mobydroid.gui;

import com.hq.jadb.Jadb;
import com.hq.jadb.JadbDevice;
import com.hq.jadb.engine.JadbDeviceWatcherListener;
import com.hq.jadb.engine.JadbException;
import com.hq.jadb.engine.JadbStatics;
import com.hq.jadb.manager.JadbDeviceProperties;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.MobydroidDevice;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.Timer;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JFrame_Main extends javax.swing.JFrame {

    // ************************** My variable **************************
    private final JPanel_Home jPanel_Home = new JPanel_Home();
    private final JPanel_TaskManager jPanel_TaskManager = new JPanel_TaskManager();
    private final JPanel_Settings jPanel_Settings = new JPanel_Settings();
    private final JPanel_AppManager jPanel_AppManager = new JPanel_AppManager();
    private final JPanel_AppInstaller jPanel_AppInstaller = new JPanel_AppInstaller();
    private final JPanel_FileManager jPanel_FileManager = new JPanel_FileManager();
    private final JPanel_ScreenCapture jPanel_ScreenCapture = new JPanel_ScreenCapture();
    private final JPanel_DeviceInfo jPanel_DeviceInfo = new JPanel_DeviceInfo();
    private final JPanel_Terminal jPanel_Terminal = new JPanel_Terminal();
    private final ButtonMouseListener buttonMouseListener = new ButtonMouseListener();
    private final JadbDeviceWatcherListener devicesWatcher;
    private final List<MobydroidDevice> mDevices = new ArrayList<>();
    private MobydroidDevice mDevice;
    // *****************************************************************

    /**
     * Creates new form JFrame_Main
     */
    public JFrame_Main() {
        // initialize components
        initComponents();

        // set components properties
        jButton_Close.addMouseListener(buttonMouseListener);
        jButton_Minimize.addMouseListener(buttonMouseListener);
        jButton_Maximize.addMouseListener(buttonMouseListener);

        // set window adapter for dragging and resizing
        WindowAdapter windowAdapter = new WindowAdapter();
        this.addMouseListener(windowAdapter);
        this.addMouseMotionListener(windowAdapter);

        // get adb status
        updateAdbStatus();

        // get mDevices list
        updateDevices();

        // start mDevices watcher
        devicesWatcher = new JadbDeviceWatcherListener() {
            @Override
            public void onDetect(Map<JadbStatics.State, JadbDevice> detectedDevices) {
                updateDevices();
            }

            @Override
            public void onException(Exception e) {
            }

            @Override
            public void onStop() {
            }
        };
        startDevicesWatcher();

        // start ADB statusmonitor timer
        new Timer(1000, (ActionEvent evt) -> {
            // get adb status
            updateAdbStatus();

            // start DeviceWatcher
            if (!MobyDroid.getJadb().isDeviceWatcherAlive()) {
                startDevicesWatcher();
            }
        }).start();

        // add home panel
        homeHandle();

        // hide for non expert
        if (!Boolean.valueOf(Settings.get("Express_Settings"))) {
            jButton_Terminal.setVisible(false);
        }
    }

    /**
     * Notify when a new TaskWorker PropertyChangeEvent fired to update task
     * manager table and update ProgressBar.
     */
    public void notifyTaskPropertyChangeEvent() {
        //
        jPanel_TaskManager.fireTableDataChanged();
        //
        float progress = mDevice.getTasksProgress();
        setProgressBarValue((int) progress);
        int running = mDevice.getRunningTasksNumber();
        setProgressBarString((running > 0 ? (running + " task(s) in progress " + String.format("%.2f", progress) + " %") : ("No running tasks")));
    }

    /**
     * set ProgressBar Value
     *
     * @param value
     */
    public void setProgressBarValue(int value) {
        jProgressBar_Main.setValue(value);
    }

    /**
     * set ProgressBar String
     *
     * @param string
     */
    public void setProgressBarString(String string) {
        jProgressBar_Main.setString(string);
    }

    /**
     * update adb version
     */
    private void updateAdbStatus() {
        try {
            jLabel_AdbVersion.setText("ADB version " + MobyDroid.getJadb().getHostVersion());
            jLabel_AdbVersion.setForeground(MaterialColor.GREEN_700);
        } catch (IOException | JadbException ex) {
            // show error message
            jLabel_AdbVersion.setText("ADB not started !");
            jLabel_AdbVersion.setForeground(MaterialColor.REDA_700);
            // try to start adb
            Jadb.launchAdbServer();
        }
    }

    /**
     *
     */
    private void updateDevices() {
        // get devices list
        Map<JadbStatics.State, JadbDevice> devices;
        try {
            devices = MobyDroid.getJadb().getDevices();
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "UpdateDevices", ex);
            return;
        }

        // process new devices list
        devices.values().forEach((device) -> {
            MobydroidDevice jadbdevice = new MobydroidDevice(device);
            if (!mDevices.contains(jadbdevice)) {
                // add device to mDevices list
                mDevices.add(jadbdevice);
                // add device to comboBox
                String deviceName;
                try {
                    JadbDeviceProperties jadbDeviceProperties = jadbdevice.getDeviceProperties();
                    deviceName = "<html><b>";
                    deviceName += jadbDeviceProperties.getProductManufacturer();
                    deviceName += "</b><br>";
                    if (jadbDeviceProperties.getProductModel().length() > 20) {
                        deviceName += jadbDeviceProperties.getProductModel().substring(0, 20);
                    }else{
                        deviceName += jadbDeviceProperties.getProductModel();
                    }
                    deviceName += "</html>";
                } catch (IOException | JadbException ex) {
                    deviceName = jadbdevice.getSerial() + " (Unknown)";
                    Log.log(Level.SEVERE, "GetDeviceProperties", ex);
                }
                jComboBox_Devices.addItem(deviceName);
            }
        });

        // remove ejected devices from mDevices list
        mDevices.forEach((jadbdevice) -> {
            if (!devices.containsValue(jadbdevice)) {
                // get mDevice index in the list
                int deviceIndex = mDevices.indexOf(jadbdevice);
                // remove mDevice from mDevices list
                mDevices.remove(deviceIndex);
                // remove mDevice from comboBox
                jComboBox_Devices.removeItemAt(deviceIndex);
            }
        });
    }

    /**
     *
     */
    private void startDevicesWatcher() {
        try {
            MobyDroid.getJadb().startDevicesWatcher(devicesWatcher);
        } catch (IOException | JadbException ex) {
            Log.log(Level.SEVERE, "StartDeviceWatcher", ex);
        }
    }

    /**
     *
     */
    private void homeHandle() {
        panelHandle(jPanel_Home);
    }

    private void appManagerHandle() {
        panelHandle(jPanel_AppManager);
    }

    private void appInstallerHandle() {
        panelHandle(jPanel_AppInstaller);
    }

    private void fileManagerHandle() {
        jPanel_FileManager.updateFilesList();
        panelHandle(jPanel_FileManager);
    }

    private void settingsHandle() {
        jPanel_Settings.updateInfo();
        panelHandle(jPanel_Settings);
    }

    private void tasksHandle() {
        panelHandle(jPanel_TaskManager);
    }

    private void screenCaptureHandle() {
        panelHandle(jPanel_ScreenCapture);
    }

    private void deviceInfoHandle() {
        jPanel_DeviceInfo.updateInfo();
        panelHandle(jPanel_DeviceInfo);
    }

    private void terminalHandle() {
        jPanel_Terminal.updateShellTransport();
        panelHandle(jPanel_Terminal);
    }

    /**
     * Show a panel
     */
    private void panelHandle(java.awt.Component jComponent) {
        jPanel_MainHandler.setVisible(false);
        jPanel_MainHandler.removeAll();
        jPanel_MainHandler.setLayout(new BorderLayout());
        jPanel_MainHandler.add(jComponent);
        //pack();
        jPanel_MainHandler.setVisible(true);

    }

    /**
     *
     */
    private void exitHandle() {
        // ask
        Object[] options = {"Yes", "No"};
        int retStatus = JOptionPane.showOptionDialog(this, "Are you sure ?", "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                ResourceLoader.MaterialIcons_WARNING, //do not use a custom Icon
                options, //the titles of buttons
                options[1]); //default button title        
        if (retStatus == JOptionPane.YES_OPTION) {
            // Save Settings
            Settings.save();
            // stop mDevices watcher
            try {
                MobyDroid.getJadb().stopDevicesWatcher();
            } catch (IOException ex) {
                Log.log(Level.SEVERE, "StopDevicesWatcher", ex);
            }
            // exit
            System.exit(0);
        }
    }

    /**
     *
     */
    private void maximizeHandle() {
        if ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            this.setExtendedState(this.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
        } else {
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    public class ComboBoxRenderar extends JLabel implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setBackground(isSelected ? MaterialColor.TEAL_100 : list.getBackground());
            setOpaque(isSelected);
            setIcon(ResourceLoader.MaterialIcons_PHONE_ANDROID);
            setText((String) value);
            setFont(list.getFont());
            return this;
        }

    }

    // ************************************************************* //
    // ************************************************************* //
    /**
     *
     */
    private class ButtonMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent me) {
        }

        @Override
        public void mousePressed(MouseEvent me) {
            //component.setBackground(MaterialColor.BLUE_500);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            //component.setBackground(MaterialColor.BLUE_600);
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            me.getComponent().setBackground(MaterialColor.BLUE_600);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            me.getComponent().setBackground(MaterialColor.BLUE_700);
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    /**
     * Window mover (drag) and resizer.
     */
    class WindowAdapter extends MouseAdapter {

        private boolean dragging = false;
        private boolean resizing = false;
        private int prevX = 0;
        private int prevY = 0;
        private Rectangle prevR;
        private int outcode = 0;
        private final int PROX_DIST = 5;// Give user some leeway for selections.

        @Override
        public void mouseClicked(MouseEvent me) {
            // maximize if clicked two times on jPanel_TitleBar
            if (jPanel_TitleBar.contains(me.getPoint())) {
                if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2) {
                    maximizeHandle();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.getButton() == MouseEvent.BUTTON1) {
                prevX = me.getXOnScreen();
                prevY = me.getYOnScreen();
                if ((outcode = getOutcode(me.getPoint(), new Rectangle(me.getComponent().getSize()))) != 0) {
                    resizing = true;
                    prevR = me.getComponent().getBounds();
                } else if (jPanel_TitleBar.contains(me.getPoint()) && ((getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH)) {
                    dragging = true;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            resizing = false;
            dragging = false;
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            // dragging
            if (dragging) {
                int x = me.getXOnScreen();
                int y = me.getYOnScreen();
                // Move frame by the mouse delta
                setLocation(getLocationOnScreen().x + x - prevX, getLocationOnScreen().y + y - prevY);
                prevX = x;
                prevY = y;
            }
            // resizing
            if (resizing) {
                Component component = me.getComponent();
                Rectangle rect = prevR;
                int xInc = me.getXOnScreen() - prevX;
                int yInc = me.getYOnScreen() - prevY;

                //  Resizing the West or North border affects the size and location
                switch (outcode) {
                    case Rectangle.OUT_TOP:
                        rect.y += yInc;
                        rect.height -= yInc;
                        break;
                    case Rectangle.OUT_TOP + Rectangle.OUT_LEFT:
                        rect.y += yInc;
                        rect.height -= yInc;
                        rect.x += xInc;
                        rect.width -= xInc;
                        break;
                    case Rectangle.OUT_LEFT:
                        rect.x += xInc;
                        rect.width -= xInc;
                        break;
                    case Rectangle.OUT_LEFT + Rectangle.OUT_BOTTOM:
                        rect.height += yInc;
                        rect.x += xInc;
                        rect.width -= xInc;
                        break;
                    case Rectangle.OUT_BOTTOM:
                        rect.height += yInc;
                        break;
                    case Rectangle.OUT_BOTTOM + Rectangle.OUT_RIGHT:
                        rect.height += yInc;
                        rect.width += xInc;
                        break;
                    case Rectangle.OUT_RIGHT:
                        rect.width += xInc;
                        break;
                    case Rectangle.OUT_RIGHT + Rectangle.OUT_TOP:
                        rect.y += yInc;
                        rect.height -= yInc;
                        rect.width += xInc;
                        break;
                    default:
                        break;
                }

                prevX = me.getXOnScreen();
                prevY = me.getYOnScreen();

                component.setBounds(rect);
                component.validate();
                component.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            Component component = me.getComponent();
            Point point = me.getPoint();

            // Locate cursor relative to center of rect.
            Rectangle rect = new Rectangle(component.getSize());
            switch (getOutcode(point, new Rectangle(component.getSize()))) {
                case Rectangle.OUT_TOP:
                    if (Math.abs(point.y - rect.y) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_TOP + Rectangle.OUT_LEFT:
                    if (Math.abs(point.y - rect.y) < PROX_DIST && Math.abs(point.x - rect.x) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_LEFT:
                    if (Math.abs(point.x - rect.x) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_LEFT + Rectangle.OUT_BOTTOM:
                    if (Math.abs(point.x - rect.x) < PROX_DIST && Math.abs(point.y - (rect.y + rect.height)) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_BOTTOM:
                    if (Math.abs(point.y - (rect.y + rect.height)) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_BOTTOM + Rectangle.OUT_RIGHT:
                    if (Math.abs(point.x - (rect.x + rect.width)) < PROX_DIST && Math.abs(point.y - (rect.y + rect.height)) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_RIGHT:
                    if (Math.abs(point.x - (rect.x + rect.width)) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    }
                    break;
                case Rectangle.OUT_RIGHT + Rectangle.OUT_TOP:
                    if (Math.abs(point.x - (rect.x + rect.width)) < PROX_DIST && Math.abs(point.y - rect.y) < PROX_DIST) {
                        component.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                    }
                    break;
                default:    // center
                    component.setCursor(Cursor.getDefaultCursor());
            }
        }

        /**
         * Make a smaller Rectangle and use it to locate the cursor relative to
         * the Rectangle center.
         */
        private int getOutcode(Point point, Rectangle rect) {
            Rectangle r = (Rectangle) rect.clone();
            r.grow(-PROX_DIST, -PROX_DIST);
            return r.outcode(point.x, point.y);
        }
    }
    // ************************************************************* //
    // ************************************************************* //

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_MainPanel = new javax.swing.JPanel();
        jPanel_TitleBar = new javax.swing.JPanel();
        jButton_Minimize = new javax.swing.JButton();
        jButton_Close = new javax.swing.JButton();
        jButton_Maximize = new javax.swing.JButton();
        jLabel_MainLabel = new javax.swing.JLabel();
        jPanel_MainHandler = new javax.swing.JPanel();
        jPanel_Button = new javax.swing.JPanel();
        jComboBox_Devices = new javax.swing.JComboBox<>();
        jButton_Home = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_AppManager = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_AppInstaller = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_FileManager = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_TaskManager = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_ScreenCapture = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_PhoneInfo = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_Terminal = new com.hq.mobydroid.gui.MaterialButtonH();
        jButton_Settings = new com.hq.mobydroid.gui.MaterialButtonH();
        jPanel_Status = new javax.swing.JPanel();
        jLabel_AdbVersion = new javax.swing.JLabel();
        jSeparator_vertical1 = new javax.swing.JSeparator();
        jProgressBar_Main = new javax.swing.JProgressBar();
        jLabel_MainMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("MobyDroid");
        setMinimumSize(new java.awt.Dimension(796, 496));
        setName("MainFrame"); // NOI18N
        setUndecorated(true);
        setSize(new java.awt.Dimension(0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel_MainPanel.setBackground(new java.awt.Color(250, 250, 250));

        jPanel_TitleBar.setBackground(new java.awt.Color(25, 118, 210));
        jPanel_TitleBar.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(25, 118, 210)));

        jButton_Minimize.setBackground(new java.awt.Color(25, 118, 210));
        jButton_Minimize.setFont(MaterialIcons.ICON_FONT.deriveFont(20f));
        jButton_Minimize.setForeground(new java.awt.Color(255, 255, 255));
        jButton_Minimize.setText(String.valueOf(MaterialIcons.EXPAND_MORE));
        jButton_Minimize.setToolTipText("Minimize");
        jButton_Minimize.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton_Minimize.setBorderPainted(false);
        jButton_Minimize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MinimizeActionPerformed(evt);
            }
        });

        jButton_Close.setBackground(new java.awt.Color(25, 118, 210));
        jButton_Close.setFont(MaterialIcons.ICON_FONT.deriveFont(20f));
        jButton_Close.setForeground(new java.awt.Color(255, 255, 255));
        jButton_Close.setText(String.valueOf(MaterialIcons.CLOSE));
        jButton_Close.setToolTipText("Exit");
        jButton_Close.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton_Close.setBorderPainted(false);
        jButton_Close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_CloseActionPerformed(evt);
            }
        });

        jButton_Maximize.setBackground(new java.awt.Color(25, 118, 210));
        jButton_Maximize.setFont(MaterialIcons.ICON_FONT.deriveFont(20f));
        jButton_Maximize.setForeground(new java.awt.Color(255, 255, 255));
        jButton_Maximize.setText(String.valueOf(MaterialIcons.FULLSCREEN));
        jButton_Maximize.setToolTipText("Maximize");
        jButton_Maximize.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jButton_Maximize.setBorderPainted(false);
        jButton_Maximize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_MaximizeActionPerformed(evt);
            }
        });

        jLabel_MainLabel.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel_MainLabel.setForeground(new java.awt.Color(255, 255, 255));
        jLabel_MainLabel.setText("MobyDroid");

        javax.swing.GroupLayout jPanel_TitleBarLayout = new javax.swing.GroupLayout(jPanel_TitleBar);
        jPanel_TitleBar.setLayout(jPanel_TitleBarLayout);
        jPanel_TitleBarLayout.setHorizontalGroup(
            jPanel_TitleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_TitleBarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_MainLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Minimize, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Maximize, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Close, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel_TitleBarLayout.setVerticalGroup(
            jPanel_TitleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_TitleBarLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel_TitleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_MainLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel_TitleBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton_Maximize, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton_Close, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton_Minimize, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6))
        );

        jPanel_MainHandler.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_MainHandler.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(217, 217, 217)));

        javax.swing.GroupLayout jPanel_MainHandlerLayout = new javax.swing.GroupLayout(jPanel_MainHandler);
        jPanel_MainHandler.setLayout(jPanel_MainHandlerLayout);
        jPanel_MainHandlerLayout.setHorizontalGroup(
            jPanel_MainHandlerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel_MainHandlerLayout.setVerticalGroup(
            jPanel_MainHandlerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel_Button.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Button.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(217, 217, 217)));

        jComboBox_Devices.setBackground(new java.awt.Color(250, 250, 250));
        jComboBox_Devices.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jComboBox_Devices.setForeground(new java.awt.Color(97, 97, 97));
        jComboBox_Devices.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(250, 250, 250)));
        jComboBox_Devices.setRenderer(new ComboBoxRenderar());
        jComboBox_Devices.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox_DevicesItemStateChanged(evt);
            }
        });

        jButton_Home.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                homeHandle();
            }
        });
        jButton_Home.setFocusable(true);
        jButton_Home.setIcon(MaterialIcons.HOME);
        jButton_Home.setText("Home");

        jButton_AppManager.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                appManagerHandle();
            }
        });
        jButton_AppManager.setFocusable(true);
        jButton_AppManager.setIcon(MaterialIcons.APPS);
        jButton_AppManager.setText("App Manager");

        jButton_AppInstaller.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                appInstallerHandle();
            }
        });
        jButton_AppInstaller.setFocusable(true);
        jButton_AppInstaller.setIcon(MaterialIcons.ARCHIVE);
        jButton_AppInstaller.setText("App Installer");

        jButton_FileManager.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                fileManagerHandle();
            }
        });
        jButton_FileManager.setFocusable(true);
        jButton_FileManager.setIcon(MaterialIcons.FOLDER_OPEN);
        jButton_FileManager.setText("File Manager");

        jButton_TaskManager.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                tasksHandle();
            }
        });
        jButton_TaskManager.setFocusable(true);
        jButton_TaskManager.setIcon(MaterialIcons.MEMORY);
        jButton_TaskManager.setText("Tasks");

        jButton_ScreenCapture.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                screenCaptureHandle();
            }
        });
        jButton_ScreenCapture.setFocusable(true);
        jButton_ScreenCapture.setIcon(MaterialIcons.PHOTO_CAMERA);
        jButton_ScreenCapture.setText("Screen Capture");

        jButton_PhoneInfo.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                deviceInfoHandle();
            }
        });
        jButton_PhoneInfo.setFocusable(true);
        jButton_PhoneInfo.setIcon(MaterialIcons.PERM_DEVICE_INFORMATION);
        jButton_PhoneInfo.setText("Phone Info");

        jButton_Terminal.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                terminalHandle();
            }
        });
        jButton_Terminal.setFocusable(true);
        jButton_Terminal.setIcon(MaterialIcons.CALL_TO_ACTION);
        jButton_Terminal.setText("Terminal");

        jButton_Settings.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                settingsHandle();
            }
        });
        jButton_Settings.setFocusable(true);
        jButton_Settings.setIcon(MaterialIcons.SETTINGS);
        jButton_Settings.setText("Settings");

        javax.swing.GroupLayout jPanel_ButtonLayout = new javax.swing.GroupLayout(jPanel_Button);
        jPanel_Button.setLayout(jPanel_ButtonLayout);
        jPanel_ButtonLayout.setHorizontalGroup(
            jPanel_ButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton_Home, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_AppManager, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_AppInstaller, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_FileManager, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jComboBox_Devices, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton_TaskManager, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_ScreenCapture, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_PhoneInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_Terminal, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addComponent(jButton_Settings, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
        );
        jPanel_ButtonLayout.setVerticalGroup(
            jPanel_ButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_ButtonLayout.createSequentialGroup()
                .addComponent(jComboBox_Devices, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_Home, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_AppInstaller, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_AppManager, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_FileManager, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_ScreenCapture, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_PhoneInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_Terminal, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                .addComponent(jButton_Settings, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton_TaskManager, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel_Status.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Status.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(217, 217, 217)));
        jPanel_Status.setMinimumSize(new java.awt.Dimension(100, 32));
        jPanel_Status.setPreferredSize(new java.awt.Dimension(137, 32));

        jLabel_AdbVersion.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel_AdbVersion.setForeground(new java.awt.Color(97, 97, 97));
        jLabel_AdbVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        jSeparator_vertical1.setBackground(new java.awt.Color(250, 250, 250));
        jSeparator_vertical1.setForeground(new java.awt.Color(217, 217, 217));
        jSeparator_vertical1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jProgressBar_Main.setForeground(new java.awt.Color(76, 175, 80));
        jProgressBar_Main.setToolTipText("");
        jProgressBar_Main.setStringPainted(true);
        jProgressBar_Main.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jProgressBar_MainMouseClicked(evt);
            }
        });

        jLabel_MainMessage.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_MainMessage.setForeground(new java.awt.Color(97, 97, 97));
        jLabel_MainMessage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel_StatusLayout = new javax.swing.GroupLayout(jPanel_Status);
        jPanel_Status.setLayout(jPanel_StatusLayout);
        jPanel_StatusLayout.setHorizontalGroup(
            jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_StatusLayout.createSequentialGroup()
                .addComponent(jProgressBar_Main, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel_MainMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addGap(6, 6, 6)
                .addComponent(jSeparator_vertical1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel_AdbVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6))
        );
        jPanel_StatusLayout.setVerticalGroup(
            jPanel_StatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator_vertical1)
            .addComponent(jLabel_AdbVersion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jProgressBar_Main, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
            .addComponent(jLabel_MainMessage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel_MainPanelLayout = new javax.swing.GroupLayout(jPanel_MainPanel);
        jPanel_MainPanel.setLayout(jPanel_MainPanelLayout);
        jPanel_MainPanelLayout.setHorizontalGroup(
            jPanel_MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_TitleBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel_MainPanelLayout.createSequentialGroup()
                .addComponent(jPanel_Button, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel_MainHandler, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel_Status, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
        );
        jPanel_MainPanelLayout.setVerticalGroup(
            jPanel_MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_MainPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel_TitleBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(jPanel_MainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_MainHandler, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_Button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(jPanel_Status, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel_MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_MinimizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_MinimizeActionPerformed
        this.setExtendedState(this.getExtendedState() | JFrame.ICONIFIED);
    }//GEN-LAST:event_jButton_MinimizeActionPerformed

    private void jButton_MaximizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_MaximizeActionPerformed
        maximizeHandle();
    }//GEN-LAST:event_jButton_MaximizeActionPerformed

    private void jButton_CloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_CloseActionPerformed
        //this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        exitHandle();
    }//GEN-LAST:event_jButton_CloseActionPerformed

    private void jComboBox_DevicesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox_DevicesItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            int selectedDevice = jComboBox_Devices.getSelectedIndex();
            if (selectedDevice >= 0) {
                // get selected device
                mDevice = mDevices.get(selectedDevice);
                // check for binaries files in device system
                try {
                    mDevice.checkBinaries();
                } catch (JadbException | IOException ex) {
                    Log.log(Level.SEVERE, "CheckBinaries", ex);
                }
                // start tasks runner
                mDevice.startTasksRunner();
                // set this device as the selected device
                MobyDroid.setDevice(mDevice);
            }
        }
    }//GEN-LAST:event_jComboBox_DevicesItemStateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // exit handle
        exitHandle();
    }//GEN-LAST:event_formWindowClosing

    private void jProgressBar_MainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jProgressBar_MainMouseClicked
        tasksHandle();
    }//GEN-LAST:event_jProgressBar_MainMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.hq.mobydroid.gui.MaterialButtonH jButton_AppInstaller;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_AppManager;
    private javax.swing.JButton jButton_Close;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_FileManager;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_Home;
    private javax.swing.JButton jButton_Maximize;
    private javax.swing.JButton jButton_Minimize;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_PhoneInfo;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_ScreenCapture;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_Settings;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_TaskManager;
    private com.hq.mobydroid.gui.MaterialButtonH jButton_Terminal;
    private javax.swing.JComboBox<String> jComboBox_Devices;
    private javax.swing.JLabel jLabel_AdbVersion;
    private javax.swing.JLabel jLabel_MainLabel;
    private javax.swing.JLabel jLabel_MainMessage;
    private javax.swing.JPanel jPanel_Button;
    private javax.swing.JPanel jPanel_MainHandler;
    private javax.swing.JPanel jPanel_MainPanel;
    private javax.swing.JPanel jPanel_Status;
    private javax.swing.JPanel jPanel_TitleBar;
    private javax.swing.JProgressBar jProgressBar_Main;
    private javax.swing.JSeparator jSeparator_vertical1;
    // End of variables declaration//GEN-END:variables
}
