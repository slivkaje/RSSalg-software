/** 	
 * Name: Algorithm.java
 * Purpose: Abstract class representing an algorithm that can be run. This class should be inherited when adding a new algorithm.
 * Author: Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
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
package algorithms;

import algorithms.RSSalg.MajorityVote;
import algorithms.RSSalg.RSSalg;
import algorithms.RSSalg.resultStatistic.ClassifierEnsemble;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;
import classificationResult.ClassificationResult;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;

/**
 * Abstract class representing an algorithm that can be run. This class should be inherited when adding a new algorithm.
 * @author Jelena Slivka
 */
public abstract class Algorithm {
	/**
	 * The Data to perform the experiment on. Set by call to {@link #setData} method at the beginning of {@link #run} method
	 */
	protected CoTrainingData data;
	/**
	 * Current fold of fold-cross validation
	 */
	protected int currentFold;
	/**
	 * Current feature split
	 */
	protected int currentSplit; 
	/**
	 * Statistics (instance ids, predictions and confidences for each prediction) about the instances labeled during the algorithm execution (instances formally belonging to the unlabeled set, labeled and added to train data by the algorithm) 
	 */
	protected ClassifierEnsembleList classifiers = null;
	/**
	 * Statistics (instance ids, predictions and confidences for each prediction) about how the formed classifier classifies test data
	 */
	protected ClassifierEnsemble classifierTestData = null; 
	
	/**
	 * Running time of the algorithm (duration of running the <code>run</code> method)
	 */
	protected long runningTime;
	
	/**
	 * Whether or not should the statistic about adding unlabeled instances to the training set ({@link Algorithm#classifiers}) and 
	 * statistics about how the formed classifier classifies test data be recorded ({@link Algorithm#classifierTestData}) 
	 * (True if writeClassifiers=true in experimentSettings or if setRecordClassifiers(true) method is
	 * called).
	 */
	protected boolean recordClassifiers = ExperimentSettings.getInstance().isWriteClassifiers();
	
	/**
	 * Returns the current state of data (labeled, unlabeled and test data)
	 * @return data for semi-supervised experiment (labeled, unlabeled and test data)
	 */
	public CoTrainingData getData() {
		return data;
	}
	
	/**
	 * Set the data to run experiment on. In this method, everything should be restarted to run the next experiment
	 * @param data separated labeled, unlabeled and test data
	 * @param fold current fold of the n-fold-cross validation  
	 * @param splitNo the current feature split run for the fold (e.g. in RSSalg for each fold, m feature splits are created for co-training)
	 * @param recordClassifiers whether to record statistics about building and testing each of the classifiers (see {@link #getClassifiers()} and {@link #getClassifiersTestData})
	 */
	protected void setData(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers){
		this.data = data;
		this.currentFold = fold;
		this.currentSplit = splitNo;
		this.recordClassifiers = recordClassifiers;		
		if(recordClassifiers){
			classifierTestData = new ClassifierEnsemble();
			classifierTestData.setId(splitNo);			
		}else{
			classifierTestData = null;
		}
	}
	
	/**
	 * Run the algorithm
	 * @param data separated labeled, unlabeled and test data
	 * @param fold current fold of the n-fold-cross validation  
	 * @param splitNo the current feature split run for the fold (e.g. in RSSalg for each fold, m feature splits are created for co-training)
	 * @param recordClassifiers whether to record statistics about building and testing each of the classifiers (see {@link #getClassifiers()} and {@link #getClassifiersTestData})
	 * @return Classification result. The performance of the algorithm  is evaluated on the test 
	 * 			set defined in {@link CoTrainingData} object
	 * @throws Exception if there was an error running an algorithm
	 */
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers) throws Exception {		
		setData(data, fold, splitNo, recordClassifiers);
		
		if(ExperimentSettings.getInstance().isLoadClassifierStatistic()){
			ClassifierEnsembleList cl = new ClassifierEnsembleList();
			cl.fromXML(DatasetSettings.getInstance().getResultFolder()+"/fold_"+ fold + "/" + ExperimentSettings.getInstance().getClassifiersFilename());
			setClassifiers(cl);
		}
		
		return null;
	}
	
	/**
	 * Returns the statistics about building each of the classifiers.
	 * Statistics: information about each instance labeled during the training process of the classifier (instances formally belonging to the unlabeled set, 
	 * labeled and added to train data by the algorithm) - instance id, assigned label, confidence for each prediction. 
	 * For example, in {@link RSSalg} multiple co-training classifiers are built. Statistics about building these classifiers is recorded in the {@link ClassifierEnsembleList}
	 * object and is later used for building the final training set in RSSalg. 
	 * @return records about building a classifier (by labeling instances form unlabeled data and transferring them to the labeled set) 
	 */
	public ClassifierEnsembleList getClassifiers(){
		return classifiers;
	}
	
	/**
	 * Returns the statistics about testing each of the trained classifiers. 
	 * Statistics: information about each instance from the test set labeled during the algorithm evaluation - instance id, assigned label, confidence for each prediction.
	 * For example, in {@link MajorityVote} multiple co-training classifiers are built. Each trained classifier is then applied to the test set and statistics about 
	 * testing these classifiers is recorded in the {@link ClassifierEnsembleList} object. This statistics is later used for labeling test instances by a simple majority vote 
	 * of the created classifiers.
	 * @return records about evaluating a classifier on the test data
	 */
	public ClassifierEnsembleList getClassifiersTestData(){
		if(classifierTestData != null){
			ClassifierEnsembleList testDataClassifiers = new ClassifierEnsembleList();
			testDataClassifiers.addClassifier(classifierTestData);
			return testDataClassifiers;
		}
		return null;
	}
	
	/**
	 * Returns the name of the algorithm
	 * @return name of the algorithm. 
	 */
	public abstract String getName();
	
	/**
	 * Algorithm can be supplied with already recorded statistic about building a classifier. E.g. user can run the experiment of co-training 
	 * with multiple feature splits and record the statistic. Then, user can run RSSalg by supplying the recorded statistic (in order to use
	 * exactly the same classifiers created in co-training with multiple random splits experiment). In such case, RSSalg will not run 
	 * co-training with multiple random splits again but simply aggregate the recorded votes in order to create the final classifier.  
	 * @param classifiers list of created classifier ensembles
	 */
	protected void setClassifiers(ClassifierEnsembleList classifiers){
		this.classifiers = classifiers;
	}
	
	/**
	 * Returns the algorithm running time. Running time is the time needed for the execution of {@link #run} method.
	 * @return running  time of the algorithm
	 */
	public long getRunningTimeMillis(){
		return runningTime;
	}
}
