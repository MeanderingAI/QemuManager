package com.qemumanager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Main application class for QEMU Manager
 */
public class QemuManagerApp extends JFrame {
    private static final String APP_TITLE = "QEMU Manager";
    private static final String APP_VERSION = "1.0.0";
    
    private QemuVmPanel vmPanel;
    private QemuDiskPanel diskPanel;
    private QemuConsolePanel consolePanel;
    
    public QemuManagerApp() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle(APP_TITLE + " v" + APP_VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle close manually
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        // Add window closing listener to save state
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                QemuManagerApp.this.saveApplicationState();
                System.exit(0);
            }
        });
        
        // Set look and feel - removing problematic call for now
        
        // Create menu bar
        createMenuBar();
        
        // Create main layout
        createMainLayout();
        
        // Set icon
        setIconImage(createAppIcon());
        
        // Link console panel to VM panel
        vmPanel.setConsolePanel(consolePanel);
        
        // Link console panel and VM list to disk panel
        diskPanel.setConsolePanel(consolePanel);
        
        // Update disk panel when VMs change - simplified for now
        refreshDiskManagement();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newVmItem = new JMenuItem("New Virtual Machine");
        JMenuItem newDiskItem = new JMenuItem("New Disk Image");
        JMenuItem importVmItem = new JMenuItem("Import VM");
        JMenuItem saveStateItem = new JMenuItem("Save VM State");
        JMenuItem loadStateItem = new JMenuItem("Reload VM State");
        JMenuItem clearStateItem = new JMenuItem("Clear All VMs");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newVmItem.addActionListener(e -> showNewVmDialog());
        newDiskItem.addActionListener(e -> showNewDiskDialog());
        importVmItem.addActionListener(e -> showImportVmDialog());
        saveStateItem.addActionListener(e -> saveVmState());
        loadStateItem.addActionListener(e -> reloadVmState());
        clearStateItem.addActionListener(e -> clearAllVMs());
        exitItem.addActionListener(e -> {
            saveApplicationState();
            System.exit(0);
        });
        
        fileMenu.add(newVmItem);
        fileMenu.add(newDiskItem);
        fileMenu.add(importVmItem);
        fileMenu.addSeparator();
        fileMenu.add(saveStateItem);
        fileMenu.add(loadStateItem);
        fileMenu.add(clearStateItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Tools menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem settingsItem = new JMenuItem("Settings");
        JMenuItem qemuPathItem = new JMenuItem("Configure QEMU Path");
        
        settingsItem.addActionListener(e -> showSettingsDialog());
        qemuPathItem.addActionListener(e -> showQemuPathDialog());
        
        toolsMenu.add(settingsItem);
        toolsMenu.add(qemuPathItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createMainLayout() {
        setLayout(new BorderLayout());
        
        // Create split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(500);
        mainSplitPane.setResizeWeight(0.7);
        
        // Create tabbed pane for VM and Disk management
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // VM management panel
        vmPanel = new QemuVmPanel();
        JScrollPane vmScrollPane = new JScrollPane(vmPanel);
        tabbedPane.addTab("Virtual Machines", vmScrollPane);
        
        // Disk management panel
        diskPanel = new QemuDiskPanel();
        JScrollPane diskScrollPane = new JScrollPane(diskPanel);
        tabbedPane.addTab("Disk Images", diskScrollPane);
        
        // Console panel (bottom)
        consolePanel = new QemuConsolePanel();
        JScrollPane consoleScrollPane = new JScrollPane(consolePanel);
        consoleScrollPane.setBorder(BorderFactory.createTitledBorder("Console Output"));
        
        mainSplitPane.setTopComponent(tabbedPane);
        mainSplitPane.setBottomComponent(consoleScrollPane);
        
        add(mainSplitPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private Image createAppIcon() {
        // Create a simple icon for the application
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple computer icon
        g2d.setColor(Color.BLUE);
        g2d.fillRoundRect(4, 8, 24, 16, 4, 4);
        g2d.setColor(Color.BLACK);
        g2d.fillRoundRect(6, 10, 20, 12, 2, 2);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(14, 24, 4, 2);
        g2d.fillRect(10, 26, 12, 2);
        
        g2d.dispose();
        return icon;
    }
    
    private void showNewVmDialog() {
        QemuVmDialog dialog = new QemuVmDialog(this, "Create New Virtual Machine", true);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            QemuVm vm = dialog.getVirtualMachine();
            vmPanel.addVirtualMachine(vm);
            refreshDiskManagement();
        }
    }
    
    private void showNewDiskDialog() {
        // Create a temporary dialog parent for the disk creation dialog
        JDialog tempDialog = new JDialog(this, true);
        tempDialog.setVisible(false);
        
        QemuDiskCreationDialog dialog = new QemuDiskCreationDialog(tempDialog, consolePanel);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            refreshDiskManagement();
            consolePanel.appendMessage("New disk created: " + dialog.getDiskPath());
        }
    }
    
    private void showImportVmDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Virtual Machine");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "QEMU VM files (*.qcow2, *.img)", "qcow2", "img"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Handle VM import
            String vmPath = fileChooser.getSelectedFile().getAbsolutePath();
            consolePanel.appendMessage("Importing VM from: " + vmPath);
        }
    }
    
    private void showSettingsDialog() {
        QemuSettingsDialog dialog = new QemuSettingsDialog(this);
        dialog.setVisible(true);
    }
    
    private void showQemuPathDialog() {
        String currentPath = QemuSettings.getInstance().getQemuPath();
        String newPath = JOptionPane.showInputDialog(this, 
            "Enter QEMU executable path:", 
            currentPath);
        
        if (newPath != null && !newPath.trim().isEmpty()) {
            QemuSettings.getInstance().setQemuPath(newPath.trim());
            consolePanel.appendMessage("QEMU path updated to: " + newPath.trim());
        }
    }
    
    private void showAboutDialog() {
        String stateFilePath = QemuVmStateManager.getInstance().getStateFilePath();
        String settingsFilePath = System.getProperty("user.home") + "/.qemumanager.properties";
        
        String message = String.format(
            "%s v%s\n\n" +
            "A Java-based GUI for managing QEMU virtual machines.\n\n" +
            "Features:\n" +
            "• Create and manage virtual machines\n" +
            "• Configure VM settings\n" +
            "• Monitor VM status\n" +
            "• Console output viewing\n" +
            "• Persistent VM state storage\n\n" +
            "Configuration Files:\n" +
            "• Settings: %s\n" +
            "• VM State: %s\n\n" +
            "Author: QEMU Manager Team\n" +
            "License: MIT",
            APP_TITLE, APP_VERSION, settingsFilePath, stateFilePath
        );
        
        JOptionPane.showMessageDialog(this, message, "About " + APP_TITLE, 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Saves the application state before closing
     */
    private void saveApplicationState() {
        try {
            // Save VM state through the VM panel
            if (vmPanel != null) {
                // Trigger save of current VM state
                vmPanel.saveVmState();
            }
            
            // Save application settings
            QemuSettings.getInstance().saveSettings();
            
            if (consolePanel != null) {
                consolePanel.appendMessage("Application state saved successfully");
            }
        } catch (Exception e) {
            if (consolePanel != null) {
                consolePanel.appendMessage("Failed to save application state: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }
    
    /**
     * Manually save VM state
     */
    private void saveVmState() {
        if (vmPanel != null) {
            vmPanel.saveVmState();
            consolePanel.appendMessage("VM state saved manually");
        }
    }
    
    /**
     * Reload VM state from file
     */
    private void reloadVmState() {
        if (vmPanel != null) {
            // Clear current VMs and reload from file
            vmPanel.clearVMs();
            vmPanel.loadVmState();
            consolePanel.appendMessage("VM state reloaded from file");
            refreshDiskManagement();
        }
    }
    
    /**
     * Clear all VMs after confirmation
     */
    private void clearAllVMs() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all virtual machines?\nThis will also clear the saved state file.",
            "Clear All VMs", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            if (vmPanel != null) {
                vmPanel.clearVMs();
                QemuVmStateManager.getInstance().clearVmState();
                consolePanel.appendMessage("All VMs cleared and state file deleted");
                refreshDiskManagement();
            }
        }
    }
    
    /**
     * Refresh disk management panel with current VM list
     */
    private void refreshDiskManagement() {
        if (diskPanel != null && vmPanel != null) {
            diskPanel.setVirtualMachines(vmPanel.getVirtualMachines());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Ensure QemuManager directories exist before starting the application
            QemuSettings.getInstance().ensureDirectoriesExist();
            new QemuManagerApp().setVisible(true);
        });
    }
}