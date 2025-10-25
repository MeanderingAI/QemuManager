package com.qemumanager;

import java.io.*;
import java.util.Properties;

/**
 * Manages application settings and configuration
 */
public class QemuSettings {
    private static QemuSettings instance;
    private Properties properties;
    private final String CONFIG_FILE = System.getProperty("user.home") + "/.qemumanager.properties";
    
    private QemuSettings() {
        properties = new Properties();
        loadSettings();
    }
    
    public static QemuSettings getInstance() {
        if (instance == null) {
            instance = new QemuSettings();
        }
        return instance;
    }
    
    private void loadSettings() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                }
            } else {
                setDefaultSettings();
            }
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultSettings();
        }
    }
    
    private void setDefaultSettings() {
        // Set default QEMU path based on common installation locations
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            properties.setProperty("qemu.path", "qemu-system-x86_64.exe");
        } else if (os.contains("mac")) {
            properties.setProperty("qemu.path", "/usr/local/bin/qemu-system-x86_64");
        } else {
            properties.setProperty("qemu.path", "/usr/bin/qemu-system-x86_64");
        }
        
        properties.setProperty("vm.default.memory", "1024");
        properties.setProperty("vm.default.cores", "1");
        properties.setProperty("vm.default.architecture", "x86_64");
        properties.setProperty("vnc.viewer.path", "");
        properties.setProperty("auto.save.settings", "true");
        
        // Set default QemuManager paths
        String userHome = System.getProperty("user.home");
        properties.setProperty("qemumanager.base.path", userHome + File.separator + ".QemuManager");
        properties.setProperty("qemumanager.disks.path", userHome + File.separator + ".QemuManager" + File.separator + "disks");
        properties.setProperty("qemumanager.vms.path", userHome + File.separator + ".QemuManager" + File.separator + "vms");
    }
    
    public void saveSettings() {
        try {
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                properties.store(fos, "QEMU Manager Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getQemuPath() {
        return properties.getProperty("qemu.path", "/usr/bin/qemu-system-x86_64");
    }
    
    public void setQemuPath(String path) {
        properties.setProperty("qemu.path", path);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public int getDefaultMemory() {
        return Integer.parseInt(properties.getProperty("vm.default.memory", "1024"));
    }
    
    public void setDefaultMemory(int memory) {
        properties.setProperty("vm.default.memory", String.valueOf(memory));
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public int getDefaultCores() {
        return Integer.parseInt(properties.getProperty("vm.default.cores", "1"));
    }
    
    public void setDefaultCores(int cores) {
        properties.setProperty("vm.default.cores", String.valueOf(cores));
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public String getDefaultArchitecture() {
        return properties.getProperty("vm.default.architecture", "x86_64");
    }
    
    public void setDefaultArchitecture(String architecture) {
        properties.setProperty("vm.default.architecture", architecture);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public String getVncViewerPath() {
        return properties.getProperty("vnc.viewer.path", "");
    }
    
    public void setVncViewerPath(String path) {
        properties.setProperty("vnc.viewer.path", path);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public boolean isAutoSaveEnabled() {
        return Boolean.parseBoolean(properties.getProperty("auto.save.settings", "true"));
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        properties.setProperty("auto.save.settings", String.valueOf(enabled));
        saveSettings(); // Always save this setting
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    // QemuManager Path Settings
    public String getQemuManagerBasePath() {
        String userHome = System.getProperty("user.home");
        return properties.getProperty("qemumanager.base.path", userHome + File.separator + ".QemuManager");
    }
    
    public void setQemuManagerBasePath(String path) {
        properties.setProperty("qemumanager.base.path", path);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public String getQemuManagerDisksPath() {
        String basePath = getQemuManagerBasePath();
        return properties.getProperty("qemumanager.disks.path", basePath + File.separator + "disks");
    }
    
    public void setQemuManagerDisksPath(String path) {
        properties.setProperty("qemumanager.disks.path", path);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    public String getQemuManagerVmsPath() {
        String basePath = getQemuManagerBasePath();
        return properties.getProperty("qemumanager.vms.path", basePath + File.separator + "vms");
    }
    
    public void setQemuManagerVmsPath(String path) {
        properties.setProperty("qemumanager.vms.path", path);
        if (isAutoSaveEnabled()) {
            saveSettings();
        }
    }
    
    // Utility method to ensure directories exist
    public void ensureDirectoriesExist() {
        createDirectoryIfNotExists(getQemuManagerBasePath());
        createDirectoryIfNotExists(getQemuManagerDisksPath());
        createDirectoryIfNotExists(getQemuManagerVmsPath());
    }
    
    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}