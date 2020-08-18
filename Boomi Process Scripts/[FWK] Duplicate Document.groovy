/**
*	Description:	Duplicates document on the flow by a specified amount decalred in DDP_FWK_Duplicate_Quantity
*	Input:			DDP_FWK_Duplicate_Quantity	-	The number of duplicates required. Default: 1 (No duplicates)
*					Document on the flow
*	Output:			Duplicated Documents on the flow
*					DDP_FWK_Duplicate_Index - The duplicate index e.g. first duplicate is index 1, second is 2, etc. Can be used to route by index.
* 					DDP_FWK_Duplicate_Error - Populated by error message on error else not set. 
*	Author:			Tristan Margot (tristan_margot@dell.com)
**/

import java.util.Properties;
import java.io.InputStream;
import com.boomi.execution.ExecutionUtil;

int numberDuplicates = 1;

for( int i = 0; i < dataContext.getDataCount(); i++ ) {
    InputStream is = dataContext.getStream(i);
    Properties props = dataContext.getProperties(i);
    String ddpQuantity = props.getProperty("document.dynamic.userdefined.DDP_FWK_Duplicate_Quantity");
	ddpQuantity = ddpQuantity != null && !ddpQuantity.isEmpty() ? ddpQuantity : "1";
	int tmpNumberDuplicates = tryParseInteger(ddpQuantity);	
	if(tmpNumberDuplicates == null){
		props.setProperty("document.dynamic.userdefined.DDP_FWK_Duplicate_Error", "DDP_FWK_Duplicate_Quantity value is not a parseabke integer value");				
	}else{
		numberDuplicates = tmpNumberDuplicates;
	}
	
    for(int x=0; x< numberDuplicates; x++){
        is.reset();
        props.setProperty("document.dynamic.userdefined.DDP_FWK_Duplicate_Index", (x+1).toString());
        dataContext.storeStream(is, props);    
    }    
}

static Integer tryParseInteger(String str) {
	Integer out = null;
    try {
        out = Integer.parseInt(str);
        return out;
    } catch (Exception e){}
    return out;
}