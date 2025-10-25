package com.qemumanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Panel for managing disk images
 */
public class QemuDiskPanel extends JPanel {
    private JTable diskTable;
    private DefaultTableModel tableModel;
    private QemuConsolePanel consolePanel;
    private List<QemuVm> virtualMachines;
    
    public QemuDiskPanel() {
        virtualMachines = new ArrayList<>();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columnNames = {"File Name", "Path", "Format", "Size", "Used By", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        diskTable = new JTable(tableModel);
        diskTable.setRowHeight(60); // Increased from 50 to 60 for much larger buttons
        diskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configure column widths
        diskTable.getColumnModel().getColumn(0).setPreferredWidth(150); // File Name
        diskTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Path
        diskTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Format
        diskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Size
        diskTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Used By
        diskTable.getColumnModel().getColumn(5).setPreferredWidth(300); // Actions - increased from 250 to 300
        diskTable.getColumnModel().getColumn(5).setMinWidth(300); // Increased minimum width
        
        // Set up actions column with buttons
        diskTable.getColumn("Actions").setCellRenderer(new DiskButtonRenderer());
        diskTable.getColumn("Actions").setCellEditor(new DiskButtonEditor(new JCheckBox()));
        
        // Add double-click listener to show disk info
        diskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = diskTable.getSelectedRow();
                    if (row >= 0) {
                        showDiskInfo(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(diskTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton createDiskButton = new JButton("Create New Disk");
        JButton refreshButton = new JButton("Refresh");
        JButton scanButton = new JButton("Scan Directory");
        JButton cleanupButton = new JButton("Cleanup Unused");
        
        createDiskButton.setToolTipText("Create a new disk image");
        refreshButton.setToolTipText("Refresh list (includes VM disks + scans disk directory)");
        scanButton.setToolTipText("Scan a different directory for disk images");
        cleanupButton.setToolTipText("Find unused disk images for cleanup");
        
        createDiskButton.addActionListener(e -> createNewDisk());
        refreshButton.addActionListener(e -> refreshDiskList());
        scanButton.addActionListener(e -> scanForDisks());
        cleanupButton.addActionListener(e -> cleanupUnusedDisks());
        
        toolbar.add(createDiskButton);
        toolbar.add(refreshButton);
        toolbar.add(scanButton);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(cleanupButton);
        
        add(toolbar, BorderLayout.NORTH);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Ready - Double-click a disk for detailed information");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    public void setConsolePanel(QemuConsolePanel consolePanel) {
        this.consolePanel = consolePanel;
    }
    
    public void setVirtualMachines(List<QemuVm> virtualMachines) {
        this.virtualMachines = virtualMachines;
        refreshDiskList();
    }
    
    private void refreshDiskList() {
        tableModel.setRowCount(0);
        
        // Get all disk paths from VMs
        Set<String> diskPaths = new HashSet<>();
        for (QemuVm vm : virtualMachines) {
            if (vm.getDiskPath() != null && !vm.getDiskPath().trim().isEmpty()) {
                diskPaths.add(vm.getDiskPath().trim());
            }
        }
        
        // Add disk information to table for VM-associated disks
        for (String diskPath : diskPaths) {
            File diskFile = new File(diskPath);
            if (diskFile.exists()) {
                addDiskToTable(diskFile, getVmUsingDisk(diskPath));
            } else {
                // Add missing disk with warning
                Object[] rowData = {
                    diskFile.getName(),
                    diskPath,
                    "Unknown",
                    "Missing File",
                    getVmUsingDisk(diskPath),
                    "Actions"
                };
                tableModel.addRow(rowData);
            }
        }
        
        // Also scan the default disk directory for standalone disks
        QemuSettings settings = QemuSettings.getInstance();
        settings.ensureDirectoriesExist(); // Make sure directories exist
        File defaultDir = new File(settings.getQemuManagerDisksPath());
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            scanDirectoryForDisks(defaultDir, false); // false = don't show scan message
        }
        
        if (consolePanel != null) {
            consolePanel.appendMessage("Disk list refreshed - showing " + tableModel.getRowCount() + " disk images");
        }
    }
    
    private void addDiskToTable(File diskFile, String usedBy) {
        try {
            String fileName = diskFile.getName();
            String fullPath = diskFile.getAbsolutePath();
            String format = getImageFormat(diskFile);
            String size = formatFileSize(diskFile.length());
            
            Object[] rowData = {
                fileName,
                fullPath,
                format,
                size,
                usedBy,
                "Actions"
            };
            
            tableModel.addRow(rowData);
            
        } catch (Exception e) {
            if (consolePanel != null) {
                consolePanel.appendMessage("Error processing disk file " + diskFile.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private String getImageFormat(File diskFile) {
        String fileName = diskFile.getName().toLowerCase();
        if (fileName.endsWith(".qcow2")) return "QCOW2";
        if (fileName.endsWith(".img")) return "RAW";
        if (fileName.endsWith(".vmdk")) return "VMDK";
        if (fileName.endsWith(".vdi")) return "VDI";
        if (fileName.endsWith(".vhd")) return "VHD";
        if (fileName.endsWith(".iso")) return "ISO";
        return "Unknown";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        return String.format("%.1f %s", bytes / Math.pow(1024, exp), units[exp]);
    }
    
    private String getVmUsingDisk(String diskPath) {
        List<String> vmNames = new ArrayList<>();
        for (QemuVm vm : virtualMachines) {
            if (vm.getDiskPath() != null && vm.getDiskPath().equals(diskPath)) {
                vmNames.add(vm.getName());
            }
        }
        
        if (vmNames.isEmpty()) {
            return "Not Used";
        } else if (vmNames.size() == 1) {
            return vmNames.get(0);
        } else {
            return vmNames.size() + " VMs";
        }
    }
    
    private void createNewDisk() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        QemuDiskCreationDialog dialog;
        
        if (parentWindow instanceof JFrame) {
            // Create a temporary dialog parent
            JDialog tempDialog = new JDialog((JFrame) parentWindow, true);
            tempDialog.setVisible(false);
            dialog = new QemuDiskCreationDialog(tempDialog, consolePanel);
        } else {
            dialog = new QemuDiskCreationDialog((JDialog) parentWindow, consolePanel);
        }
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshDiskList();
            if (consolePanel != null) {
                consolePanel.appendMessage("New disk created: " + dialog.getDiskPath());
            }
        }
    }
    
    private void scanForDisks() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setDialogTitle("Select Directory to Scan for Additional Disk Images");
        
        // Start from default disk directory
        QemuSettings settings = QemuSettings.getInstance();
        File defaultDir = new File(settings.getQemuManagerDisksPath());
        if (defaultDir.exists()) {
            dirChooser.setCurrentDirectory(defaultDir);
        }
        
        if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = dirChooser.getSelectedFile();
            scanDirectoryForDisks(selectedDir, true); // true = show scan messages
        }
    }
    
    private void scanDirectoryForDisks(File directory, boolean showMessages) {
        try {
            Set<String> knownDisks = new HashSet<>();
            for (QemuVm vm : virtualMachines) {
                if (vm.getDiskPath() != null) {
                    knownDisks.add(vm.getDiskPath());
                }
            }
            
            // Also check disks already in the table to avoid duplicates
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String path = (String) tableModel.getValueAt(i, 1);
                knownDisks.add(path);
            }
            
            String[] diskExtensions = {".qcow2", ".img", ".vmdk", ".vdi", ".vhd"};
            File[] files = directory.listFiles();
            int foundCount = 0;
            
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName().toLowerCase();
                        for (String ext : diskExtensions) {
                            if (fileName.endsWith(ext) && !knownDisks.contains(file.getAbsolutePath())) {
                                addDiskToTable(file, "Not Used");
                                foundCount++;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (showMessages && consolePanel != null) {
                consolePanel.appendMessage("Scanned " + directory.getAbsolutePath() + " - found " + foundCount + " new disk images");
            }
            
        } catch (Exception e) {
            if (consolePanel != null) {
                consolePanel.appendMessage("Error scanning directory: " + e.getMessage());
            }
            if (showMessages) {
                JOptionPane.showMessageDialog(this, 
                    "Error scanning directory: " + e.getMessage(),
                    "Scan Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cleanupUnusedDisks() {
        int result = JOptionPane.showConfirmDialog(this,
            "This will show you unused disk images for potential cleanup.\n" +
            "No files will be deleted automatically.\n" +
            "Continue?",
            "Cleanup Unused Disks", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // Implementation for cleanup dialog
            showUnusedDisksDialog();
        }
    }
    
    private void showUnusedDisksDialog() {
        // Find unused disks
        java.util.List<String> unusedDisks = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String usedBy = (String) tableModel.getValueAt(i, 4);
            if ("Not Used".equals(usedBy)) {
                unusedDisks.add((String) tableModel.getValueAt(i, 1)); // Path
            }
        }
        
        if (unusedDisks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No unused disk images found.", 
                "Cleanup", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String message = "Found " + unusedDisks.size() + " unused disk images:\n\n";
            for (String disk : unusedDisks) {
                message += "• " + disk + "\n";
            }
            message += "\nYou can manually delete these files if no longer needed.";
            
            JOptionPane.showMessageDialog(this, message, 
                "Unused Disk Images", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showDiskInfo(int row) {
        String fileName = (String) tableModel.getValueAt(row, 0);
        String filePath = (String) tableModel.getValueAt(row, 1);
        String format = (String) tableModel.getValueAt(row, 2);
        String size = (String) tableModel.getValueAt(row, 3);
        String usedBy = (String) tableModel.getValueAt(row, 4);
        
        File diskFile = new File(filePath);
        StringBuilder info = new StringBuilder();
        
        info.append("Disk Image Information\n");
        info.append("=====================\n\n");
        info.append("File Name: ").append(fileName).append("\n");
        info.append("Full Path: ").append(filePath).append("\n");
        info.append("Format: ").append(format).append("\n");
        info.append("Size: ").append(size).append("\n");
        info.append("Used By: ").append(usedBy).append("\n\n");
        
        if (diskFile.exists()) {
            info.append("File Details:\n");
            info.append("Last Modified: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(diskFile.lastModified()))).append("\n");
            info.append("Readable: ").append(diskFile.canRead() ? "Yes" : "No").append("\n");
            info.append("Writable: ").append(diskFile.canWrite() ? "Yes" : "No").append("\n");
        } else {
            info.append("⚠️ File does not exist or is not accessible\n");
        }
        
        JTextArea textArea = new JTextArea(info.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Disk Information - " + fileName, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteDisk(int row) {
        String fileName = (String) tableModel.getValueAt(row, 0);
        String filePath = (String) tableModel.getValueAt(row, 1);
        String usedBy = (String) tableModel.getValueAt(row, 4);
        
        if (!"Not Used".equals(usedBy)) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete disk image '" + fileName + "' because it is used by: " + usedBy,
                "Disk In Use", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete the disk image?\n\n" +
            "File: " + fileName + "\n" +
            "Path: " + filePath + "\n\n" +
            "⚠️ This action cannot be undone!",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            File diskFile = new File(filePath);
            if (diskFile.exists() && diskFile.delete()) {
                tableModel.removeRow(row);
                if (consolePanel != null) {
                    consolePanel.appendMessage("Deleted disk image: " + filePath);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete the disk image file.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void openDiskLocation(int row) {
        String filePath = (String) tableModel.getValueAt(row, 1);
        File diskFile = new File(filePath);
        
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (diskFile.exists()) {
                    desktop.open(diskFile.getParentFile());
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Disk file does not exist: " + filePath,
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Desktop operations not supported on this system.",
                    "Not Supported", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to open file location: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Button renderer for actions column
    class DiskButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton infoButton, deleteButton, openButton;
        
        public DiskButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Increased spacing
            
            infoButton = new JButton("Info");
            deleteButton = new JButton("Delete");
            openButton = new JButton("Open");
            
            // Much larger button sizes to ensure all text is clearly visible
            infoButton.setPreferredSize(new Dimension(90, 50)); // Was 75, 42
            deleteButton.setPreferredSize(new Dimension(100, 50)); // Was 85, 42  
            openButton.setPreferredSize(new Dimension(90, 50)); // Was 75, 42
            
            // Larger font for excellent readability
            Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 14); // Increased from 13 to 14
            infoButton.setFont(buttonFont);
            deleteButton.setFont(buttonFont);
            openButton.setFont(buttonFont);
            
            infoButton.setBackground(new Color(70, 130, 180));
            infoButton.setForeground(Color.WHITE);
            infoButton.setToolTipText("Show disk information");
            
            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setToolTipText("Delete disk image");
            
            openButton.setBackground(new Color(60, 179, 113));
            openButton.setForeground(Color.WHITE);
            openButton.setToolTipText("Open file location");
            
            add(infoButton);
            add(deleteButton);
            add(openButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (row < tableModel.getRowCount()) {
                String usedBy = (String) tableModel.getValueAt(row, 4);
                deleteButton.setEnabled("Not Used".equals(usedBy));
            }
            
            return this;
        }
    }
    
    // Button editor for actions column
    class DiskButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton infoButton, deleteButton, openButton;
        private int currentRow;
        
        public DiskButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5)); // Increased spacing
            
            infoButton = new JButton("Info");
            deleteButton = new JButton("Delete");
            openButton = new JButton("Open");
            
            // Much larger button sizes to match renderer and ensure excellent visibility
            infoButton.setPreferredSize(new Dimension(90, 50)); // Was 75, 42
            deleteButton.setPreferredSize(new Dimension(100, 50)); // Was 85, 42
            openButton.setPreferredSize(new Dimension(90, 50)); // Was 75, 42
            
            // Larger font for excellent readability to match renderer
            Font buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 14); // Increased from 13 to 14
            infoButton.setFont(buttonFont);
            deleteButton.setFont(buttonFont);
            openButton.setFont(buttonFont);
            
            infoButton.setBackground(new Color(70, 130, 180));
            infoButton.setForeground(Color.WHITE);
            infoButton.setToolTipText("Show disk information");
            
            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setToolTipText("Delete disk image");
            
            openButton.setBackground(new Color(60, 179, 113));
            openButton.setForeground(Color.WHITE);
            openButton.setToolTipText("Open file location");
            
            infoButton.addActionListener(e -> {
                showDiskInfo(currentRow);
                fireEditingStopped();
            });
            
            deleteButton.addActionListener(e -> {
                deleteDisk(currentRow);
                fireEditingStopped();
            });
            
            openButton.addActionListener(e -> {
                openDiskLocation(currentRow);
                fireEditingStopped();
            });
            
            panel.add(infoButton);
            panel.add(deleteButton);
            panel.add(openButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            if (row < tableModel.getRowCount()) {
                String usedBy = (String) tableModel.getValueAt(row, 4);
                deleteButton.setEnabled("Not Used".equals(usedBy));
            }
            
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
    }
}