import java.util.Properties;
import java.io.InputStream;
import groovy.json.JsonSlurper;
import groovy.xml.MarkupBuilder;
import java.time.*
import com.boomi.execution.*;
import com.boomi.process.logging.BaseProcessLogger;

BaseProcessLogger logger = ExecutionUtil.getBaseLogger();
LocalDateTime dateTime = LocalDateTime.now();

/*---------CSS---------
<style type="text/css">
body {margin: 100px;}
h1 {font-family:Arial, sans-serif;font-size:20px;font-weight:bold}
h2 {font-family:Arial, sans-serif;font-size:14px;font-weight:bold}
p {font-family:Arial, sans-serif;font-size:11px}
.tbl {border-collapse:collapse;border-spacing:0;}
.tbl td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:5px 5px;word-break:normal;}
.tbl th{background-color:#033D58;border-color:black;border-style:solid;border-width:1px;color:#ffffff;font-family:Arial, sans-serif;font-size:11px;font-weight:bold;text-align:center;overflow:hidden;padding:5px 5px;word-break:normal;}
.tbl .category{border-color:inherit;font-weight:bold;font-size:0.75em;text-align:center;vertical-align:middle}
.tbl .label{border-color:inherit;font-weight:bold;font-size:0.75em;text-align:right;vertical-align:top}
.tbl .value{border-color:inherit;font-size:0.75em;text-align:left;vertical-align:top}
</style>
*/


for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    def jsonSlurper = new JsonSlurper()
    def object = jsonSlurper.parse(is)
    
    InputStream ins = new StringWriter().with { sw ->
        new MarkupBuilder( sw ).build {
            body {
                h1"Boomi PSO Report"
                p"created on $object.createDate"
                h2"Runtime Review"
                table(class: "tbl") {
                    tr {
                        th("Category")
                        th("Name")
                        th("Value")
                        th("Recommendation")
                    }
                    tr {
                        if ( object.runtimeType == "Atom" ) {
                            td(rowspan:"3", class:"category","General")
                        }
                        else {
                            td(rowspan:"4", class:"category","General")
                        }
                        td(class:"label","Name")
                        td(class:"value",object.atomName)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Runtime Type")
                        td(class:"value",object.runtimeType)
                        td(class:"value","")
                    }
                    if ( object.runtimeType != "Atom" ) {
                        tr{
                            td(class:"label","Number of View Files")
                            td(class:"value",object.viewFilesCount)
                            td(class:"value","")
                        }
                    }
                    tr{
                        td(class:"label","Hostname")
                        td(class:"value",object.hostname)
                        td(class:"value","")
                    }
                    //Virtual Machine
                    tr{
                        td(rowspan:"3", class:"category","Virtual Machine")
                        td(class:"label","CPU Count (per node)")
                        td(class:"value",object.cpuCount)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","CPU Architecture")
                        td(class:"value",object.osProperties.architecture)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Memory RAM (per node)")
                        td(class:"value",object.totalMachineMemory)
                        td(class:"value","")
                    }
                    //Storage
                    tr{
                        if ( object.filesystem.tmp.storage == null ) {
                            td(rowspan:"5", class:"category","Storage")
                        }
                        else {
                            td(rowspan:"10", class:"category","Storage")
                        }
                        td(class:"label","Install Dir Filesystem")
                        td(class:"value",object.filesystem.install.inodes.fileSystem)
                        td(class:"value","")
                    }
                    
                    installStorageTotal = Math.round(object.filesystem.install.storage.total / 1024 / 1000 / 1000)
                    tr{
                        td(class:"label","Install Dir Size")
                        td(class:"value",installStorageTotal + "GB")
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Install Dir Usage")
                        td(class:"value",object.filesystem.install.storage.usedPercentage + "%")
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Install Dir Inodes")
                        td(class:"value",object.filesystem.install.inodes.total)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Install Dir Inodes Usage")
                        td(class:"value",object.filesystem.install.inodes.usedPercentage + "%")
                        td(class:"value","")
                    }
                    //Tmp Directory
                    if ( object.filesystem.tmp.storage != null ) {
                        tr{
                            td(class:"label","Tmp Dir Filesystem")
                            td(class:"value",object.filesystem.tmp.inodes.fileSystem)
                            td(class:"value","")
                        }
                        tmpStorageTotal = Math.round(object.filesystem.tmp.storage.total / 1024 / 1000 / 1000)
                        tr{
                            td(class:"label","Tmp Dir Size")
                            td(class:"value",tmpStorageTotal + "GB")
                            td(class:"value","")
                        }
                        tr{
                            td(class:"label","Tmp Dir Usage")
                            td(class:"value",object.filesystem.tmp.storage.usedPercentage + "%")
                            td(class:"value","")
                        }
                        tr{
                            td(class:"label","Tmp Dir Inodes")
                            td(class:"value",object.filesystem.tmp.inodes.total)
                            td(class:"value","")
                        }
                        tr{
                            td(class:"label","Tmp Dir Inodes Usage")
                            td(class:"value",object.filesystem.tmp.inodes.usedPercentage + "%")
                            td(class:"value","")
                        }
                    }
                    //OS
                    tr{
                        td(rowspan:"3", class:"category","Operating System")
                        td(class:"label","Name")
                        td(class:"value",object.osProperties.name)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Version")
                        td(class:"value",object.osProperties.version)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Max Open Files")
                        td(class:"value",object.maxFileDescriptorCount)
                        td(class:"value","")
                    }
                    //Java
                    tr{
                        td(rowspan:"6", class:"category","Java")
                        td(class:"label","Vendor")
                        td(class:"value",object.javaProperties.vendor)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Version")
                        td(class:"value",object.javaProperties.version)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","VM Name")
                        td(class:"value",object.javaProperties.vmName)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","VM Uptime")
                        td(class:"value", Math.round(object.javaProperties.vmUptime / 6000 / 60) + "h")
                        td(class:"value","")
                    }
                    heapSize = Math.round(object.heapMetrics.max / 1024 / 1000 )
                    tr{
                        td(class:"label","Memory Heap (per node)")
                        td(class:"value",heapSize + "MB")
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Peak Thread Count")
                        td(class:"value",object.peakThreadCount)
                        td(class:"value","")
                    }
                }
                //-----------------------
                h2"GC Usage Metrics"
                table(class: "tbl") {
                    tr {
                        th("Name")
                        th("Total Time (ms)")
                        th("Total Count")
                        th("Average Time (ms)")
                    }
                    object.garbageCollectionMetrics.each { entry ->
                        tr {
                            td(class:"label",entry.name)
                            td(class:"value",entry.time)
                            td(class:"value",entry.count)
                            td(class:"value",entry.averageTime)
                        }
                    }
                }
                //-----------------------
                h2"Memory Pool Metrics"
                table(class: "tbl") {
                    tr {
                        th("Name")
                        th("Peak Committed")
                        th("Peak Used")
                        th("Peak Usage")
                    }
                    object.memoryPoolMetrics.each { entry ->
                        tr {
                            peakUsageCommitted = Math.round(entry.peakUsageCommitted / 1024 / 1000)
                            peakUsageUsed = Math.round(entry.peakUsageUsed / 1024 / 1000)
                            td(class:"label",entry.name)
                            td(class:"value",peakUsageCommitted + "MB")
                            td(class:"value",peakUsageUsed + "MB")
                            td(class:"value",entry.peakUsagePercentage + "%")
                        }
                    }
                }
                //-----------------------
                h2"Container Properties"
                table(class: "tbl") {
                    tr {
                        th("Name")
                        th("Default Value")
                        th("Configured Value")
                        th("Recommendation")
                    }
                    object.containerProperties.each { entry ->
                        ContainerProperties containerPropertiesRef = new ContainerProperties(entry.name);
                        def labelValue = containerPropertiesRef.getLabel();
                        def defaultValue = containerPropertiesRef.getDefaultValue();
                        def isExcluded = containerPropertiesRef.isExcluded();
                        if ( isExcluded ) {
                            return;
                        }
                        else {
                            tr {
                                td(class:"label", labelValue)
                                td(class:"value", defaultValue)
                                td(class:"value", entry.value)
                                td(class:"value", "")
                            }
                        }
                    }
                }
                //-----------------------
                h2"Atom (System) Properties"
                table(class: "tbl") {
                    tr {
                        th("Name")
                        th("Default Value")
                        th("Configured Value")
                        th("Recommendation")
                    }
                    object.atomProperties.system.each { entry ->
                        AtomVMOptions atomOptionsRef = new AtomVMOptions(entry.name);
                        def labelValue = atomOptionsRef.getLabel();
                        def defaultValue = atomOptionsRef.getDefaultValue();
                        def isExcluded = atomOptionsRef.isExcluded();
                        
                        if ( isExcluded ) {
                            return;
                        }
                        else {
                            tr {
                                td(class:"label", labelValue)
                                td(class:"value", defaultValue)
                                td(class:"value", entry.value)
                                td(class:"value", "")
                            }
                        }                       
                    }
                }
                //-----------------------
                h2"Atom (Advanced) Properties"
                table(class: "tbl") {
                    tr {
                        th("Property")
                        th("Comment")
                    }
                    object.atomProperties.advanced.each { entry ->
                        tr {
                            td(class:"value",entry)
                            td(class:"value","")
                        }
                    }
                }
                //-----------------------
                if ( object.atomProperties.other[0] ){
                    h2"Atom (Other) Properties"
                    table(class: "tbl") {
                        tr {
                            th("Property")
                            th("Comment")
                        }
                        object.atomProperties.other.each { entry ->
                            tr {
                                td(class:"value",entry)
                                td(class:"value","")
                            }
                        }
                    }
                }
            }
        }
        isHTML = new ByteArrayInputStream( sw.toString().getBytes( 'UTF-8' ) )
        dataContext.storeStream(isHTML, props);
    }
}

public class ContainerProperties {
    private String label;
    private String defaultValue;
    private boolean exclude;
    public ContainerProperties(key){
        def containerPropertiesRef = [
            "com.boomi.container.name": [
                label: "Container Name",
                defaultValue: "",
                exclude: true
            ],
            "com.boomi.container.platformURL": [
                label: "Platform URL",
                defaultValue: "",
                exclude: true
            ],
            "com.boomi.container.proxyPassword": [
                label: "Proxy Password",
                defaultValue: "",
                exclude: true
            ],
            "com.boomi.container.trackURL": [
                label: "Track URL",
                defaultValue: "",
                exclude: true
            ],
            "com.boomi.container.forceRestart": [
                label: "Force Restart After X Minutes",
                defaultValue: "0",
                exclude: false
            ],
            "com.boomi.container.purgeDays": [
                label: "Purge History After X Days",
                defaultValue: "30",
                exclude: false
            ],
            "com.boomi.container.purgeImmediately": [
                label: "Purge Data Immediately",
                defaultValue: "false",
                exclude: false
            ],
            "com.boomi.container.dataDirNestLevel": [
                label: "Atom Data Directory Level",
                defaultValue: "0",
                exclude: false
            ],
            "com.boomi.container.executionDirNestLevel": [
                label: "Process Execution Directory Level",
                defaultValue: "0",
                exclude: false
            ],
            "com.boomi.container.cloudlet.clusterConfig": [
                label: "Cluster Network Transport Type",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.cloudlet.initialHosts": [
                label: "Initial Host for Unicast",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.maxRunningExecutions": [
                label: "Maximum Simultaneous Executions per Node",
                defaultValue: "Unlimited",
                exclude: false
            ],
            "com.boomi.container.maxQueuedExecutions": [
                label: "Maximum Queued Executions per Node",
                defaultValue: "200",
                exclude: false
            ],
            "com.boomi.container.cluster.rollingRestart.clusterRestartPercentage": [
                label: "Rolling Restart Cluster Restart Percentage",
                defaultValue: "20",
                exclude: false
            ],
            "com.boomi.container.sharedServer.http.maxConnectionThreadPoolSize": [
                label: "Web Server - Maximum Number of Threads",
                defaultValue: "250",
                exclude: false
            ],
            "com.boomi.container.maxExecutionTime": [
                label: "Maximum Forked Execution Time in Cloud",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.resource.restartOnOutOfMemoryError": [
                label: "Auto Restart on Out Of Memory",
                defaultValue: "TRUE",
                exclude: false
            ],
            "com.boomi.container.restartOnTooManyOpenFilesError": [
                label: "Auto Restart on Too Many Files Error",
                defaultValue: "TRUE",
                exclude: false
            ],
            "com.boomi.container.script.type.restart": [
                label: "Customized Restart Script File Name",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.processExecMode": [
                label: "Execute Processes as Forked JVMs",
                defaultValue: "MULTI_THREAD",
                exclude: false
            ],
            "com.boomi.container.httpsProtocols": [
                label: "HTTPS Protocols",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.flowControl.maxUnitCount": [
                label: "Maximum Flow Control Units",
                defaultValue: "10",
                exclude: false
            ],
            "com.boomi.container.purge.numPurgeThreads": [
                label: "Purge Manager Threads",
                defaultValue: "1",
                exclude: false
            ],
            "com.boomi.container.numSyncScheduleThreads": [
                label: "Threads for Atom Scheduling",
                defaultValue: "5",
                exclude: false
            ],
            "com.boomi.container.cloudlet.numSyncClusterThreads": [
                label: "Threads for Atom to Atom Messages",
                defaultValue: "3 (Molecule)",
                exclude: false
            ],
            "com.boomi.container.localDir": [
                label: "Working Data Local Storage Directory",
                defaultValue: "",
                exclude: false
            ],
            "com.boomi.container.sharedServer.queue.maxTaskThreadPoolSize": [
                label: "Atom Queue - Maximum Thread Number",
                defaultValue: "250",
                exclude: false
            ],
            "com.boomi.container.sharedServer.queue.memoryUsagePercent": [
                label: "Atom Queue - Maximum Memory Allocated (%)",
                defaultValue: "25",
                exclude: false
            ],"com.boomi.container.pendingShutdownWarnTime": [
                label: "Atom Pending Shutdown Delay",
                defaultValue: "0",
                exclude: false
            ]
        ]
        if ( containerPropertiesRef[key] ) {
            label = containerPropertiesRef[key].label
            defaultValue = containerPropertiesRef[key].defaultValue
            exclude = containerPropertiesRef[key].exclude
        }
        else {
            label = key;
            defaultValue = "";
            exclude = false
        }
    }

    public getLabel() {
        return label;
    }

    public getDefaultValue() {
        return defaultValue;
    }

    public isExcluded() {
        return exclude;
    }
}

public class AtomVMOptions {
    private String label;
    private String defaultValue;
    private boolean exclude;
    public AtomVMOptions(key){
        def atomVMOptionsRef = [
            "-Dsun.net.http.retryPost": [
                label: "Retry HTTP Post",
                defaultValue: "false",
                exclude: false
            ],
            "-Djava.io.tmpdir": [
                label: "Temporary Directory",
                defaultValue: "",
                exclude: false
            ],
            "-Dcom.sun.management.jmxremote.port": [
                label: "JMX Remote Port",
                defaultValue: "5002",
                exclude: false
            ],
            "-Dcom.sun.management.jmxremote.rmi.port": [
                label: "JMX Remote RMI Port",
                defaultValue: "5002",
                exclude: false
            ],
            "-Dsun.net.client.defaultConnectTimeout": [
                label: "Client Default Connect Timeout",
                defaultValue: "120000",
                exclude: false
            ],
            "-Dsun.net.client.defaultReadTimeout": [
                label: "Client Default Read Timeout",
                defaultValue: "120000",
                exclude: false
            ],
            "-Djava.endorsed.dirs": [
                label: "Endorsed Directories",
                defaultValue: "<java-home>/lib/endorsed",
                exclude: true
            ],
            "-Dcom.boomi.container.securityCompatibility": [
                label: "Java Security Compatibility",
                defaultValue: "2019.01",
                exclude: true
            ]
        ]
        if ( atomVMOptionsRef[key] ) {
            label = atomVMOptionsRef[key].label
            defaultValue = atomVMOptionsRef[key].defaultValue
            exclude = atomVMOptionsRef[key].exclude
        }
        else {
            label = key;
            defaultValue = "";
            exclude = false
        }
    }

    public getLabel() {
        return label;
    }

    public getDefaultValue() {
        return defaultValue;
    }

    public isExcluded() {
        return exclude;
    }
}



