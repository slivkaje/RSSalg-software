/** 	
 * Name: CVSettings.java
 * 
 * Purpose:  Singleton object encapsulating all experiment settings for n-fold-cross validation experiment.
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
package experimentSetting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import util.PropertiesReader;

/**
 * Singleton object encapsulating all experiment settings for n-fold-cross validation experiment described in:
 * <p>
 *  F. Feger and I. Koprinska. Co–training Using RBF Nets and Different Feature Splits. In Proceedings of 2006 International Joint Conference on Neural Network, pp. 1878–1885, 2006.
 */
public class CVSettings {	
	private static CVSettings instance = null; 

	/**
	 * Total number of folds (n) for n-fold-cross validation
	 */
	protected int noFolds = 0;  
	/**
	 * Number of folds merged and used as unlabeled data
	 */
	protected int noFoldsUnlabeled = 0;
	/**
	 * Number of folds merged and used as test data
	 */
	protected int noFoldsTest = 0;
	/**
	 * For each class: the number of instances randomly chosen as labeled data 
	 */
	protected Map<String, Integer> noLabeled = new HashMap<String, Integer>();
	
	/**
	 * If true: when creating an experiment remove the labels from unlabeled instances
	 */
	protected boolean removeLabelsFromUnlabeled = false;

	/**
	 * Returns a singleton instance of CVSettings (cross-validation experiment settings)
	 * @return instance of CoTrainingSettings
	 */
	public static CVSettings getInstance() {
		if(instance == null) {
			instance = new CVSettings();
	    }
	    return instance;
	} 
	private CVSettings(){}
	public void clear(){
		noFolds = 0;
		noFoldsUnlabeled = 0;
		noFoldsTest = 0;
		noLabeled.clear();
		removeLabelsFromUnlabeled = false;
		System.out.println("CV settings cleared.");
	}
	
	public boolean isInitiated(){
		return noLabeled.size() > 0;
			
	}
	/**
	 * Returns the total number of folds (n) for n-fold-cross validation
	 * @return number of folds (n) for n-fold-cross validation
	 */
	public int getNoFolds() {
		return noFolds;
	}
	private void setNoFolds(int noFolds) throws Exception {
		if (noFolds < 2)
			throw new Exception("ERROR: there must be at least 2 folds (trying to set " + noFolds + " folds)");
		this.noFolds = noFolds;
	}
	
	/**
	 * Returns the number of folds that will be used as unlabeled data
	 * @return number of folds that will be used as unlabeled data
	 */
	public int getNoFoldsUnlabeled() {
		return noFoldsUnlabeled;
	}
	private void setNoFoldsUnlabeled(int noFoldsUnlabeled) throws Exception {
		if (noFoldsUnlabeled < 1)
			throw new Exception("ERROR: there must be at least 1 fold for unlabeled data (trying to set " + noFoldsUnlabeled + " folds)");
		this.noFoldsUnlabeled = noFoldsUnlabeled;
	}
	
	/**
	 * Returns the number of folds that will be used as test data
	 * @return number of folds that will be used as test data
	 */
	public int getNoFoldsTest() {
		return noFoldsTest;
	}
	private void setNoFoldsTest(int noFoldsTest) throws Exception {
		if (noFoldsTest < 1)
			throw new Exception("ERROR: there must be at least 1 fold for test data (trying to set " + noFoldsTest + " folds)");
		this.noFoldsTest = noFoldsTest;
	}
	
	/**
	 * For each class: the number of instances randomly chosen as labeled data. Key: class name, value: number of seed examples for that class
	 * @return number of labeled instances for each class
	 */
	public Map<String, Integer> getNoLabeled() {
		return noLabeled;
	}
	private void setNoLabeled(List<String> classNames, int[] noLabeled) throws Exception {		
		if(classNames.size() != noLabeled.length)
			throw new Exception("ERROR: error assigning labeled data: num classes " + classNames.size() + " num examples per class: " + noLabeled.length);
		int totalLabeled = 0;
		for(int i=0; i<classNames.size(); i++){
			totalLabeled += noLabeled[i];
			this.noLabeled.put(classNames.get(i), noLabeled[i]);
		}
		
		if(classNames.size() == 0 || totalLabeled == 0)
			throw new Exception("ERROR: there must be at least 1 labeled example");
	}
	
	/**
	 * If true: when creating an experiment remove the labels from unlabeled instances
	 * @return whether to remove labeles from unlabeled data
	 */
	public boolean isRemoveLabelsFromUnlabeled() {
		return removeLabelsFromUnlabeled;
	}

	/**
	 * Print n-fold-cross validation used in the experiment
	 * @param out : PrintStream for writing   
	 */
	public void printSettings(PrintStream out){
		out.println("CV SETTINGS:");
		if(DatasetSettings.getInstance().isLoadPresetExperiment()){
			out.println("Reading a preset CV experiment, CV settings ignored.");
			return;
		}
		
		String noLabeledStr = "";
		for(String className : noLabeled.keySet()){		
			noLabeledStr += className + ": " + noLabeled.get(className) + "; ";
		}		
		out.println("\tNumber of folds: " + noFolds);
		out.println("\tNumber of folds used as unlabeled data: " + noFoldsUnlabeled);
		out.println("\tNumber of folds used as test data: " + noFoldsTest);		
		out.println("\tNumber of labeled examples: " + noLabeledStr);
		if(removeLabelsFromUnlabeled)
			out.println("\tRemoving labels from unlabeled instances");
	}
		
	/**
	 * Reads the cross-validation experiment settings from properties file
	 * @param propertiesFile path and file name for the properties file
	 * @throws Exception if there was an error reading the properties
	 */
	public void readProperties(String propertiesFile) throws Exception{
		if(DatasetSettings.getInstance().isLoadPresetExperiment())
			return; //Reading a preset CV experiment, CV settings ignored
		
		Properties properties = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(propertiesFile));
		}catch (FileNotFoundException e) {
			throw new Exception("ERROR: error reading properties file: file '" + propertiesFile + "' does not exist", e);
		}
		clear();
		System.out.println("Reading cross-validation properties from file: " + propertiesFile);
		
		
		setNoFolds(PropertiesReader.readInt(properties, "noFolds"));
		setNoFoldsUnlabeled(PropertiesReader.readInt(properties, "noUnlabeled"));
		setNoFoldsTest(PropertiesReader.readInt(properties, "noTest"));
		setNoLabeled(PropertiesReader.readStringListParam(properties, "className"), 
					 PropertiesReader.readIntArrayParam(properties, "noLabeled")); 
		try{
			removeLabelsFromUnlabeled = PropertiesReader.readBooleanParam(properties, "removeLabels");			
		}catch(Exception e){
			System.out.println("Removing labels from unlabeled instances");
			removeLabelsFromUnlabeled = false;
		}
		String resultFolder = "";
		try{
			resultFolder = DatasetSettings.getInstance().getResultFolder();
			PrintStream writer = new PrintStream(new FileOutputStream(resultFolder + "/Experiment.txt", true)); 
			printSettings(writer);
			writer.close();
		}catch(Exception e){
			System.out.println("WARNING: could not write the data settings for the experiment in file " + resultFolder + "/Experiment.txt");
		}
	}
}
