package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Instances;

/**
 * Utility class for reading various data types  from property files
 */
public class PropertiesReader {
	/**
	 * Reads an int value from the properties
	 * 
	 * @param properties the given properties
	 * @param key key for the value to be retrieved.
	 * @return the retrieved int value
	 * @throws Exception if the parameter under the given key is missing or if it didn't contain the value that could be cast as int 	 
	 */
	public static int readInt(Properties properties, String key) throws Exception{
		String value = properties.getProperty(key);
		if (value == null) {
			throw new Exception("ERROR: the parameter: " + key + " is missing");					
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new Exception("ERROR: paramter " + key + "with value " + value + " can not be cast to int", e);
		}
	}
	
	/**
	 * Reads a double value from the properties
	 * 
	 * @param properties the given properties
	 * @param key key for the value to be retrieved
	 * @return the retrieved double value
	 * @throws Exception if the parameter under the given key is missing or if it didn't contain the value that could be cast as double 	 
	 */
	public static double readDoubleParam(Properties properties, String key) throws Exception{
		String value = properties.getProperty(key);
		if (value == null) {
			throw new Exception("ERROR: the parameter: " + key + " is missing");	
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new Exception("ERROR: paramter " + key + "with value " + value + " can not be cast to double", e);
		}
	}

	
	/**
	 * Reads String value from properties
	 * 
	 * @param properties the given properties
	 * @param key key for the value to be retrieved
	 * @return the retrieved String value
	 * @throws Exception if the parameter under the given key is missing 	 
	 */	
	public static String readStringParam(Properties properties,	String key) throws Exception {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new Exception("ERROR: the parameter: " + key + " is missing");
		}
		if (value.equals("")){
//			System.out.println("WARNING: the parameter: " + key + " has no specified value (empty string). Using null");
			return null;
		}
		return value;
	}

	
	/**
	 * Reads boolean value from properties.
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved.
	 * @return the retrieved boolean value
	 * @throws Exception if the parameter under the given key is missing or if it didn't contain the value that could be cast as boolean 		 
	 */	
	public static boolean readBooleanParam(Properties properties, String key) throws Exception {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new Exception("ERROR: the parameter: " + key + " is missing");
		} else if (value.equalsIgnoreCase("true")) {
			return true;
		} else if (value.equalsIgnoreCase("false")) {
			return false;
		} else {
			throw new Exception("ERROR: paramter " + key + "with value " + value + " can not be cast to boolean");
		}
	}

	/**
	 * Reads Instances from an .ARFF file (Note: the name of the class attribute must be specified before calling this)
	 * @param properties The given properties
	 * @param key key for the value to be retrieved  
	 * @throws Exception if the key parameter is missing          
	 * @return the Instances object
	 */
	public static Instances readArff(Properties properties, String key) throws Exception {		
		String filename = PropertiesReader.readStringParam(properties, key);
		if (filename == null) {
			throw new Exception("ERROR: the parameter: " + key + " is missing");
		}
		return InstancesManipulation.readArff(filename, true);	
	}

	/**
	 * Reads String value from properties that represents the name of the class (e.g. weka.classifiers.bayes.NaiveBayes)
	 * and creates a class instance using the default constructor
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved           
	 * @return instance of the class as <code>Object</code>	 
	 * @throws Exception if the key is missing
	 */
	public static Object readObjectParam(Properties properties, String key) throws Exception {
		String className = readStringParam(properties, key);
		return getObject(className);
	}

	/**
	 * Creates an object of the class specified by the class name
	 * @param className class name
	 * @return an object of the specified class
	 * @throws Exception if there was an error creating an object of the desired class
	 */
	public static Object getObject(String className) throws Exception{
		try {			
			ClassLoader loader = ClassLoader.getSystemClassLoader();			
			@SuppressWarnings("rawtypes")
			Class c = loader.loadClass(className);
			Object returnObj = c.newInstance();

			if (returnObj == null) {
				throw new Exception("ERROR: error creating object of class '" + className + "'");
			}else
				return returnObj;
		} catch (Exception e) {
			throw new Exception("ERROR: error creating object of class '" + className + "'", e);
		}
	}
	
	/**
	 * Reads array of String values divided by blank spaces (e.g. str1 str2 str3)	
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved           
	 * @return array of String values <code>String[]</code>	
	 * @throws Exception if there was an error reading one of the String parameters 
	 */ 
//	public static String[] readStringArrayParam(Properties properties, String key) throws Exception{
//		String value = readStringParam(properties, key);	
//		if(value!=null)
//			return value.split(" ");
//		else
//			return null;
//	}
	
	/**
	 * Reads array of quoted String values divided by blank spaces (e.g. "str1" "str2" "str3")	
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved           
	 * @return list of String values	 
	 * @throws Exception if the key is missing
	 */ 
	public static List<String> readStringListParam(Properties properties, String key) throws Exception{
		List<String> strValuesList = new ArrayList<String>();
		Pattern regex = Pattern.compile("\"[^\"]+\"");
		Matcher regexMatcher = regex.matcher(readStringParam(properties, key));
		while (regexMatcher.find()) {
			String str = regexMatcher.group();
		     strValuesList.add(str.substring(1, str.length()-1));
		} 
		return strValuesList;
	}
	
	/**
	 * Reads array of int values divided by blank spaces (e.g. 1 2 3)	
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved           
	 * @return array of int values
	 * @throws Exception if the key is missing or the value is not array of integers
	 */  
	public static int[] readIntArrayParam(Properties properties, String key) throws Exception {
		String[] strValues = readStringParam(properties, key).split(" ");		
		int[] intValues = new int[strValues.length];
		
		for(int i=0; i<strValues.length; i++){
			try{
				int intVal = Integer.parseInt(strValues[i]);
				intValues[i] = intVal;
			}catch(NumberFormatException ex){
				throw new Exception("Parameter " + i + " is not an int value (read value '" + strValues[i] + "')");
			}
		}		
		return intValues;
	}
}