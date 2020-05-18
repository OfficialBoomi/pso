import java.util.Properties;
import java.io.InputStream;
import java.net.URLEncoder;
import com.boomi.execution.ExecutionUtil;
import java.util.logging.Logger;
import java.time.*
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;

//Script computes SAS token for Azure Service Bus authentication
//Inputs: Process Property with resourceUri, sasKeyName and sasKey defined
//Output: authentication token stored in Dynamic Process Property "DPP_FWK_SAS_TOKEN"
//Note: ttl is hardcoded to 60 seconds

Logger logger = ExecutionUtil.getBaseLogger();
String resourceUri = ExecutionUtil.getProcessProperty("<<PROCESS_PROP_COMPONENT_ID>>", "<<PROCESS_PROP_KEY_ID (resourceUri)>>");
logger.info("resourceUri: " + resourceUri);
String sasKeyName = ExecutionUtil.getProcessProperty("<<PROCESS_PROP_COMPONENT_ID>>", "<<PROCESS_PROP_KEY_ID (sasKeyName)>>");
logger.info("sasKeyName: " + sasKeyName);
String sasKey = ExecutionUtil.getProcessProperty("<<PROCESS_PROP_COMPONENT_ID>>", "<<PROCESS_PROP_KEY_ID (sasKey)>>");
String encodedUri = URLEncoder.encode(resourceUri, "UTF-8");

Instant instant = Instant.now();
long ttl = instant.getEpochSecond() +60;
logger.info("ttl: " + ttl);
String signature = encodedUri + "\n" + ttl;

def hash = hmac_sha256(sasKey, signature)
String encodedHash = hash.encodeBase64().toString()
String token = 'SharedAccessSignature sr=' + encodedUri + '&sig=' + URLEncoder.encode(encodedHash, "UTF-8") + '&se=' + ttl + '&skn=' + sasKeyName;
logger.info("token: " + token);

ExecutionUtil.setDynamicProcessProperty("DPP_FWK_SAS_TOKEN", "token",false);

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);

    dataContext.storeStream(is, props);
}

def hmac_sha256(String sasKey, String signature) {
 try {
    Mac mac = Mac.getInstance("HmacSHA256")
    SecretKeySpec secretKeySpec = new SecretKeySpec(sasKey.getBytes(), "HmacSHA256")
    mac.init(secretKeySpec)
    byte[] digest = mac.doFinal(signature.getBytes())
    return digest
   } catch (InvalidKeyException e) {
    throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
  }
}