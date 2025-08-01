package com.fae.EDI.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.fae.EDI.util.FAENumericUtil;
import com.fae.EDI.util.FAEStringUtil;

/**
 * 
 * Ofbiz service to create EDIMessage records (with MD5 validations and such)
 *
 */
public class EDISequenceCreateService {

	private static final String module = EDISequenceCreateService.class.getName();
	
	/**
	 * 
	 * @param dctx
	 * @param context
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> jServiceGetNextSeqId(DispatchContext dctx, Map<String, ? extends Object> context) {		
		Map<String, Object> result = null;
		long nextSeqId = -1;
		
		String seqName = (String) context.get("seqName");
		Debug.log("=============================Debug===== jServiceGetNextSeqId seqName:"+seqName, module);

		try {
			
			Debug.log("=============================Debug===== jServiceGetNextSeqId", module);
			
			// Get Next Sequence
			Delegator delegator = dctx.getDelegator();
			nextSeqId = delegator.getNextSeqIdLong(seqName);
			
			String responseMsg = "Successfully. NextId for Sequence Name " + seqName + ":" + nextSeqId;			
			result = ServiceUtil.returnSuccess(responseMsg);	 			
			result.put("nextSeqId", String.valueOf(nextSeqId));			
			
			Debug.log("=============================Debug===== " + responseMsg, module);

		} catch (Exception e) {	
			String messageError = "Error getting nextId for Sequence Name: " + seqName + " with error: " + e.getMessage();
			Debug.log(messageError, module);
			Debug.log(e, module);
			result = ServiceUtil.returnError(messageError);
		}

		return result;
	}
	
    private final static String methodServiceGetNextSeqIdWS = "serviceGetNextSeqId";
    private final static String url = "http://localhost:8080/webtools/control/SOAPService/"; 
    
    @SuppressWarnings("unchecked")
    public static Map<String, String> getServiceGetNextSeqId(){
        Map<String, String> mapListStoppedReasons = new HashMap<String, String>();
                       
        try
        {                              
               Map<String, Object> mapListStoppedReasonsWS = new HashMap<String, Object>();
               OFBizSOAPClient objWS = new OFBizSOAPClient(url, new HashMap<String, String>());
               Map<String, Object> argsWS = new HashMap<String, Object>(); 
               argsWS.put("seqName", "prueba");
               Map<String, Object> objResult = objWS.call(methodServiceGetNextSeqIdWS, argsWS);                              
               if (objResult != null)
               {
                               if (objResult.containsKey("responseMessage"))
                               {
                                               if (objResult.get("responseMessage").toString().equals("success"))
                                               {
                                                               if (objResult.containsKey("returnListStoppedReasons"))
                                                               {
                                                                              mapListStoppedReasonsWS = (Map<String, Object>)objResult.get("returnListStoppedReasons");
                                                                              for(Map.Entry<String, Object> objParameterValue : mapListStoppedReasonsWS.entrySet())
                                                                              {
                                                                                              mapListStoppedReasons.put(objParameterValue.getKey(), (String)objParameterValue.getValue());                                                                                              
                                                                              }
                                                               }
                                               }
                               }
               }
        }
        catch (Exception e)
        {
                       System.out.println("[StoppedReasonService] Error in - GetListStoppedReasons->  " + e.getMessage());
        }
        
        return mapListStoppedReasons;
    }
    
    public static Map<String, Object> getNextSeqIdPrueba(DispatchContext dctx, Map<String, ? extends Object> context){
    	String serverURL="http://localhost:8080/webtools/control/SOAPService/";
    	String seqName="prueba";
    	String prefix="";
    	
    	Map<String, Object> result = null;
    	
    	try {
	    	long resultL = getNextSeqId(serverURL, seqName, prefix);	    		    		    	
	    	String responseMsg = "Record getNextSeqIdPrueba next seq:" + resultL;
	        result = ServiceUtil.returnSuccess(responseMsg);
    	 } catch (Exception e) {
         	Debug.log("Error in getNextSeqIdPrueba entity ........" +module);         	
            return ServiceUtil.returnError("Error in getNextSeqIdPrueba ........" +module);
         }        
    	
    	return result;
    }
  
	public static long getNextSeqId(String serverURL, String seqName, String prefix){
		
		long nextSeqId = -1;
		String service = "serviceGetNextSeqId";
		try{
			if (!FAEStringUtil.isNullOrBlank(serverURL)){
				// Call service				
				OFBizSOAPClient objWS = new OFBizSOAPClient(serverURL, new HashMap<String, String>());
				Map<String, Object> argsWS = new HashMap<String, Object>();
				if (FAEStringUtil.isNullOrBlank(prefix)){
					argsWS.put("seqName", seqName);
				}else{
					argsWS.put("seqName", prefix + seqName);
				}
			    Map<String, Object> objResult = objWS.call(service, argsWS);
			    if (objResult != null){			    					    	
			    	if (objResult.containsKey("responseMessage")){
			    		if (objResult.containsKey("nextSeqId")) {	
			    			nextSeqId = FAENumericUtil.parseLong((String) objResult.get("nextSeqId"),new Long(-1));
			    		}
			    	}
			    }
			}
		}catch (Exception e){
			System.out.println("FAESequenceUtil getSequence error: {} stack: {}" + e.getMessage());
			return -1;
		}
		
		if (nextSeqId == -1){
			String messageWarn = "Error searching next sequence serverURL: " + serverURL + " service: " + service + " seqName: " + seqName + ". Job aborted.";
			System.out.println(messageWarn);
			//throw new EDIMessageDiscardException(messageWarn);									
		}else{
			System.out.println("Next value for sequence {} {}" +  (prefix + seqName) + " " +  nextSeqId);			
		}		
		
		return nextSeqId;
	}			
		
	
	
}
