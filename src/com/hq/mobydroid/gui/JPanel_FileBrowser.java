/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.jadb.MyFile;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.Settings;
import com.hq.mobydroid.device.FileBrowserAbstract;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultRowSorter;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author bilux (i.bilux@gmail.com)
 */
public class JPanel_FileBrowser extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final FileTableModel fileTableModel = new FileTableModel();
    private final String[] fileTableColumnNames = {"", "Name", "Type", "Size", "Date Modified", "Permission"};
    private final FileBrowserAbstract fileBrowserAbstract;
    private final ComboBoxAutoComplete comboBoxAutoComplete;
    private String tableFilter;
    private String jComboBox_Path_String;
    // *************************************************************

    /**
     * Creates new form JPanel_FileBrowser
     *
     * @param fileBrowserAbstract
     */
    public JPanel_FileBrowser(FileBrowserAbstract fileBrowserAbstract) {
        // initialize components
        initComponents();

        // set file browser
        this.fileBrowserAbstract = fileBrowserAbstract;

        // table dimension
        jTable_Browser.setRowHeight(28);
        setColumnWidth(0, 28, 28);
        setColumnWidth(1, 256, 0);
        setColumnWidth(2, 96, 128);
        setColumnWidth(3, 96, 128);
        setColumnWidth(4, 96, 128);

        // right align nd column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        jTable_Browser.getColumnModel().getColumn(jTable_Browser.convertColumnIndexToView(3)).setCellRenderer(renderer);

        // set ComboBox Auto Complete
        comboBoxAutoComplete = new ComboBoxAutoComplete(jComboBox_Path);
        jComboBox_Path.getEditor().getEditorComponent().addKeyListener(comboBoxAutoComplete);
        ((JTextField) jComboBox_Path.getEditor().getEditorComponent()).getDocument().addDocumentListener(comboBoxAutoComplete);

        // set tab action to change focus component outside jtable
        jTable_Browser.getActionMap().put(jTable_Browser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        // KeyBinding
        jTable_Browser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        jTable_Browser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "none");
        jTable_Browser.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "none");

        // set Table Row Sorter
        TableRowSorter tableRowSorter = new FileTableRowSorter(jTable_Browser.getModel());
        tableRowSorter.setComparator(0, (Comparator<String>) (o1, o2) -> o1.compareTo(o2));
        tableRowSorter.setComparator(3, (Comparator<Long>) (o1, o2) -> o1.compareTo(o2));
        tableRowSorter.setComparator(4, (Comparator<Long>) (o1, o2) -> o1.compareTo(o2));
        tableRowSorter.setComparator(5, (Comparator<Integer>) (o1, o2) -> o1.compareTo(o2));

        // Sorting by multiple columns (folders first)
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();

        // add table filter for search
        tableFilter = "";
        tableRowSorter.setRowFilter(new RowFilter<Object, Object>() {
            @Override
            public boolean include(RowFilter.Entry<? extends Object, ? extends Object> entry) {
                if (tableFilter.isEmpty()) {
                    return true;
                } else {
                    return ((String) entry.getValue(1)).contains(tableFilter);
                }
            }
        });
        jTable_Browser.setRowSorter(tableRowSorter);

        // add JTextField value Change Listener
        jTextField_Filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTableFiler();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTableFiler();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTableFiler();
            }
        });

        // add jComboBox Action Event Listener
        jComboBox_Path.getEditor().addActionListener((ActionEvent evt) -> {
            updateFilesList(jComboBox_Path.getEditor().getItem().toString());
        });

        // hide for non expert
        if (!Boolean.valueOf(Settings.get("Expert_Settings"))) {
            mButton_MakeDirectory.setVisible(false);
            mButton_MakeFile.setVisible(false);
            mButton_Delete.setVisible(false);
            mButton_Rename.setVisible(false);
            jTable_Browser.removeColumn(jTable_Browser.getColumnModel().getColumn(5));
        }
    }

    /**
     * Enable the User Interface.
     */
    public void enableUI() {
        // enable buttons
        mButton_Copy.setEnabled(true);
        mButton_Delete.setEnabled(true);
        mButton_GoUp.setEnabled(true);
        mButton_MakeDirectory.setEnabled(true);
        mButton_MakeFile.setEnabled(true);
        mButton_Paste.setEnabled(true);
        mButton_Refresh.setEnabled(true);
        mButton_Rename.setEnabled(true);
        // enable input
        jComboBox_Path.setEnabled(true);
        jTextField_Filter.setEnabled(true);
        // show jTable
        jTable_Browser.setVisible(true);
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
        jTable_Browser.setVisible(false);
        // disable input
        jComboBox_Path.setEnabled(false);
        jTextField_Filter.setEnabled(false);
        // disable buttons
        mButton_Copy.setEnabled(false);
        mButton_Delete.setEnabled(false);
        mButton_GoUp.setEnabled(false);
        mButton_MakeDirectory.setEnabled(false);
        mButton_MakeFile.setEnabled(false);
        mButton_Paste.setEnabled(false);
        mButton_Refresh.setEnabled(false);
        mButton_Rename.setEnabled(false);
    }

    /**
     * Handle buttons events.
     */
    private void goUpHandle() {
        updateFilesList(fileBrowserAbstract.getParent());
    }

    public void refreshHandle() {
        updateFilesList(fileBrowserAbstract.getPath());
    }

    private void mkdirHandle() {
        Object input = JOptionPane.showInputDialog(this, "Directory name :", "Make new Directory", JOptionPane.PLAIN_MESSAGE, ResourceLoader.MaterialIcons_CREATE_NEW_FOLDER, null, null);
        if (input == null) {
            return;
        }
        fileBrowserAbstract.mkdir(fileBrowserAbstract.resolvePath(input.toString()));
    }

    private void mkfileHandle() {
        Object input = JOptionPane.showInputDialog(this, "File name :", "Make new File", JOptionPane.PLAIN_MESSAGE, ResourceLoader.MaterialIcons_NOTE_ADD, null, null);
        if (input == null) {
            return;
        }
        fileBrowserAbstract.mkfile(fileBrowserAbstract.resolvePath(input.toString()));
    }

    private void copyHandle() {
        if (!isFileSelected()) {
            return;
        }
        List<MyFile> src = new ArrayList<>();
        int selectedRow[] = jTable_Browser.getSelectedRows();
        for (int row : selectedRow) {
            src.add(fileTableModel.getFile(jTable_Browser.convertRowIndexToModel(row)));
        }
        fileBrowserAbstract.onCopy(src);
    }

    private void pasteHandle() {
        fileBrowserAbstract.onPaste();
    }

    private void deleteHandle() {
        if (!isFileSelected()) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ResourceLoader.MaterialIcons_DELETE_FOREVER);
        if (result == JOptionPane.YES_OPTION) {
            List<String> dst = new ArrayList<>();
            int selectedRow[] = jTable_Browser.getSelectedRows();
            for (int row : selectedRow) {
                dst.add(fileTableModel.getFile(jTable_Browser.convertRowIndexToModel(row)).getPath());
            }
            fileBrowserAbstract.delete(dst);
        }
    }

    private void renameHandle() {
        if (!isFileSelected()) {
            return;
        }
        MyFile file = fileTableModel.getFile(jTable_Browser.convertRowIndexToModel(jTable_Browser.getSelectedRow()));
        Object input = JOptionPane.showInputDialog(this, "New name:", "Rename File", JOptionPane.PLAIN_MESSAGE, ResourceLoader.MaterialIcons_EDIT, null, file.getName());
        if (input == null) {
            return;
        }
        fileBrowserAbstract.rename(file.getPath(), file.resolveName(input.toString()));
    }

    private void surootHandle() {

    }

    private boolean isFileSelected() {
        if (jTable_Browser.getSelectedRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please select files for operation.", "No files selected", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return false;
        }
        return true;
    }

    private void updateTableFiler() {
        tableFilter = jTextField_Filter.getText().trim();
        fileTableModel.fireTableDataChanged();
    }

    /**
     * Update files list.
     *
     * @param path
     */
    private void updateFilesList(String path) {

        // start fetching files
        new SwingWorker<Void, MyFile>() {
            @Override
            public Void doInBackground() {
                // disable UI
                disableUI();

                // clear current table and ComboBox path list
                fileTableModel.removeAll();
                jComboBox_Path.removeAllItems();

                // This is a deliberate pause to allow the UI time to render
                /*try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }*/
                // get files list
                List<MyFile> list = fileBrowserAbstract.goTo(path);

                // publish
                list.forEach((file) -> {
                    publish(file);
                });

                return null;
            }

            @Override
            protected void process(List<MyFile> list) {
                // add list to table
                fileTableModel.addFile(list);
                // if it's folder, add it to ComboBox
                list.stream().filter((file) -> (file.isDirectory())).forEach((file) -> {
                    jComboBox_Path.addItem(file.getPath());
                });
            }

            @Override
            protected void done() {
                // set current path
                jComboBox_Path.setSelectedItem(fileBrowserAbstract.getPath());
                jComboBox_Path_String = fileBrowserAbstract.getPath();
                // update combobox suggetion default model
                comboBoxAutoComplete.updateDefaultModel();
                // enable UI
                enableUI();
                //set focus to table
                jTable_Browser.requestFocus();
                jTable_Browser.requestFocusInWindow();
            }
        }.execute();
    }

    /**
     *
     */
    private void setColumnWidth(int column, int minWidth, int maxWidth) {
        TableColumn tableColumn = jTable_Browser.getColumnModel().getColumn(jTable_Browser.convertColumnIndexToView(column));
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

    // *************************************************************
    private class PopUpDemo extends JPopupMenu {

        JMenuItem refreshMenuItem = new JMenuItem("Refresh", MaterialIcons.REFRESH);
        JMenuItem copyMenuItem = new JMenuItem("Copy", MaterialIcons.CONTENT_COPY);
        JMenuItem pasteMenuItem = new JMenuItem("Paste", MaterialIcons.CONTENT_PASTE);

        public PopUpDemo() {
            refreshMenuItem.addActionListener((ActionEvent evt) -> {
                refreshHandle();
            });
            copyMenuItem.addActionListener((ActionEvent evt) -> {
                copyHandle();
            });
            pasteMenuItem.addActionListener((ActionEvent evt) -> {
                pasteHandle();
            });

            add(refreshMenuItem);
            add(copyMenuItem);
            add(pasteMenuItem);
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class FileTableModel extends AbstractTableModel {

        private final List<MyFile> files;

        FileTableModel() {
            this.files = new ArrayList<>();
        }

        @Override
        public int getColumnCount() {
            return fileTableColumnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return fileTableColumnNames[column];
        }

        @Override
        public int getRowCount() {
            return files.size();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return ImageIcon.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
                case 4:
                    return Date.class;
                case 5:
                    return String.class;
            }
            return String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row >= files.size()) {
                return null;
            }
            MyFile file = files.get(row);
            switch (column) {
                case 0:
                    return (file.isDirectory() ? ResourceLoader.folder24Icon : GuiUtils.getIcon(file.getType().toLowerCase()));
                case 1:
                    return file.getName();
                case 2:
                    /*
                    if(file.type.equalsIgnoreCase("desktop") || file.type.equalsIgnoreCase("computer") || file.type.equalsIgnoreCase("hdd") || file.type.equalsIgnoreCase("fdd") || file.type.equalsIgnoreCase("cd") || file.type.equalsIgnoreCase("home") || file.type.equalsIgnoreCase("dir")){
                        return ":" + file.type.toUpperCase();
                    }else if(file.type.equalsIgnoreCase("")){
                        return ".";
                    }else{
                        return file.type;
                    }
                     */
                    return file.getType();
                case 3:
                    return GuiUtils.getFormatedSize(file.getSize());
                case 4:
                    return file.getLastModified();
                case 5:
                    return file.getPermission();
                default:
                    return null;
            }
        }

        public Object getRawValueAt(int row, int column) {
            MyFile file = files.get(row);
            switch (column) {
                case 0:
                    file.getType();
                case 1:
                    return file.getName();
                case 2:
                    return file.getType();
                case 3:
                    return file.getSize();
                case 4:
                    return file.getLastModified();
                case 5:
                    return file.getMode();
                default:
                    return null;
            }
        }

        public MyFile getFile(int row) {
            return files.get(row);
        }

        public void addFile(MyFile file) {
            // add new file

            //files.add(file);
            //fireTableDataChanged();
            //fireTableRowsInserted(files.size()-1, files.size()-1);
            int row = getRowCount();
            files.add(row, file);
            //fireTableRowsInserted(row, row);
            fireTableDataChanged();
        }

        public void removeFile(int row) {
            // remove file
            files.remove(row);
            //fireTableRowsDeleted(row, row);
            fireTableDataChanged();
        }

        public void addFile(List<MyFile> files) {
            int row = getRowCount();
            this.files.addAll(files);
            //fireTableRowsInserted(row, getRowCount() - 1);
            fireTableDataChanged();
        }

        public void removeAll() {
            // remove all file
            //fireTableRowsDeleted(0, files.size());
            //fireTableDataChanged();
            /*
            int z =files.size();
            /*files.forEach((t) -> {
                files.remove(t);
            });
            files.clear();
            fireTableRowsDeleted(0, z);*/
 /*
            int z =files.size();
            if(z>0){
            files.clear();
            fireTableRowsDeleted(0, z-1);
            }*/
 /*
            int row = getRowCount();
            if(row>0){
            for(int ii=0;ii<row;ii++){
                removeFile(ii);
            }}*/
 /*
            int rowCount = getRowCount();
            //Remove rows one by one from the end of the table
            for (int i = rowCount - 1; i >= 1; i--) {
                removeFile(i);
            }*/
 /*
            int row = getRowCount();
            if (row > 0) {
                files.removeAll(files);
                fireTableRowsDeleted(0, row);
            }
            fireTableDataChanged();
             */

            files.removeAll(files);
            fireTableDataChanged();
            //fireTableDataChanged();
            //fireTableDataChanged();
            //fireTableStructureChanged();
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class FileTableRowSorter<M extends TableModel> extends TableRowSorter<M> {

        public FileTableRowSorter(M model) {
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
                return fileTableModel.getRawValueAt(row, column);
            }
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class ComboBoxAutoComplete extends KeyAdapter implements DocumentListener {

        private final JComboBox<String> jComboBox;
        private final DefaultComboBoxModel<String> suggestionModel = new DefaultComboBoxModel<>();
        private ComboBoxModel<String> defaultModel;
        private boolean documentUpdated;

        public ComboBoxAutoComplete(JComboBox<String> jComboBox) {
            this.jComboBox = jComboBox;
            this.defaultModel = jComboBox.getModel();
            this.documentUpdated = false;
        }

        public void updateDefaultModel() {
            defaultModel = jComboBox.getModel();
        }

        @Override
        public void keyPressed(KeyEvent ke) {
            // check for some actions
            switch (ke.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    // hide popup
                    jComboBox.hidePopup();
                    // set default model
                    jComboBox.setModel(defaultModel);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent ke) {
            // check for change in text
            if (!documentUpdated || ke.isActionKey()) {
                return;
            }
            // get written newText
            String newText = ((JTextField) ke.getSource()).getText();

            // is it really changed?
            if (newText.equals(jComboBox_Path_String)) {
                return;
            } else {
                jComboBox_Path_String = newText;
            }

            // start working
            if (newText.isEmpty()) {
                // hide popup
                jComboBox.hidePopup();
                // set default model
                jComboBox.setModel(defaultModel);
                // set newText
                ((JTextField) jComboBox.getEditor().getEditorComponent()).setText(newText);
            } else {
                // clear old suggestions Model
                suggestionModel.removeAllElements();
                // iterate over all items
                for (int ii = 0; ii < defaultModel.getSize(); ii++) {
                    Object currentItem = defaultModel.getElementAt(ii);
                    // current item starts with the pattern?
                    if (currentItem != null && currentItem.toString().toLowerCase().startsWith(newText.toLowerCase())) {
                        suggestionModel.addElement(currentItem.toString());
                    }
                }
                if (suggestionModel.getSize() == 0) {
                    // hide popup
                    jComboBox.hidePopup();
                    // set new model
                    jComboBox.setModel(suggestionModel);
                    // set newText
                    ((JTextField) jComboBox.getEditor().getEditorComponent()).setText(newText);
                } else {
                    // hide popup
                    jComboBox.hidePopup();
                    // set new model
                    jComboBox.setModel(suggestionModel);
                    // set newText
                    ((JTextField) jComboBox.getEditor().getEditorComponent()).setText(newText);
                    // set selected index
                    //jComboBox_URL.setSelectedIndex(-1);
                    // show popup
                    jComboBox.showPopup();
                }
            }
            documentUpdated = false;
        }

        public void setDefaultModel(ComboBoxModel<String> mdl) {
            defaultModel = mdl;
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            documentUpdated = true;
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            documentUpdated = true;
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            documentUpdated = true;
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

        jScrollPane_Browser = new javax.swing.JScrollPane();
        jTable_Browser = new javax.swing.JTable(fileTableModel){

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    component.setBackground(MaterialColor.BLUE_100);
                }else{
                    component.setBackground(row % 2 == 0 ? MaterialColor.WHITE : MaterialColor.GREY_50);
                }
                return component;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        }
        ;
        jTextField_Filter = new javax.swing.JTextField();
        jComboBox_Path = new javax.swing.JComboBox<>();
        mButton_GoUp = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Refresh = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Copy = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Paste = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_MakeDirectory = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_MakeFile = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Delete = new com.hq.mobydroid.gui.MaterialButtonIconV();
        mButton_Rename = new com.hq.mobydroid.gui.MaterialButtonIconV();

        setBackground(new java.awt.Color(250, 250, 250));

        jScrollPane_Browser.setBackground(new java.awt.Color(250, 250, 250));
        jScrollPane_Browser.setComponentPopupMenu(new PopUpDemo());

        jTable_Browser.setBackground(new java.awt.Color(250, 250, 250));
        jTable_Browser.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable_Browser.setModel(fileTableModel);
        jTable_Browser.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable_Browser.setComponentPopupMenu(new PopUpDemo());
        jTable_Browser.setShowHorizontalLines(false);
        jTable_Browser.setShowVerticalLines(false);
        jTable_Browser.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable_BrowserFocusGained(evt);
            }
        });
        jTable_Browser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_BrowserMouseClicked(evt);
            }
        });
        jTable_Browser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTable_BrowserKeyTyped(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable_BrowserKeyReleased(evt);
            }
        });
        jScrollPane_Browser.setViewportView(jTable_Browser);

        jTextField_Filter.setMaximumSize(new java.awt.Dimension(64, 2147483647));
        jTextField_Filter.setPreferredSize(new java.awt.Dimension(4, 24));

        jComboBox_Path.setBackground(new java.awt.Color(250, 250, 250));
        jComboBox_Path.setEditable(true);
        jComboBox_Path.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jComboBox_Path.setAutoscrolls(true);

        mButton_GoUp.setToolTipText("Go Up");
        mButton_GoUp.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                goUpHandle();
            }
        });
        mButton_GoUp.setAlignmentX(0.0F);
        mButton_GoUp.setAlignmentY(0.0F);
        mButton_GoUp.setFocusable(true);
        mButton_GoUp.setIcon(MaterialIcons.ARROW_UPWARD);

        mButton_Refresh.setToolTipText("Refresh");
        mButton_Refresh.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                refreshHandle();
            }
        });
        mButton_Refresh.setAlignmentX(0.0F);
        mButton_Refresh.setAlignmentY(0.0F);
        mButton_Refresh.setFocusable(true);
        mButton_Refresh.setIcon(MaterialIcons.REFRESH);

        mButton_Copy.setToolTipText("Copy");
        mButton_Copy.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                copyHandle();
            }
        });
        mButton_Copy.setAlignmentX(0.0F);
        mButton_Copy.setAlignmentY(0.0F);
        mButton_Copy.setFocusable(true);
        mButton_Copy.setIcon(MaterialIcons.CONTENT_COPY);

        mButton_Paste.setToolTipText("Paste");
        mButton_Paste.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                pasteHandle();
            }
        });
        mButton_Paste.setAlignmentX(0.0F);
        mButton_Paste.setAlignmentY(0.0F);
        mButton_Paste.setFocusable(true);
        mButton_Paste.setIcon(MaterialIcons.CONTENT_PASTE);

        mButton_MakeDirectory.setToolTipText("New Directory");
        mButton_MakeDirectory.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                mkdirHandle();
            }
        });
        mButton_MakeDirectory.setAlignmentX(0.0F);
        mButton_MakeDirectory.setAlignmentY(0.0F);
        mButton_MakeDirectory.setFocusable(true);
        mButton_MakeDirectory.setIcon(MaterialIcons.CREATE_NEW_FOLDER);

        mButton_MakeFile.setToolTipText("New File");
        mButton_MakeFile.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                mkfileHandle();
            }
        });
        mButton_MakeFile.setAlignmentX(0.0F);
        mButton_MakeFile.setAlignmentY(0.0F);
        mButton_MakeFile.setFocusable(true);
        mButton_MakeFile.setIcon(MaterialIcons.NOTE_ADD);

        mButton_Delete.setToolTipText("Delete");
        mButton_Delete.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                deleteHandle();
            }
        });
        mButton_Delete.setAlignmentX(0.0F);
        mButton_Delete.setAlignmentY(0.0F);
        mButton_Delete.setFocusable(true);
        mButton_Delete.setIcon(MaterialIcons.DELETE);

        mButton_Rename.setToolTipText("Rename");
        mButton_Rename.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                renameHandle();
            }
        });
        mButton_Rename.setAlignmentX(0.0F);
        mButton_Rename.setAlignmentY(0.0F);
        mButton_Rename.setFocusable(true);
        mButton_Rename.setIcon(MaterialIcons.EDIT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mButton_GoUp, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Copy, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Paste, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_MakeDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_MakeFile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mButton_Rename, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox_Path, 0, 384, Short.MAX_VALUE)
                        .addGap(2, 2, 2)
                        .addComponent(jTextField_Filter, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane_Browser, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField_Filter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox_Path))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mButton_Refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_Copy, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_Paste, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_Rename, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_MakeDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mButton_MakeFile, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane_Browser, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mButton_GoUp, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(184, 184, 184))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable_BrowserMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_BrowserMouseClicked
        // TODO add your handling code here:
        if ((evt.getClickCount() == 2) && (evt.getButton() == 1)) {
            //int row = jTable_Browser.getSelectedRow();
            int row = jTable_Browser.convertRowIndexToModel(jTable_Browser.rowAtPoint(evt.getPoint()));
            MyFile file = fileTableModel.getFile(row);
            if (file.isDirectory()) {
                updateFilesList(file.getPath());
            }
        }
    }//GEN-LAST:event_jTable_BrowserMouseClicked

    private void jTable_BrowserKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_BrowserKeyTyped
        int startRow = jTable_Browser.getSelectedRow();
        if (startRow < 0) {
            startRow = 0;
        } else {
            startRow++;
        }
        for (int row = startRow; row < jTable_Browser.getRowCount(); row++) {
            if (((String) jTable_Browser.getValueAt(row, 1)).toLowerCase().startsWith(String.valueOf(Character.toLowerCase(evt.getKeyChar())))) {
                jTable_Browser.changeSelection(row, 0, false, false);
                return;
            }
        }

        for (int row = 0; row < jTable_Browser.getRowCount(); row++) {
            if (((String) jTable_Browser.getValueAt(row, 1)).toLowerCase().startsWith(String.valueOf(Character.toLowerCase(evt.getKeyChar())))) {
                jTable_Browser.changeSelection(row, 0, false, false);
                return;
            }
        }
    }//GEN-LAST:event_jTable_BrowserKeyTyped

    private void jTable_BrowserFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable_BrowserFocusGained
        // select first row if Selection Model is Empty
        if (jTable_Browser.getSelectionModel().isSelectionEmpty() && jTable_Browser.getModel().getRowCount() > 0) {
            jTable_Browser.setRowSelectionInterval(0, 0);
        }
    }//GEN-LAST:event_jTable_BrowserFocusGained

    private void jTable_BrowserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_BrowserKeyReleased
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                //if(currentFile.type.equalsIgnoreCase("desktop") || currentFile.type.equalsIgnoreCase("computer") || currentFile.type.equalsIgnoreCase("hdd") || currentFile.type.equalsIgnoreCase("fdd") || currentFile.type.equalsIgnoreCase("cd") || currentFile.type.equalsIgnoreCase("home") || currentFile.type.equalsIgnoreCase("dir")){
                //    getChildren();
                //}
                int row = jTable_Browser.getSelectedRow();
                MyFile file = fileTableModel.getFile(jTable_Browser.convertRowIndexToModel(row));
                if (file.isDirectory()) {
                    updateFilesList(file.getPath());
                }
                break;
            case KeyEvent.VK_BACK_SPACE:
                goUpHandle();
                break;
            case KeyEvent.VK_HOME:
                jTable_Browser.changeSelection(0, 0, false, false);
                break;
            case KeyEvent.VK_END:
                jTable_Browser.changeSelection(jTable_Browser.getRowCount() - 1, 0, false, false);
                break;
            default:
                break;
        }
    }//GEN-LAST:event_jTable_BrowserKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jComboBox_Path;
    private javax.swing.JScrollPane jScrollPane_Browser;
    private javax.swing.JTable jTable_Browser;
    private javax.swing.JTextField jTextField_Filter;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Copy;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Delete;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_GoUp;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_MakeDirectory;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_MakeFile;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Paste;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Refresh;
    private com.hq.mobydroid.gui.MaterialButtonIconV mButton_Rename;
    // End of variables declaration//GEN-END:variables
}
