
package app;

/** 
* Created on 2006. 01. 20. 
* 
* To change the template for this generated file go to 
* Window - Preferences - Java - Code Generation - Code and Comments 
*/ 

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties; 

/**  
* @author kangteok 
* 
* To change the template for this generated type comment go to 
* Window - Preferences - Java - Code Generation - Code and Comments 
*/ 
public class ConfigFactory {  

        public static String getProperties(String prop_key) { 
        	String rtn_prop = null;
	        Properties props = new Properties(); 
	        InputStream fip = null;
	        ClassLoader cl;
            
            try{
            	cl = Thread.currentThread().getContextClassLoader();
                if( cl == null ){
                    cl = ClassLoader.getSystemClassLoader();
                }
                
                fip = cl.getResourceAsStream("jdbc.properties");
	        	
                props.load(fip);
	        	fip.close();
	        	fip = null;
		        
	        	rtn_prop =  props.getProperty(prop_key);
	        	props = null;
	        	
	        }catch(IOException e){
	        	e.printStackTrace();
	        	return null;
	        }catch(Exception e){
	        	e.printStackTrace();
	        	return null;
	        }        
	        
	        return rtn_prop;
        
        }//end of getProperties method()
} //end of ConfigFactory class
