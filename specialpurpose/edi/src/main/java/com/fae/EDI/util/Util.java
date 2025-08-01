package com.fae.EDI.util;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.ofbiz.base.util.Debug;

import com.fae.EDI.beans.Fichero;
import com.fae.EDI.beans.Position;
import com.fae.EDI.beans.conf.Configuration;
import com.fae.EDI.beans.conf.PartnerProperty;

public final class Util {

	static String EMAILER_WARNING_RECIPIENTS = "gabriel.gueto@eu.fujikura.com";
				
	public static String getConfigurationValue(List<Configuration> aConf, String sKey){
		
		String sValue = "";
		Debug.log("=======================INI getConfigurationValue: "+sKey);
		for (int i = 0; i < aConf.size(); i++) { 
			if (aConf.get(i).getsCfgName().equalsIgnoreCase(sKey)){
				sValue = aConf.get(i).getsCfgValue();
				break;
			}
		}
		
		Debug.log("=======================FIN getConfigurationValue: ("+aConf.size()+ ") " +sKey + "->" + sValue);
		
		return sValue;
	}	
	
	public static String getPartnerConfigurationValue(List<PartnerProperty> aPartnerConf, String sKey){	
		String sValue = "";
		Debug.log("=======================INI getPartnerConfigurationValue: "+sKey);
		for (int i = 0; i < aPartnerConf.size(); i++) { 
			if (aPartnerConf.get(i).getPptProperty().equalsIgnoreCase(sKey)){
				sValue = aPartnerConf.get(i).getPptValue();
				break;
			}
		}
		
		Debug.log("=======================FIN getPartnerConfigurationValue: ("+aPartnerConf.size()+ ") " +sKey + "->" + sValue);
		
		return sValue;
	}	
	
	@SuppressWarnings("finally")
	public static String getCurrentTimeString(String timeZone) {
		String string_date = "";
		Date date = new Date();
		
		try {			
			string_date = dateToString(date,"dd/MM/yyyy kk:mm:ss",timeZone);			
		} catch (Exception e) {
			Debug.log("Error parsing current date", e);
		} finally {			
			return string_date;
		}
	}
	
	public static Timestamp getCurrentTimestamp() {	   
        //method 1
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //System.out.println(timestamp);

        /*
        //method 2 - via Date
        Date date = new Date();
        System.out.println(new Timestamp(date.getTime()));

        //return number of milliseconds since January 1, 1970, 00:00:00 GMT
        System.out.println(timestamp.getTime());

        //format timestamp
        System.out.println(sdf.format(timestamp));
        */
        
        return timestamp;
    }	
	
    public static String repeatChar(String c,int nLong){
      	String sResult = "";
      	for(int i=1;i<=nLong;i++){  
      		sResult = sResult + c; 
      	}
      	 
      	return sResult;
      }
		
	public static String dateToString(Date date,String format,String timeZone) {	
		String s_date = "";
			
		try{
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone)); //PropertyContainer.TIMEZONE_TIMEZONE)); //"EET"));
			s_date = sdf.format(date);
		}  catch (Exception e){
			s_date = null;	
		}
		
		return s_date;
	}
	
    public static String lastCharDir(String directory,Boolean lSlash, String sSlash){
  	  String dir 		= directory;
  	  String lastChr 	= dir.substring(directory.length() - 1);
  	  //String sSlash 	= "/";
  	  
  	Debug.log("=======================INI lastCharDir:");
  	  if (lSlash){
  		  // Si no acaba lo anadimos
  		  if (!lastChr.equalsIgnoreCase(sSlash)){
  			  dir = dir + sSlash;
  		  } 		  
  	  }else{
			  // Si acaba lo quitamos
			  if (lastChr.equalsIgnoreCase(sSlash)){
				  dir = dir.substring(0, dir.length() - 1);
			  }       		      		  
  	  }
  	  
  	Debug.log("=======================FIN lastCharDir:");
  	  return dir;
    }	
	
	public static String stack2string(Exception e) {
		  try {
		    StringWriter sw = new StringWriter();
		    PrintWriter pw = new PrintWriter(sw);
		    e.printStackTrace(pw);
		    return "------\r\n" + sw.toString() + "------\r\n";
		  }
		  catch(Exception e2) {
		    return "bad stack2string";
		  }
		  		  		  
	}
	
	public static boolean isNullOrBlank(String s)
	{
	  return (s==null || s.trim().equals(""));
	}
	
    public static void sendEmail(String aFromEmailAddr, String aSubject, String aBody, Boolean attachFile, String fileName) {
     	Emailer emailer = new Emailer();
 		String body = aBody;
 		String subject = aSubject;
 		
 		// Mail footer
 		body = body + "\n\r" + "This is an automated e-mail, please do not answer to this mail.";     			
 		
 		// For every recipient an e-mail is sent
 		for(String recipient : convertStringToArray(EMAILER_WARNING_RECIPIENTS, ",")) {
 			if (isNullOrBlank(fileName)){
 				emailer.sendEmail(aFromEmailAddr, recipient, subject, body);
 			}else{
 				if (attachFile){
 					emailer.sendEmailwAttach(aFromEmailAddr, recipient, subject, body, fileName);
 				}else{
 					emailer.sendEmail(aFromEmailAddr, recipient, subject, body);
 				}
 			} 									
 		}	    	
     } 	
    
	/**
	 * Converts passed string into an array. Splits it with the value of the second parameter.
	 * 
	 * @param string_to_convert		String to be converted
	 * @param separator 			String delimiter of the values
	 * @return						List of values
	 */
	public static List<String> convertStringToArray(String string_to_convert, String separator){
		ArrayList<String> list = new ArrayList<String>();
		String[] string_vector;
		
		string_vector = string_to_convert.split(separator);
		
		for(String bucle : string_vector) {
			list.add(bucle);
		}
		
		return list;
	}    
	
	public static ArrayList<Fichero> filewalker( String path, String patternString, ArrayList<Fichero> ficheros ) {

		String sSkipDir = "enviado";
        File root = new File( path );
        File[] list = root.listFiles();
        //ArrayList<Fichero> ficheros = new ArrayList<Fichero>();
        //Fichero fich = new Fichero();

        if (list == null) return ficheros;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	filewalker( f.getAbsolutePath(), patternString, ficheros );
            	//Debug.log("==================Filewalke: Dir:" + f.getAbsoluteFile() );
            }
            else {
            	String lastDir =  f.getParentFile().getName();

            	Pattern p = Pattern.compile(".*INVOIC.*",Pattern.CASE_INSENSITIVE);
            	Matcher m = p.matcher(f.getName());
            	
            	Pattern p1 = Pattern.compile(".*\\.TXT",Pattern.CASE_INSENSITIVE);
            	Matcher m1 = p.matcher(f.getName());

            	//Debug.log("==================Filewalke: File:" + lastDir + " " + f.getName() +" "+patternString + " " + m.matches() + " " +m1.matches()+" " + m.find()  + " " + f.getName().matches(patternString)+" "+!lastDir.equalsIgnoreCase(sSkipDir));            	            	
            	//if (f.getName().matches(patternString) && !lastDir.equalsIgnoreCase(sSkipDir)){
            	if (m.matches() && m1.matches() && !lastDir.equalsIgnoreCase(sSkipDir)){	
            		Debug.log("==================ENCONTRADO: File:" + f.getName() +" "+patternString );
	                String absolutePath = f.getAbsolutePath();
	                String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));                                	                	
	                Fichero fich = new Fichero();
	                fich.setDirectory(filePath);
	                fich.setName(f.getName());
	                ficheros.add(fich);	               
            	}                
            }
        }        		
        return ficheros;
    }	

/* TODO
    public static Boolean checkSaiFile(String fileName, String INVOICESAI_CONFIGURATION){
 	   Boolean lOk = true;     	   
 	   InputStream stream = null;
 	   List<Position> aSaiPositions = new ArrayList<Position>();
 	   int nMaxCab = 0;
 	   int nMaxLin = 0;
 	   int nMaxRes = 0;    	   
 	   int nCab = 0;
 	   int nLin = 0;
 	   int nRes = 0;
 	   int nExcepcionCabecera = 46;
 	   
 	   try{     		      		 
 		  //stream = new FileInputStream(PropertyContainer.INVOICESAI_CONFIGURATION); //"mapping/invoiceSai.txt");
 		  stream = loader.getResourceAsStream(INVOICESAI_CONFIGURATION);
			  String jsonString = Util.convertStreamToString(stream);		  
			  
			  ObjectMapper mapper = new ObjectMapper();		 
			  Positions aJSONPositions = mapper.readValue(jsonString,  Positions.class);
			  aSaiPositions = aJSONPositions.getPositions();
			  
			  for (int i = 0; i < aSaiPositions.size(); i++) {   
				 if (aSaiPositions.get(i).getType().equalsIgnoreCase("CAB")){
					 if ( nMaxCab < aSaiPositions.get(i).getFinalPos() ){
						 nMaxCab = aSaiPositions.get(i).getFinalPos();
					 }	 
				 }
				 if (aSaiPositions.get(i).getType().equalsIgnoreCase("LIN")){
					 if ( nMaxLin < aSaiPositions.get(i).getFinalPos() ){
						nMaxLin = aSaiPositions.get(i).getFinalPos();
					 }	 
				 }
				 if (aSaiPositions.get(i).getType().equalsIgnoreCase("RES")){
					 if ( nMaxRes < aSaiPositions.get(i).getFinalPos() ){
						nMaxRes = aSaiPositions.get(i).getFinalPos();
					 }	 
				 } 				 
			  }
			  
			  //System.out.println(String.valueOf(nMaxCab)+" "+String.valueOf(nMaxLin)+" "+String.valueOf(nMaxRes));    		  
			  
  		  stream = new FileInputStream(fileName); //"Fichero_SAI.txt");		   		    			   		    			   
 		  String str = null;		    
 		  BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		  if (stream != null) {   
 			   while ((str = reader.readLine()) != null) {  
 				   //System.out.println(str);
	    			   String typeLine = str.substring(0,3);
	    			   if (typeLine.equalsIgnoreCase("CAB")){
	    				   nCab = nCab + 1;
	    				   //System.out.println("CAB:"+String.valueOf(str.length() + 46));
	    				   if (str.length() != nMaxCab && (str.length() + nExcepcionCabecera) != nMaxCab){
	    					   lOk = false;
	    					   logger.error("checkSaiFile: Error en longitud cabecera. "+fileName);
	    					   break;
	    				   }
	    			   }else if (typeLine.equalsIgnoreCase("LIN")){
	    				   nLin = nLin + 1;   	    				   
	    				   if (str.length() != nMaxLin){
	    					   lOk = false;
	    					   logger.error("checkSaiFile: Error en longitud linea. "+fileName);
	    					   break;
	    				   }	    				   
	    			   }else if (typeLine.equalsIgnoreCase("RES")){
	    				   nRes = nRes + 1;   
	    				   //System.out.println("RES:"+String.valueOf(str.length()));
	    				   if (str.length() != nMaxRes){
	    					   lOk = false;
	    					   logger.error("checkSaiFile: Error en longitud resumen. "+fileName);
	    					   break;
	    				   }
	    			   }	    	    			   
 			   }
 		  }
 		  
 		   reader.close();
 	   }catch (Exception e){
 		   lOk = false;
 		   logger.error("checkSaiFile: Excepcion. "+fileName+ " "+Util.stack2string(e));
 	   }
 	   
 	   if ( nCab == 0 || nLin == 0 || nRes == 0){
 		   lOk = false;
 		   logger.error("checkSaiFile: Error falta algun segmento. "+fileName);
 	   }
 	   
 	   //System.out.println(String.valueOf(nCab)+" "+String.valueOf(nLin)+" "+String.valueOf(nRes)+" "+lOK);
 	   
 	   return lOk;
   }	
*/    
	public static void loggerAndArray(String logType, String sTexto, ArrayList<String> aResultado) {
		Debug.log(logType + " " + sTexto);
			
		aResultado.add(sTexto);
	}    
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void orderPosition(List<Position> positions) {

        Collections.sort(positions, new Comparator() {

            public int compare(Object o1, Object o2) {

                String s1 = ((Position) o1).getType();
                String s2 = ((Position) o2).getType();
                int sComp = s1.compareTo(s2);

                if (sComp != 0) {
                   return sComp;
                } else {
                   Integer x1 = ((Position) o1).getInitialPos();
                   Integer x2 = ((Position) o2).getInitialPos();
                   return x1.compareTo(x2);
                }
        }});
    }    	
    
	public static String getSubString(String str, int initialPos, int finalPos){	
		String salida;
		
		initialPos = initialPos - 1;		
		
		str = str.substring(0, str.length()-1);
		if ((finalPos) > str.length() && initialPos <= str.length()){
			finalPos = str.length();
		}
		
		try{
			salida = str.substring(initialPos,finalPos);
		} catch (Exception ex) {
			salida = "";
			//System.out.println("getSubString Error ("+initialPos+","+finalPos+"/"+str.length()+"): "+ex.getMessage());
			Debug.log("=======getSubString Error ("+initialPos+","+finalPos+"/"+str.length()+"): "+ex.getMessage());
		}		
		
		return salida;
	}    
	
	public static void invokeSetter(Object obj, String variableName, Object variableValue){
	      /* variableValue is Object because value can be an Object, Integer, String, etc... */
	      try {
	        /**
	         * Get object of PropertyDescriptor using variable name and class
	         * Note: To use PropertyDescriptor on any field/variable, the field must have both `Setter` and `Getter` method.
	         */
	    	 PropertyDescriptor objPropertyDescriptor = new PropertyDescriptor(variableName, obj.getClass());
	         /* Set field/variable value using getWriteMethod() */
	         objPropertyDescriptor.getWriteMethod().invoke(obj, variableValue);
	      } catch (Exception e) {
	        /* Java 8: Multiple exception in one catch. Use Different catch block for lower version. */
	    	  System.out.println(e.getMessage());
	    	  e.printStackTrace();
	      }
	   }	
	
	@SuppressWarnings("unused")
	private static void invokeGetter(Object obj, String variableName){
	      try {
	        /**
	         * Get object of PropertyDescriptor using variable name and class
	         * Note: To use PropertyDescriptor on any field/variable, the field must have both `Setter` and `Getter` method.
	         */
	         PropertyDescriptor objPropertyDescriptor = new PropertyDescriptor(variableName, obj.getClass());
	        /**
	         * Get field/variable value using getReadMethod()
	         * variableValue is Object because value can be an Object, Integer, String, etc...
	         */
	         Object variableValue = objPropertyDescriptor.getReadMethod().invoke(obj);
	        /* Print value of variable */
	         System.out.println(variableValue);
	      } catch (Exception e) {
	       /* Java 8: Multiple exception in one catch. Use Different catch block for lower version. */
	    	  System.out.println(e.getMessage());
	      }
	   }		
	
	static public String formatDecimal(String str, int position){
		  String newStr;
		  		  
		  //System.out.print(str.length()+" "+(position +1));
		  str =str.replace(".", "");
		  
		  if (str.length() > position +1){
			  newStr = str.replaceFirst ("^0*", "");  // Quita ceros x la izda			  
			  if (newStr == null || newStr.equalsIgnoreCase("")){
				  //newStr="0000";
				  for ( int i=0;i < position+1;i++) {
					  newStr = newStr + "0";
				  }
			  }
		  }else{
			  newStr = str;
		  }
		  newStr = new StringBuilder(newStr).insert(newStr.length()-position, ".").toString();
		  // Sincronization
		  //newStr = new StringBuffer(newStr).insert(newStr.length()-position, ".").toString();
		  
		  return newStr;
	}
	
	static public String todayDateAsString(){
		return new SimpleDateFormat("yyMMdd").format(new Date());
	}
	
	static public String todayTimeAsString(){
		return new SimpleDateFormat("HHmm").format(new Date());
	}
	
	static public String splitIban(String cCadena,String solicitud){
		String cSalida = null;
		try{			
			String cPais              = cCadena.substring(0,2);
			String cPaisDigitoControl = cCadena.substring(2,4);
			String cBanco             = cCadena.substring(4,8);
			String cSucursal          = cCadena.substring(8,12); 
			String cDc                = cCadena.substring(12,14);
			String cCta               = cCadena.substring(14,24);
	        
			if(solicitud.equalsIgnoreCase("PAIS")){
				cSalida = cPais;
			}else if(solicitud.equalsIgnoreCase("FULLPAIS")){	
				cSalida = cPais+cPaisDigitoControl;
			}else if(solicitud.equalsIgnoreCase("BANCO")){
				cSalida = cBanco;			
			}else if(solicitud.equalsIgnoreCase("SUCURSAL")){
				cSalida = cSucursal;			
			}else if(solicitud.equalsIgnoreCase("FULLBANCO")){
				cSalida = cBanco + cSucursal;	
			}else if(solicitud.equalsIgnoreCase("DC")){
				cSalida = cDc;				
			}else if(solicitud.equalsIgnoreCase("CTA")){
				cSalida = cCta;				
			}else{
				cSalida = cCadena;
			}
		}catch(Exception e){
			System.out.println("Error en splitIban (asumiendo nulo): "+ e.getMessage() + " " +stack2string(e));
			cSalida = null;
		}
				
		return cSalida;
	}
	
	static public BigDecimal bigDecimaTax(BigDecimal base, BigDecimal percent, int roundDecimals, Boolean quitSeparartor){
		BigDecimal tax = new BigDecimal(0);
		
		//System.out.println("Base: "+ base.toString()+ " Percent: "+ percent.toString() + "Round: "+ roundDecimals);
		//System.out.println(percent.divide(new BigDecimal(100),2,RoundingMode.HALF_UP).add(new BigDecimal(1)));
		
		tax = ((percent.divide(new BigDecimal(100),roundDecimals,RoundingMode.HALF_UP).add(new BigDecimal(1))).multiply(base)).subtract(base);
		
		if (quitSeparartor){
			tax = new BigDecimal(tax.toString().replace(".", ""));
		}
		
		//System.out.println("Base: "+ base.toString()+ " Percent: "+ percent.toString()+ " Tax: "+tax + "Round: "+ roundDecimals);
		
		return tax; //.setScale(roundDecimals, RoundingMode.CEILING);
	}	
	
	public static String strRenameFileExtension(String source, String newExtension){
	    String target;
	    String currentExtension = getFileExtension(source);

	    if (currentExtension.equals("")){
	      target = source + "." + newExtension;
	    }
	    else {
	      target = source.replaceFirst(Pattern.quote("." +
	          currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));

	    }
	    return target;
	  }		
	
	  public static String getFileExtension(String f) {
		    String ext = "";
		    int i = f.lastIndexOf('.');
		    if (i > 0 &&  i < f.length() - 1) {
		      ext = f.substring(i + 1);
		    }
		    return ext;
		  }	
	  
	    public static int stringToFile(String strContent,String fName, Boolean lineToLine){
	    	int status = 0; 
	    	BufferedWriter bufferedWriter = null;
	        try {            
	            File myFile = new File(fName);
	            // check if file exist, otherwise create the file before writing
	            if (!myFile.exists()) {
	                myFile.createNewFile();
	            }                                
	            Writer writer = new FileWriter(myFile);
	            bufferedWriter = new BufferedWriter(writer);
	            
	            if (lineToLine){                        
		            Scanner scanner = new Scanner(strContent.replace("\\n","").replaceAll("\n\r",""));
		            while (scanner.hasNextLine()) {
		              String line = scanner.nextLine();              
		              bufferedWriter.write(line);              
		              if (scanner.hasNextLine()){ 
		            	 bufferedWriter.newLine();
		              }
		              // process the line
		            }
		            scanner.close();
	            }else{            	
	    			bufferedWriter.write(strContent);
	            }
	            
	            // bufferedWriter.write(strContent);              
	        } catch (IOException e) {
	            e.printStackTrace();
	            status = 1;
	        } finally{
	            try{
	                if(bufferedWriter != null) bufferedWriter.close();
	            } catch(Exception ex){
	                 
	            }
	        }    	
	        
	        return status;
	    }	  
	    
	      public static Boolean checkEdiFile(String fileName,String segmentoInicio,String segmentoFinal,String INTERCHANGE41_INTERCHANGEDELIMITERS_SEGMENT){
	    	  Boolean lOk = true;     	   
	   	   	  InputStream stream = null;
	   	      String segmento = "";
	   	   	  int i = 0;
	   	   	  try{   
		    	  stream = new FileInputStream(fileName); //"Fichero_SAI.txt");		   		    			   		    			   
				  String str = null;		    
				  BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				  if (stream != null) {   
					   while ((str = reader.readLine()) != null) {
						   i = i + 1;
						   str = str.trim();
						   if (!isNullOrBlank(str)){
							   segmento = str.substring(0,3);
							   if (i == 1){						  
								  if (!segmento.equals(segmentoInicio)){
									  lOk = false; 
									  Debug.log("checkEdiFile: Error en segmentoInicio. "+fileName);
									  break;
								  }
							   }
							   //System.out.println(str);
							   
							   if ( !(str.substring(str.length() - 1).equalsIgnoreCase(INTERCHANGE41_INTERCHANGEDELIMITERS_SEGMENT)) ){
								   lOk = false; 
								   Debug.log("checkEdiFile: Error en separador de segmento. "+fileName+ " Separador:"+INTERCHANGE41_INTERCHANGEDELIMITERS_SEGMENT);
								   break;
							   }	
					   	   }
					   }
					   if (!segmento.equals(segmentoFinal) && lOk){
							  lOk = false;
							  Debug.log("checkEdiFile: Error en segmentoFinal. "+fileName);
					   }
				  /*}else{
					  lOk = false;
					  logger.error("checkEdiFile: Error de stream. "+fileName);*/
				  }
				  reader.close();
	   	   	  }catch (Exception e){
	   	   		  lOk = false;
	   	   		  //System.out.println(Util.stack2string(e));
	   	   		  Debug.log("checkEdiFile: Excepcion. "+fileName+ " "+Util.stack2string(e));
	   	   	  }
			
	   	   	  return lOk;
	      }	    
	      
	 public static String subStrProtected(String cadena,int longitud){	     
	      final String value;
	      
	      if (cadena == null || cadena.length() <= 0) {
	          value = "_";
	      } else if (cadena.length() <= longitud) {
	          value = cadena;
	      } else { 
	          value = cadena.substring(0, longitud);
	      }
	     	      
	      return value;
	 }
}
