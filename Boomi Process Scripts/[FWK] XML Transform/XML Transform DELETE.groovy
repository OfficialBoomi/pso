/*
------------------------------------------------------------
Purpose:    Part of the XMl Transform Services suite - DELETE - Removes XML element based on XPath match
Input(s):
            1. DDP_FWK_XMLDelete_XPath - Dynamic Document Property to set the XPath reference to match
            2. Documents on the flow

Output(s):
            1. Documents on the flow
            2. DDP_FWK_XMLDelete_Error - Dynamic Document Property - only set on error
Note(s):    Works in conjuction with rest of the XML Transform Services
Authour:    Tristan Margot (tristan_margot@dell.com)
------------------------------------------------------------
 */
import java.util.Properties;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

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
import org.w3c.dom.Node;
import org.w3c.dom.DOMException

import org.xml.sax.InputSource;

import com.boomi.execution.ExecutionUtil;
def logger = ExecutionUtil.getBaseLogger();

try{
    for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    
        InputStream is = dataContext.getStream(i);
        Properties props = dataContext.getProperties(i);
        
        try{
            
            String stXPath = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLDelete_XPath");
            logger.info(stXPath);
            if(isEmpty(stXPath)){
                throw new IllegalArgumentException("DDP_FWK_XMLDelete_XPath dynamic document property was not found");
            }
    
            DocumentBuilderFactory dbfPipe = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbPipe = dbfPipe.newDocumentBuilder();
            Document docPipe = dbPipe.parse(is); 
        
            NodeList nlLineItems = XPathSearch(stXPath,docPipe);     
            
                                                                             
            for(int y=0; y<nlLineItems.getLength(); y++) {
                Node nlLineItem = nlLineItems.item(y);
                nlLineItem.getParentNode().removeChild(nlLineItem);                    
            }    
            
            logger.info(DocumentToString(docPipe));
            is = DocumentToInputStream(docPipe);
                                        
        }catch( TransformerException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLDelete_Error", ex.getMessage()); 
        }catch(DOMException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLDelete_Error", ex.getMessage()); 
        }catch(IllegalArgumentException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLDelete_Error", ex.getMessage()); 
        }catch(Exception ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLDelete_Error", ex.getMessage()); 
        }
    
        
        dataContext.storeStream(is, props);
    }
} catch (Exception ex) {
        LogException(ex);
        throw new Exception(ex.getMessage() + "\nCheck process log for stack trace.");
}

private static isEmpty(st){
    return st == null || st.isEmpty();
}


public static NodeList XPathSearch(String xPathStr, Document doc) throws XPathExpressionException{
    XPathFactory xfact = XPathFactory.newInstance();
    XPath xPath = xfact.newXPath();                  
    XPathExpression xPathExpr = xPath.compile(xPathStr);               
    NodeList nl = (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);
    return nl;
}

public static Document StringToDocument(String st) throws Exception {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(st));
    return db.parse(is);
}

public static String DocumentToString(Document doc) throws Exception {
    
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
   
}
    
private static InputStream DocumentToInputStream(Document doc) throws TransformerException{
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Source xmlSource = new DOMSource(doc);
    Result outputTarget = new StreamResult(outputStream);
    TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
    InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
    return is;
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
