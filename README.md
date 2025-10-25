# QEMU Manager

[![Build QemuManager](https://github.com/MeanderingAI/QemuManager/actions/workflows/build.yml/badge.svg)](https://github.com/MeanderingAI/QemuManager/actions/workflows/build.yml)
[![Release QemuManager](https://github.com/MeanderingAI/QemuManager/actions/workflows/release.yml/badge.svg)](https://github.com/MeanderingAI/QemuManager/actions/workflows/release.yml)

A Java-based GUI application for managing QEMU virtual machines with an intuitive interface.

## Features

- **Virtual Machine Management**: Create, edit, and delete virtual machines
- **VM Control**: Start, stop, and monitor virtual machine status
- **Disk Image Management**: Comprehensive disk image management with dedicated tab
- **Persistent State**: Automatically saves and restores VM configurations between sessions
- **Disk Image Creation**: Built-in tool for creating QCOW2, RAW, VMDK, and VDI disk images
- **Disk Organization**: View all disk images, their usage, and manage unused disks
- **Settings Management**: Configure QEMU paths, default VM settings, and application preferences
- **Console Output**: Real-time monitoring of VM output and logs
- **VNC Support**: Connect to running VMs via VNC viewer
- **Cross-platform**: Runs on Linux, macOS, and Windows

## Download

### Latest Release
Download the latest pre-built JAR from the [Releases page](https://github.com/MeanderingAI/QemuManager/releases).

**Two options available:**
- **QemuManager-X.X.X.jar**: Standard JAR (requires dependencies)
- **QemuManager-X.X.X-executable.jar**: Standalone executable with all dependencies included

### Quick Start
```bash
# Download and run (replace X.X.X with latest version)
java -jar QemuManager-X.X.X-executable.jar
```

## Requirements

- Java 11 or higher
- QEMU installed on your system
- Optional: VNC viewer for connecting to VM displays

## Installation

### Prerequisites

1. **Install Java 11+**:
   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-11-jdk
   
   # CentOS/RHEL
   sudo yum install java-11-openjdk-devel
   
   # macOS (with Homebrew)
   brew install openjdk@11
   
   # Windows: Download from Oracle or use Chocolatey
   choco install openjdk11
   ```

2. **Install QEMU**:
   ```bash
   # Ubuntu/Debian
   sudo apt install qemu-system
   
   # CentOS/RHEL
   sudo yum install qemu-kvm
   
   # macOS (with Homebrew)
   brew install qemu
   
   # Windows: Download from https://www.qemu.org/download/
   ```

3. **Optional: Install VNC Viewer**:
   ```bash
   # Ubuntu/Debian
   sudo apt install vncviewer
   
   # CentOS/RHEL
   sudo yum install tigervnc
   
   # macOS
   brew install --cask vnc-viewer
   
   # Windows: Download TightVNC or RealVNC
   ```

### Building from Source

1. **Clone or download the project**:
   ```bash
   git clone <repository-url>
   cd QemuManager
   ```

2. **Build with Maven**:
   ```bash
   mvn clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/qemu-manager-1.0.0.jar
   ```

   Or use Maven to run directly:
   ```bash
   mvn exec:java
   ```

## Usage

### First Time Setup

1. **Launch the application**
2. **Configure QEMU path**: Go to Tools → Configure QEMU Path and set the path to your QEMU executable
   - Linux: Usually `/usr/bin/qemu-system-x86_64`
   - macOS: Often `/usr/local/bin/qemu-system-x86_64`
   - Windows: `C:\Program Files\qemu\qemu-system-x86_64.exe`

3. **Optional: Configure VNC viewer**: Go to Tools → Settings → Paths tab and set your VNC viewer path

### Creating a Virtual Machine

1. **Click "New VM"** or go to File → New Virtual Machine
2. **Fill in the VM details**:
   - **VM Name**: Choose a descriptive name
   - **Memory (MB)**: Amount of RAM (e.g., 1024 for 1GB)
   - **CPU Cores**: Number of virtual CPU cores
   - **Architecture**: Target architecture (x86_64, ARM, etc.)
   - **Network**: Network configuration (user, tap, bridge, none)

3. **Create or select a disk image**:
   - Use "Browse" to select an existing disk image
   - Or click "Create New Disk" to create a fresh disk image

4. **Optional: Add CD-ROM**: Browse for an ISO file to boot from

5. **Click OK** to create the virtual machine

### Managing Virtual Machines

- **Start VM**: Click the "Start" button in the Actions column
- **Stop VM**: Click the "Stop" button (appears when VM is running)
- **Connect**: Click "Connect" to open VNC viewer (when VM is running)
- **Edit VM**: Double-click a VM row or select and click "Edit"
- **Delete VM**: Select a VM and click "Delete"

### Managing Disk Images

The **Disk Images** tab provides comprehensive disk image management:

#### Viewing Disk Images
- See all disk images used by your virtual machines
- View file paths, formats, sizes, and which VMs use each disk
- Double-click any disk for detailed information

#### Disk Operations
- **Info**: View detailed disk properties and metadata
- **Delete**: Remove unused disk images (disabled for disks in use)
- **Open**: Open the disk file location in your file manager

#### Disk Management Tools
- **Create New Disk**: Launch the disk creation wizard
- **Refresh**: Update the disk list with current VM configurations
- **Scan Directory**: Find existing disk images in a selected folder
- **Cleanup Unused**: Identify disk images not used by any VM

#### Disk Image Status
- **Used By**: Shows which VM(s) are using each disk image
- **Not Used**: Indicates orphaned disk images that can be safely deleted
- **Missing File**: Warns about disk images referenced by VMs but not found on disk

### Console Output

The bottom panel shows:
- VM startup messages
- QEMU output and errors
- Application status messages
- Real-time monitoring of running VMs

Use the "Clear" button to clear the console or "Save Log" to export the output.

### Settings

Access via Tools → Settings:

- **Paths tab**: Configure QEMU and VNC viewer paths
- **Defaults tab**: Set default values for new VMs
- **General tab**: Configure auto-save and other preferences

## VM Configuration Options

### Architecture Support
- **x86_64**: Standard PC architecture (64-bit)
- **i386**: 32-bit PC architecture
- **aarch64**: 64-bit ARM architecture
- **arm**: 32-bit ARM architecture
- **mips**: MIPS architecture
- **ppc**: PowerPC architecture

### Network Types
- **user**: QEMU user networking (NAT)
- **tap**: TAP interface (requires setup)
- **bridge**: Bridge networking (requires setup)
- **none**: No network

### Disk Formats
- **qcow2**: QEMU's native format (supports compression and snapshots)
- **raw**: Simple format (good performance, larger files)
- **vmdk**: VMware format
- **vdi**: VirtualBox format

### Boot Order
Specify boot device priority using letters:
- **a, b**: Floppy drives
- **c**: Hard disk
- **d**: CD-ROM
- **n**: Network

Example: "dc" means try CD-ROM first, then hard disk.

## Command Line Options

The application generates QEMU commands with these typical options:
- `-m <memory>`: Memory allocation
- `-smp <cores>`: CPU cores
- `-drive file=<path>,format=<format>`: Disk image
- `-cdrom <path>`: CD-ROM image
- `-boot <order>`: Boot device order
- `-vnc :1`: VNC display on port 5901
- `-enable-kvm`: KVM acceleration (when available)

## Troubleshooting

### Common Issues

1. **"QEMU not found" error**:
   - Verify QEMU is installed: `qemu-system-x86_64 --version`
   - Check the QEMU path in settings
   - On Windows, ensure the .exe extension is included

2. **Permission denied**:
   - On Linux/macOS, you may need to add your user to the `kvm` group
   - Ensure QEMU has proper permissions

3. **VM won't start**:
   - Check the console output for error messages
   - Verify disk image files exist and are readable
   - Ensure adequate system resources

4. **VNC connection fails**:
   - Verify the VNC viewer path in settings
   - Check that the VM is running
   - Ensure port 5901 is not blocked by firewall

### Getting Help

- Check the console output for detailed error messages
- Verify QEMU and VNC viewer installations
- Ensure all file paths are correct and accessible

## File Locations

- **Settings file**: `~/.qemumanager.properties` (Linux/macOS) or `%USERPROFILE%\.qemumanager.properties` (Windows)
- **VM state file**: `~/.qemumanager_vms.txt` (Linux/macOS) or `%USERPROFILE%\.qemumanager_vms.txt` (Windows)
- **Log files**: Can be saved manually from the console panel

### State Persistence

The application automatically saves and restores your VM configurations:

- **Automatic Save**: VM configurations are saved whenever you create, edit, or delete a VM
- **Automatic Load**: When you start the application, it automatically loads your previously configured VMs
- **Manual Control**: Use File menu options to manually save, reload, or clear VM state
- **File Format**: VM state is stored in a human-readable text format

**State Management Options:**
- **File → Save VM State**: Manually save current VMs to file
- **File → Reload VM State**: Reload VMs from the state file
- **File → Clear All VMs**: Remove all VMs and delete the state file

## Building Distribution

To create a standalone JAR with all dependencies:

```bash
mvn clean package -Pdist
```

This creates `target/qemu-manager-1.0.0-jar-with-dependencies.jar` which can be run on any system with Java 11+.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## System Requirements

- **Minimum RAM**: 512MB for the application + VM memory requirements
- **Disk Space**: Varies based on VM disk images
- **Java**: OpenJDK or Oracle JDK 11 or higher
- **QEMU**: Any recent version (5.0+ recommended)

## Security Notes

- VM disk images may contain sensitive data - store securely
- QEMU runs with user privileges, not root
- VNC connections are unencrypted by default
- Consider firewall rules for VNC ports

## Keyboard Shortcuts

- **Ctrl+N**: New Virtual Machine
- **F5**: Refresh VM list
- **Delete**: Delete selected VM
- **Enter**: Edit selected VM (when focused on table)

This application provides a user-friendly interface for QEMU while maintaining access to advanced virtualization features. For complex scenarios, you can always use QEMU directly from the command line.