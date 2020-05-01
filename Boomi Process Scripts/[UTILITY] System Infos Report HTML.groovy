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
                        td(rowspan:"4", class:"category","General")
                        td(class:"label","Name")
                        td(class:"value",object.atomName)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Runtime Type")
                        td(class:"value",object.runtimeType)
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Number of View Files")
                        td(class:"value",object.viewFilesCount)
                        td(class:"value","")
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
                    tr{
                        td(class:"label","Force Restart After X Minutes")
                        td(class:"value","0")
                        td(class:"value",object.containerProperties["com.boomi.container.forceRestart"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Purge History After X Days")
                        td(class:"value","30")
                        td(class:"value",object.containerProperties["com.boomi.container.purgeDays"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Purge Data Immediately")
                        td(class:"value","false")
                        td(class:"value",object.containerProperties["com.boomi.container.purgeImmediately"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Atom Data Directory Level")
                        td(class:"value","0")
                        td(class:"value",object.containerProperties["com.boomi.container.dataDirNestLevel"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Process Execution Directory Level")
                        td(class:"value","0")
                        td(class:"value",object.containerProperties["com.boomi.container.executionDirNestLevel"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Cluster Network Transport Type")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.cloudlet.clusterConfig"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Initial Host for Unicast")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.cloudlet.initialHosts"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Maximum Simultaneous Executions per Node")
                        td(class:"value","Unlimited")
                        td(class:"value",object.containerProperties["com.boomi.container.maxRunningExecutions"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Maximum Queued Executions per Node")
                        td(class:"value","200")
                        td(class:"value",object.containerProperties["com.boomi.container.maxQueuedExecutions"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Rolling Restart Cluster Restart Percentage")
                        td(class:"value","20")
                        td(class:"value",object.containerProperties["com.boomi.container.cluster.rollingRestart.clusterRestartPercentage"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Web Server - Maximum Number of Threads")
                        td(class:"value","250")
                        td(class:"value",object.containerProperties["com.boomi.container.sharedServer.http.maxConnectionThreadPoolSize"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Maximum Forked Execution Time in Cloud")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.maxExecutionTime"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Auto Restart on Out Of Memory")
                        td(class:"value","TRUE")
                        td(class:"value",object.containerProperties["com.boomi.container.resource.restartOnOutOfMemoryError"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Auto Restart on Too Many Files Error")
                        td(class:"value","TRUE")
                        td(class:"value",object.containerProperties["com.boomi.container.restartOnTooManyOpenFilesError"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Customized Restart Script File Name")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.script.type.restart"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Execute Processes as Forked JVMs")
                        td(class:"value","MULTI_THREAD")
                        td(class:"value",object.containerProperties["com.boomi.container.processExecMode"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","HTTPS Protocols")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.httpsProtocols"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Maximum Flow Control Units")
                        td(class:"value","10")
                        td(class:"value",object.containerProperties["com.boomi.container.flowControl.maxUnitCount"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Purge Manager Threads")
                        td(class:"value","1")
                        td(class:"value",object.containerProperties["com.boomi.container.purge.numPurgeThreads"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Threads for Atom Scheduling")
                        td(class:"value","5")
                        td(class:"value",object.containerProperties["com.boomi.container.numSyncScheduleThreads"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Threads for Atom to Atom Messages")
                        td(class:"value","3 (Molecule)")
                        td(class:"value",object.containerProperties["com.boomi.container.cloudlet.numSyncClusterThreads"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Working Data Local Storage Directory")
                        td(class:"value","")
                        td(class:"value",object.containerProperties["com.boomi.container.localDir"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Atom Queue - Maximum Thread Number")
                        td(class:"value","250")
                        td(class:"value",object.containerProperties["com.boomi.container.sharedServer.queue.maxTaskThreadPoolSize"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Atom Queue - Maximum Memory Allocated (%)")
                        td(class:"value","25")
                        td(class:"value",object.containerProperties["com.boomi.container.sharedServer.queue.memoryUsagePercent"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Atom Pending Shutdown Delay")
                        td(class:"value","0")
                        td(class:"value",object.containerProperties["com.boomi.container.pendingShutdownWarnTime"])
                        td(class:"value","")
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
                    tr{
                        td(class:"label","Retry HTTP Post")
                        td(class:"value","false")
                        td(class:"value",object.atomProperties.system["-Dsun.net.http.retryPost"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","Temporary Directory")
                        td(class:"value","")
                        td(class:"value",object.atomProperties.system["-Djava.io.tmpdir"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","JMX Remote Port")
                        td(class:"value","5002")
                        td(class:"value",object.atomProperties.system["-Dcom.sun.management.jmxremote.port"])
                        td(class:"value","")
                    }
                    tr{
                        td(class:"label","JMX Remote RMI Port")
                        td(class:"value","5002")
                        td(class:"value",object.atomProperties.system["-Dcom.sun.management.jmxremote.rmi.port"])
                        td(class:"value","")
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
            }
        }
        isHTML = new ByteArrayInputStream( sw.toString().getBytes( 'UTF-8' ) )
        dataContext.storeStream(isHTML, props);
    }
}



