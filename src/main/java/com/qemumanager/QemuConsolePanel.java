package com.qemumanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Panel for displaying console output and logs
 */
public class QemuConsolePanel extends JPanel {
    private JTextArea consoleTextArea;
    private JScrollPane scrollPane;
    private JButton clearButton;
    private JButton saveButton;
    
    public QemuConsolePanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Console text area
        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        consoleTextArea.setBackground(Color.BLACK);
        consoleTextArea.setForeground(Color.GREEN);
        consoleTextArea.setCaretColor(Color.GREEN);
        
        scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        clearButton = new JButton("Clear");
        saveButton = new JButton("Save Log");
        
        clearButton.addActionListener(e -> clearConsole());
        saveButton.addActionListener(e -> saveLog());
        
        controlPanel.add(clearButton);
        controlPanel.add(saveButton);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Welcome message
        appendMessage("QEMU Manager Console initialized");
        appendMessage("==================================");
    }
    
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            consoleTextArea.append("[" + timestamp + "] " + message + "\n");
            
            // Auto-scroll to bottom
            consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        });
    }
    
    public void monitorProcess(Process process, String vmName) {
        // Monitor process output in a separate thread
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    appendMessage("[" + vmName + "] " + line);
                }
            } catch (IOException e) {
                appendMessage("[" + vmName + "] Error reading process output: " + e.getMessage());
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
        
        // Monitor process termination
        Thread monitorThread = new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                appendMessage("[" + vmName + "] Process terminated with exit code: " + exitCode);
            } catch (InterruptedException e) {
                appendMessage("[" + vmName + "] Process monitoring interrupted");
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    private void clearConsole() {
        consoleTextArea.setText("");
        appendMessage("Console cleared");
    }
    
    private void saveLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Console Log");
        fileChooser.setSelectedFile(new java.io.File("qemu-console-" + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".log"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile());
                writer.write(consoleTextArea.getText());
                writer.close();
                appendMessage("Log saved to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (IOException e) {
                appendMessage("Failed to save log: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Failed to save log: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void setConsoleFont(Font font) {
        consoleTextArea.setFont(font);
    }
    
    public void setConsoleColors(Color background, Color foreground) {
        consoleTextArea.setBackground(background);
        consoleTextArea.setForeground(foreground);
        consoleTextArea.setCaretColor(foreground);
    }
}