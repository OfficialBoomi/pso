import java.util.*;
import java.io.*;
import java.time.*;
import groovy.lang.*;
import java.lang.management.*;
import com.boomi.execution.*;
import com.boomi.process.logging.BaseProcessLogger;
import com.sun.management.UnixOperatingSystemMXBean;
import java.net.InetAddress;
import groovy.json.JsonOutput;

BaseProcessLogger logger = ExecutionUtil.getBaseLogger();

String createDate = LocalDateTime.now().toString();

//Atom name
atomName = ExecutionManager.getCurrent().getContainerConfig().getContainerName();

//Host name
hostname = getHostname()

//Thread Metrics
peakThreadCount = getThreadStats()

//CPU Count
cpuCount = getCPUCount()

//File Descriptor Stats
maxFileDescriptorCount = getFileDescriptorStats();

//GC Metrics
GarbageCollector gc = new GarbageCollector();
def gcMetrics = gc.getMetrics();

//Heap Metrics
Heap heap = new Heap();
def heapMetrics = heap.getMetrics();

//Memory Pool Metrics
MemoryPool mp = new MemoryPool();
def mpMetrics = mp.getMetrics();

MachineMemory mm = new MachineMemory();
String totalMachineMemory = mm.getTotal();

//container.properties
Container container = new Container();
def containerProperties = container.getProperties();

//atom.vmoptions
AtomVMOptions atomOptions = new AtomVMOptions();
def atomOptionsSystemProperties = atomOptions.getSystemProperties();
def atomOptionsAdvancedProperties = atomOptions.getAdvancedProperties();
def atomOptionsOtherProperties = atomOptions.getOtherProperties();

//Runtime Type
String runtimeType;
viewsDir = new File("bin/views");
Integer viewFilesCount;
if ( viewsDir.exists() ) {
    runtimeType = "Molecule";
    MoleculeCluster mc = new MoleculeCluster(viewsDir);
    viewFilesCount = mc.getViews().size();
}
else {
    runtimeType = "Atom"
    viewFilesCount = null;
}


//Filesystem & Inodes
File installDir = new File("").getAbsoluteFile();
FileSystem fsInstallDir = new FileSystem(installDir);
fsInstallDirMetrics = fsInstallDir.getFSMetrics();
inodesInstallDirMetrics = fsInstallDir.getInodeMetrics();

def tmpdirStr = atomOptionsSystemProperties.find { entry -> entry.name == "-Djava.io.tmpdir" }
if ( tmpdirStr ){
    File tmpDir = new File(tmpdirStr.value);
    FileSystem fsTmpDir = new FileSystem(tmpDir);
    fsTmpDirMetrics = fsTmpDir.getFSMetrics();
    inodesTmpDirMetrics = fsTmpDir.getInodeMetrics();
}
else {
    fsTmpDirMetrics = null;
    inodesTmpDirMetrics = null;
}

//Runtime Properties
RuntimeProperties rp = new RuntimeProperties();
def javaProperties = rp.getJavaProperties();
def osProperties = rp.getOsProperties();

//Create output
output = [
        createDate: createDate,
        atomName: atomName,
        runtimeType: runtimeType,
        viewFilesCount: viewFilesCount,
        hostname: hostname,
        cpuCount: cpuCount,
        totalMachineMemory: totalMachineMemory,
        peakThreadCount: peakThreadCount,
        maxFileDescriptorCount: maxFileDescriptorCount,
        javaProperties: javaProperties,
        osProperties: osProperties,
        garbageCollectionMetrics: gcMetrics,
        memoryPoolMetrics: mpMetrics,
        heapMetrics: heapMetrics,
        filesystem: [
                install: [
                        storage: fsInstallDirMetrics, 
                        inodes: inodesInstallDirMetrics
                    ],
                tmp: [
                        storage: fsTmpDirMetrics, 
                        inodes: inodesTmpDirMetrics
                    ]
            ],
        containerProperties: containerProperties,
        atomProperties: [
                system: atomOptionsSystemProperties,
                advanced: atomOptionsAdvancedProperties,
                other: atomOptionsOtherProperties
            ]
    ]


def outputJson = JsonOutput.toJson(output)

Properties props = dataContext.getProperties(0);
is = new ByteArrayInputStream(outputJson.toString().getBytes());
dataContext.storeStream(is, props);


//--------------------------------------------------------------------------

public class MachineMemory {
    private String total;
    public MachineMemory(){
        def process = Runtime.getRuntime().exec("free -mh ");
        process.waitFor();
        def output = process.text;
        output = output.readLines().get(1);
        process.destroy();
        String[] outputParts = output.split("\\s+");
        total = outputParts[1];
    }
    
    public getTotal() {
        return total;
    }
}

public class MoleculeCluster {
    private List<String> viewsList = new ArrayList<>();
    public MoleculeCluster(File viewsDir){
        viewsDir.eachFileMatch(~/node.*.dat/) { line -> 
            viewsList << line;
        }
    }

    public getViews() {
        return viewsList;
    }
}

public class RuntimeProperties {
    private javaProperties;
    private osProperties;
    public RuntimeProperties(){
        RuntimeMXBean runtimeMXBeans = ManagementFactory.getRuntimeMXBean();
        Map<String, String> spMap = new HashMap<>();
        spMap = runtimeMXBeans.getSystemProperties()
        spMap.put("vmUptime", runtimeMXBeans.getUptime())
        javaProperties = [
            vendor: spMap["java.vendor"],
            version: spMap["java.runtime.version"],
            vmName: spMap["java.vm.name"],
            vmUptime: spMap["vmUptime"]
        ]
        
        osProperties = [
            name: spMap["os.name"],
            version: spMap["os.version"],
            architecture: spMap["os.arch"]
        ]
    }
    
    public getJavaProperties() {
        return javaProperties;
    }
    
    public getOsProperties() {
        return osProperties;
    }
}

public class FileSystem {
    private long totalSpace;
    private long freeSpace;
    private long usedSpace;
    private int usedPercentage;
    private Map<String, Object> fsMap = new HashMap<>();
    
    private String fileSystem;
    private long totalInodes;
    private long usedInodes;
    private long freeInodes;
    private int usedInodesPercentage;
    private Map<String, Object> inMap = new HashMap<>();

    public FileSystem(File fs){
        totalSpace = fs.getTotalSpace();
        freeSpace = fs.getUsableSpace();
        usedSpace = totalSpace - freeSpace;
        if ( totalSpace > 0 ) {
            usedPercentage = Math.round(( usedSpace / totalSpace ) * 100);
        }
        fsMap.put("total", totalSpace);
        fsMap.put("free", freeSpace);
        fsMap.put("used", usedSpace);
        fsMap.put("usedPercentage", usedPercentage);
        
        def process = Runtime.getRuntime().exec("df -i " + fs);
        process.waitFor();
        def output = process.text;
        output = output.readLines().get(1);
        process.destroy();
        String[] outputParts = output.split("\\s+");
        fileSystem = outputParts[0].toString();
        totalInodes = outputParts[1].toLong();
        usedInodes = outputParts[2].toLong();
        freeInodes = outputParts[3].toLong();
        if ( outputParts[4].endsWith("%") ) {
            outputParts[4] = removeLastChar(outputParts[4]);
        }
        else if ( outputParts[4].endsWith("-") ) {
            outputParts[4] = 0
        }
        usedInodesPercentage = outputParts[4].toInteger();
        inMap.put("fileSystem", fileSystem);
        inMap.put("total", totalInodes);
        inMap.put("used", usedInodes);
        inMap.put("free", freeInodes);
        inMap.put("usedPercentage", usedInodesPercentage);
    }
    
    public getFSMetrics() {
        return fsMap;
    }
    
    public getInodeMetrics() {
        return inMap;
    }
    
    public String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length()-1);
    }
}

public class AtomVMOptions {
    private List<String> aoxList = new ArrayList<>();
    private List<String> aooList = new ArrayList<>();
    private List<Map<String, Object>> aodList = new ArrayList<>();
    public AtomVMOptions(){
        new File("bin/atom.vmoptions").eachLine { line -> 
            Map<String, Object> aodMap = new HashMap<>();
            if (!line.startsWith("#")){
                def entry = line.tokenize("=");
                String key = entry[0];
                def value;
                if ( entry[1] && entry[1].isInteger() ) {
                    value = entry[1].toInteger();
                }
                else {
                    value = entry[1];
                }
                if ( key.startsWith("-X") ) {
                    aoxList << key;
                }
                else if ( key.startsWith("-D") ) {
                    aodMap.put("name", key);
                    aodMap.put("value", value);
                    aodList.add(aodMap);
                }
                else if ( key ){
                    aooList << key;
                }
                else {
                    throw new Exception("Issue with parsing atom.vmoptions file");
                }
            }
        }
    }
    
    public getSystemProperties() {
        return aodList;
    }
    
    public getAdvancedProperties() {
        return aoxList;
    }

    public getOtherProperties() {
        return aooList;
    }
}

public class GarbageCollector {
    private List<Map<String, Object>> gcMetrics = new ArrayList<>();
    private List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    public GarbageCollector(){
        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            Map<String, Object> gcMap = new HashMap<>();
            String name = gcMXBean.getName();
            Long count = gcMXBean.getCollectionCount()
            Long time = gcMXBean.getCollectionTime()
            gcMap.put("name", name);
            gcMap.put("count", count);
            gcMap.put("time", time);
            if ( time > 0 ) {
                gcMap.put("averageTime", Math.round( time / count ));
            }
            gcMetrics.add(gcMap);
        }
    }

    public getMetrics() {
        return gcMetrics;
    }
}

public class MemoryPool {
    private List<Map<String, Object>> mpMetrics = new ArrayList<>();
    private List<MemoryPoolMXBean> mpMXBeans = ManagementFactory.getMemoryPoolMXBeans();
    public MemoryPool(){
        for (MemoryPoolMXBean mpMXBean : mpMXBeans) {
            Map<String, Object> mpMap = new HashMap<>();
            String name = mpMXBean.getName();
            Long peakUsageCommitted = mpMXBean.getPeakUsage().getCommitted();
            Long peakUsageUsed = mpMXBean.getPeakUsage().getUsed();
            mpMap.put("name", name);
            mpMap.put("peakUsageCommitted", peakUsageCommitted);
            mpMap.put("peakUsageUsed", peakUsageUsed);
            if ( peakUsageCommitted > 0 ) {
                int peakUsagePercentage = Math.round(( peakUsageUsed / peakUsageCommitted ) * 100);
                mpMap.put("peakUsagePercentage", peakUsagePercentage);
            }
            mpMetrics.add(mpMap);
        }

    }
    
    public getMetrics() {
        return mpMetrics;
    }
}

public class Heap {
    private Map<String, Object> heapMap = new HashMap<>();
    public Heap(){
        MemoryMXBean heapBeans = ManagementFactory.getMemoryMXBean();
        Long max = heapBeans.getHeapMemoryUsage().getMax();
        Long used = heapBeans.getHeapMemoryUsage().getUsed();
        Long committed = heapBeans.getHeapMemoryUsage().getCommitted();
        heapMap.put("max", max);
        heapMap.put("used", used);
        heapMap.put("committed", committed);
        if ( committed > 0 ) {
            int usedPercentage = Math.round(( used / committed ) * 100);
            heapMap.put("usedPercentage", usedPercentage);
        }
    }

    public getMetrics() {
        return heapMap;
    }
}

public class Container {
    private List<Map<String, Object>> cpList = new ArrayList<>();
    public Container(){
        new File("conf/container.properties").eachLine { line -> 
            Map<String, Object> cpMap = new HashMap<>();
            if (!line.startsWith("#")){
                def entry = line.tokenize("=");
                String key = entry[0];
                def value;
                if ( entry[1] && entry[1].isInteger() ) {
                    value = entry[1].toInteger();
                }
                else {
                    value = entry[1]
                }
                cpMap.put("name", key);
                cpMap.put("value", value);
                cpList.add(cpMap);
            }
        }
    }
    
    public getProperties() {
        return cpList;
    }
}

public static int getThreadStats() {
    ThreadMXBean threadBeans = ManagementFactory.getThreadMXBean();
    int peakThreadCount =  threadBeans.getPeakThreadCount()
    return peakThreadCount;
}

public static String getHostname() {
    InetAddress hostDetails = InetAddress.getLocalHost();
    String hostname = hostDetails.getHostName()
    return hostname;
}

public static long getFileDescriptorStats() {
    UnixOperatingSystemMXBean fileDescriptorBeans = ManagementFactory.getOperatingSystemMXBean();
    long maxFileDescriptorCount =  fileDescriptorBeans.getMaxFileDescriptorCount()
    return maxFileDescriptorCount;
}

public static int getCPUCount(){
    OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    int cpuCount = osMXBean.getAvailableProcessors();
    return cpuCount;
}
