package com.qemumanager;

import javax.swing.*;
import java.awt.*;

/**
 * Settings dialog for QEMU Manager
 */
public class QemuSettingsDialog extends JDialog {
    private QemuSettings settings;
    
    // Form components
    private JTextField qemuPathField;
    private JTextField vncViewerPathField;
    private JTextField basePathField;
    private JTextField disksPathField;
    private JTextField vmsPathField;
    private JSpinner defaultMemorySpinner;
    private JSpinner defaultCoresSpinner;
    private JComboBox<String> defaultArchCombo;
    private JCheckBox autoSaveCheckBox;
    
    public QemuSettingsDialog(JFrame parent) {
        super(parent, "QEMU Manager Settings", true);
        this.settings = QemuSettings.getInstance();
        initializeUI();
        loadCurrentSettings();
    }
    
    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Paths", createPathsPanel());
        tabbedPane.addTab("Defaults", createDefaultsPanel());
        tabbedPane.addTab("General", createGeneralPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createPathsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // QEMU Path
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("QEMU Executable:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel qemuPanel = new JPanel(new BorderLayout());
        qemuPathField = new JTextField();
        JButton qemuBrowseButton = new JButton("Browse");
        qemuBrowseButton.addActionListener(e -> browseQemuPath());
        qemuPanel.add(qemuPathField, BorderLayout.CENTER);
        qemuPanel.add(qemuBrowseButton, BorderLayout.EAST);
        panel.add(qemuPanel, gbc);
        
        row++;
        
        // VNC Viewer Path
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("VNC Viewer:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel vncPanel = new JPanel(new BorderLayout());
        vncViewerPathField = new JTextField();
        JButton vncBrowseButton = new JButton("Browse");
        vncBrowseButton.addActionListener(e -> browseVncViewerPath());
        vncPanel.add(vncViewerPathField, BorderLayout.CENTER);
        vncPanel.add(vncBrowseButton, BorderLayout.EAST);
        panel.add(vncPanel, gbc);
        
        row++;
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        
        row++;
        
        // QemuManager Base Path
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("QemuManager Base Path:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel basePathPanel = new JPanel(new BorderLayout());
        basePathField = new JTextField();
        JButton basePathBrowseButton = new JButton("Browse");
        basePathBrowseButton.addActionListener(e -> browseBasePath());
        basePathPanel.add(basePathField, BorderLayout.CENTER);
        basePathPanel.add(basePathBrowseButton, BorderLayout.EAST);
        panel.add(basePathPanel, gbc);
        
        row++;
        
        // Disks Path
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Disk Images Path:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel disksPathPanel = new JPanel(new BorderLayout());
        disksPathField = new JTextField();
        JButton disksPathBrowseButton = new JButton("Browse");
        disksPathBrowseButton.addActionListener(e -> browseDisksPath());
        disksPathPanel.add(disksPathField, BorderLayout.CENTER);
        disksPathPanel.add(disksPathBrowseButton, BorderLayout.EAST);
        panel.add(disksPathPanel, gbc);
        
        row++;
        
        // VMs Path
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("VMs Data Path:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel vmsPathPanel = new JPanel(new BorderLayout());
        vmsPathField = new JTextField();
        JButton vmsPathBrowseButton = new JButton("Browse");
        vmsPathBrowseButton.addActionListener(e -> browseVmsPath());
        vmsPathPanel.add(vmsPathField, BorderLayout.CENTER);
        vmsPathPanel.add(vmsPathBrowseButton, BorderLayout.EAST);
        panel.add(vmsPathPanel, gbc);
        
        row++;
        
        // Help text
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea pathHelpText = new JTextArea(
            "Path Configuration:\n\n" +
            "QEMU Executable: Path to qemu-system-x86_64 or equivalent for your architecture.\n" +
            "Common locations:\n" +
            "• Linux: /usr/bin/qemu-system-x86_64\n" +
            "• macOS: /usr/local/bin/qemu-system-x86_64\n" +
            "• Windows: C:\\Program Files\\qemu\\qemu-system-x86_64.exe\n\n" +
            "VNC Viewer: Optional VNC client for connecting to VM displays.\n" +
            "Examples: vncviewer, gvncviewer, or TightVNC viewer.\n\n" +
            "QemuManager Paths: Configure where QemuManager stores its data.\n" +
            "• Base Path: Main directory for QemuManager data (default: ~/.QemuManager)\n" +
            "• Disk Images Path: Directory for disk image files\n" +
            "• VMs Data Path: Directory for VM configuration and state files"
        );
        pathHelpText.setEditable(false);
        pathHelpText.setOpaque(false);
        pathHelpText.setFont(pathHelpText.getFont().deriveFont(11f));
        pathHelpText.setWrapStyleWord(true);
        pathHelpText.setLineWrap(true);
        panel.add(pathHelpText, gbc);
        
        return panel;
    }
    
    private JPanel createDefaultsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Default Memory
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Default Memory (MB):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        defaultMemorySpinner = new JSpinner(new SpinnerNumberModel(1024, 128, 32768, 128));
        panel.add(defaultMemorySpinner, gbc);
        
        row++;
        
        // Default CPU Cores
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Default CPU Cores:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        defaultCoresSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));
        panel.add(defaultCoresSpinner, gbc);
        
        row++;
        
        // Default Architecture
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Default Architecture:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        defaultArchCombo = new JComboBox<>(new String[]{"x86_64", "i386", "aarch64", "arm", "mips", "ppc"});
        panel.add(defaultArchCombo, gbc);
        
        row++;
        
        // Help text
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea defaultsHelpText = new JTextArea(
            "Default VM Settings:\n\n" +
            "These values will be used as defaults when creating new virtual machines. " +
            "You can always override these settings for individual VMs.\n\n" +
            "Memory: Amount of RAM allocated to new VMs (in megabytes).\n" +
            "CPU Cores: Number of virtual CPU cores for new VMs.\n" +
            "Architecture: Default target architecture for new VMs."
        );
        defaultsHelpText.setEditable(false);
        defaultsHelpText.setOpaque(false);
        defaultsHelpText.setFont(defaultsHelpText.getFont().deriveFont(11f));
        defaultsHelpText.setWrapStyleWord(true);
        defaultsHelpText.setLineWrap(true);
        panel.add(defaultsHelpText, gbc);
        
        return panel;
    }
    
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Auto-save settings
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Auto-save settings:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        autoSaveCheckBox = new JCheckBox("Automatically save settings when changed");
        panel.add(autoSaveCheckBox, gbc);
        
        row++;
        
        // Settings file location
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        String configPath = System.getProperty("user.home") + "/.qemumanager.properties";
        JLabel configPathLabel = new JLabel("Settings file: " + configPath);
        configPathLabel.setFont(configPathLabel.getFont().deriveFont(Font.ITALIC, 11f));
        panel.add(configPathLabel, gbc);
        
        row++;
        
        // Buttons for manual save/reset
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton saveNowButton = new JButton("Save Settings Now");
        JButton resetButton = new JButton("Reset to Defaults");
        
        saveNowButton.addActionListener(e -> {
            applySettings();
            settings.saveSettings();
            JOptionPane.showMessageDialog(this, "Settings saved successfully.", 
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        });
        
        resetButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "Reset all settings to defaults? This cannot be undone.",
                "Reset Settings", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                resetToDefaults();
            }
        });
        
        actionPanel.add(saveNowButton);
        actionPanel.add(resetButton);
        panel.add(actionPanel, gbc);
        
        row++;
        
        // Help text
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea generalHelpText = new JTextArea(
            "General Settings:\n\n" +
            "Auto-save: When enabled, settings are automatically saved whenever you make changes. " +
            "When disabled, you must manually save settings using the 'Save Settings Now' button.\n\n" +
            "The settings file is stored in your home directory and contains all configuration options."
        );
        generalHelpText.setEditable(false);
        generalHelpText.setOpaque(false);
        generalHelpText.setFont(generalHelpText.getFont().deriveFont(11f));
        generalHelpText.setWrapStyleWord(true);
        generalHelpText.setLineWrap(true);
        panel.add(generalHelpText, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton applyButton = new JButton("Apply");
        
        okButton.addActionListener(e -> {
            applySettings();
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        applyButton.addActionListener(e -> {
            applySettings();
            JOptionPane.showMessageDialog(this, "Settings applied successfully.", 
                "Settings Applied", JOptionPane.INFORMATION_MESSAGE);
        });
        
        panel.add(okButton);
        panel.add(cancelButton);
        panel.add(applyButton);
        
        return panel;
    }
    
    private void loadCurrentSettings() {
        qemuPathField.setText(settings.getQemuPath());
        vncViewerPathField.setText(settings.getVncViewerPath());
        basePathField.setText(settings.getQemuManagerBasePath());
        disksPathField.setText(settings.getQemuManagerDisksPath());
        vmsPathField.setText(settings.getQemuManagerVmsPath());
        defaultMemorySpinner.setValue(settings.getDefaultMemory());
        defaultCoresSpinner.setValue(settings.getDefaultCores());
        defaultArchCombo.setSelectedItem(settings.getDefaultArchitecture());
        autoSaveCheckBox.setSelected(settings.isAutoSaveEnabled());
    }
    
    private void applySettings() {
        settings.setQemuPath(qemuPathField.getText().trim());
        settings.setVncViewerPath(vncViewerPathField.getText().trim());
        settings.setQemuManagerBasePath(basePathField.getText().trim());
        settings.setQemuManagerDisksPath(disksPathField.getText().trim());
        settings.setQemuManagerVmsPath(vmsPathField.getText().trim());
        settings.setDefaultMemory((Integer) defaultMemorySpinner.getValue());
        settings.setDefaultCores((Integer) defaultCoresSpinner.getValue());
        settings.setDefaultArchitecture((String) defaultArchCombo.getSelectedItem());
        settings.setAutoSaveEnabled(autoSaveCheckBox.isSelected());
        
        // Ensure the new directories exist
        settings.ensureDirectoriesExist();
    }
    
    private void resetToDefaults() {
        qemuPathField.setText("/usr/bin/qemu-system-x86_64");
        vncViewerPathField.setText("");
        String userHome = System.getProperty("user.home");
        basePathField.setText(userHome + "/.QemuManager");
        disksPathField.setText(userHome + "/.QemuManager/disks");
        vmsPathField.setText(userHome + "/.QemuManager/vms");
        defaultMemorySpinner.setValue(1024);
        defaultCoresSpinner.setValue(1);
        defaultArchCombo.setSelectedItem("x86_64");
        autoSaveCheckBox.setSelected(true);
    }
    
    private void browseQemuPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select QEMU Executable");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            qemuPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseVncViewerPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select VNC Viewer Executable");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            vncViewerPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseBasePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select QemuManager Base Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            basePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseDisksPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Disk Images Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            disksPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseVmsPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select VMs Data Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            vmsPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
}