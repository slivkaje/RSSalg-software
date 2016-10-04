/** 	
 * Name: CoTrainingData.java
 * 
 * Purpose: Represents a collection of datasets used for one Co-Training experiment: labeled data, unlabeled data, small unlabeled pool u' and test data.
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
package algorithms.co_training;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import classificationResult.ClassificationResult;
import classificationResult.ClassifiedInstance;
import classificationResult.ClassifiedInstanceList;
import util.Evaluation;
import util.InstancesManipulation;
import weka.classifiers.Classifier;
import weka.core.Instances;
import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;

/**
 * Represents a collection of datasets used for one Co-Training experiment: 
 * labeled data, unlabeled data, small unlabeled pool u' and test data.
 * <p>
 * Each dataset is represented as the array of WEKA Instances objects: each element of such array represents one view
 * of the data (e.g. labeledData[0] - the first view of the labeled data, labeledData[1] - the second view of the labeled data)
 */
public class CoTrainingData {
	/**
	 * Array of different views of labeled data. Each view should contain the same instances, but different attribute sets (except for class and id attribute which exist
	 * in each of the views)
	 */
	protected Instances[] labeledData;
	/**
	 * Array of different views of unlabeled data. Each view should contain the same instances, but different attribute sets (except for class and id attribute which exist
	 * in each of the views)
	 */
	protected Instances[] unlabeledData;
	/**
	 * Array of different views of unlabeled pool data (u'). This array is null if the unlabeled pool is not used in the experiment.
	 * Each view should contain the same instances, but different attribute sets (except for class and id attribute which exist
	 * in each of the views).
	 */
	protected Instances[] poolData;
	/**
	 * Array of different views of test data. Each view should contain the same instances, but different attribute sets (except for class and id attribute which exist
	 * in each of the views)
	 */
	protected Instances[] testData;
	/**
	 * Settings for co-training algorithm
	 */
	private CoTrainingSettings ctSettings = CoTrainingSettings.getInstance();
	/**
	 * Data settings for the experiment
	 */
	private DatasetSettings dataSettings = DatasetSettings.getInstance();
		
	/**
	 * Creates a new instance of <code>CoTrainingData</code> from <code>Instances[]</code> arrays that represent labeled data, unlabeled data and test data.
	 * Each <code>Instances</code> object represents one view of the data. It is assumed that the corresponding views 
	 * (e.g. 1st view of the labeled data and the 1st view of the unlabeled and test data) contain the same set of attributes 
	 * @param labeledData array of <code>Instances</code> objects that represent different views of labeled data
	 * @param unlabeledData array of <code>Instances</code> objects that represent different views of unlabeled data
	 * @param testData array of <code>Instances</code> objects that represent different views of test data
	 */
	public CoTrainingData(Instances[] labeledData, Instances[] unlabeledData, Instances[] testData) {
		super();
		this.labeledData = labeledData;
		this.unlabeledData = unlabeledData;
		this.testData = testData;	
	}
	
	/**
	 * Copy constructor: creates a new instance of <code>CoTrainingData</code> from other <code>CoTrainingData</code> object (deep copy of the data). 
	 * @param data <code>CoTrainingData</code> object to clone
	 */
	public CoTrainingData(CoTrainingData data){
		this.labeledData = new Instances[data.labeledData.length];
		for(int i=0; i<labeledData.length; i++)
			this.labeledData[i] = InstancesManipulation.cloneDataset(data.labeledData[i]);
		
		this.unlabeledData = new Instances[data.unlabeledData.length];
		for(int i=0; i<unlabeledData.length; i++)
			this.unlabeledData[i] = InstancesManipulation.cloneDataset(data.unlabeledData[i]);
		
		this.testData = new Instances[data.testData.length];
		for(int i=0; i<testData.length; i++)
			this.testData[i] = InstancesManipulation.cloneDataset(data.testData[i]);
		
		if(data.poolData != null){
			this.poolData = new Instances[data.poolData.length];
			for(int i=0; i<poolData.length; i++)
				this.poolData[i] = InstancesManipulation.cloneDataset(data.poolData[i]);
		}
	}
	
	/**
	 * Loads <code>CoTrainingData</code> object from the given folder.
	 * <p>
	 * Assumes that the folder contains 4 x &lt;no of views&gt; arff files (e.g. for 2 views: labeled_view0.arff, labeled_view1.arff, 
	 * unlabeled_view0.arff, unlabeled_view1.arff, test_view0.arff, test_view1.arff, pool_view0.arff, pool_view1.arff).
	 * Pool files (u') are only read if they exist.
	 * @param path path to the folder that contains the files needed to populate CoTrainingData object
	 * @param noViews number of views to read
	 * @param setClass whether or not to set the class and id attributes
	 * @throws Exception 
	 * <ul>
	 * <li> the ARFF file is missing
	 * <li> class attribute is missing (there is no attribute in the dataset that matches the name of the class attribute given in the data properties)
	 * <li> adding an id attribute failed
	 * </ul>
	 */
	public CoTrainingData(String path, int noViews, boolean setClass) throws Exception{
		loadData(path, noViews, setClass);
	}

	/**
	 * Returns the current state of labeled data.
	 * @return array of {@link weka.core.Instances} object that represent different views of labeled data
	 */
	public Instances[] getLabeledData() {
		return labeledData;
	}
	/**
	 * Set the labeled data.
	 * @param labeledData array of {@link weka.core.Instances} object that represent different views of labeled data
	 */
	public void setLabeledData(Instances[] labeledData) {
		this.labeledData = labeledData;
	}
	/**
	 * Returns the current state of unlabeled data.
	 * @return array of {@link weka.core.Instances} object that represent different views of unlabeled data
	 */
	public Instances[] getUnlabeledData() {
		return unlabeledData;
	}
	/**
	 * Set the unlabeled data.
	 * @param unlabeledData array of {@link weka.core.Instances} object that represent different views of unlabeled data
	 */
	public void setUnlabeledData(Instances[] unlabeledData) {
		this.unlabeledData = unlabeledData;
	}
	/**
	 * Returns the current state of unlabeled pool data (u').
	 * @return array of {@link weka.core.Instances} object that represent different views of unlabeled pool data (u')
	 */
	public Instances[] getPoolData() {
		return poolData;
	}
	/**
	 * Set the unlabeled pool data.
	 * @param poolData array of {@link weka.core.Instances} object that represent different views of unlabeled pool data (u')
	 */
	public void setPoolData(Instances[] poolData) {
		this.poolData = poolData;
	}
	/**
	 * Returns the current state of test data.
	 * @return array of {@link weka.core.Instances} object that represent different views of test data
	 */
	public Instances[] getTestData() {
		return testData;
	}
	/**
	 * Set the test data.
	 * @param testData array of <code>Instances</code> object that represent different views of test data.
	 */
	public void setTestData(Instances[] testData) {
		this.testData = testData;
	}

	/**
	 * Saves the current state of the CoTrainingData object to the given folder
	 * <p>
	 * Data is saved as 4 x &lt;no of views&gt; arff files (e.g. for 2 views: labeled_view0.arff, labeled_view1.arff, 
	 * unlabeled_view0.arff, unlabeled_view1.arff, test_view0.arff, test_view1.arff, pool_view0.arff, pool_view1.arff).	  
	 * Pool files (u') are not saved if the pool is empty
	 * 
	 * @param path to the folder to which co-traning data is saved. If the folder doesn't exist it will be created 
	 * @throws IOException if there was an error writing the files (e.g. missing folder)
	 */
	public void saveData(String path) throws IOException{
		if(!Files.exists(Paths.get(path))){
			Files.createDirectory(Paths.get(path));			
		}
		
		for(int view=0; view<labeledData.length; view++){
			InstancesManipulation.writeArff(path + File.separator + "labeled_view" + view + ".arff", this.labeledData[view]);
			InstancesManipulation.writeArff(path + File.separator + "unlabeled_view" + view + ".arff", this.unlabeledData[view]);						
			InstancesManipulation.writeArff(path + File.separator + "test_view" + view + ".arff", this.testData[view]);
			if(ctSettings.getPoolSize() != 0) // if pool is used
				if(poolData != null && poolData[0].numInstances() > 1) // if pool not empty
					InstancesManipulation.writeArff(path + File.separator + "pool_view" + view + ".arff", this.poolData[view]);
		}		
	}

	/**
	 * Loads <code>CoTrainingData</code> object from the given folder
	 * <p>
	 * Assumes that the data is saved as 4 x &lt;no of views&gt; arff files (e.g. for 2 views: labeled_view1.arff, labeled_view2.arff, 
	 * unlabeled_view1.arff, unlabeled_view2.arff, test_view1.arff, test_view2.arff, pool_view1.arff, pool_view2.arff).
	 * Pool files (u') are only read if they exist
	 *   
	 * @param path to folder from which co-traning data is loaded 
	 * @param noViews number of views to read
	 * @param setClass whether or not to set the class and id attributes
	 * @throws Exception
	 * <ul>
	 * <li> the ARFF file is missing
	 * <li> class attribute is missing (there is no attribute in the dataset that matches the name of the class attribute given in the data properties)
	 * <li> adding an id attribute failed
	 * </ul> 
	 */
	private void loadData(String path, int noViews, boolean setClass) throws Exception{
		
//		int noViews = 0;
//		while(true){
//			File labeledViewFile = new File(path + "/labeled_view" + noViews + ".arff");
//			if(!labeledViewFile.exists())
//				break;
//			noViews++;
//		}
//		if(noViews != DatasetSettings.getInstance().getNoViews())
//			throw new Exception("Found " + noViews + " views in fold. Expected " + DatasetSettings.getInstance().getNoViews() + " (number of views in fold_0).");
		
		this.labeledData = new Instances[noViews];
		this.unlabeledData = new Instances[noViews];
		this.testData = new Instances[noViews];
		File poolFile = new File(path + File.separator + "pool_view0.arff");
		if(poolFile.exists())				
			this.poolData = new Instances[noViews];
		
		
		for(int view=0; view<noViews; view++){			
			this.labeledData[view] = InstancesManipulation.readArff(path + File.separator + "labeled_view" + view + ".arff", setClass);
			this.unlabeledData[view] = InstancesManipulation.readArff(path + File.separator + "unlabeled_view" + view + ".arff", setClass);
			this.testData[view] = InstancesManipulation.readArff(path + File.separator + "test_view" + view + ".arff", setClass);
			
			if(poolFile.exists())			
				this.poolData[view] = InstancesManipulation.readArff(path + File.separator + "pool_view" + view + ".arff", setClass);
		}
	}
	
	/**
	 * Initializes the unlabeled pool u' 
	 * <p>
	 * Creates an empty dataset that is described with the same attributes as unlabeled instances and fills it by random sampling of 
	 * unlabeled data. If pool size is set to 0 (i.e. pool u' is not used in the experiment), pool won't be initialized
	 */
	public void initPool(){
		if(ctSettings.getPoolSize() == 0) // pool not used in the experiment
			return;
		if(poolData != null) // already initialized
			return;
		
		poolData = new Instances[unlabeledData.length];
		for(int view=0; view < unlabeledData.length; view++){
			poolData[view] = InstancesManipulation.createEmptyDataset(unlabeledData[view]);
		}

		refillPool();
	}
		
	/**
	 * Refills the unlabeled pool u' to the size defined by the experiment settings
	 */
	public void refillPool(){
		if(ctSettings.getPoolSize() == 0) // pool not used in the experiment
			return;
		
		int numInstancesToSample = ctSettings.getPoolSize() - poolData[0].numInstances();  
		
		if (numInstancesToSample >= unlabeledData[0].size()){ // not enough unlabeled instances to sample, copy the remaining instances to pool
			InstancesManipulation.moveAllInstances(unlabeledData, poolData);
			return;
		}
		
		int tempRandSeed = dataSettings.getNextRandom();
		// sample instances
		Random tempRand = new Random(tempRandSeed);		
		while (poolData[0].numInstances() < ctSettings.getPoolSize()) {			
			int index = tempRand.nextInt(unlabeledData[0].numInstances());			
			InstancesManipulation.moveInstance(unlabeledData, poolData, index);
		}
	}
	
	/**
	 * Empties the unlabeled pool u' (all instances are moved back to unlabeled data) and resamples the pool from unlabeled data. 
	 */
	public void resamplePool(){
		emptyPool();
		refillPool();
	}
	
	/**
	 * Empties the unlabeled pool u'. Moves all instances form pool back to unlabeled data  
	 */
	public void emptyPool(){
		if(ctSettings.getPoolSize() == 0)
			return;
		
		for(int view=0; view<poolData.length; view++)
			InstancesManipulation.moveAllInstances(poolData[view], unlabeledData[view]);
	}
	
	/**
	 * Labels instance from the dataset (with the given label) and moves it from the dataset to labeled data 
	 * @param inst classified instance (contains id, prediction, confidence and actual label). Instance will be labeled by the given prediction.
	 * @param dataset: dataset that should contain the instance (pool or unlabeled set)  
	 * @throws Exception if the instance for labeling is not found in the dataset that should contain it
	 */
	private void findAndLabelInstance(ClassifiedInstance inst, Instances[] dataset) throws Exception{
		int instanceInd = InstancesManipulation.findInstance(dataset[0], ""+inst.getInstanceId());
		if(instanceInd == -1)
			throw new Exception("ERROR: instance " + inst.getInstanceId() + " for labeling with class " + inst.getPrediction() + " not found in the dataset.");
		for(int view=0; view<dataset.length; view++)
			dataset[view].instance(instanceInd).setClassValue(inst.getPrediction());
		InstancesManipulation.moveInstance(dataset, labeledData, instanceInd);
	}
	
	/**
	 * Label the unlabeled instance and move it to labeled data 
	 * <p>
	 * The instance is searched for in pool if the pool is used in the experiment,  otherwise it is searched for in unlabeled data. 
	 * {@link ClassifiedInstance} object contains the label for the instance in {@link ClassifiedInstance#getPrediction()} 
	 * @param instance classified instance (contains id, prediction (the label that should be assigned to the instance), confidence and actual label if available)
	 * @throws Exception if the instance for labeling is not found
	 */
	public void labelInstance(ClassifiedInstance instance) throws Exception{
//		System.out.println("\tLabeling instance " + instance.getInstanceId() + " as " + instance.getPrediction());
		if(ctSettings.getPoolSize() != 0){ // label instance from pool
			findAndLabelInstance(instance, poolData);
		}else{ // label instance from unlabeled
			findAndLabelInstance(instance, unlabeledData);
		}
	}
	
	/**
	 * Label all instances from the {@link MostConfidentInstances} object. These are the most confidently labeled instances selected in co-training 
	 * process. The instances are labeled with assigned predictions and moved to labeled data. The instances for labeling are searched for in pool
	 * if the pool is used in the experiment, otherwise they are searched for in unlabeled data. 
	 * @param mostConfidentInst The most confidently labeled instances selected during the co-training process
	 * @throws Exception if one or more instances for labeling are not found
	 */
	public void labelInstances(MostConfidentInstances mostConfidentInst) throws Exception{
		List<String> classNames = dataSettings.getClassNames();
		for(String className : classNames){ // get the most confidently labeled instances for each class
			ClassifiedInstanceList instances =  mostConfidentInst.getMostConfidentInstances(className);
			
			Iterator<ClassifiedInstance> it = instances.getIterator();
			while (it.hasNext()) {
				ClassifiedInstance inst = it.next();
				labelInstance(inst);		
			}	
		}
	}
	
	/**
	 * Tests the strength of the view. Trains a supervised model on the labeled set using only features from the selected view and evaluates the 
	 * performance on the test set
	 * @param view number of view to test.
	 * @param recordPredictions whether to record the assigned predictions in the resulting <code>ClassificationResult</code> object
	 * @return {@link ClassificationResult} object or null if testing failed.
	 */
	public ClassificationResult testLabeled(int view, boolean recordPredictions){		
		try {
			return Evaluation.performTest(dataSettings.getClassifier(view), labeledData[view], testData[view], recordPredictions);
		} catch (Exception e) {
			System.out.println("ERROR: error testing the strength of view " + view);
			e.printStackTrace();			
		}
		return null;
	}
	
	/**
	 * Tests the strength of combined classifier. Trains a supervised model on the labeled set using features from all views and evaluates the 
	 * performance on the test set. The feature set for the classifier is obtained by merging features from all views. This method does not modify
	 * the <code>CoTraningData</code> object
	 * @param recordPredictions whether to record the assigned predictions in the resulting <code>ClassificationResult</code> object 
	 * @return object <code>ClassificationResult</code> or null if testing failed
	 */
	public ClassificationResult testLabeledMergedViews(boolean recordPredictions){		
		try {					
			return Evaluation.performTest(dataSettings.getCombinedClassifier(), InstancesManipulation.getMerged(labeledData), InstancesManipulation.getMerged(testData), recordPredictions);
		} catch (Exception e) {
			System.out.println("ERROR: error testing the strength of labeled data with merged views");
			e.printStackTrace();			
		}
		return null;
	}
	
	/**
	 * Tests the strength of co-training style combined classifier. Trains a different supervised model for each view of the labeled set. 
	 * Each supervised model is built using same instances but only features from one separate view. The instance is classified in the following way: 
	 * for each class the probability that the instance belongs to this class is calculated by multiplying the probabilities output by each of the 
	 * trained models; instance is than assigned the class that has the highest probability. The performance is evaluated on the test set. 
	 * @param recordPredictions whether to record the assigned predictions in the resulting <code>ClassificationResult</code> object. This method 
	 * does not modify the <code>CoTraningData</code> object
	 * @return object <code>ClassificationResult</code> or null if testing failed
	 */
	public ClassificationResult testLabeled(boolean recordPredictions){
		try {
			// form a list of classifiers - one for each view according to user settings
			List<Classifier> classifiers = new ArrayList<Classifier>();
			for(int view = 0; view<labeledData.length; view++)
				classifiers.add(dataSettings.getClassifier(view));
			return Evaluation.performTest(classifiers, labeledData, testData, recordPredictions);
		} catch (Exception e) {
			System.out.println("ERROR: error testing the strength of labeled data (co-training style combined views)");
			e.printStackTrace();			
		}
		return null;
	}
	
	/**
	 * Checks whether there is more data for labeling (in the unlabeled pool or unlabeled datasets).
	 * @return whether there are more unlabeled instances
	 */
	public boolean noMoreDataToLabel(){
		if(getPoolData()[0].size() == 0 && getUnlabeledData()[0].size() == 0)
			return true;
		else
			return false;
	}
	
	
	
	/**
	 * Merge all views in this <code>CoTrainingData</code> object. Attributes form all views will be moved to the first view (for labeled, unlabeled, 
	 * pool and test data). In the rest of the views only the label and id attribute will remain. 
	 * @throws Exception if the merging failed
	 */
	public void mergeViews() throws Exception {
		if(labeledData.length == 0) // already just one view
			return;
		
		// move features from all other views to the first one
		for(int view=1; view < labeledData.length; view++){
			try{
				labeledData[0] = InstancesManipulation.mergeAttributes(labeledData[0], labeledData[view]);
				labeledData[view] = InstancesManipulation.removeAllAttributes(labeledData[view]);
			}catch(Exception e){
				throw new Exception("ERROR: error merging views of labeled data.", e);
			}
			try{
				unlabeledData[0] = InstancesManipulation.mergeAttributes(unlabeledData[0], unlabeledData[view]);
				unlabeledData[view] = InstancesManipulation.removeAllAttributes(unlabeledData[view]);
			}catch(Exception e){
				throw new Exception("ERROR: error merging views of unlabeled data.", e);
			}
			try{
				testData[0] = InstancesManipulation.mergeAttributes(testData[0], testData[view]);
				testData[view] = InstancesManipulation.removeAllAttributes(testData[view]);
			}catch(Exception e){
				throw new Exception("ERROR: error merging views of test data.", e);
			}
			if(poolData != null)
				try{
					poolData[0] = InstancesManipulation.mergeAttributes(poolData[0], poolData[view]);
					poolData[view] = InstancesManipulation.removeAllAttributes(poolData[view]);
				}catch(Exception e){
					throw new Exception("ERROR: error merging views of pool data.", e);
				}
		}
	}
	
	/**
	 * Move attributes (defined by the supplied attribute indices) from one view to another. This will affect labeled, unlabeled, pool and test data
	 * @param originView index of the view to copy from 
	 * @param destinationView index of the view to copy to
	 * @param attIndices set of (Integer) attribute indices to move from originView to destinationView
	 * @throws Exception if the error in moving the attributes occurred
	 */
	public void moveAttributes(int originView, int destinationView, Set<Integer> attIndices) throws Exception{
		try {			
			labeledData[destinationView] = InstancesManipulation.copyAttributes(labeledData[originView], labeledData[destinationView], attIndices);
			labeledData[originView] = InstancesManipulation.removeAttributes(labeledData[originView], attIndices, false);
			
			unlabeledData[destinationView] = InstancesManipulation.copyAttributes(unlabeledData[originView], unlabeledData[destinationView], attIndices);
			unlabeledData[originView] = InstancesManipulation.removeAttributes(unlabeledData[originView], attIndices, false);
			
			testData[destinationView] = InstancesManipulation.copyAttributes(testData[originView], testData[destinationView], attIndices);
			testData[originView] = InstancesManipulation.removeAttributes(testData[originView], attIndices, false);
			
			if(poolData != null){
				poolData[destinationView] = InstancesManipulation.copyAttributes(poolData[originView], poolData[destinationView], attIndices);
				poolData[originView] = InstancesManipulation.removeAttributes(poolData[originView], attIndices, false);
			}
		} catch (Exception e) {
			throw new Exception("ERROR: error moving attributes " + attIndices.toString() + " from view " + originView + " to view " + destinationView, e);
		}
	}
	
	/**
	 * Returns attribute names for the selected views
	 * @param view number
	 * @return string consisting of attribute names
	 */
	public String getViewString(int view){
		String res = "";
		for(int attInd=0; attInd<labeledData[view].numAttributes(); attInd++){
			String attName = labeledData[view].attribute(attInd).name();
			res += attName + "; ";
		}
		return res;
	}
}
