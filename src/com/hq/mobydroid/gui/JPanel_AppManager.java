/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.ApkgManager;
import com.hq.mobydroid.device.TaskListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JPanel_AppManager extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final PackageTableModel packageTableModel = new PackageTableModel();
    private final String[] packageTableColumnNames = {"", "App", "Version", "Size", "Location", "Install Time"};
    // *************************************************************
    private final ListSelectionListener listSelectionListener;

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_AppManager() {
        // initialize components
        initComponents();

        // table dimension
        jTable_Apps.setRowHeight(GuiUtils.APK_ICON_HEIGTH + 6);
        setColumnWidth(0, 32, 32);
        setColumnWidth(1, 256, -1);
        setColumnWidth(2, 64, 128);
        setColumnWidth(3, 64, 128);
        setColumnWidth(4, 64, 128);
        setColumnWidth(5, 96, 128);

        // set Table Row Sorter
        TableRowSorter tableRowSorter = new PackageTableRowSorter(jTable_Apps.getModel());
        tableRowSorter.setComparator(3, (Comparator<Long>) (o1, o2) -> o1.compareTo(o2));
        jTable_Apps.setRowSorter(tableRowSorter);

        // set table header for  0nd column
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(0)).setHeaderRenderer(new JCheckBoxTableHeaderCellRenderer());

        // set cell render 1th column
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(1)).setCellRenderer(new ApkLablelCellRenderer());

        // right align 3rd column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(3)).setCellRenderer(renderer);

        // center align 4th & 5th column
        renderer.setHorizontalAlignment(JLabel.CENTER);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(4)).setCellRenderer(renderer);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(5)).setCellRenderer(renderer);

        // header click event
        jTable_Apps.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int column = jTable_Apps.convertColumnIndexToModel(jTable_Apps.getColumnModel().getColumnIndexAtX(mouseEvent.getX()));
                if (mouseEvent.getClickCount() == 1 && column != -1) {
                    packageTableModel.headerClicked(column);
                }
            }
        });

        // set tab action to change focus component outside jtable
        jTable_Apps.getActionMap().put(jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        // KeyBinding
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "none");
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "none");

        // set table selection listner
        listSelectionListener = (ListSelectionEvent lse) -> {
            int viewIndex = jTable_Apps.getSelectionModel().getLeadSelectionIndex();
            if (viewIndex != -1) { // Ensure a row is selected
                int modelIndex = jTable_Apps.convertRowIndexToModel(viewIndex); // Convert view index to model index
                setPackageDetails(packageTableModel.getPackage(modelIndex));
            }
        };
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);

        // load checkbox values
        jCheckBox_EnabledApps.setSelected(Boolean.valueOf(Settings.get("AppManager_EnabledApps")));
        jCheckBox_DisabledApps.setSelected(Boolean.valueOf(Settings.get("AppManager_DisabledApps")));
        jCheckBox_SystemApps.setSelected(Boolean.valueOf(Settings.get("AppManager_SystemApps")));

        // hide for non expert
        if (!Boolean.valueOf(Settings.get("Expert_Settings"))) {
            jCheckBox_EnabledApps.setVisible(false);
            jCheckBox_DisabledApps.setVisible(false);
            jCheckBox_SystemApps.setVisible(false);
            materialButtonH_Enable.setVisible(false);
            materialButtonH_Disable.setVisible(false);
            materialButtonH_Backup.setVisible(false);
            materialButtonH_Restore.setVisible(false);
            jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(5));
        }
    }

    /**
     * Handle buttons events.
     */
    private void uninstallHandle() {
        // check for connected device
        if (!isDeviceConnected()) {
            return;
        }

        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }
        // confirm uninstalling
        if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Uninstall packages", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ResourceLoader.MaterialIcons_DELETE_FOREVER) != JOptionPane.YES_OPTION) {
            return;
        }

        uninstallPackages();

        // show tasks progress window
        MobyDroid.showTasksPanel();
    }

    private void enableHandle() {
        // check for connected device
        if (!isDeviceConnected()) {
            return;
        }

        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }

        enablePackages();

        // show tasks progress window
        MobyDroid.showTasksPanel();
    }

    private void disableHandle() {
        // check for connected device
        if (!isDeviceConnected()) {
            return;
        }

        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }

        disablePackages();

        // show tasks progress window
        MobyDroid.showTasksPanel();
    }

    private void backupHandle() {
        backupPackages();
    }

    private void restoreHandle() {
        restorePackages();
    }

    private void pullHandle() {
        // check for connected device
        if (!isDeviceConnected()) {
            return;
        }

        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }

        pullPackages();

        // show tasks progress window
        MobyDroid.showTasksPanel();
    }

    private void refreshHandle() {
        // check for connected device
        if (!isDeviceConnected()) {
            return;
        }

        updatePackagesList();

        // show tasks progress window
        // MobyDroid.showTasksPanel();
    }

    private boolean isDeviceConnected() {
        // check for connected device
        if (MobyDroid.getDevice() == null) {
            JOptionPane.showMessageDialog(this, "Please connect to a device first.", "No device", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return false;
        }
        return true;
    }

    private boolean isPackageMarked() {
        int marked = (int) packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).count();
        if (marked == 0) {
            JOptionPane.showMessageDialog(this, "Please select packages for operation.", "No packages selected", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return false;
        }
        return true;
    }

    private void enableUI() {
        // enable buttons
        materialButtonH_Disable.setEnabled(true);
        materialButtonH_Enable.setEnabled(true);
        materialButtonH_Backup.setEnabled(true);
        materialButtonH_PullApk.setEnabled(true);
        materialButtonH_Refresh.setEnabled(true);
        materialButtonH_Restore.setEnabled(true);
        materialButtonH_Uninstall.setEnabled(true);
        // enable CheckBoxs
        jCheckBox_EnabledApps.setEnabled(true);
        jCheckBox_DisabledApps.setEnabled(true);
        jCheckBox_SystemApps.setEnabled(true);
        // add back the ListSelectionListener
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);
        // enable jTable
        jTable_Apps.setVisible(true);
        // turn off the wait cursor
        setCursor(null);
    }

    private void disableUI() {
        // turn on the wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // disable jTable
        jTable_Apps.setVisible(false);
        // remove ListSelectionListener
        jTable_Apps.getSelectionModel().removeListSelectionListener(listSelectionListener);
        // disable CheckBoxs
        jCheckBox_EnabledApps.setEnabled(false);
        jCheckBox_DisabledApps.setEnabled(false);
        jCheckBox_SystemApps.setEnabled(false);
        // disable buttons
        materialButtonH_Disable.setEnabled(false);
        materialButtonH_Enable.setEnabled(false);
        materialButtonH_Backup.setEnabled(false);
        materialButtonH_PullApk.setEnabled(false);
        materialButtonH_Refresh.setEnabled(false);
        materialButtonH_Restore.setEnabled(false);
        materialButtonH_Uninstall.setEnabled(false);
    }

    /**
     * Update packages list.
     */
    public void updatePackagesList() {
        // the old packages to check marked packages
        //List<ApkgManager> oldPackages = packageTableModel.getPackages();

        // run the packages list task
        boolean enabled = jCheckBox_EnabledApps.isSelected();
        boolean disabled = jCheckBox_DisabledApps.isSelected();
        boolean system = jCheckBox_SystemApps.isSelected();
        if (!Boolean.valueOf(Settings.get("Expert_Settings"))) {
            enabled = true;
            disabled = false;
            system = false;
        }
        MobyDroid.getDevice().runPackagesListTask(enabled, disabled, system, new TaskListener<ApkgManager>() {
            @Override
            public void onStart() {
                // disable UI
                disableUI();

                // clear old packages
                packageTableModel.removeAll();
            }

            @Override
            public void onProcess(List<ApkgManager> list) {
                list.forEach((pkg) -> {
                    packageTableModel.addPackage(pkg);
                });
            }

            @Override
            public void onDone() {
                // enable UI
                enableUI();
            }
        });
    }

    /**
     * Uninstall packages.
     */
    private void uninstallPackages() {
        // disable UI
        disableUI();

        // start uninstall tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackageUninstallTask(pkg);
        });

        // enable UI
        enableUI();
    }

    /**
     * Enable packages.
     */
    private void enablePackages() {
        // disable UI
        disableUI();

        // start uninstall tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackageEnableTask(pkg);
        });

        // enable UI
        enableUI();
    }

    /**
     * Disable packages.
     */
    private void disablePackages() {
        // disable UI
        disableUI();

        // start uninstall tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackageDisableTask(pkg);
        });

        // enable UI
        enableUI();
    }

    /**
     * Pull apk file for packages.
     */
    private void pullPackages() {
        // disable UI
        disableUI();

        // start pull tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackagePullTask(pkg);
        });

        // enable UI
        enableUI();
    }

    /**
     * Backup packages.
     */
    private void backupPackages() {
        /*
        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }
        // disable UI
        disableUI();

        // start backup tasks
        MobyDroid.getDevice().runPackageBackupTask(packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).collect(Collectors.toList()));
        //MobyDroid.getDevice().runPackageBackupTask(packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())));
        //packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isPackageMarked())).forEach((pkg) -> {
        //        MobyDroid.getDevice().runPackageBackupTask(pkg);
        //    });

        // enable UI
        enableUI();
         */
    }

    /**
     * Restore packages.
     */
    private void restorePackages() {
        /*
        // disable UI
        disableUI();

        JFileChooser fileChooser = new JFileChooser();
        File path = new File(Settings.get("AppManager_RestorePath"));
        if (!path.exists()) {
            path = new File(MobydroidStatic.HOME_PATH);
        }
        fileChooser.setCurrentDirectory(path);
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                MobyDroid.getDevice().runPackageRestoreTask(file.getPath());
            }
            // save last directory to settings ..
            Settings.set("AppManager_RestorePath", fileChooser.getSelectedFile().getParent());
            Settings.save();
        }
        // enable UI
        enableUI();
         */
    }

    /**
     * Update the package details view with the details of this package.
     */
    private void setPackageDetails(ApkgManager pkgManager) {
        jLabel_AppIcon.setIcon(pkgManager.getIcon());
        jLabel_AppLabel.setText(pkgManager.getLabel());
        jLabel_AppPackage.setText(pkgManager.getPackage());
        jLabel_AppVersion.setText("Version: " + pkgManager.getVersion());
        jLabel_AppSize.setText("Size: " + GuiUtils.getFormatedSize(pkgManager.getSize()));
        jLabel_Marked.setText("Marked: " + (pkgManager.isMarked() ? "Yes" : "No"));
        jLabel_Enabled.setText("Enabled: " + (pkgManager.isEnabled() ? "Yes" : "No"));
        jLabel_System.setText("System: " + (pkgManager.isSystem() ? "Yes" : "No"));
    }

    /**
     *
     */
    private void setColumnWidth(int column, int minWidth, int maxWidth) {
        TableColumn tableColumn = jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(column));
        if (minWidth >= 0 && maxWidth >= 0) {
            tableColumn.setPreferredWidth((minWidth + maxWidth) / 2);
        }
        if (minWidth >= 0) {
            tableColumn.setMinWidth(minWidth);
        }
        if (maxWidth >= 0) {
            tableColumn.setMaxWidth(maxWidth);
        }
    }

    ///////////////////////////////////////////////
    // *************************************************************
    class PopUpDemo extends JPopupMenu {

        JMenuItem refreshMenuItem = new JMenuItem("Refresh", MaterialIcons.REFRESH);
        JMenuItem uninstallMenuItem = new JMenuItem("Uninstall", MaterialIcons.DELETE_FOREVER);
        JMenuItem enableMenuItem = new JMenuItem("Enable", MaterialIcons.TOGGLE_ON);
        JMenuItem disableMenuItem = new JMenuItem("Disable", MaterialIcons.TOGGLE_OFF);
        JMenuItem pullMenuItem = new JMenuItem("Pull", MaterialIcons.SAVE);

        public PopUpDemo() {
            refreshMenuItem.addActionListener((ActionEvent evt) -> {
                refreshHandle();
            });

            uninstallMenuItem.addActionListener((ActionEvent evt) -> {
                uninstallHandle();
            });

            enableMenuItem.addActionListener((ActionEvent evt) -> {
                enableHandle();
            });

            disableMenuItem.addActionListener((ActionEvent evt) -> {
                disableHandle();
            });

            pullMenuItem.addActionListener((ActionEvent evt) -> {
                pullHandle();
            });

            add(refreshMenuItem);
            add(uninstallMenuItem);
            add(enableMenuItem);
            add(disableMenuItem);
            add(pullMenuItem);
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class PackageTableModel extends AbstractTableModel {

        private final List<ApkgManager> packages;

        PackageTableModel() {
            packages = new ArrayList<>();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Boolean.class;
                case 1:
                    return JPanel.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
                case 4:
                    return String.class;
                case 5:
                    return String.class;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    pkgManager.setMark(!pkgManager.isMarked());
                    fireTableCellUpdated(row, column);
                    setPackageDetails(pkgManager);
                    break;
            }
        }

        @Override
        public int getColumnCount() {
            return packageTableColumnNames.length;
        }

        @Override
        public int getRowCount() {
            return packages.size();
        }

        @Override
        public String getColumnName(int column) {
            return packageTableColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    return pkgManager.isMarked();
                case 1:
                    return pkgManager;
                case 2:
                    return pkgManager.getVersion();
                case 3:
                    return GuiUtils.getFormatedSize(pkgManager.getSize());
                case 4:
                    return ("SD");
                case 5:
                    return (new SimpleDateFormat("yyyy-MM-dd")).format(new Date(pkgManager.getInstallTime()));
                default:
                    return null;
            }
        }

        public Object getRawValueAt(int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    return pkgManager.isMarked();
                case 1:
                    return pkgManager.getLabel();
                case 2:
                    return pkgManager.getVersion();
                case 3:
                    return pkgManager.getSize();
                case 4:
                    return ("SD");
                case 5:
                    return (new SimpleDateFormat("yyyy-MM-dd")).format(new Date(pkgManager.getInstallTime()));
                default:
                    return null;
            }
        }

        public List<ApkgManager> getPackages() {
            return packages;
        }

        public boolean contains(ApkgManager pkg) {
            return packages.contains(pkg);
        }

        public ApkgManager getPackage(int row) {
            return packages.get(row);
        }

        public void addPackage(ApkgManager pkg) {
            // check if already exist
            for (ApkgManager pkgManager : packages) {
                if (pkgManager.equals(pkg)) {
                    return;
                }
            }
            // add new package
            packages.add(pkg);
            fireTableDataChanged();
        }

        public void removePackage(int row) {
            // remove package
            packages.remove(row);
            fireTableDataChanged();
        }

        public void removeAll() {
            // remove all packages
            packages.clear();
            fireTableDataChanged();
        }

        private void headerClicked(int column) {
            if (column == 0) {
                JCheckBox jCheckBox = (JCheckBox) jTable_Apps.getTableHeader().getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(column)).getHeaderRenderer().getTableCellRendererComponent(null, null, false, false, 0, 0);
                jCheckBox.setSelected(!jCheckBox.isSelected());
                packages.forEach((pkgManager) -> {
                    pkgManager.setMark(jCheckBox.isSelected());
                });
                // Forces the header to resize and repaint itself
                jTable_Apps.getTableHeader().resizeAndRepaint();
                // fire
                fireTableDataChanged();
            }
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class PackageTableRowSorter<M extends TableModel> extends TableRowSorter<M> {

        public PackageTableRowSorter(M model) {
            super(model);
        }

        @Override
        public void modelStructureChanged() {
            // deletes comparators, so we must set again
            super.modelStructureChanged();
        }

        @Override
        public void setModel(M model) {
            // also calls setModelWrapper method
            super.setModel(model);
            // calls modelStructureChanged method
            setModelWrapper(new TableRowSorterModelWrapper(getModelWrapper()));
        }

        /**
         *
         */
        private class TableRowSorterModelWrapper extends DefaultRowSorter.ModelWrapper {

            private final DefaultRowSorter.ModelWrapper modelWrapperImplementation;

            public TableRowSorterModelWrapper(DefaultRowSorter.ModelWrapper modelWrapperImplementation) {
                this.modelWrapperImplementation = modelWrapperImplementation;
            }

            @Override
            public Object getModel() {
                return modelWrapperImplementation.getModel();
            }

            @Override
            public int getColumnCount() {
                return modelWrapperImplementation.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return modelWrapperImplementation.getRowCount();
            }

            @Override
            public Object getIdentifier(int row) {
                return modelWrapperImplementation.getIdentifier(row);
            }

            @Override
            public Object getValueAt(int row, int column) {
                return packageTableModel.getRawValueAt(row, column);
            }
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class ApkLablelCellRenderer implements TableCellRenderer {

        private final JPanel jpanel;
        private final JLabel jLabel_Label;
        private final JLabel jLabel_Package;
        private final JLabel jLabel_Icon;

        public ApkLablelCellRenderer() {
            jpanel = new JPanel();
            jLabel_Label = new JLabel();
            jLabel_Package = new JLabel();
            jLabel_Icon = new javax.swing.JLabel();

            jLabel_Label.setFont(new java.awt.Font("Dialog", 1, 12));
            jLabel_Label.setForeground(Color.BLACK);

            jLabel_Package.setFont(new java.awt.Font("Dialog", 1, 10));
            jLabel_Package.setForeground(MaterialColor.GREY_700);

            jLabel_Icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jpanel);
            jpanel.setLayout(layout);

            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, 0)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel_Package, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                            .addComponent(jLabel_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE)
                                    .addGap(0, 0, 0)
                                    .addComponent(jLabel_Package, javax.swing.GroupLayout.PREFERRED_SIZE, 15, Short.MAX_VALUE))
            );
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            ApkgManager pkg = (ApkgManager) value;
            jLabel_Label.setText(pkg.getLabel());
            jLabel_Package.setText(pkg.getPackage());
            jLabel_Icon.setIcon(pkg.getIcon());

            if (pkg.isEnabled()) {
                if (pkg.isSystem()) {
                    jLabel_Label.setForeground(MaterialColor.REDA_700);
                    jLabel_Package.setForeground(MaterialColor.REDA_400);
                } else {
                    jLabel_Label.setForeground(MaterialColor.GREEN_500);
                    jLabel_Package.setForeground(MaterialColor.LIGHTGREEN_500);
                }
            } else {
                jLabel_Label.setForeground(MaterialColor.GREY_700);
                jLabel_Package.setForeground(MaterialColor.GREY_500);
            }

            if (hasFocus) {
                jpanel.setBorder(javax.swing.BorderFactory.createLineBorder(MaterialColor.BLUE_400));
            } else {
                jpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            }

            return jpanel;
        }
    }

    private class JCheckBoxTableHeaderCellRenderer implements TableCellRenderer {

        private final JCheckBox jCheckBox;

        public JCheckBoxTableHeaderCellRenderer() {
            jCheckBox = new JCheckBox();
            jCheckBox.setFont(UIManager.getFont("TableHeader.font"));
            jCheckBox.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            jCheckBox.setBackground(UIManager.getColor("TableHeader.background"));
            jCheckBox.setForeground(UIManager.getColor("TableHeader.foreground"));
            jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            jCheckBox.setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            jCheckBox.setText((String) value);
            return jCheckBox;
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

        jTableScrollPane_Apps = new javax.swing.JScrollPane();
        jTable_Apps = new javax.swing.JTable(){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    component.setBackground(MaterialColor.BLUE_100);
                }else{
                    component.setBackground(row % 2 == 0 ? Color.white : MaterialColor.GREY_50);
                }
                return component;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        materialButtonH_Uninstall = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Backup = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_PullApk = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Enable = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Disable = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Restore = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Refresh = new com.hq.mobydroid.gui.MaterialButtonV();
        jCheckBox_EnabledApps = new javax.swing.JCheckBox();
        jCheckBox_DisabledApps = new javax.swing.JCheckBox();
        jCheckBox_SystemApps = new javax.swing.JCheckBox();
        jPanel_Package = new javax.swing.JPanel();
        jLabel_AppIcon = new javax.swing.JLabel();
        jLabel_AppLabel = new javax.swing.JLabel();
        jLabel_AppPackage = new javax.swing.JLabel();
        jLabel_AppVersion = new javax.swing.JLabel();
        jLabel_AppSize = new javax.swing.JLabel();
        jLabel_Marked = new javax.swing.JLabel();
        jLabel_Enabled = new javax.swing.JLabel();
        jLabel_System = new javax.swing.JLabel();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Install New Apps : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        jTableScrollPane_Apps.setBackground(new java.awt.Color(250, 250, 250));
        jTableScrollPane_Apps.setComponentPopupMenu(new PopUpDemo());

        jTable_Apps.setBackground(new java.awt.Color(250, 250, 250));
        jTable_Apps.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable_Apps.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTable_Apps.setModel(packageTableModel);
        jTable_Apps.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable_Apps.setComponentPopupMenu(new PopUpDemo());
        jTable_Apps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable_AppsFocusGained(evt);
            }
        });
        jTable_Apps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable_AppsKeyPressed(evt);
            }
        });
        jTableScrollPane_Apps.setViewportView(jTable_Apps);

        materialButtonH_Uninstall.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                uninstallHandle();
            }
        });
        materialButtonH_Uninstall.setFocusable(true);
        materialButtonH_Uninstall.setIcon(MaterialIcons.DELETE_FOREVER);
        materialButtonH_Uninstall.setText("Uninstall");

        materialButtonH_Backup.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                backupHandle();
            }
        });
        materialButtonH_Backup.setFocusable(true);
        materialButtonH_Backup.setIcon(MaterialIcons.UNARCHIVE);
        materialButtonH_Backup.setText("Backup");

        materialButtonH_PullApk.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                pullHandle();
            }
        });
        materialButtonH_PullApk.setFocusable(true);
        materialButtonH_PullApk.setIcon(MaterialIcons.SAVE);
        materialButtonH_PullApk.setText("Pull Apk");

        materialButtonH_Enable.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                enableHandle();
            }
        });
        materialButtonH_Enable.setFocusable(true);
        materialButtonH_Enable.setIcon(MaterialIcons.TOGGLE_ON);
        materialButtonH_Enable.setText("Enable");

        materialButtonH_Disable.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                disableHandle();
            }
        });
        materialButtonH_Disable.setFocusable(true);
        materialButtonH_Disable.setIcon(MaterialIcons.TOGGLE_OFF);
        materialButtonH_Disable.setText("Disable");

        materialButtonH_Restore.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                restoreHandle();
            }
        });
        materialButtonH_Restore.setFocusable(true);
        materialButtonH_Restore.setIcon(MaterialIcons.ARCHIVE);
        materialButtonH_Restore.setText("Restore");

        materialButtonH_Refresh.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                refreshHandle();
            }
        });
        materialButtonH_Refresh.setFocusable(true);
        materialButtonH_Refresh.setIcon(MaterialIcons.REFRESH);
        materialButtonH_Refresh.setText("Refresh");

        jCheckBox_EnabledApps.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_EnabledApps.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_EnabledApps.setText("Show enabled packages");
        jCheckBox_EnabledApps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_EnabledAppsActionPerformed(evt);
            }
        });

        jCheckBox_DisabledApps.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_DisabledApps.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_DisabledApps.setText("Show disabled packages");
        jCheckBox_DisabledApps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_DisabledAppsActionPerformed(evt);
            }
        });

        jCheckBox_SystemApps.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_SystemApps.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_SystemApps.setText("Show system packages");
        jCheckBox_SystemApps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_SystemAppsActionPerformed(evt);
            }
        });

        jPanel_Package.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Package.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Package.setFocusable(false);
        jPanel_Package.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jPanel_Package.setMaximumSize(new java.awt.Dimension(0, 0));

        jLabel_AppIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_AppIcon.setFocusable(false);
        jLabel_AppIcon.setOpaque(true);

        jLabel_AppLabel.setFocusable(false);
        jLabel_AppLabel.setOpaque(true);

        jLabel_AppPackage.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppPackage.setFocusable(false);
        jLabel_AppPackage.setOpaque(true);

        jLabel_AppVersion.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppVersion.setFocusable(false);
        jLabel_AppVersion.setOpaque(true);

        jLabel_AppSize.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppSize.setFocusable(false);
        jLabel_AppSize.setOpaque(true);

        jLabel_Marked.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Marked.setFocusable(false);
        jLabel_Marked.setOpaque(true);

        jLabel_Enabled.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Enabled.setFocusable(false);
        jLabel_Enabled.setOpaque(true);

        jLabel_System.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_System.setFocusable(false);
        jLabel_System.setOpaque(true);

        javax.swing.GroupLayout jPanel_PackageLayout = new javax.swing.GroupLayout(jPanel_Package);
        jPanel_Package.setLayout(jPanel_PackageLayout);
        jPanel_PackageLayout.setHorizontalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_System, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Enabled, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel_AppLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Marked, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel_PackageLayout.setVerticalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jLabel_AppLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Marked, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Enabled, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_System, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel_Package, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_Uninstall, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_PullApk, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_Enable, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_Disable, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_Backup, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(materialButtonH_Restore, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox_EnabledApps, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox_DisabledApps, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox_SystemApps, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(materialButtonH_Uninstall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_PullApk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_Backup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_Restore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_Disable, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(materialButtonH_Enable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox_EnabledApps, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox_DisabledApps, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox_SystemApps, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Package, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable_AppsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_AppsKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_TAB:
                //KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                break;
        }
    }//GEN-LAST:event_jTable_AppsKeyPressed

    private void jTable_AppsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable_AppsFocusGained
        // select first row if Selection Model is Empty
        if (jTable_Apps.getSelectionModel().isSelectionEmpty() && jTable_Apps.getModel().getRowCount() > 0) {
            jTable_Apps.setRowSelectionInterval(0, 0);
        }
    }//GEN-LAST:event_jTable_AppsFocusGained

    private void jCheckBox_EnabledAppsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_EnabledAppsActionPerformed
        // save value
        Settings.set("AppManager_EnabledApps", String.valueOf(jCheckBox_EnabledApps.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_EnabledAppsActionPerformed

    private void jCheckBox_DisabledAppsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_DisabledAppsActionPerformed
        // save value
        Settings.set("AppManager_DisabledApps", String.valueOf(jCheckBox_DisabledApps.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_DisabledAppsActionPerformed

    private void jCheckBox_SystemAppsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_SystemAppsActionPerformed
        // save value
        Settings.set("AppManager_SystemApps", String.valueOf(jCheckBox_SystemApps.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_SystemAppsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox_DisabledApps;
    private javax.swing.JCheckBox jCheckBox_EnabledApps;
    private javax.swing.JCheckBox jCheckBox_SystemApps;
    private javax.swing.JLabel jLabel_AppIcon;
    private javax.swing.JLabel jLabel_AppLabel;
    private javax.swing.JLabel jLabel_AppPackage;
    private javax.swing.JLabel jLabel_AppSize;
    private javax.swing.JLabel jLabel_AppVersion;
    private javax.swing.JLabel jLabel_Enabled;
    private javax.swing.JLabel jLabel_Marked;
    private javax.swing.JLabel jLabel_System;
    private javax.swing.JPanel jPanel_Package;
    private javax.swing.JScrollPane jTableScrollPane_Apps;
    private javax.swing.JTable jTable_Apps;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Backup;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Disable;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Enable;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_PullApk;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Refresh;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Restore;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Uninstall;
    // End of variables declaration//GEN-END:variables
}
