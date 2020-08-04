/*
------------------------------------------------------------
Purpose:    Part of the XMl Transform Services suite - EXTRACT - Extracts XML element based on XPath match to 
            named Dyanmic Document Property
Input(s):
            1.  DDP_FWK_XMLExtract_XPath - Dynamic Document Property to set the XPath reference to match and extract
            2.  DDP_FWK_XMLExtract_DDPName - The name of the Dynamic Document Property to create and store the extracted XML
            3. Documents on the flow

Output(s):
            1. Documents on the flow
            2. DDP_FWK_XMLExtract_Error - Dynamic Document Property - only set on error
Note(s):    Works in conjuction with rest of the XML Transform Services
Authour:    Tristan Margot (tristan_margot@dell.com)
------------------------------------------------------------
 */

import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

try{
    for( int i = 0; i < dataContext.getDataCount(); i++ ) {
        
        InputStream is = dataContext.getStream(i);
        Properties props = dataContext.getProperties(i);
    
        try{
            //XPatch Working - Extract node to string
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);            
        
            XPathFactory xfact = XPathFactory.newInstance();
            XPath xPath = xfact.newXPath();                  
            //String xPathStr = "//*[local-name()='header']"; 
            String stXPath = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_XPath");
            String stDDPName = props.getProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_DDPName");
           
            if(isEmpty(stXPath)){
                throw new IllegalArgumentException("DDP_FWK_XMLExtract_XPath dynamic document property was not found");
            }
            
            if(isEmpty(stDDPName)){
                 throw new IllegalArgumentException("DDP_FWK_XMLExtract_DDPName dynamic document property was not found");
            }
            
            XPathExpression xPathExpr = xPath.compile(stXPath);               
            NodeList nl = (NodeList) xPathExpr.evaluate(doc, XPathConstants.NODESET);
            
            String stNL = nodeListToString(nl); //save this to ddp
            // Store the new property value
            props.setProperty("document.dynamic.userdefined."+stDDPName, stNL);           
            is = dataContext.getStream(i);   
            
        }catch( TransformerException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_Error", ex.getMessage()); 
        }catch(DOMException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_Error", ex.getMessage()); 
        }catch(IllegalArgumentException ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_Error", ex.getMessage()); 
        }catch(Exception ex){
            LogException(ex);
            props.setProperty("document.dynamic.userdefined.DDP_FWK_XMLExtract_Error", ex.getMessage()); 
        }
        
        dataContext.storeStream(is, props);
    }
    
} catch (Exception ex) {
        LogException(ex);
        throw new Exception(ex.getMessage() + "\nCheck process log for stack trace.");
}

private static String nodeListToString(NodeList nodes) {
    DOMSource source = new DOMSource();
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    for (int i = 0; i < nodes.getLength(); ++i) {
        source.setNode(nodes.item(i));
        transformer.transform(source, result);
    }
    
    return writer.toString();
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

