# QEMU Manager - State Persistence Test

## Testing State Persistence

To test the new state persistence functionality:

### 1. Create Test VMs
```bash
# Start the application
./run.sh

# Create a few test VMs with different configurations:
# - VM1: "Ubuntu Server" with 2GB RAM, 2 cores, User networking
# - VM2: "Windows 10" with 4GB RAM, 4 cores, Bridge networking  
# - VM3: "Test VM" with 1GB RAM, 1 core, TAP networking
```

### 2. Verify Auto-Save
- Create the VMs above
- Check console output - should show "VM state saved" messages
- Close the application
- Check that `~/.qemumanager_vms.txt` file exists and contains VM data

### 3. Test Auto-Load  
- Restart the application
- Verify all VMs are automatically loaded
- Check that all configurations (memory, CPU, network) are preserved
- Console should show "Loaded X virtual machines from saved state"

### 4. Test Manual Controls
- Use **File → Save VM State** to manually save
- Use **File → Reload VM State** to reload from file
- Use **File → Clear All VMs** to remove all (with confirmation)

### 5. State File Format
The state file (`~/.qemumanager_vms.txt`) uses this format:

```
# QEMU Manager VM State File
# Generated on: [timestamp]

[VM_START]
name=Ubuntu Server
diskPath=/path/to/ubuntu.qcow2
memoryMB=2048
cpuCores=2
architecture=x86_64
networkType=user
enableKvm=true
cdromPath=/path/to/ubuntu.iso
bootOrder=dc
status=STOPPED
[VM_END]

[VM_START]
name=Windows 10
diskPath=/path/to/windows.qcow2
memoryMB=4096
cpuCores=4
architecture=x86_64
networkType=bridge
enableKvm=true
cdromPath=
bootOrder=dc
status=STOPPED
[VM_END]
```

### 6. Features Verified
✅ **Automatic state saving** when VMs are created/edited/deleted  
✅ **Automatic state loading** on application startup  
✅ **Manual save/load/clear** via File menu  
✅ **Graceful shutdown** with state preservation  
✅ **Human-readable format** for easy inspection/editing  
✅ **Error handling** for corrupted or missing state files  
✅ **Console feedback** for all state operations  

### 7. File Locations
- **Linux/macOS**: `~/.qemumanager_vms.txt`
- **Windows**: `%USERPROFILE%\.qemumanager_vms.txt`

The state persistence ensures your VM configurations are never lost between sessions!