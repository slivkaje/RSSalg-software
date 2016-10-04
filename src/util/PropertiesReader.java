/** 	
 * Name: PropertiesReader.java
 * 
 * Purpose: Utility class for reading various data types  from property files.
 * 
 * Author: Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * Copyright: (c) 2016 Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * This file is a part of RSSalg software, a flexible, highly configurable tool for experimenting 
 * with co-training based techniques. RSSalg Software encompasses the implementation of 
 * co-training and RSSalg, a co-training based technique that can be applied to single-view 
 * datasets published in the paper: 
 * 
 * Slivka, J., Kovacevic, A. and Konjovic, Z., 2013. 
 * Combining Co-Training with Ensemble Learning for Application on Single-View Natural 
 * Language Datasets. Acta Polytechnica Hungarica, 10(2).
 *   
 * RSSalg software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RSSalg software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
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
	 * Reads array of int values. Possible formats: integers divided by blank spaces (e.g. 1 2 3); start:end; start:end:step	
	 * 
	 * @param properties The given properties
	 * @param key key for the value to be retrieved           
	 * @return array of int values
	 * @throws Exception if the key is missing or the value is not array of integers
	 */  
	public static int[] readIntArrayParam(Properties properties, String key) throws Exception {
		String representation = readStringParam(properties, key);
		
		if(representation.contains(":")){
			String[] strValues = representation.split(":");			
			if (strValues.length > 3 || strValues.length < 2){
				throw new Exception("Error reading an int array '" + representation + "'. Examples: key=1 2 3; key=1:3; key=1:3:1");
			}
			try{
				int startVal = Integer.parseInt(strValues[0]);
				int endVal = Integer.parseInt(strValues[1]);
				if (endVal <= startVal){
					throw new Exception("Error reading an int array '" + representation + "' - end value less or equal to start value");
				}
				int step = 1;
				if(strValues.length == 3){
					step = Integer.parseInt(strValues[2]);
				}
				int[] res = new int[(endVal - startVal)/step + 1];
				int counter = 0;
				for(int i=startVal; i<=endVal; i = i+step){
					res[counter] = i;
					counter++;
				}
				return res;
			}catch(NumberFormatException ex){
				throw new Exception("Error reading an int array '" + representation + "' - not an int value");
			}
		}
		
		
		String[] strValues = representation.split(" ");		
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