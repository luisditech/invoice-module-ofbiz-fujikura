package com.fae.EDI.services;

import java.sql.Timestamp;
import java.util.Map;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.transaction.GenericTransactionException;
import org.apache.ofbiz.entity.transaction.TransactionUtil;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.fae.EDI.util.Util;

public class ConfigurationServices {

	public static final String module = ConfigurationServices.class.getName();
	 
    public static Map<String, Object> jCreateEdiConfiguration(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = null; //ServiceUtil.returnSuccess();
             
        Debug.log("=============================Debug===== Inicio jCreateEdiConfiguration");
        
        Delegator delegator = dctx.getDelegator();
        try {
        	        	        	
        	// Buscar valores repetidos
        	String value = null; 
            for (Map.Entry<String, ? extends Object> entry : context.entrySet()) {
                String key = entry.getKey();
                if (key.equalsIgnoreCase("cfgName")){
                	value = entry.getValue().toString();
                }
                //String value = entry.getValue().toString();
                //Debug.log("--------------------Key: "+ key + " ------Value: "+value);
            }
            
            Debug.log("--------------------Value: "+value);
            EntityCondition cond = EntityCondition.makeCondition(EntityCondition.makeCondition("cfgName", value));
            long contador = delegator.findCountByCondition("EdiConfiguration", cond, null, null);
            Debug.log("--------------------Registros EdiConfiguration:"+ contador);            
            if (contador>0){
            	return ServiceUtil.returnError("EdiConfiguration: This record already exists: " + value);
            }
            // Fin de comprobacion
        	
            GenericValue ediConfiguration = delegator.makeValue("EdiConfiguration");
            // Auto generating next sequence of ofbizDemoId primary key
            ediConfiguration.setNextSeqId();
            
            ediConfiguration.setPKFields(context);
            //String cfgName = (String)context.get("cfgName");
            //Debug.log("=============================Debug=====cfgName: "+cfgName);            
            //ediConfiguration.set("cfgName", cfgName);
                        
            // Setting up all non primary key field values from context map
            ediConfiguration.setNonPKFields(context);

            // Creating record in database for OfbizDemo entity for prepared value
            ediConfiguration = delegator.create(ediConfiguration);
                                    
            String responseMsg = "Record EdiConfiguration created with ID: " + ediConfiguration.getString("cfgName");
            result = ServiceUtil.returnSuccess(responseMsg);
            
            result.put("cfgId", ediConfiguration.getString("cfgId"));
            Debug.log("=============================Debug===== Record EdiConfiguration created successfully with ofbizDemoId: "+ediConfiguration.getString("cfgName"));
                        
        } catch (GenericEntityException e) {
        	Debug.log("Error in creating record in EdiConfiguration entity ........" +module);
        	result = ServiceUtil.returnError("Error in creating record in EdiConfiguration entity");
            return ServiceUtil.returnError("Error in creating record in EdiConfiguration entity ........" +module);
        }
        
        return result;
    }

    public static Map<String, Object> jUpdateEdiConfiguration(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = null; //ServiceUtil.returnSuccess();
        
    	String cfgName = (String)context.get("cfgName");   	
    	String cfgId = (String)context.get("cfgId");   
		Debug.log("--------------------CONTEXT: cfgName:"+cfgName);
        
        Delegator delegator = dctx.getDelegator();
        try {        	                       	
        	// Buscar la modificaciones
            GenericValue EdiConfiguration = delegator.findOne("EdiConfiguration", false, "cfgId", cfgId);            
            Map<String, Object> gvMap = EdiConfiguration.getAllFields();
                                    
            //Debug.log("**************************INICIO BBDD: "+gvMap.size());
            TransactionUtil.begin();
            for (Map.Entry<String, Object> entryOld : gvMap.entrySet()) {
            	String keyOld = entryOld.getKey();
            	String valueOld = entryOld.getValue().toString();
            	String ObjectTypeOld = entryOld.getValue().getClass().getName();
            	//Debug.log("**************************Key: " + keyOld +" Value:"+ valueOld + " Type:"+ObjectTypeOld);
            	
            	// Busco en el map de modificaciones
            	for (Map.Entry<String, ? extends Object> entryNew : context.entrySet()) {
                	String keyNew = entryNew.getKey();
                	String valueNew = entryNew.getValue().toString();
                	String ObjectTypeNew = entryNew.getValue().getClass().getName();
                	if (keyNew.equalsIgnoreCase(keyOld) && !valueNew.equalsIgnoreCase(valueOld)){
                		//Debug.log("========================= CAMBIO : Key: "+ keyNew + " OldValue: "+valueOld+ " NewValue: "+valueNew+ " Type:"+ObjectTypeOld);
                		Debug.log("=====SETKEY:"+keyNew +" --> "+ valueNew);
                		if (ObjectTypeOld.equalsIgnoreCase("java.sql.Timestamp")){
                			EdiConfiguration.set(keyNew, Timestamp.valueOf(valueNew));                			
                		}else if (ObjectTypeOld.equalsIgnoreCase("java.lang.Long")){
                			EdiConfiguration.set(keyNew, Long.parseLong(valueNew)); 
                		}else{
                			EdiConfiguration.set(keyNew, valueNew);
                		}
                	}
                	//Debug.log("++++++++++++++++++++++++++++Key: "+ keyNew + " Value: "+valueNew);
                }
            	
            }
            EdiConfiguration.store();
            TransactionUtil.commit();
                                          
            String responseMsg = "Record EdiConfiguration updated with ID: " + cfgName;            
            result = ServiceUtil.returnSuccess(responseMsg);        
            Debug.log("=============================Debug: "+ responseMsg);
        } catch (Exception e) {
        	Debug.log("==============jUpdateEdiConfiguration: "+Util.stack2string(e));
        	try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
				Debug.log("========================Error deleting record in EdiConfiguration entity: "+Util.stack2string(e1));
				result = ServiceUtil.returnError("Error deleting record in EdiConfiguration entity: "+e1.getMessage());
			}
        	Debug.log("Error in updating record in EdiConfiguration entity ........" +module);
        	result = ServiceUtil.returnError("Error in updating record in EdiConfiguration entity: "+e.getMessage());
        	e.printStackTrace();
            return ServiceUtil.returnError("Error in updating record in EdiConfiguration entity ........" +module);
        }        
                        
        return result;
    }           
                
    public static Map<String, Object> jDeleteEdiConfiguration(DispatchContext dctx, Map<String, ? extends Object> context) {
            Map<String, Object> result = null; //ServiceUtil.returnSuccess();        
    	String cfgName = (String)context.get("cfgName");
    	String cfgId = (String)context.get("cfgId");
		
		Debug.log("--------------------CONTEXT jDeleteEdiConfiguration: cfgName:"+cfgName);
        
        Delegator delegator = dctx.getDelegator();
        try {     
        	TransactionUtil.begin();
            GenericValue historyType = delegator.findOne("EdiConfiguration", false, "cfgId", cfgId);
            historyType.remove();
            TransactionUtil.commit();  
            
            String responseMsg = "Record deleted with ID: " + cfgId;
            result = ServiceUtil.returnSuccess(responseMsg);
                      
            Debug.log("=============================Debug====="+"EdiConfiguration Record deleted with ID: " + cfgName);
            //return ServiceUtil.returnError("Not my product"); 
            //return ServiceUtil.returnSuccess();
            
        } catch (Exception e) {
        	Debug.log("==============jDeleteEdiConfiguration: "+Util.stack2string(e));
        	try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				e1.printStackTrace();
				result = ServiceUtil.returnError("Error deleting record in EdiConfiguration entity "+module);
				Debug.log("Error deleting record in EdiConfiguration entity "+module+ ": " + Util.stack2string(e));
			}
        	Debug.log("Error in deleting record in EdiConfiguration entity ........" +module+ ": " + Util.stack2string(e));
        	result = ServiceUtil.returnError("Error deleting record in EdiConfiguration entity: " + module);
        	//e.printStackTrace();
            return ServiceUtil.returnError("Error deleting record in EdiConfiguration entity ........" +module);
        }        
        
        return result;
    }        
    
}
