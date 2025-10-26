package com.qemumanager;

/**
 * Represents a QEMU virtual machine configuration
 */
public class QemuVm {
    private String name;
    private String diskPath;
    private int memoryMB;
    private int cpuCores;
    private String architecture;
    private String networkType;
    private boolean enableKvm;
    private String cdromPath;
    private String bootOrder;
    private int vncPort;
    private VmStatus status;
    private Process qemuProcess;
    
    public enum VmStatus {
        STOPPED("Stopped"),
        RUNNING("Running"),
        PAUSED("Paused"),
        STARTING("Starting"),
        STOPPING("Stopping");
        
        private final String displayName;
        
        VmStatus(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public QemuVm(String name) {
        this.name = name;
        this.memoryMB = 1024;
        this.cpuCores = 1;
        this.architecture = "x86_64";
        this.networkType = "user";
        this.enableKvm = true;
        this.bootOrder = "dc";
        this.vncPort = 5901; // Default VNC port (5900 + 1)
        this.status = VmStatus.STOPPED;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDiskPath() {
        return diskPath;
    }
    
    public void setDiskPath(String diskPath) {
        this.diskPath = diskPath;
    }
    
    public int getMemoryMB() {
        return memoryMB;
    }
    
    public void setMemoryMB(int memoryMB) {
        this.memoryMB = memoryMB;
    }
    
    public int getCpuCores() {
        return cpuCores;
    }
    
    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }
    
    public String getArchitecture() {
        return architecture;
    }
    
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }
    
    public String getNetworkType() {
        return networkType;
    }
    
    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }
    
    /**
     * Returns a user-friendly network description
     */
    public String getNetworkDescription() {
        switch (networkType.toLowerCase()) {
            case "user":
                return "User (NAT)";
            case "tap":
                return "TAP Interface";
            case "bridge":
                return "Bridge Network";
            case "none":
                return "Disabled";
            case "netdev":
                return "Network Device";
            default:
                return networkType.substring(0, 1).toUpperCase() + networkType.substring(1);
        }
    }
    
    public boolean isEnableKvm() {
        return enableKvm;
    }
    
    public void setEnableKvm(boolean enableKvm) {
        this.enableKvm = enableKvm;
    }
    
    public String getCdromPath() {
        return cdromPath;
    }
    
    public void setCdromPath(String cdromPath) {
        this.cdromPath = cdromPath;
    }
    
    public String getBootOrder() {
        return bootOrder;
    }
    
    public void setBootOrder(String bootOrder) {
        this.bootOrder = bootOrder;
    }
    
    public int getVncPort() {
        return vncPort;
    }
    
    public void setVncPort(int vncPort) {
        this.vncPort = vncPort;
    }
    
    public VmStatus getStatus() {
        return status;
    }
    
    public void setStatus(VmStatus status) {
        this.status = status;
    }
    
    public Process getQemuProcess() {
        return qemuProcess;
    }
    
    public void setQemuProcess(Process qemuProcess) {
        this.qemuProcess = qemuProcess;
    }
    
    /**
     * Generates the QEMU command line arguments for this VM
     */
    public String[] generateQemuCommand() {
        java.util.List<String> command = new java.util.ArrayList<>();
        
        command.add(QemuSettings.getInstance().getQemuPath());
        
        // Architecture
        if (!architecture.equals("x86_64")) {
            command.add("-machine");
            command.add(getArchitectureMachine());
        }
        
        // Memory
        command.add("-m");
        command.add(String.valueOf(memoryMB));
        
        // CPU cores
        command.add("-smp");
        command.add(String.valueOf(cpuCores));
        
        // KVM acceleration
        if (enableKvm) {
            command.add("-enable-kvm");
            command.add("-cpu");
            command.add("host"); // Use host CPU features for better performance
        } else {
            command.add("-cpu");
            command.add("qemu64"); // Generic CPU for compatibility
        }
        
        // Disk
        if (diskPath != null && !diskPath.isEmpty()) {
            command.add("-drive");
            command.add("file=" + diskPath + ",format=qcow2");
        }
        
        // CD-ROM
        if (cdromPath != null && !cdromPath.isEmpty()) {
            command.add("-cdrom");
            command.add(cdromPath);
        }
        
        // Boot order
        command.add("-boot");
        command.add(bootOrder);
        
        // Network
        command.add("-netdev");
        command.add(networkType + ",id=net0");
        command.add("-device");
        command.add("e1000,netdev=net0");
        
        // VNC display
        command.add("-vnc");
        command.add(":" + (vncPort - 5900)); // VNC display number (port - 5900)
        
        // Graphics acceleration and display
        command.add("-vga");
        command.add("std"); // Standard VGA adapter (compatible with most OS)
        
        // USB support for better device compatibility
        command.add("-usb");
        command.add("-device");
        command.add("usb-tablet"); // Better mouse integration
        
        // Monitor
        command.add("-monitor");
        command.add("stdio");
        
        return command.toArray(new String[0]);
    }
    
    private String getArchitectureMachine() {
        switch (architecture) {
            case "aarch64":
                return "virt";
            case "arm":
                return "versatilepb";
            case "mips":
                return "malta";
            case "ppc":
                return "prep";
            default:
                return "pc";
        }
    }
    
    @Override
    public String toString() {
        return name + " (" + status + ")";
    }
}