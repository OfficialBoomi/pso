package Boomi.GroovyDebugger
import Boomi.GroovyDebugger.Context.*

/*
STEP 1: Update the Config() function with details as per requirements:
    - pathFiles - folder path where files are to be loaded that would be on the pipe in Boomi
    - listDDP - add additional Dynamic Document Property to Hashmaps and add to list. Each doc can have multiple DDPs

STEP 2: Add your scripting to BoomiGroovyScript()
*/

class BoomiGroovyDebugger {

    private static String pathFiles;
    private static List<HashMap<String, String>> listDDP = new ArrayList<HashMap<String, String>>();
    private static ContextCreator dataContext;
    private static ExecutionUtil ExecutionUtil;
    private static Logger logger;
    private static String newline = System.getProperty("line.separator");

    //add configuration details as per requirements
    private static void Config(){
        //Path to your documents to be loaded into context (emulate documents on the flow).
        pathFiles ="C:\\Work\\Other\\Boomi Debugger\\Boomi Groovy Debugger Input Files\\";

        //add multiple Dynamic Process Properties for each document
        HashMap<String, String> hpDoc1 = new HashMap();
        hpDoc1.put("dppDoc1Test1","dppDoc1Test1Value");
        hpDoc1.put("dppDoc1Test2","dppDoc1Test2Value");
        listDDP.add(hpDoc1);

        HashMap<String, String> hpDoc2 = new HashMap();
        hpDoc1.put("dppDoc2Test1","dppDoc2Test1Value");
        listDDP.add(hpDoc2);
    }

    // your custom scripting that would usually reside in a Data Process Shape
    private static void BoomiGroovyScript(){

        def xs = new XmlSlurper();

        // main loop in ProcessData shape
        for( int i = 0; i < dataContext.getDataCount(); i++ ) {
            InputStream is = dataContext.getStream(i);
            Properties props = dataContext.getProperties(i);

            String doc = is.getText();
            def xsDoc = xs.parseText(doc);

            xsDoc.book.eachWithIndex{ bk, idx ->
                logger.info("$idx - $bk.title by $bk.author ( $bk.genre )\n");
            }

            def myBook = xsDoc.book.find { it-> it.title == "XML Developer's Guide"  }

            //logger.info(">>>> $myBook.title\n");

            myBook.title = "Something else";

            logger.info(">>>> $myBook.title\\n");
            logger.info(groovy.xml.XmlUtil.serialize(xsDoc));

            is = new ByteArrayInputStream(groovy.xml.XmlUtil.serialize(xsDoc).getBytes('UTF-8'));
            dataContext.storeStream(is, props);
        }

    }

    private static void Startup(){
        //setup ddp
        Config();

        dataContext = new ContextCreator();
        ExecutionUtil = new ExecutionUtil();
        logger = ExecutionUtil.getBaseLogger();

        // load files to streams
        dataContext.AddFiles(pathFiles);

        //create empty property objects
        dataContext.createEmptyProperties(dataContext.getDataCount());

        //create properties per stream;
        for( int i = 0; i < dataContext.getDataCount(); i++ ) {
            if(listDDP.size() > 0) {
                HashMap<String, String> docDDPs = listDDP.get(0);
                for (String ddpKey : docDDPs.keySet()) {
                    dataContext.addPropertyValues(i, ddpKey, docDDPs.get(ddpKey));
                }
            }
        }

    }

    static void main(String[] args) {
        Startup();
        BoomiGroovyScript();
    }
}


