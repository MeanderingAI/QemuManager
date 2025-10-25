package com.qemumanager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Dialog for creating new disk images
 */
public class QemuDiskCreationDialog extends JDialog {
    private boolean confirmed = false;
    private String diskPath;
    private QemuConsolePanel consolePanel;
    
    // Form components
    private JTextField pathField;
    private JSpinner sizeSpinner;
    private JComboBox<String> unitCombo;
    private JComboBox<String> formatCombo;
    
    public QemuDiskCreationDialog(JDialog parent) {
        super(parent, "Create New Disk Image", true);
        initializeUI();
    }
    
    public QemuDiskCreationDialog(JDialog parent, QemuConsolePanel consolePanel) {
        super(parent, "Create New Disk Image", true);
        this.consolePanel = consolePanel;
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(450, 300);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Title
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JLabel titleLabel = new JLabel("Create New Virtual Disk Image");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(titleLabel, gbc);
        
        row++;
        gbc.gridwidth = 1;
        
        // File path
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Save to:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel pathPanel = new JPanel(new BorderLayout());
        pathField = new JTextField();
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseSaveLocation());
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browseButton, BorderLayout.EAST);
        panel.add(pathPanel, gbc);
        
        row++;
        
        // Size
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Size:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.7;
        sizeSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        panel.add(sizeSpinner, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.3;
        unitCombo = new JComboBox<>(new String[]{"GB", "MB", "TB"});
        panel.add(unitCombo, gbc);
        
        row++;
        
        // Format
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Format:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formatCombo = new JComboBox<>(new String[]{"qcow2", "raw", "vmdk", "vdi"});
        formatCombo.addActionListener(e -> updateDefaultPathExtension());
        panel.add(formatCombo, gbc);
        
        // Set default path after all components are created
        setDefaultPath();
        
        row++;
        gbc.gridwidth = 1;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea descriptionArea = new JTextArea(
            "This will create a new virtual disk image file that can be used as storage for a virtual machine.\n\n" +
            "Formats:\n" +
            "• qcow2: QEMU's native format, supports compression and snapshots\n" +
            "• raw: Simple format, good performance but larger file size\n" +
            "• vmdk: VMware format, compatible with VMware products\n" +
            "• vdi: VirtualBox format"
        );
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(descriptionArea.getFont().deriveFont(11f));
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        panel.add(descriptionArea, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> createDisk());
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(createButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void setDefaultPath() {
        // Create default directory structure
        QemuSettings settings = QemuSettings.getInstance();
        settings.ensureDirectoriesExist(); // Make sure directories exist
        File defaultDir = new File(settings.getQemuManagerDisksPath());
        
        // Get the selected format for the default filename
        String format = (String) formatCombo.getSelectedItem();
        if (format == null) {
            format = "qcow2"; // fallback
        }
        
        // Create default filename with timestamp to avoid conflicts
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String defaultFilename = "disk_" + timestamp + "." + format;
        
        // Set the default path
        File defaultFile = new File(defaultDir, defaultFilename);
        pathField.setText(defaultFile.getPath());
        
        // Create the directory if it doesn't exist (without showing error if it fails)
        try {
            defaultDir.mkdirs();
        } catch (Exception e) {
            // Silently ignore directory creation errors
            // User can choose a different path if needed
        }
    }
    
    private void updateDefaultPathExtension() {
        String currentPath = pathField.getText();
        if (currentPath != null && !currentPath.trim().isEmpty()) {
            // Only update if it looks like a default path (contains .QemuManager/disks)
            QemuSettings settings = QemuSettings.getInstance();
            String disksPath = settings.getQemuManagerDisksPath();
            if (currentPath.contains(disksPath) || currentPath.contains(".QemuManager")) {
                String newFormat = (String) formatCombo.getSelectedItem();
                if (newFormat != null) {
                    // Replace the extension in the current path
                    String pathWithoutExt = currentPath.replaceAll("\\.[^.]*$", "");
                    pathField.setText(pathWithoutExt + "." + newFormat);
                }
            }
        }
    }
    
    private void browseSaveLocation() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Disk Image As");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Set initial directory to configured disks directory
        QemuSettings settings = QemuSettings.getInstance();
        settings.ensureDirectoriesExist(); // Make sure directories exist
        File defaultDir = new File(settings.getQemuManagerDisksPath());
        if (defaultDir.exists() || defaultDir.mkdirs()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }
        
        String format = (String) formatCombo.getSelectedItem();
        fileChooser.setSelectedFile(new File("disk." + format));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            
            // Add extension if not present
            if (!path.toLowerCase().endsWith("." + format)) {
                path += "." + format;
            }
            
            pathField.setText(path);
        }
    }
    
    private void createDisk() {
        if (validateInput()) {
            if (executeQemuImgCreate()) {
                confirmed = true;
                diskPath = pathField.getText().trim();
                dispose();
            }
        }
    }
    
    private boolean validateInput() {
        if (pathField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify a file path.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        File file = new File(pathField.getText().trim());
        if (file.exists()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "File already exists. Overwrite?",
                "File Exists", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        // Check if parent directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Parent directory does not exist. Create it?",
                "Create Directory", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if (!parentDir.mkdirs()) {
                    JOptionPane.showMessageDialog(this, "Failed to create parent directory.", 
                        "Directory Creation Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean executeQemuImgCreate() {
        try {
            // Build qemu-img command
            String qemuImgPath = getQemuImgPath();
            String format = (String) formatCombo.getSelectedItem();
            String sizeUnit = (String) unitCombo.getSelectedItem();
            
            // Convert size units to qemu-img format (single letters)
            String qemuSizeUnit;
            switch (sizeUnit) {
                case "GB": qemuSizeUnit = "G"; break;
                case "MB": qemuSizeUnit = "M"; break;
                case "TB": qemuSizeUnit = "T"; break;
                default: qemuSizeUnit = "G"; break; // fallback to GB
            }
            
            String size = sizeSpinner.getValue() + qemuSizeUnit;
            String filePath = pathField.getText().trim();
            
            // Test if qemu-img is accessible
            try {
                ProcessBuilder testPb = new ProcessBuilder(qemuImgPath, "--version");
                Process testProcess = testPb.start();
                int testResult = testProcess.waitFor();
                if (testResult != 0) {
                    JOptionPane.showMessageDialog(this,
                        "qemu-img not found or not executable at: " + qemuImgPath + "\n" +
                        "Please install QEMU or check your installation.",
                        "qemu-img Not Found", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to execute qemu-img at: " + qemuImgPath + "\n" +
                    "Error: " + e.getMessage() + "\n\n" +
                    "Please install QEMU or check your installation.",
                    "qemu-img Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            String[] command = {
                qemuImgPath,
                "create",
                "-f", format,
                filePath,
                size
            };
            
            // Show progress dialog
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JDialog progressDialog = new JDialog(this, "Creating Disk Image", true);
            progressDialog.add(new JLabel("Creating disk image, please wait..."), BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                private String errorMessage = "";
                
                @Override
                protected Boolean doInBackground() throws Exception {
                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.redirectErrorStream(true); // Combine stdout and stderr
                    Process process = pb.start();
                    
                    // Capture output for error reporting
                    java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                    
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        errorMessage = "Command: " + String.join(" ", command) + "\n\n" +
                                     "Exit code: " + exitCode + "\n\n" +
                                     "Output:\n" + output.toString();
                        
                        // Also log to console if available
                        if (consolePanel != null) {
                            consolePanel.appendMessage("Disk creation failed:");
                            consolePanel.appendMessage("Command: " + String.join(" ", command));
                            consolePanel.appendMessage("Exit code: " + exitCode);
                            consolePanel.appendMessage("Output: " + output.toString());
                        }
                    }
                    
                    return exitCode == 0;
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(QemuDiskCreationDialog.this,
                                "Disk image created successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            // Show detailed error message
                            JTextArea textArea = new JTextArea(errorMessage);
                            textArea.setEditable(false);
                            textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
                            JScrollPane scrollPane = new JScrollPane(textArea);
                            scrollPane.setPreferredSize(new java.awt.Dimension(600, 300));
                            
                            JOptionPane.showMessageDialog(QemuDiskCreationDialog.this,
                                scrollPane,
                                "Disk Creation Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(QemuDiskCreationDialog.this,
                            "Error creating disk image: " + e.getMessage(),
                            "Creation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
            
            return true; // We'll handle the actual result in the SwingWorker
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error creating disk image: " + e.getMessage(),
                "Creation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    private String getQemuImgPath() {
        // Try to find qemu-img in common locations
        String[] possiblePaths = {
            "qemu-img",
            "/usr/bin/qemu-img",
            "/usr/local/bin/qemu-img",
            "qemu-img.exe"
        };
        
        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "--version");
                Process process = pb.start();
                if (process.waitFor() == 0) {
                    return path;
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }
        
        // Default to qemu-img in PATH
        return "qemu-img";
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getDiskPath() {
        return diskPath;
    }
}