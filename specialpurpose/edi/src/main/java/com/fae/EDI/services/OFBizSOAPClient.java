package com.fae.EDI.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class copy from CuttingKaizen project 
 *
 */
public class OFBizSOAPClient {
 	private static Logger logger = LoggerFactory.getLogger(OFBizSOAPClient.class);

	private OMFactory factory;
	private OMNamespace omNs;
	private String URL;
	private Map<String,String> ofbizParameters;
	
	public OFBizSOAPClient(String URL, Map<String, String> params) {
		this.URL = URL;
		ofbizParameters = params;
		factory = OMAbstractFactory.getOMFactory();
		omNs = factory.createOMNamespace("http://ofbiz.apache.org/service/", "ns1");
	}

	private OMElement createMapEntry(String key, String val) {
		 
		OMElement mapEntry = factory.createOMElement("map-Entry", omNs);
		 
		// create the key entry
		OMElement mapKey = factory.createOMElement("map-Key", omNs);
		OMElement keyElement = factory.createOMElement("std-String", omNs);
		OMAttribute keyAttribute = factory.createOMAttribute("value", null, key);
		mapKey.addChild(keyElement);
		keyElement.addAttribute(keyAttribute);
		 
		// create the value entry
		OMElement mapValue = factory.createOMElement("map-Value", omNs);
		OMElement valElement = factory.createOMElement("std-String", omNs);
		OMAttribute valAttribute = factory.createOMAttribute("value", null, val);
		mapValue.addChild(valElement);
		valElement.addAttribute(valAttribute);
		 
		// attach to map-Entry
		mapEntry.addChild(mapKey);
		mapEntry.addChild(mapValue);
		 
		return mapEntry;
	}
		
	private void unmarshall_map_entry(Map<String, Object> map, OMElement mapEntry)
	{
		OMElement mapKey = (mapEntry.getChildrenWithLocalName("map-Key").hasNext()) ? (OMElement)mapEntry.getChildrenWithLocalName("map-Key").next() : null;		
		if (mapKey != null)
		{
			OMElement keyElement = (mapKey.getChildrenWithLocalName("std-String").hasNext()) ? (OMElement)mapKey.getChildrenWithLocalName("std-String").next() : null;
			
			String key = null;
			if (keyElement != null) {
				key = keyElement.getAttributeValue(new QName("value"));
			}
			
			Object objValue = null;	
			OMElement mapValue = (mapEntry.getChildrenWithLocalName("map-Value").hasNext()) ? (OMElement)mapEntry.getChildrenWithLocalName("map-Value").next() : null;
			if (mapValue != null)
			{		
				Iterator<?> itValue = mapValue.getChildElements();
				while (itValue.hasNext())
				{
					OMElement mapVal = (OMElement)itValue.next();
					String typeData = mapVal.getLocalName();
					switch (typeData)
					{
						case "std-String":
							objValue = mapVal.getAttributeValue(new QName("value"));
							break;
						case "cus-obj":
							// serialized object as String
							objValue = mapVal.getText();
							break;
						case "col-ArrayList":
							List<String> lsValues = new ArrayList<>();
							Iterator<?> itArrayValues = mapVal.getChildElements();
							while (itArrayValues.hasNext())
							{
								OMElement mapArrayVal = (OMElement)itArrayValues.next();
								lsValues.add(mapArrayVal.getAttributeValue(new QName("value")));
							}
							objValue = lsValues;
							break;
						case "map-HashMap":
							Map<String, Object> mapChildren = new ConcurrentHashMap<String, Object>();
							Iterator<?> mapEntries = mapVal.getChildrenWithName(new QName("map-Entry"));
							while(mapEntries.hasNext())
							{
								OMElement mapEntryChildren = (OMElement)mapEntries.next();
								unmarshall_map_entry(mapChildren, mapEntryChildren);
							}
							objValue = mapChildren;
							break;
					}
				}
			}
			
			if (key == null || objValue == null ) {
				logger.warn("Found some field null key: {} value: {}", key, objValue);
			} else {
				map.put(key, objValue);
			}
			
		}					
	}

	private Object unmarshall(OMElement item) {
		logger.info("Type : {} - {}" , item.getType(), item.getLocalName() );
				
		while (item.getChildElements().hasNext())
		{
			OMElement itemChild = (OMElement)item.getChildElements().next();
			String type = itemChild.getLocalName();
			switch (type)
			{
				case "map-HashMap":
					Map<String, Object> map = new ConcurrentHashMap<>();
					Iterator<?> mapEntries = itemChild.getChildrenWithName(new QName("map-Entry"));
					while(mapEntries.hasNext())
					{
						OMElement mapEntry = (OMElement)mapEntries.next();
						unmarshall_map_entry(map, mapEntry);
					}
					
					return map;
					
				case "std-String":
					logger.info("Value--> {}", item.getAttributeValue(new QName("value")));

				default:
					return null;
			}
		}
		
		return null;
	}

	
	/**
	 * Main call to ofbiz webService (default display=true)
	 * 
	 * @param method
	 * @param arguments
	 * @return Map<String, Object>
	 */
	public Map<String, Object> call(String method, Map<String, Object> arguments) {
		return call(method, arguments, true);
	}
	
	/**
	 * Main call to ofbiz webService
	 * 
	 * @param method
	 * @param arguments
	 * @param displayCall true: print xml webservice call and result, false not
	 * @return Map<String, Object>
	 */
	public Map<String, Object> call(String method, Map<String, Object> arguments, boolean displayCall) {

		//logger.info("Calling SOAP WS {} at {}", method, URL);
		logger.info("Calling SOAP WS " + method + " at " + URL);
						
		ServiceClient sc;
		try {
			sc = new ServiceClient();
		} catch (AxisFault e1) {
			//logger.error("Unable to create SOAP Client for web service {}", method ,e1);
			logger.error("Unable to create SOAP Client for web service " + method + "Error: " + e1.getMessage());
			return null;
		}
		
		Options opts = new Options();
      	opts.setTo(new EndpointReference(URL));
		opts.setAction(method);
		int timeOutInMilliSeconds = 500000;
		opts.setTimeOutInMilliSeconds(timeOutInMilliSeconds);
		opts.setProperty(HTTPConstants.SO_TIMEOUT, timeOutInMilliSeconds);
		opts.setProperty(HTTPConstants.CONNECTION_TIMEOUT, timeOutInMilliSeconds);
		sc.setOverrideOptions(opts);
		
		OMElement payload = factory.createOMElement(method, omNs);
		OMElement mapMap = factory.createOMElement("map-Map", omNs);
		
		for( Map.Entry<String, String> entry : ofbizParameters.entrySet() ) {
			mapMap.addChild( createMapEntry(entry.getKey(), entry.getValue()) );
		}
		
		for( Map.Entry<String, Object> entry : arguments.entrySet() ) {
			mapMap.addChild( createMapEntry(entry.getKey(), (String)entry.getValue()) );				
		}

		payload.addChild(mapMap);
		
		Map<String, Object> map = new HashMap<String, Object>();
      	OMElement res;
		try {
			if (displayCall)
				//logger.info("CALLING SOAP WS {}", method);
			    logger.info("CALLING SOAP WS " + method);
			res = sc.sendReceive(payload);
			if (displayCall)
				//logger.info("SOAP WS {} Result {}", method, res);
			    logger.info("SOAP WS " + method + " Result " + res);
			map = (Map<String, Object>)unmarshall(res);
		} catch (AxisFault e1) {
			//logger.error("Error calling SOAP web service {}", method, e1);
			logger.error("Error calling SOAP web service " + method + " Error:"+ e1.getMessage());
			return null;
		}
						
		return map;
	}
}