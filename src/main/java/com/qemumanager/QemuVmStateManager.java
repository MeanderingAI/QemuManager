package com.qemumanager;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Manages persistence of virtual machine configurations
 */
public class QemuVmStateManager {
    private static QemuVmStateManager instance;
    
    private QemuVmStateManager() {
    }
    
    private String getVmStateFile() {
        QemuSettings settings = QemuSettings.getInstance();
        return settings.getQemuManagerVmsPath() + File.separator + "vms.txt";
    }
    
    public static QemuVmStateManager getInstance() {
        if (instance == null) {
            instance = new QemuVmStateManager();
        }
        return instance;
    }
    
    /**
     * Saves the list of virtual machines to the state file
     */
    public void saveVmState(List<QemuVm> virtualMachines) {
        QemuSettings settings = QemuSettings.getInstance();
        settings.ensureDirectoriesExist(); // Make sure directories exist
        String vmStateFile = getVmStateFile();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(vmStateFile))) {
            writer.write("# QEMU Manager VM State File");
            writer.newLine();
            writer.write("# Generated on: " + new Date());
            writer.newLine();
            writer.write("# Format: Each VM is separated by [VM_START] and [VM_END] markers");
            writer.newLine();
            writer.newLine();
            
            for (QemuVm vm : virtualMachines) {
                saveVmToFile(writer, vm);
                writer.newLine();
            }
            
            System.out.println("VM state saved to: " + vmStateFile);
        } catch (IOException e) {
            System.err.println("Failed to save VM state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the list of virtual machines from the state file
     */
    public List<QemuVm> loadVmState() {
        List<QemuVm> virtualMachines = new ArrayList<>();
        
        String vmStateFile = getVmStateFile();
        File stateFile = new File(vmStateFile);
        if (!stateFile.exists()) {
            System.out.println("No existing VM state file found. Starting with empty VM list.");
            return virtualMachines;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(vmStateFile))) {
            String line;
            StringBuilder vmData = new StringBuilder();
            boolean inVmBlock = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                
                if (line.equals("[VM_START]")) {
                    inVmBlock = true;
                    vmData = new StringBuilder();
                } else if (line.equals("[VM_END]")) {
                    if (inVmBlock) {
                        QemuVm vm = parseVmFromData(vmData.toString());
                        if (vm != null) {
                            virtualMachines.add(vm);
                        }
                    }
                    inVmBlock = false;
                } else if (inVmBlock) {
                    vmData.append(line).append("\n");
                }
            }
            
            System.out.println("Loaded " + virtualMachines.size() + " VMs from state file.");
            
        } catch (IOException e) {
            System.err.println("Failed to load VM state: " + e.getMessage());
            e.printStackTrace();
        }
        
        return virtualMachines;
    }
    
    /**
     * Saves a single VM configuration to the writer
     */
    private void saveVmToFile(BufferedWriter writer, QemuVm vm) throws IOException {
        writer.write("[VM_START]");
        writer.newLine();
        
        writer.write("name=" + escapeValue(vm.getName()));
        writer.newLine();
        
        writer.write("diskPath=" + escapeValue(vm.getDiskPath() != null ? vm.getDiskPath() : ""));
        writer.newLine();
        
        writer.write("memoryMB=" + vm.getMemoryMB());
        writer.newLine();
        
        writer.write("cpuCores=" + vm.getCpuCores());
        writer.newLine();
        
        writer.write("architecture=" + escapeValue(vm.getArchitecture()));
        writer.newLine();
        
        writer.write("networkType=" + escapeValue(vm.getNetworkType()));
        writer.newLine();
        
        writer.write("enableKvm=" + vm.isEnableKvm());
        writer.newLine();
        
        writer.write("cdromPath=" + escapeValue(vm.getCdromPath() != null ? vm.getCdromPath() : ""));
        writer.newLine();
        
        writer.write("bootOrder=" + escapeValue(vm.getBootOrder()));
        writer.newLine();
        
        writer.write("vncPort=" + vm.getVncPort());
        writer.newLine();
        
        writer.write("status=" + vm.getStatus().name());
        writer.newLine();
        
        writer.write("[VM_END]");
        writer.newLine();
    }
    
    /**
     * Parses VM data from the saved format
     */
    private QemuVm parseVmFromData(String vmData) {
        try {
            Properties props = new Properties();
            props.load(new StringReader(vmData));
            
            String name = unescapeValue(props.getProperty("name"));
            if (name == null || name.trim().isEmpty()) {
                System.err.println("Skipping VM with missing name");
                return null;
            }
            
            QemuVm vm = new QemuVm(name);
            
            String diskPath = unescapeValue(props.getProperty("diskPath", ""));
            if (!diskPath.isEmpty()) {
                vm.setDiskPath(diskPath);
            }
            
            vm.setMemoryMB(Integer.parseInt(props.getProperty("memoryMB", "1024")));
            vm.setCpuCores(Integer.parseInt(props.getProperty("cpuCores", "1")));
            vm.setArchitecture(unescapeValue(props.getProperty("architecture", "x86_64")));
            vm.setNetworkType(unescapeValue(props.getProperty("networkType", "user")));
            vm.setEnableKvm(Boolean.parseBoolean(props.getProperty("enableKvm", "true")));
            
            String cdromPath = unescapeValue(props.getProperty("cdromPath", ""));
            if (!cdromPath.isEmpty()) {
                vm.setCdromPath(cdromPath);
            }
            
            vm.setBootOrder(unescapeValue(props.getProperty("bootOrder", "dc")));
            
            String vncPortStr = props.getProperty("vncPort", "5901");
            try {
                vm.setVncPort(Integer.parseInt(vncPortStr));
            } catch (NumberFormatException e) {
                vm.setVncPort(5901); // Default fallback
            }
            
            // Always start with STOPPED status regardless of saved status
            vm.setStatus(QemuVm.VmStatus.STOPPED);
            
            return vm;
            
        } catch (Exception e) {
            System.err.println("Failed to parse VM data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Escapes special characters in values
     */
    private String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("=", "\\=");
    }
    
    /**
     * Unescapes special characters in values
     */
    private String unescapeValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\=", "=")
                   .replace("\\r", "\r")
                   .replace("\\n", "\n")
                   .replace("\\\\", "\\");
    }
    
    /**
     * Deletes the VM state file
     */
    public void clearVmState() {
        String vmStateFile = getVmStateFile();
        File stateFile = new File(vmStateFile);
        if (stateFile.exists()) {
            boolean deleted = stateFile.delete();
            if (deleted) {
                System.out.println("VM state file cleared.");
            } else {
                System.err.println("Failed to delete VM state file.");
            }
        }
    }
    
    /**
     * Returns the path to the VM state file
     */
    public String getStateFilePath() {
        return getVmStateFile();
    }
}