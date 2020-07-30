/*
------------------------------------------------------------
Purpose:    Part of the XMl Transform Services suite - REMOVE CDATA - Removes all CDATA tag in document and attempts to replace illegal XML (non-CDATA) characters

Input(s):   Documents on the flow

Output(s):
            1. Documents on the flow
            2. DDP_FWK_XMLCDATA_Error - Dynamic Document Property - only set on error
Note(s):    Works in conjuction with rest of the XML Transform Services
Authour:    Tristan Margot (tristan_margot@dell.com)
------------------------------------------------------------
 */

import java.util.Properties;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.CharacterData;

import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;
import com.boomi.execution.ExecutionUtil;
import java.util.logging.Logger;
Logger logger = ExecutionUtil.getBaseLogger();


try{
    for( int i = 0; i < dataContext.getDataCount(); i++ ) {
        InputStream is = dataContext.getStream(i);
        Properties props = dataContext.getProperties(i);
        
        try{
            
            if(is.available()){
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document document = docBuilder.parse(is);
                processNode(document.getDocumentElement()); //call recursive function
                is = DocumentToInputStream(document);
            }
            
        }catch( TransformerException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLCDATA_Error", ex.getMessage()); 
        }catch(DOMException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLCDATA_Error", ex.getMessage()); 
        }catch(Exception ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLCDATA_Error", ex.getMessage()); 
        }
        
        dataContext.storeStream(is, props);
    }

} catch (Exception ex) {
        LogException(ex);
        throw new Exception(ex.getMessage() + "\nCheck process log for stack trace.");
}

private static String replaceSpecialChars(String st){
    
    String[][] charReplace = [
        ["<",">","\"","\'","&"],
        ["&lt;","&gt;","&quot;","&apos;","&amp;"]
    ];

    for(int c = 0; c<charReplace[0].length; c++){
        st = st.replaceAll(charReplace[1][c], charReplace[0][c]);
    }
    
    return st;
}

private static InputStream DocumentToInputStream(Document doc) throws TransformerException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
        return is;
    }

//recursive function
public static void processNode(Node node) throws DOMException {
    String nodeName = node.getNodeName();
    NodeList childnl = node.getChildNodes();
    for (int x = 0; x < childnl.getLength(); x++) {
        Node childNode = childnl.item(x);
        if (childNode.getNodeType() == Node.CDATA_SECTION_NODE) {
            String cdataContent = replaceSpecialChars(childNode.getData());
        	node.removeChild(childNode);
        	node.setTextContent(cdataContent); //by default escapes illegal chars with special chars
        }
    }
    
    // recursive call block
    NodeList nodeList = node.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
        Node currentNode = nodeList.item(i);
        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
            processNode(currentNode);
        }
    }
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
