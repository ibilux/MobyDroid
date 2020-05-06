/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.device.MobydroidDevice;
import com.hq.mobydroid.device.TaskWorker;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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
public class JPanel_TaskManager extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final TaskTableModel taskTableModel = new TaskTableModel();
    private final String[] taskTableColumnNames = {"", "Task", "Progess", "Status"};
    // *************************************************************

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_TaskManager() {
        // initialize components
        initComponents();

        // table dimension
        jTable_Tasks.setRowHeight(GuiUtils.APK_ICON_HEIGTH + 6);
        setColumnWidth(0, 32, 32);
        setColumnWidth(1, 384, -1);
        setColumnWidth(2, 64, 128);
        setColumnWidth(3, 64, 128);

        // set Table Row Sorter
        TableRowSorter tableRowSorter = new TaskTableRowSorter(jTable_Tasks.getModel());
        jTable_Tasks.setRowSorter(tableRowSorter);
        tableRowSorter.setComparator(2, (Comparator<Integer>) (o1, o2) -> o1.compareTo(o2));
        tableRowSorter.setComparator(3, (Comparator<TaskWorker.Status>) (o1, o2) -> o1.compareTo(o2));

        // set table header for  0nd column
        jTable_Tasks.getColumnModel().getColumn(0).setHeaderRenderer(new JCheckBoxTableHeaderCellRenderer());

        // set cell render 1th column
        jTable_Tasks.getColumnModel().getColumn(1).setCellRenderer(new TaskNameCellRenderer());

        // set cell render 2nd column
        jTable_Tasks.getColumnModel().getColumn(2).setCellRenderer(new TaskProgressCellRenderer());

        // set cell color 3rd column
        jTable_Tasks.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof TaskWorker.Status) {
                    TaskWorker.Status status = (TaskWorker.Status) value;
                    JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    component.setText(status.toString());
                    component.setForeground(GuiUtils.getStatusColor(status));
                    return component;
                }
                return this;
            }
        });

        // header click event
        jTable_Tasks.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int column = jTable_Tasks.convertColumnIndexToModel(jTable_Tasks.getColumnModel().getColumnIndexAtX(mouseEvent.getX()));
                if (mouseEvent.getClickCount() == 1 && column != -1) {
                    taskTableModel.headerClicked(column);
                }
            }
        });

        // set tab action to change focus component outside jtable
        jTable_Tasks.getActionMap().put(jTable_Tasks.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        // KeyBinding
        jTable_Tasks.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        jTable_Tasks.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "none");
        jTable_Tasks.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "none");

    }

    /**
     * fire Table Data Changed when a new TaskWorker PropertyChangeEvent fired.
     */
    public void fireTableDataChanged() {
        jTable_Tasks.setVisible(false);
        taskTableModel.fireTableDataChanged();
        jTable_Tasks.setVisible(true);
    }

    /**
     * Handle buttons events.
     */
    private void pauseResumeHandle() {
        pauseResumeTasks();
    }

    private void cancelHandle() {
        cancelTasksList();
    }

    private void clearHandle() {
        clearTasksList();
    }

    private void RefreshHandle() {
        updateTasksList();
    }

    private boolean isTasksMarked() {
        MobydroidDevice mDevice = MobyDroid.getDevice();
        if (mDevice == null) {
            return false;
        }
        int marked = (int) mDevice.getTasks().stream().filter((task) -> (task.isMarked())).count();
        if (marked == 0) {
            JOptionPane.showMessageDialog(this, "Please mark task for operation.", "No task marked", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return false;
        }
        return true;
    }

    private void enableUI() {
        // enable buttons
        materialButtonH_Cancel.setEnabled(true);
        materialButtonH_Clear.setEnabled(true);
        materialButtonH_PauseResume.setEnabled(true);
        materialButtonH_Refresh.setEnabled(true);
        // enable jTable
        jTable_Tasks.setVisible(true);
    }

    private void disableUI() {
        // disable jTable
        jTable_Tasks.setVisible(false);
        // disable buttons
        materialButtonH_Cancel.setEnabled(false);
        materialButtonH_Clear.setEnabled(false);
        materialButtonH_PauseResume.setEnabled(false);
        materialButtonH_Refresh.setEnabled(false);
    }

    /**
     * Start or Stop tasks runner.
     */
    private void pauseResumeTasks() {
        MobydroidDevice mDevice = MobyDroid.getDevice();
        if (mDevice == null) {
            return;
        }
        if (mDevice.getTasksRunnerStatus()) { // is running
            mDevice.stopTasksRunner();
            materialButtonH_PauseResume.setIcon(MaterialIcons.PLAY_ARROW);
            materialButtonH_PauseResume.setText("Resume");
        } else { // is stoped
            mDevice.startTasksRunner();
            materialButtonH_PauseResume.setIcon(MaterialIcons.PAUSE);
            materialButtonH_PauseResume.setText("Pause");
        }
    }

    /**
     * Cancel tasks.
     */
    private void cancelTasksList() {
        // check if any tasks are marked
        MobydroidDevice mDevice = MobyDroid.getDevice();
        if (mDevice == null) {
            return;
        }
        if (isTasksMarked()) {
            // disable UI
            disableUI();

            // start cancelling tasks
            mDevice.getTasks().stream().filter((task) -> (task.isMarked())).forEach((task) -> {
                task.cancel(false);
            });

            // enable UI
            enableUI();

            // update
            updateTasksList();
        }
    }

    /**
     * Clear dead tasks list.
     */
    private void clearTasksList() {
        // check before clearing
        MobydroidDevice mDevice = MobyDroid.getDevice();
        if (mDevice == null) {
            return;
        }
        // disable UI
        disableUI();

        // clear
        mDevice.clearDeadTasks();

        // enable UI
        enableUI();

        // update
        updateTasksList();
    }

    /**
     * Update tasks list.
     */
    private void updateTasksList() {
        // doesn't do much really ! :|
        fireTableDataChanged();
    }

    /**
     *
     */
    private void setColumnWidth(int column, int minWidth, int maxWidth) {
        TableColumn tableColumn = jTable_Tasks.getColumnModel().getColumn(column);
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

// ************************************************************* //
// ************************************************************* //
    private class TaskTableModel extends AbstractTableModel {

        TaskTableModel() {
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
                    return TaskWorker.Status.class;
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
            switch (column) {
                case 0:
                    MobydroidDevice mDevice = MobyDroid.getDevice();
                    if (mDevice == null) {
                        return;
                    }
                    TaskWorker task = mDevice.getTasks().get(row);
                    task.setMark(!task.isMarked());
                    break;
            }
        }

        @Override
        public int getColumnCount() {
            return taskTableColumnNames.length;
        }

        @Override
        public int getRowCount() {
            MobydroidDevice mDevice = MobyDroid.getDevice();
            if (mDevice == null) {
                return 0;
            }
            return mDevice.getTasks().size();
        }

        @Override
        public String getColumnName(int column) {
            return taskTableColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            MobydroidDevice mDevice = MobyDroid.getDevice();
            if (mDevice == null) {
                return null;
            }
            TaskWorker task = mDevice.getTasks().get(row);
            switch (column) {
                case 0:
                    return task.isMarked();
                case 1:
                    return task;
                case 2:
                    return task.getProgress();
                case 3:
                    return task.getStatus();
                default:
                    return null;
            }
        }

        public Object getRawValueAt(int row, int column) {
            MobydroidDevice mDevice = MobyDroid.getDevice();
            if (mDevice == null) {
                return null;
            }
            TaskWorker task = mDevice.getTasks().get(row);
            switch (column) {
                case 0:
                    return task.isMarked();
                case 1:
                    return task;
                case 2:
                    return task.getProgress();
                case 3:
                    return task.getState();
                default:
                    return null;
            }
        }

        private void headerClicked(int column) {
            if (column == 0) {
                MobydroidDevice mDevice = MobyDroid.getDevice();
                if (mDevice == null) {
                    return;
                }
                JCheckBox jCheckBox = (JCheckBox) jTable_Tasks.getTableHeader().getColumnModel().getColumn(jTable_Tasks.convertColumnIndexToView(column)).getHeaderRenderer().getTableCellRendererComponent(null, null, false, false, 0, 0);
                jCheckBox.setSelected(!jCheckBox.isSelected());
                mDevice.getTasks().forEach((task) -> {
                    task.setMark(jCheckBox.isSelected());
                });
                // Forces the header to resize and repaint itself
                jTable_Tasks.getTableHeader().resizeAndRepaint();
                // fire
                fireTableDataChanged();
            }
        }

    }

// ************************************************************* //
// ************************************************************* //
    private class TaskTableRowSorter<M extends TableModel> extends TableRowSorter<M> {

        public TaskTableRowSorter(M model) {
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
                return taskTableModel.getRawValueAt(row, column);
            }
        }
    }

// ************************************************************* //
// ************************************************************* //
    private class TaskNameCellRenderer implements TableCellRenderer {

        private final JPanel jpanel;
        private final JLabel jLabel_Name;
        private final JLabel jLabel_Message;
        private final JLabel jLabel_Icon;

        public TaskNameCellRenderer() {
            jpanel = new JPanel();
            jLabel_Name = new JLabel();
            jLabel_Message = new JLabel();
            jLabel_Icon = new javax.swing.JLabel();

            jLabel_Name.setFont(new java.awt.Font("Dialog", 1, 12));
            jLabel_Name.setForeground(Color.BLACK);

            jLabel_Message.setFont(new java.awt.Font("Dialog", 1, 10));
            jLabel_Message.setForeground(MaterialColor.GREY_700);

            jLabel_Icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jpanel);
            jpanel.setLayout(layout);

            layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel_Message, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                    .addComponent(jLabel_Name, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            );
            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel_Name, javax.swing.GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE)
                            .addGap(0, 0, 0)
                            .addComponent(jLabel_Message, javax.swing.GroupLayout.PREFERRED_SIZE, 15, Short.MAX_VALUE))
            );
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            TaskWorker task = (TaskWorker) value;
            jLabel_Name.setText(task.getName());
            jLabel_Message.setForeground(GuiUtils.getStatusColor(task.getStatus()));
            jLabel_Message.setText(task.getMessage());
            jLabel_Icon.setIcon(task.getIcon());

            if (hasFocus) {
                jpanel.setBorder(javax.swing.BorderFactory.createLineBorder(MaterialColor.BLUE_400));
            } else {
                jpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            }

            return jpanel;
        }
    }
// ************************************************************* //
// ************************************************************* //

    private class TaskProgressCellRenderer implements TableCellRenderer {

        private final JProgressBar jProgressBar;

        public TaskProgressCellRenderer() {
            jProgressBar = new JProgressBar(0, 100);
            jProgressBar.setBackground(MaterialColor.WHITE);
            jProgressBar.setForeground(MaterialColor.BLUE_700);
            jProgressBar.setStringPainted(true);
            jProgressBar.setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            jProgressBar.setValue((int) value);
            jProgressBar.setString(String.valueOf((int) value) + " %");
            return jProgressBar;
        }
    }
// ************************************************************* //
// ************************************************************* //

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

        jTableScrollPane_Tasks = new javax.swing.JScrollPane();
        jTable_Tasks = new javax.swing.JTable(){
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
        materialButtonH_PauseResume = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Cancel = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Clear = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Refresh = new com.hq.mobydroid.gui.MaterialButtonV();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Install New Apps : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        jTableScrollPane_Tasks.setBackground(new java.awt.Color(250, 250, 250));

        jTable_Tasks.setBackground(new java.awt.Color(250, 250, 250));
        jTable_Tasks.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable_Tasks.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTable_Tasks.setModel(taskTableModel);
        jTable_Tasks.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable_Tasks.setShowHorizontalLines(false);
        jTable_Tasks.setShowVerticalLines(false);
        jTable_Tasks.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable_TasksFocusGained(evt);
            }
        });
        jTable_Tasks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable_TasksKeyPressed(evt);
            }
        });
        jTableScrollPane_Tasks.setViewportView(jTable_Tasks);

        materialButtonH_PauseResume.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                pauseResumeHandle();
            }
        });
        materialButtonH_PauseResume.setFocusable(true);
        materialButtonH_PauseResume.setIcon(MaterialIcons.PAUSE);
        materialButtonH_PauseResume.setText("Pause");

        materialButtonH_Cancel.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                cancelHandle();
            }
        });
        materialButtonH_Cancel.setFocusable(true);
        materialButtonH_Cancel.setIcon(MaterialIcons.UNARCHIVE);
        materialButtonH_Cancel.setText("Cancel");

        materialButtonH_Clear.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                clearHandle();
            }
        });
        materialButtonH_Clear.setFocusable(true);
        materialButtonH_Clear.setIcon(MaterialIcons.SAVE);
        materialButtonH_Clear.setText("Clear");

        materialButtonH_Refresh.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                RefreshHandle();
            }
        });
        materialButtonH_Refresh.setFocusable(true);
        materialButtonH_Refresh.setIcon(MaterialIcons.REFRESH);
        materialButtonH_Refresh.setText("Refresh");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(materialButtonH_PauseResume, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Clear, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jTableScrollPane_Tasks, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(materialButtonH_PauseResume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Cancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTableScrollPane_Tasks, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable_TasksKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_TasksKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_TAB:
                //KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                break;
        }
    }//GEN-LAST:event_jTable_TasksKeyPressed

    private void jTable_TasksFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable_TasksFocusGained
        // select first row if Selection Model is Empty
        if (jTable_Tasks.getSelectionModel().isSelectionEmpty() && jTable_Tasks.getModel().getRowCount() > 0) {
            jTable_Tasks.setRowSelectionInterval(0, 0);
        }
    }//GEN-LAST:event_jTable_TasksFocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jTableScrollPane_Tasks;
    private javax.swing.JTable jTable_Tasks;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Cancel;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Clear;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_PauseResume;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Refresh;
    // End of variables declaration//GEN-END:variables
}
