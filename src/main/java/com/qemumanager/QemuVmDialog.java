package com.qemumanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Dialog for creating and editing virtual machines
 */
public class QemuVmDialog extends JDialog {
    private QemuVm virtualMachine;
    private boolean confirmed = false;
    
    // Form components
    private JTextField nameField;
    private JTextField diskPathField;
    private JSpinner memorySpinner;
    private JSpinner cpuSpinner;
    private JComboBox<String> architectureCombo;
    private JComboBox<String> networkCombo;
    private JCheckBox kvmCheckBox;
    private JTextField cdromPathField;
    private JTextField bootOrderField;
    private JSpinner vncPortSpinner;
    
    public QemuVmDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        initializeUI();
    }
    
    private void initializeUI() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Form panel
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // VM Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("VM Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);
        
        row++;
        
        // Disk Path
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Disk Image:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel diskPanel = new JPanel(new BorderLayout());
        diskPathField = new JTextField();
        JButton diskBrowseButton = new JButton("Browse");
        diskBrowseButton.addActionListener(e -> browseDiskFile());
        diskPanel.add(diskPathField, BorderLayout.CENTER);
        diskPanel.add(diskBrowseButton, BorderLayout.EAST);
        panel.add(diskPanel, gbc);
        
        row++;
        
        // Memory
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Memory (MB):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        memorySpinner = new JSpinner(new SpinnerNumberModel(1024, 128, 32768, 128));
        panel.add(memorySpinner, gbc);
        
        row++;
        
        // CPU Cores
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("CPU Cores:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        cpuSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));
        panel.add(cpuSpinner, gbc);
        
        row++;
        
        // Architecture
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Architecture:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        architectureCombo = new JComboBox<>(new String[]{"x86_64", "i386", "aarch64", "arm", "mips", "ppc"});
        panel.add(architectureCombo, gbc);
        
        row++;
        
        // Network
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Network:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        networkCombo = new JComboBox<>(new String[]{"user", "tap", "bridge", "none"});
        panel.add(networkCombo, gbc);
        
        row++;
        
        // KVM
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Enable KVM:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        kvmCheckBox = new JCheckBox();
        kvmCheckBox.setSelected(true);
        panel.add(kvmCheckBox, gbc);
        
        row++;
        
        // CD-ROM
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("CD-ROM Image:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        
        JPanel cdromPanel = new JPanel(new BorderLayout());
        cdromPathField = new JTextField();
        JButton cdromBrowseButton = new JButton("Browse");
        cdromBrowseButton.addActionListener(e -> browseCdromFile());
        cdromPanel.add(cdromPathField, BorderLayout.CENTER);
        cdromPanel.add(cdromBrowseButton, BorderLayout.EAST);
        panel.add(cdromPanel, gbc);
        
        row++;
        
        // Boot Order
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("Boot Order:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        bootOrderField = new JTextField("dc");
        panel.add(bootOrderField, gbc);
        
        row++;
        
        // VNC Port
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(new JLabel("VNC Port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        vncPortSpinner = new JSpinner(new SpinnerNumberModel(5901, 5901, 5999, 1));
        panel.add(vncPortSpinner, gbc);
        
        row++;
        
        // Help text
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextArea helpText = new JTextArea(
            "Boot Order: 'a' or 'b' for floppy, 'c' for hard disk, 'd' for CD-ROM, 'n' for network.\n" +
            "Example: 'dc' means try CD-ROM first, then hard disk.\n\n" +
            "VNC Port: Port number for VNC remote display access (5901-5999)."
        );
        helpText.setEditable(false);
        helpText.setOpaque(false);
        helpText.setFont(helpText.getFont().deriveFont(Font.ITALIC, 11f));
        helpText.setWrapStyleWord(true);
        helpText.setLineWrap(true);
        panel.add(helpText, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton createDiskButton = new JButton("Create New Disk");
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        createDiskButton.addActionListener(e -> createNewDisk());
        okButton.addActionListener(e -> confirmDialog());
        cancelButton.addActionListener(e -> dispose());
        
        panel.add(createDiskButton);
        panel.add(okButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void browseDiskFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Disk Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Disk Images (*.qcow2, *.img, *.vmdk, *.vdi, *.vhd)", "qcow2", "img", "vmdk", "vdi", "vhd"));
        
        // Set default directory to configured disks directory
        QemuSettings settings = QemuSettings.getInstance();
        settings.ensureDirectoriesExist(); // Make sure directories exist
        File defaultDir = new File(settings.getQemuManagerDisksPath());
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            diskPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseCdromFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CD-ROM Image");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "ISO Images (*.iso)", "iso"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cdromPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void createNewDisk() {
        QemuDiskCreationDialog diskDialog = new QemuDiskCreationDialog(this);
        diskDialog.setVisible(true);
        
        if (diskDialog.isConfirmed()) {
            String diskPath = diskDialog.getDiskPath();
            if (diskPath != null) {
                diskPathField.setText(diskPath);
            }
        }
    }
    
    private void confirmDialog() {
        if (validateInput()) {
            updateVirtualMachine();
            confirmed = true;
            dispose();
        }
    }
    
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a VM name.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        
        String diskPath = diskPathField.getText().trim();
        if (!diskPath.isEmpty() && !new File(diskPath).exists()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Disk image file does not exist. Continue anyway?",
                "File Not Found", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        String cdromPath = cdromPathField.getText().trim();
        if (!cdromPath.isEmpty() && !new File(cdromPath).exists()) {
            int result = JOptionPane.showConfirmDialog(this, 
                "CD-ROM image file does not exist. Continue anyway?",
                "File Not Found", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        return true;
    }
    
    private void updateVirtualMachine() {
        if (virtualMachine == null) {
            virtualMachine = new QemuVm(nameField.getText().trim());
        } else {
            virtualMachine.setName(nameField.getText().trim());
        }
        
        virtualMachine.setDiskPath(diskPathField.getText().trim());
        virtualMachine.setMemoryMB((Integer) memorySpinner.getValue());
        virtualMachine.setCpuCores((Integer) cpuSpinner.getValue());
        virtualMachine.setArchitecture((String) architectureCombo.getSelectedItem());
        virtualMachine.setNetworkType((String) networkCombo.getSelectedItem());
        virtualMachine.setEnableKvm(kvmCheckBox.isSelected());
        virtualMachine.setCdromPath(cdromPathField.getText().trim());
        virtualMachine.setBootOrder(bootOrderField.getText().trim());
        virtualMachine.setVncPort((Integer) vncPortSpinner.getValue());
    }
    
    public void setVirtualMachine(QemuVm vm) {
        this.virtualMachine = vm;
        populateFields();
    }
    
    private void populateFields() {
        if (virtualMachine != null) {
            nameField.setText(virtualMachine.getName());
            diskPathField.setText(virtualMachine.getDiskPath() != null ? virtualMachine.getDiskPath() : "");
            memorySpinner.setValue(virtualMachine.getMemoryMB());
            cpuSpinner.setValue(virtualMachine.getCpuCores());
            architectureCombo.setSelectedItem(virtualMachine.getArchitecture());
            networkCombo.setSelectedItem(virtualMachine.getNetworkType());
            kvmCheckBox.setSelected(virtualMachine.isEnableKvm());
            cdromPathField.setText(virtualMachine.getCdromPath() != null ? virtualMachine.getCdromPath() : "");
            bootOrderField.setText(virtualMachine.getBootOrder());
            vncPortSpinner.setValue(virtualMachine.getVncPort());
        }
    }
    
    public QemuVm getVirtualMachine() {
        return virtualMachine;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}