package com.hq.mobydroid.gui;

import com.hq.mobydroid.device.ApkgInstaller;
import com.hq.apktool.Apkg;
import com.hq.apktool.ApkTool;
import com.hq.apktool.ApkToolException;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Log;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.MobydroidStatic;
import com.hq.mobydroid.Settings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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
public class JPanel_AppInstaller extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final PackageTableModel packageTableModel = new PackageTableModel();
    private final String[] packageTableColumnNames = {"App", "Version", "Size", "On SD Card", "Reinstall", "Downgrade"};
    private final ListSelectionListener listSelectionListener;
    // *************************************************************

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_AppInstaller() {
        // initialize components
        initComponents();

        // table dimension
        jTable_Apps.setRowHeight(GuiUtils.APK_ICON_HEIGTH + 6);
        setColumnWidth(0, 256, -1);
        setColumnWidth(1, 64, 128);
        setColumnWidth(2, 64, 128);
        setColumnWidth(3, 96, 128);
        setColumnWidth(4, 96, 128);
        setColumnWidth(5, 96, 128);

        // set Table Row Sorter
        TableRowSorter tableRowSorter = new PackageTableRowSorter(jTable_Apps.getModel());
        jTable_Apps.setRowSorter(tableRowSorter);
        tableRowSorter.setComparator(2, (Comparator<Long>) (o1, o2) -> o1.compareTo(o2));

        // set cell render 1th column
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(0)).setCellRenderer(new ApkLablelCellRenderer());

        // right align 2nd column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(2)).setCellRenderer(renderer);

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
            setPackageDetails(packageTableModel.getPackage(jTable_Apps.getSelectionModel().getLeadSelectionIndex()));
        };
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);

        // Handle drag & drop files into jPanel
        dropItHandle();

        // load checkbox values
        jCheckBox_Onsdcard.setSelected(Boolean.valueOf(Settings.get("AppInstaller_Onsdcard")));
        jCheckBox_Reinstall.setSelected(Boolean.valueOf(Settings.get("AppInstaller_Reinstall")));
        jCheckBox_Downgrade.setSelected(Boolean.valueOf(Settings.get("AppInstaller_Downgrade")));

        // hide for non expert
        if (!Boolean.valueOf(Settings.get("Express_Settings"))) {
            jCheckBox_Onsdcard.setVisible(false);
            jCheckBox_Reinstall.setVisible(false);
            jCheckBox_Downgrade.setVisible(false);
            jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(5));
            jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(4));
            jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(3));
        }

        //disableUI();
        //jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(1));
        //jTable_Apps.addColumn(jTable_Apps.getColumnModel().getColumn(1));
        //setColumnWidth(2, 0, 0);
        //jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
    }

    /**
     * Handle install button event.
     */
    private void installHandle() {
        // check for packages list size
        int count = packageTableModel.getPackages().size();
        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Please add packages for operation.", "No packages", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return;
        }

        // start install tasks
        packageTableModel.getPackages().forEach((pkg) -> {
            MobyDroid.getDevice().runPackageInstallTask(pkg);
        });

        // disable UI
        disableUI();
        // clear current table
        packageTableModel.removeAllPackages();
        // enable UI
        enableUI();
    }

    /**
     *
     */
    private void AddPackageEvent() {
        JFileChooser fileChooser = new JFileChooser();
        File path = new File(Settings.get("AppInstaller_AddPackagePath"));
        if (!path.exists()) {
            path = new File(MobydroidStatic.HOME_PATH);
        }
        fileChooser.setCurrentDirectory(path);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.apk", "Apk"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            // disable UI
            disableUI();
            //
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                try {
                    Apkg pkg = ApkTool.getPackage(file.getAbsolutePath());
                    if (pkg != null) {
                        packageTableModel.addPackage(new ApkgInstaller(pkg, jCheckBox_Onsdcard.isSelected(), jCheckBox_Reinstall.isSelected(), jCheckBox_Downgrade.isSelected()));
                    }
                } catch (ApkToolException | IOException ex) {
                    Log.log(Level.SEVERE, "ApkToolGetPackage", ex);
                }
            }
            // save last directory to settings ..
            Settings.set("AppInstaller_AddPackagePath", fileChooser.getSelectedFile().getParent());
            Settings.save();
            // enable UI
            enableUI();
        }
    }

    /**
     *
     */
    private void RemovePackageEvent() {
        if (jTable_Apps.getSelectedRowCount() > 0) {
            // disable UI
            disableUI();
            int rows[] = jTable_Apps.getSelectedRows();
            Arrays.sort(rows);
            for (int ii = rows.length - 1; ii >= 0; ii--) {
                packageTableModel.removePackage(rows[ii]);
            }
            // enable UI
            enableUI();
        }

    }

    /**
     * Update the package details view with the details of this package.
     */
    private void setPackageDetails(ApkgInstaller pkgInstaller) {
        jLabel_AppIcon.setIcon(pkgInstaller.getIcon());
        jTextField_AppLabel.setText(pkgInstaller.getLabel());
        jTextField_AppPackage.setText(pkgInstaller.getPackage());
        jLabel_AppVersion.setText("Version: " + pkgInstaller.getVersion());
        jLabel_AppSize.setText("Size: " + GuiUtils.getFormatedSize(pkgInstaller.getSize()));
        jLabel_OnSDCard.setText("Install on sdcard: " + (pkgInstaller.isOnSdcard() ? "Yes" : "No"));
        jLabel_Reinstall.setText("Reinstall an exisiting: " + (pkgInstaller.isReinstallable() ? "Yes" : "No"));
        jLabel_Downgrade.setText("Allow version downgrade: " + (pkgInstaller.isDowngradable() ? "Yes" : "No"));
    }

    /**
     * Enable the User Interface.
     */
    public void enableUI() {
        // enable buttons
        mButton_Add.setEnabled(true);
        mButton_Install.setEnabled(true);
        mButton_Remove.setEnabled(true);
        // enable CheckBoxs
        jCheckBox_Downgrade.setEnabled(true);
        jCheckBox_Onsdcard.setEnabled(true);
        jCheckBox_Reinstall.setEnabled(true);
        // add back the ListSelectionListener
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);
        // show jTable
        jTable_Apps.setVisible(true);
        // turn off the wait cursor
        setCursor(null);
    }

    /**
     * Disable the User Interface.
     */
    public void disableUI() {
        // turn on the wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // hide jTable (disabling show out of bound exception ! ! !)
        jTable_Apps.setVisible(false);
        // remove ListSelectionListener
        jTable_Apps.getSelectionModel().removeListSelectionListener(listSelectionListener);
        // enable CheckBoxs
        jCheckBox_Downgrade.setEnabled(false);
        jCheckBox_Onsdcard.setEnabled(false);
        jCheckBox_Reinstall.setEnabled(false);
        // disable buttons
        mButton_Add.setEnabled(false);
        mButton_Install.setEnabled(false);
        mButton_Remove.setEnabled(false);
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

    /**
     *
     */
    private void dropItHandle() {
        // Handle drag & drop files into jPanel
        this.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                // disable UI
                disableUI();
                try {
                    // set accepted drop action
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    // Just going to grab the expected DataFlavor to make sure
                    // we know what is being dropped
                    // Grab expected flavor
                    DataFlavor dragAndDropPanelFlavor = DataFlavor.javaFileListFlavor;
                    // What does the Transferable support
                    if (dtde.getTransferable().isDataFlavorSupported(dragAndDropPanelFlavor)) {
                        Object transferableArrayListObj = dtde.getTransferable().getTransferData(dragAndDropPanelFlavor);
                        if (transferableArrayListObj != null) {
                            if (transferableArrayListObj instanceof ArrayList) {
                                ((ArrayList) transferableArrayListObj).forEach(file -> {
                                    if (file instanceof File) {
                                        String filePath = ((File) file).getAbsolutePath();
                                        try {
                                            Apkg pkg = ApkTool.getPackage(filePath);
                                            if (pkg != null) {
                                                packageTableModel.addPackage(new ApkgInstaller(pkg, jCheckBox_Onsdcard.isSelected(), jCheckBox_Reinstall.isSelected(), jCheckBox_Downgrade.isSelected()));
                                            }
                                        } catch (ApkToolException | IOException ex) {
                                            Log.log(Level.SEVERE, "ApkToolGetPackage", ex);
                                        }
                                    }
                                });
                            }
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    Log.log(Level.SEVERE, "DropItHandle", ex);
                }
                // handle drop inside current panel
                //super.drop(dtde);*/
                // enable UI
                enableUI();
            }
        });
    }

    ///////////////////////////////////////////////
    // *************************************************************
    class PopUpDemo extends JPopupMenu {

        /*
        JMenuItem refreshMenuItem = new JMenuItem("Refresh",refreshIcon);
        JMenuItem downloadMenuItem = new JMenuItem("Download",downloadIcon);
        JMenuItem uploadMenuItem = new JMenuItem("Upload",uploadIcon);
        JMenuItem runMenuItem = new JMenuItem("Run",runIcon);
        JMenuItem renameMenuItem = new JMenuItem("Rename",renameIcon);
        JMenuItem deleteMenuItem = new JMenuItem("Delete",deleteIcon);
        JMenuItem mkdirMenuItem = new JMenuItem("Creat folder",folderIcon);
        JMenuItem openAgentFolderMenuItem = new JMenuItem("Open user folder",downloadsIcon);*/
        public PopUpDemo() {
            /*
            refreshMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {refreshCMD(evt);}});
            downloadMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {downloadCMD(evt);}});
            uploadMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {uploadCMD(evt);}});
            runMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {runCMD(evt);}});
            renameMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {renameCMD(evt);}});
            deleteMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {deleteCMD(evt);}});
            mkdirMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {mkdirCMD(evt);}});
            openAgentFolderMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {openAgentFolderCMD(evt);}});
            
            add(refreshMenuItem);
            add(downloadMenuItem);
            add(uploadMenuItem);
            add(runMenuItem);
            add(renameMenuItem);
            add(deleteMenuItem);
            add(mkdirMenuItem);
            add(openAgentFolderMenuItem);
             */
        }
    }

// ************************************************************* //
// ************************************************************* //
// ** A TableModel to hold File[]. ** //
    private class PackageTableModel extends AbstractTableModel {

        private final List<ApkgInstaller> packages;

        PackageTableModel() {
            packages = new ArrayList<>();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return JPanel.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                case 3:
                    return Boolean.class;
                case 4:
                    return Boolean.class;
                case 5:
                    return Boolean.class;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case 3:
                case 4:
                case 5:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            ApkgInstaller pkgInstaller = packages.get(row);
            switch (column) {
                case 3:
                    pkgInstaller.setOnSdcard(!pkgInstaller.isOnSdcard());
                    fireTableCellUpdated(row, column);
                    setPackageDetails(pkgInstaller);
                    break;
                case 4:
                    pkgInstaller.setReinstall(!pkgInstaller.isReinstallable());
                    fireTableCellUpdated(row, column);
                    setPackageDetails(pkgInstaller);
                    break;
                case 5:
                    pkgInstaller.setDowngrade(!pkgInstaller.isDowngradable());
                    fireTableCellUpdated(row, column);
                    setPackageDetails(pkgInstaller);
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
            ApkgInstaller pkgInstaller = packages.get(row);
            switch (column) {
                case 0:
                    return pkgInstaller;
                case 1:
                    return pkgInstaller.getVersion();
                case 2:
                    return GuiUtils.getFormatedSize(pkgInstaller.getSize());
                case 3:
                    return pkgInstaller.isOnSdcard();
                case 4:
                    return pkgInstaller.isReinstallable();
                case 5:
                    return pkgInstaller.isDowngradable();
                default:
                    return null;
            }
        }

        public Object getRawValueAt(int row, int column) {
            ApkgInstaller pkgInstaller = packages.get(row);
            switch (column) {
                case 0:
                    return pkgInstaller.getLabel();
                case 1:
                    return pkgInstaller.getVersion();
                case 2:
                    return pkgInstaller.getSize();
                case 3:
                    return pkgInstaller.isOnSdcard();
                case 4:
                    return pkgInstaller.isReinstallable();
                case 5:
                    return pkgInstaller.isDowngradable();
                default:
                    return null;
            }
        }

        public List<ApkgInstaller> getPackages() {
            return packages;
        }

        public ApkgInstaller getPackage(int row) {
            return packages.get(row);
        }

        public void addPackage(ApkgInstaller pkg) {
            // check if already exist
            for (ApkgInstaller pkgInstaller : packages) {
                if (pkgInstaller.equals(pkg)) {
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

        public void removeAllPackages() {
            // remove all packages
            packages.clear();
            fireTableDataChanged();
        }

        public void setValueAt(Object value, int column) {
            switch (column) {
                case 3:
                    packages.forEach((pkgInstaller) -> {
                        pkgInstaller.setOnSdcard((boolean) value);
                    });
                    break;
                case 4:
                    packages.forEach((pkgInstaller) -> {
                        pkgInstaller.setReinstall((boolean) value);
                    });
                    break;
                case 5:
                    packages.forEach((pkgInstaller) -> {
                        pkgInstaller.setDowngrade((boolean) value);
                    });
                    break;
            }
            // fire
            fireTableDataChanged();
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

        private final JPanel jPanel;
        private final JLabel jLabel_Label;
        private final JLabel jLabel_Package;
        private final JLabel jLabel_Icon;

        public ApkLablelCellRenderer() {
            jPanel = new JPanel();
            jLabel_Label = new JLabel();
            jLabel_Package = new JLabel();
            jLabel_Icon = new javax.swing.JLabel();

            jLabel_Label.setFont(new java.awt.Font("Dialog", 1, 12));
            jLabel_Label.setForeground(Color.BLACK);

            jLabel_Package.setFont(new java.awt.Font("Dialog", 1, 10));
            jLabel_Package.setForeground(MaterialColor.GREY_700);

            jLabel_Icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanel);
            jPanel.setLayout(layout);

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
            Apkg pkg = (Apkg) value;
            jLabel_Label.setText(pkg.getLabel());
            jLabel_Package.setText(pkg.getPackage());
            jLabel_Icon.setIcon(pkg.getIcon());

            if (hasFocus) {
                jPanel.setBorder(javax.swing.BorderFactory.createLineBorder(MaterialColor.BLUE_400));
            } else {
                jPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            }

            return jPanel;
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
        mButton_Add = new com.hq.mobydroid.gui.MaterialButtonV();
        mButton_Remove = new com.hq.mobydroid.gui.MaterialButtonV();
        mButton_Install = new com.hq.mobydroid.gui.MaterialButtonV();
        jCheckBox_Reinstall = new javax.swing.JCheckBox();
        jCheckBox_Downgrade = new javax.swing.JCheckBox();
        jCheckBox_Onsdcard = new javax.swing.JCheckBox();
        jPanel_Package = new javax.swing.JPanel();
        jTextField_AppLabel = new javax.swing.JTextField();
        jTextField_AppPackage = new javax.swing.JTextField();
        jLabel_AppIcon = new javax.swing.JLabel();
        jLabel_AppVersion = new javax.swing.JLabel();
        jLabel_AppSize = new javax.swing.JLabel();
        jLabel_OnSDCard = new javax.swing.JLabel();
        jLabel_Reinstall = new javax.swing.JLabel();
        jLabel_Downgrade = new javax.swing.JLabel();

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
        jTable_Apps.setShowHorizontalLines(false);
        jTable_Apps.setShowVerticalLines(false);
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

        mButton_Add.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                AddPackageEvent();
            }
        });
        mButton_Add.setFocusable(true);
        mButton_Add.setIcon(MaterialIcons.ADD_CIRCLE_OUTLINE);
        mButton_Add.setText("Add");

        mButton_Remove.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                RemovePackageEvent();
            }
        });
        mButton_Remove.setFocusable(true);
        mButton_Remove.setIcon(MaterialIcons.REMOVE_CIRCLE_OUTLINE);
        mButton_Remove.setText("Remove");

        mButton_Install.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                installHandle();
            }
        });
        mButton_Install.setFocusable(true);
        mButton_Install.setIcon(MaterialIcons.ARCHIVE);
        mButton_Install.setText("Install");

        jCheckBox_Reinstall.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_Reinstall.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_Reinstall.setText("Reinstall exisiting apps");
        jCheckBox_Reinstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_ReinstallActionPerformed(evt);
            }
        });

        jCheckBox_Downgrade.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_Downgrade.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_Downgrade.setText("Allow version code downgrade");
        jCheckBox_Downgrade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_DowngradeActionPerformed(evt);
            }
        });

        jCheckBox_Onsdcard.setBackground(new java.awt.Color(250, 250, 250));
        jCheckBox_Onsdcard.setForeground(new java.awt.Color(97, 97, 97));
        jCheckBox_Onsdcard.setText("Install apps on sdcard");
        jCheckBox_Onsdcard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_OnsdcardActionPerformed(evt);
            }
        });

        jPanel_Package.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Package.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Package.setFocusable(false);
        jPanel_Package.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        jTextField_AppLabel.setEditable(false);
        jTextField_AppLabel.setBorder(null);

        jTextField_AppPackage.setEditable(false);
        jTextField_AppPackage.setAutoscrolls(false);
        jTextField_AppPackage.setBorder(null);

        jLabel_AppIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_AppIcon.setFocusable(false);
        jLabel_AppIcon.setOpaque(true);

        jLabel_AppVersion.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppVersion.setFocusable(false);
        jLabel_AppVersion.setOpaque(true);

        jLabel_AppSize.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppSize.setFocusable(false);
        jLabel_AppSize.setOpaque(true);

        jLabel_OnSDCard.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_OnSDCard.setFocusable(false);
        jLabel_OnSDCard.setOpaque(true);

        jLabel_Reinstall.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Reinstall.setFocusable(false);
        jLabel_Reinstall.setOpaque(true);

        jLabel_Downgrade.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Downgrade.setFocusable(false);
        jLabel_Downgrade.setOpaque(true);

        javax.swing.GroupLayout jPanel_PackageLayout = new javax.swing.GroupLayout(jPanel_Package);
        jPanel_Package.setLayout(jPanel_PackageLayout);
        jPanel_PackageLayout.setHorizontalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Downgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField_AppLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel_Reinstall, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_OnSDCard, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel_PackageLayout.setVerticalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jTextField_AppLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_OnSDCard, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Reinstall, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Downgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 108, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel_Package, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mButton_Add, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Remove, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mButton_Install, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCheckBox_Reinstall, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                            .addComponent(jCheckBox_Onsdcard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jCheckBox_Downgrade, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox_Onsdcard, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox_Reinstall, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jCheckBox_Downgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mButton_Add, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mButton_Remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mButton_Install, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Package, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable_AppsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_AppsKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_TAB:
                //KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                break;
        }
        /*
        int input = evt.getKeyCode();
        if(input==KeyEvent.VK_ENTER){
            if(currentFile.type.equalsIgnoreCase("desktop") || currentFile.type.equalsIgnoreCase("computer") || currentFile.type.equalsIgnoreCase("hdd") || currentFile.type.equalsIgnoreCase("fdd") || currentFile.type.equalsIgnoreCase("cd") || currentFile.type.equalsIgnoreCase("home") || currentFile.type.equalsIgnoreCase("dir")){
                getChildren();
            }
        }else if(input==KeyEvent.VK_BACK_SPACE){
            MyFile tmpFile =((PackageTableModel)browserTable.getModel()).getFile("..");
            if(tmpFile==null){
                //tmpFile = new MyFile("..","dir","");
                //currentFile = tmpFile;
                ///getChildren();
                getChildren(computerNode);
            }else{
                currentFile = tmpFile;
                setFileDetails(currentFile);
                getChildren();
            }
        }else if(input==KeyEvent.VK_HOME){
            browserTable.changeSelection(0, 0, false, false);
        }else if(input==KeyEvent.VK_END){
            //browserTable.sets
            browserTable.changeSelection(browserTable.getRowCount() - 1, 0, false, false);
        }
         */
 /*int startRow = jTable_Apps.getSelectedRow();
        if (startRow < 0) {
            startRow = 0;
        } else {
            startRow++;
        }
        for (int row = startRow; row < jTable_Apps.getRowCount(); row++) {
            if (((String) jTable_Apps.getValueAt(row, 1)).toLowerCase().startsWith("" + Character.toLowerCase(evt.getKeyChar()))) {
                jTable_Apps.changeSelection(row, 0, false, false);
                return;
            }
        }
        for (int row = 0; row < jTable_Apps.getRowCount(); row++) {
            if (((String) jTable_Apps.getValueAt(row, 1)).toLowerCase().startsWith("" + Character.toLowerCase(evt.getKeyChar()))) {
                jTable_Apps.changeSelection(row, 0, false, false);
                return;
            }
        }*/
    }//GEN-LAST:event_jTable_AppsKeyPressed

    private void jTable_AppsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable_AppsFocusGained
        // select first row if Selection Model is Empty
        if (jTable_Apps.getSelectionModel().isSelectionEmpty() && jTable_Apps.getModel().getRowCount() > 0) {
            jTable_Apps.setRowSelectionInterval(0, 0);
        }
    }//GEN-LAST:event_jTable_AppsFocusGained

    private void jCheckBox_OnsdcardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_OnsdcardActionPerformed
        packageTableModel.setValueAt(jCheckBox_Onsdcard.isSelected(), 3);
        // save value
        Settings.set("AppInstaller_Onsdcard", String.valueOf(jCheckBox_Onsdcard.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_OnsdcardActionPerformed

    private void jCheckBox_ReinstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_ReinstallActionPerformed
        packageTableModel.setValueAt(jCheckBox_Reinstall.isSelected(), 4);
        // save value
        Settings.set("AppInstaller_Reinstall", String.valueOf(jCheckBox_Reinstall.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_ReinstallActionPerformed

    private void jCheckBox_DowngradeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_DowngradeActionPerformed
        packageTableModel.setValueAt(jCheckBox_Downgrade.isSelected(), 5);
        // save value
        Settings.set("AppInstaller_Downgrade", String.valueOf(jCheckBox_Downgrade.isSelected()));
        Settings.save();
    }//GEN-LAST:event_jCheckBox_DowngradeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox_Downgrade;
    private javax.swing.JCheckBox jCheckBox_Onsdcard;
    private javax.swing.JCheckBox jCheckBox_Reinstall;
    private javax.swing.JLabel jLabel_AppIcon;
    private javax.swing.JLabel jLabel_AppSize;
    private javax.swing.JLabel jLabel_AppVersion;
    private javax.swing.JLabel jLabel_Downgrade;
    private javax.swing.JLabel jLabel_OnSDCard;
    private javax.swing.JLabel jLabel_Reinstall;
    private javax.swing.JPanel jPanel_Package;
    private javax.swing.JScrollPane jTableScrollPane_Apps;
    private javax.swing.JTable jTable_Apps;
    private javax.swing.JTextField jTextField_AppLabel;
    private javax.swing.JTextField jTextField_AppPackage;
    private com.hq.mobydroid.gui.MaterialButtonV mButton_Add;
    private com.hq.mobydroid.gui.MaterialButtonV mButton_Install;
    private com.hq.mobydroid.gui.MaterialButtonV mButton_Remove;
    // End of variables declaration//GEN-END:variables
}
