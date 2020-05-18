/*
------------------------------------------------------------
    Purpose: Generate Type 4 UUIDs using native Java utility
   Input(s): none
  Output(s): DPP_FWK_UUID (Dynamic Process Property)
             DDP_FWK_UUID (Dynamic Document Property)
    Note(s): Script echos input document(s)
------------------------------------------------------------
*/

import com.boomi.execution.ExecutionUtil;
import java.util.logging.Logger;
import java.util.UUID;
Logger logger = ExecutionUtil.getBaseLogger();

//PROCESS LEVEL (Dynamic Process Property DPP_FWK_UUID)
String DPP_FWK_UUID = UUID.randomUUID().toString();
ExecutionUtil.setDynamicProcessProperty("DPP_FWK_UUID", DPP_FWK_UUID, false);
logger.info("DPP_FWK_UUID: " + DPP_FWK_UUID);

//echo input documents and properties
for( int i = 0; i < dataContext.getDataCount(); i++ ) {
  InputStream is = dataContext.getStream(i);
  Properties props = dataContext.getProperties(i);

  //DOCUMENT LEVEL (Dynamic Document Property DDP_FWK_UUID)
  String DDP_FWK_UUID = UUID.randomUUID().toString();
  props.setProperty("document.dynamic.userdefined.DDP_FWK_UUID", DDP_FWK_UUID);
  logger.info("DDP_FWK_UUID: " + DDP_FWK_UUID);

  dataContext.storeStream(is, props);
}
