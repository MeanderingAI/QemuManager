package com.qemumanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing QEMU virtual machines
 */
public class QemuVmPanel extends JPanel {
    private List<QemuVm> virtualMachines;
    private JTable vmTable;
    private DefaultTableModel tableModel;
    private QemuConsolePanel consolePanel;
    
    public QemuVmPanel() {
        virtualMachines = new ArrayList<>();
        initializeUI();
        loadVmState(); // Load saved VMs on startup
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {"Name", "Status", "Memory (MB)", "CPU Cores", "Architecture", "Network", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
        };
        
        vmTable = new JTable(tableModel);
        vmTable.setRowHeight(60); // Increased row height for larger buttons
        vmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Set up actions column with buttons
        vmTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        vmTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Configure column widths
        vmTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Name
        vmTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Status
        vmTable.getColumnModel().getColumn(2).setPreferredWidth(90);  // Memory
        vmTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // CPU
        vmTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Architecture
        vmTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Network (wider for descriptive text)
        vmTable.getColumnModel().getColumn(6).setPreferredWidth(320); // Actions (wider for larger Connect button)
        vmTable.getColumnModel().getColumn(6).setMinWidth(260);       // Minimum width for actions
        
        // Add double-click listener to edit VM
        vmTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = vmTable.getSelectedRow();
                    if (row >= 0) {
                        editVirtualMachine(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(vmTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton newVmButton = new JButton("New VM");
        JButton editVmButton = new JButton("Edit");
        JButton deleteVmButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");
        
        newVmButton.addActionListener(e -> createNewVm());
        editVmButton.addActionListener(e -> editSelectedVm());
        deleteVmButton.addActionListener(e -> deleteSelectedVm());
        refreshButton.addActionListener(e -> refreshVmList());
        
        toolbar.add(newVmButton);
        toolbar.add(editVmButton);
        toolbar.add(deleteVmButton);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(refreshButton);
        
        add(toolbar, BorderLayout.NORTH);
    }
    
    public void setConsolePanel(QemuConsolePanel consolePanel) {
        this.consolePanel = consolePanel;
    }
    
    public void addVirtualMachine(QemuVm vm) {
        virtualMachines.add(vm);
        refreshTable();
        saveVmState(); // Save state when VM is added
    }
    
    /**
     * Loads VM state from persistent storage
     */
    public void loadVmState() {
        try {
            List<QemuVm> savedVms = QemuVmStateManager.getInstance().loadVmState();
            virtualMachines.clear();
            virtualMachines.addAll(savedVms);
            refreshTable();
            
            if (consolePanel != null) {
                consolePanel.appendMessage("Loaded " + savedVms.size() + " virtual machines from saved state");
            }
        } catch (Exception e) {
            if (consolePanel != null) {
                consolePanel.appendMessage("Failed to load VM state: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Clears all VMs from memory (does not delete state file)
     */
    public void clearVMs() {
        virtualMachines.clear();
        refreshTable();
    }
    
    /**
     * Saves current VM state to persistent storage
     */
    public void saveVmState() {
        try {
            QemuVmStateManager.getInstance().saveVmState(virtualMachines);
        } catch (Exception e) {
            if (consolePanel != null) {
                consolePanel.appendMessage("Failed to save VM state: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    public List<QemuVm> getVirtualMachines() {
        return new ArrayList<>(virtualMachines); // Return a copy for safety
    }
    
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (QemuVm vm : virtualMachines) {
            Object[] rowData = {
                vm.getName(),
                vm.getStatus(),
                vm.getMemoryMB(),
                vm.getCpuCores(),
                vm.getArchitecture(),
                vm.getNetworkDescription(), // Use descriptive network info
                "Actions"
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void createNewVm() {
        QemuVmDialog dialog = new QemuVmDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
            "Create New Virtual Machine", true);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            QemuVm vm = dialog.getVirtualMachine();
            addVirtualMachine(vm);
        }
    }
    
    private void editSelectedVm() {
        int selectedRow = vmTable.getSelectedRow();
        if (selectedRow >= 0) {
            editVirtualMachine(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a virtual machine to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void editVirtualMachine(int index) {
        if (index >= 0 && index < virtualMachines.size()) {
            QemuVm vm = virtualMachines.get(index);
            QemuVmDialog dialog = new QemuVmDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                "Edit Virtual Machine", true);
            dialog.setVirtualMachine(vm);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                QemuVm updatedVm = dialog.getVirtualMachine();
                virtualMachines.set(index, updatedVm);
                refreshTable();
                saveVmState(); // Save state when VM is edited
            }
        }
    }
    
    private void deleteSelectedVm() {
        int selectedRow = vmTable.getSelectedRow();
        if (selectedRow >= 0) {
            QemuVm vm = virtualMachines.get(selectedRow);
            int result = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete the virtual machine '" + vm.getName() + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Stop VM if running
                if (vm.getStatus() == QemuVm.VmStatus.RUNNING) {
                    stopVirtualMachine(vm);
                }
                virtualMachines.remove(selectedRow);
                refreshTable();
                saveVmState(); // Save state when VM is deleted
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a virtual machine to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void refreshVmList() {
        // Update status of all VMs
        for (QemuVm vm : virtualMachines) {
            updateVmStatus(vm);
        }
        refreshTable();
    }
    
    private void updateVmStatus(QemuVm vm) {
        Process process = vm.getQemuProcess();
        if (process != null) {
            if (process.isAlive()) {
                vm.setStatus(QemuVm.VmStatus.RUNNING);
            } else {
                vm.setStatus(QemuVm.VmStatus.STOPPED);
                vm.setQemuProcess(null);
            }
        }
    }
    
    private void startVirtualMachine(QemuVm vm) {
        if (vm.getStatus() == QemuVm.VmStatus.RUNNING) {
            JOptionPane.showMessageDialog(this, "Virtual machine is already running.", 
                "Already Running", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            vm.setStatus(QemuVm.VmStatus.STARTING);
            refreshTable();
            
            String[] command = vm.generateQemuCommand();
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            vm.setQemuProcess(process);
            vm.setStatus(QemuVm.VmStatus.RUNNING);
            
            if (consolePanel != null) {
                consolePanel.appendMessage("Started VM: " + vm.getName());
                consolePanel.monitorProcess(process, vm.getName());
            }
            
            refreshTable();
            
        } catch (IOException e) {
            vm.setStatus(QemuVm.VmStatus.STOPPED);
            refreshTable();
            
            JOptionPane.showMessageDialog(this, 
                "Failed to start virtual machine: " + e.getMessage(),
                "Start Error", JOptionPane.ERROR_MESSAGE);
            
            if (consolePanel != null) {
                consolePanel.appendMessage("Failed to start VM " + vm.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void stopVirtualMachine(QemuVm vm) {
        Process process = vm.getQemuProcess();
        if (process != null && process.isAlive()) {
            vm.setStatus(QemuVm.VmStatus.STOPPING);
            refreshTable();
            
            process.destroy();
            
            // Wait for graceful shutdown, then force kill if needed
            new Thread(() -> {
                try {
                    boolean exited = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                    if (!exited) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                }
                
                SwingUtilities.invokeLater(() -> {
                    vm.setStatus(QemuVm.VmStatus.STOPPED);
                    vm.setQemuProcess(null);
                    refreshTable();
                    
                    if (consolePanel != null) {
                        consolePanel.appendMessage("Stopped VM: " + vm.getName());
                    }
                });
            }).start();
        }
    }
    
    private void connectToVm(QemuVm vm) {
        String vncViewer = QemuSettings.getInstance().getVncViewerPath();
        if (vncViewer.isEmpty()) {
            // Try to find a common VNC viewer automatically
            vncViewer = findVncViewer();
            if (vncViewer == null) {
                JOptionPane.showMessageDialog(this, 
                    "VNC viewer not configured and none found automatically.\n" +
                    "Please install a VNC viewer (like tigervnc-viewer, vncviewer, or gvncviewer)\n" +
                    "and configure the path in Settings.",
                    "VNC Viewer Not Found", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        try {
            String vncAddress = "localhost:" + vm.getVncPort();
            ProcessBuilder pb = new ProcessBuilder(vncViewer, vncAddress);
            pb.start();
            
            if (consolePanel != null) {
                consolePanel.appendMessage("Connecting to VM: " + vm.getName() + 
                    " via VNC on port " + vm.getVncPort());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to launch VNC viewer: " + e.getMessage(),
                "VNC Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String findVncViewer() {
        // List of common VNC viewers to try
        String[] vncViewers = {
            "vncviewer",           // TigerVNC
            "tigervnc-viewer",     // TigerVNC (alternate name)
            "gvncviewer",          // GNOME VNC viewer
            "remmina",             // Remmina
            "krdc",                // KDE Remote Desktop Connection
            "vinagre"              // GNOME VNC viewer (older)
        };
        
        for (String viewer : vncViewers) {
            try {
                ProcessBuilder pb = new ProcessBuilder("which", viewer);
                Process process = pb.start();
                if (process.waitFor() == 0) {
                    return viewer;
                }
            } catch (IOException | InterruptedException e) {
                // Continue trying next viewer
            }
        }
        
        return null; // No VNC viewer found
    }
    
    // Button renderer for actions column
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton startButton, stopButton, connectButton;
        
        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
            startButton = new JButton("Start");
            stopButton = new JButton("Stop");
            connectButton = new JButton("Connect");
            
            // Larger button sizes for better visibility
            startButton.setPreferredSize(new Dimension(90, 50));
            stopButton.setPreferredSize(new Dimension(90, 50));
            connectButton.setPreferredSize(new Dimension(110, 50)); // Wider for "Connect" text
            
            // Make buttons more visually appealing with larger font
            Font buttonFont = new Font(Font.DIALOG, Font.BOLD, 14);
            startButton.setFont(buttonFont);
            stopButton.setFont(buttonFont);
            connectButton.setFont(buttonFont);
            
            startButton.setBackground(new Color(34, 139, 34));  // Forest Green
            startButton.setForeground(Color.WHITE);
            startButton.setToolTipText("Start this virtual machine");
            
            stopButton.setBackground(new Color(220, 20, 60));   // Crimson
            stopButton.setForeground(Color.WHITE);
            stopButton.setToolTipText("Stop this virtual machine");
            
            connectButton.setBackground(new Color(30, 144, 255)); // Dodger Blue
            connectButton.setForeground(Color.WHITE);
            connectButton.setToolTipText("Connect to VM via VNC viewer");
            
            add(startButton);
            add(stopButton);
            add(connectButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (row < virtualMachines.size()) {
                QemuVm vm = virtualMachines.get(row);
                startButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.STOPPED);
                stopButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.RUNNING);
                connectButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.RUNNING);
            }
            
            return this;
        }
    }
    
    // Button editor for actions column
    class ButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton startButton, stopButton, connectButton;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            
            startButton = new JButton("Start");
            stopButton = new JButton("Stop");
            connectButton = new JButton("Connect");
            
            // Match the renderer button sizes and fonts
            startButton.setPreferredSize(new Dimension(90, 50));
            stopButton.setPreferredSize(new Dimension(90, 50));
            connectButton.setPreferredSize(new Dimension(110, 50)); // Wider for "Connect" text
            
            // Apply larger font for better readability
            Font buttonFont = new Font(Font.DIALOG, Font.BOLD, 14);
            startButton.setFont(buttonFont);
            stopButton.setFont(buttonFont);
            connectButton.setFont(buttonFont);
            
            // Apply the same colors as renderer
            startButton.setBackground(new Color(34, 139, 34));  // Forest Green
            startButton.setForeground(Color.WHITE);
            startButton.setToolTipText("Start this virtual machine");
            
            stopButton.setBackground(new Color(220, 20, 60));   // Crimson
            stopButton.setForeground(Color.WHITE);
            stopButton.setToolTipText("Stop this virtual machine");
            
            connectButton.setBackground(new Color(30, 144, 255)); // Dodger Blue
            connectButton.setForeground(Color.WHITE);
            connectButton.setToolTipText("Connect to VM via VNC viewer");
            
            startButton.addActionListener(e -> {
                if (currentRow < virtualMachines.size()) {
                    startVirtualMachine(virtualMachines.get(currentRow));
                }
                fireEditingStopped();
            });
            
            stopButton.addActionListener(e -> {
                if (currentRow < virtualMachines.size()) {
                    stopVirtualMachine(virtualMachines.get(currentRow));
                }
                fireEditingStopped();
            });
            
            connectButton.addActionListener(e -> {
                if (currentRow < virtualMachines.size()) {
                    connectToVm(virtualMachines.get(currentRow));
                }
                fireEditingStopped();
            });
            
            panel.add(startButton);
            panel.add(stopButton);
            panel.add(connectButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            if (row < virtualMachines.size()) {
                QemuVm vm = virtualMachines.get(row);
                startButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.STOPPED);
                stopButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.RUNNING);
                connectButton.setEnabled(vm.getStatus() == QemuVm.VmStatus.RUNNING);
            }
            
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }
}