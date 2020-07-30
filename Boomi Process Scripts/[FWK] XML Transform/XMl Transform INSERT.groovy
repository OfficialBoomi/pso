/*
------------------------------------------------------------
Purpose:    Part of the XMl Transform Services suite - INSERT - Extracts XML element based on XPath match to
            named Dyanmic Document Property
Input(s):
            1.  DDP_FWK_XMLExtract_XPath - Dynamic Document Property to set the XPath reference to match and insert according to mode setting
            2.  DDP_FWK_XMLInsert_Mode - Defines the method to use for insersion of XML
                    BEFORE      Insert BEFORE XML element/s matched by XPath (DDP_FWK_XMLExtract_XPath)
                    AFTER       Insert AFTER XML element/s matched by XPath
                    INTO_LAST   Insert INTO the XML element/s matched by XPath as the LAST element
                    INTO_FIRST  Insert INTO the XML element/s matched by XPath as the FIRST element
            2. DDP_FWK_XMLExtract_DDPName - The name of the Dynamic Document Property to retreive the XML from for insertion
            3. Documents on the flow - with inserted XML from DDP_FWK_XMLExtract_DDPName

Output(s):
            1. Documents on the flow
            2. DDP_FWK_XMLInsert_Error - Dynamic Document Property - only set on error
Note(s):    Works in conjuction with rest of the XML Transform Services
Authour:    Tristan Margot (tristan_margot@dell.com)
------------------------------------------------------------
 */

import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException

import org.xml.sax.InputSource;

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
            InputStream is = dataContext.getStream(i);
            Properties props = dataContext.getProperties(i);

    try{

        DocumentBuilderFactory dbfPipe = DocumentBuilderFactory.newInstance();
        DocumentBuilder dbPipe = dbfPipe.newDocumentBuilder();
        Document docPipe = dbPipe.parse(is);

        def logger = ExecutionUtil.getBaseLogger();

        String newline = System.getProperty("line.separator");
        String stDDPExtractName = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_DDPName");
        String stDDPExtractXML = props.getProperty("document.dynamic.userdefined." + stDDPExtractName);
        String stDDPMode = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_Mode");
        String stDDPInsertXPath = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_XPath");
        List<String> modes = new ArrayList<String>();
        modes.add("BEFORE");
        modes.add("AFTER");
        modes.add("INTO_LAST");
        modes.add("INTO_FIRST");

        if(isEmpty(stDDPExtractName)){
            throw new IllegalArgumentException("DDP_FWK_XMLInsert_DDPName dynamic document property was not found");
        }

        if(isEmpty(stDDPExtractXML)){
            throw new IllegalArgumentException(stDDPExtractName + " dynamic document property was not found");
        }

        if(isEmpty(stDDPInsertXPath)){
            throw new IllegalArgumentException("DDP_FWK_XMLInsert_XPath dynamic document property was not found");
        }

        if(!isEmpty(stDDPMode)){
            if(!modes.contains(stDDPMode)){
                throw new IllegalArgumentException("DDP_FWK_XMLInsert_Mode set to invalid mode " + stDDPMode);
            }
        }else{
            stDDPMode = "INTO_LAST";
        }
        
        Document docDDP = StringToDocument(stDDPExtractXML);
        Element rootDDP = (Element) docDDP.getDocumentElement();
        Element rootPipe = (Element) docPipe.getDocumentElement();
        NodeList nlLineItems = XPathSearch(stDDPInsertXPath,docPipe);

        for(int y=0; y<nlLineItems.getLength(); y++){
            Node nlLineItem = nlLineItems.item(y);
            Node nImport = docPipe.importNode(rootDDP, true);

            Node nlLineItemParent = nlLineItem.getParentNode();

            //nlLineItem == rootPipe - is XPath == root element
            if( nlLineItemParent instanceof Document) {
                throw new IllegalArgumentException("Mode ["+stDDPMode+"] not allowed with current XPath selected root element. Consider INTO_FIRST or INTO_LAST mode instead.");
            }
           
            switch (stDDPMode){
                case "BEFORE":
                    nlLineItemParent.insertBefore(nImport, nlLineItem);
                    break;
                case "AFTER":
                    nlLineItemParent.appendChild(nImport);
                    break;
                case "INTO_LAST":
                        nlLineItem.appendChild(nImport);
                    break;
                case "INTO_FIRST":
                    nlLineItem.insertBefore(nImport, nlLineItem.getFirstChild() );
                    break;
            }

        }

        logger.info(DocumentToString(docPipe));
        is = DocumentToInputStream(docPipe);

    }catch( TransformerException ex){
        LogException(ex);
        props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_Error", ex.getMessage());
    }catch(DOMException ex){
        LogException(ex);
        props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_Error", ex.getMessage());
    }catch(IllegalArgumentException ex){
        LogException(ex);
        props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_Error", ex.getMessage());
    }catch(Exception ex){
        LogException(ex);
        props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLInsert_Error", ex.getMessage());
    }
    
    dataContext.storeStream(is, props);
}


    private static String NodeListToString(NodeList nl){
        for (int i = 0; null != nl  && i < nl.getLength(); i++) {

            Node elem = nl.item(i);//Your Node
            StringWriter buf = new StringWriter();
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // optional
            xform.setOutputProperty(OutputKeys.INDENT, "yes"); // optional
            xform.transform(new DOMSource(elem), new StreamResult(buf));
            return buf.toString(); // your string

        }
    }

    private static NodeList XPathSearch(String xPathStr, Document doc) throws XPathExpressionException{
        XPathFactory xfact = XPathFactory.newInstance();
        XPath xPath = xfact.newXPath();
        XPathExpression xPathExpr = xPath.compile(xPathStr);
        NodeList nl = (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);
        return nl;
    }

    private static InputStream DocumentToInputStream(Document doc) throws TransformerException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
        return is;
    }

    private static Document StringToDocument(String st) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(st));
        return db.parse(is);
    }

    private static String DocumentToString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private static InputStream StringToInputStream(String inputString){
        InputStream targetStream = new ByteArrayInputStream(inputString.getBytes());
        return targetStream;
    }

    private static isEmpty(st){
        return st == null || st.isEmpty();
    }

    private static <T extends Exception> String LogException(T e){
        def logger = ExecutionUtil.getBaseLogger();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.info(sw.toString());
        return sw.toString();
    }

    public class ExceptionType<T> {
        private T ex;

        public T get() {
            return this.ex;
        }

        public void set(T e) {
            this.ex = e;
        }
    }
