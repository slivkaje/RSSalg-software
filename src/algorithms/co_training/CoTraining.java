package algorithms.co_training;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import util.Evaluation;
import util.InstancesManipulation;
import weka.core.Instance;
import algorithms.Algorithm;
import algorithms.RSSalg.resultStatistic.ClassifierEnsemble;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.RSSalg.resultStatistic.Confidences;
import classificationResult.ClassificationResult;
import classificationResult.measures.MeasureIF;
import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;

/**
 * Class implementing the co-training algorithm as proposed in 
 * <p>
 * A. Blum, T. Mitchell, "Combining labeled and unlabeled data with co-training", COLT: Proceedings of the Workshop on Computational Learning Theory, 
 * Morgan Kaufmann, 1998, p. 92-100.
 * <p>
 */
public class CoTraining extends Algorithm{
	/**
	 * Whether or not should the co-training classifier be evaluated on test data in each iteration. If true, the results will be 
	 * written to {@link #logFileLocation} file.
	 */
	protected boolean testEachIteration;
	/**
	 * If each iteration should be tested, write results to {@link #logFileLocation} file (results_folder/fold_currentFold/CTlog_split_currentSplit.txt).
	 */
	protected String logFileLocation;
	/**
	 * Settings for co-training algorithm.
	 */
	private CoTrainingSettings ctSettings = CoTrainingSettings.getInstance();
	/**
	 * Data settings for the experiment.
	 */
	private DatasetSettings dataSettings = DatasetSettings.getInstance();
	/**
	 * Current co-training iteration.
	 */
	protected int currentIteration = 0; 
	
	/**
	 * Set the data to run experiment on and restarts everything in order to run the new experiment. For co-training this method also: 
	 * 1. initializes unlabeled pool 
	 * 2. initializes training classifier statistics. Statistics: information about each instance labeled during the training process of the classifier 
	 * (instances formally belonging to the unlabeled set, labeled and added to train data by the algorithm) - instance id, assigned label, confidence for each 
	 * prediction. */
	@Override
	protected void setData(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers){
		super.setData(data, fold, splitNo, recordClassifiers);
		
		this.data.initPool();
		
		this.testEachIteration = ctSettings.isTestEachIteration();	
		if(this.testEachIteration){
			this.logFileLocation = dataSettings.getResultFolder() + "/fold_" + currentFold + "/CTlog" + "_split_" + currentSplit + ".txt";
			File logFile = new File(logFileLocation);
			if(logFile.exists())
				logFile.delete();
		}
		this.currentIteration = 0;
		
		initClassifiers();
	}
	
	
	
	/**
	 * Initializes training classifier statistics Statistics: information about each instance labeled during the training process of the classifier (instances 
	 * formally belonging to the unlabeled set, labeled and added to train data by the algorithm) - instance id, assigned label, confidence for each prediction.
	 * Initialization: add all already labeled instances and assign them confidence 1.
	 */
	private void initClassifiers(){
		if(!recordClassifiers){
			classifiers = null;
			return;
		}
		
		classifiers = new ClassifierEnsembleList();
		
		ClassifierEnsemble CTclassifier = new ClassifierEnsemble();
		CTclassifier.setId(currentSplit);
		// add instances that are already present in the labeled data. These instances will be labeled by 100% of the classifiers and will have the same (prediction) 
		// label for each classifier as co-training only adds instances to the labeled set and does not modify the starting labeled instances
		for(Instance inst : data.getLabeledData()[0]){
			String label = InstancesManipulation.getLabel(inst);
			Double id = Double.parseDouble(InstancesManipulation.getInstanceID(inst));
			
			Confidences confidences = new Confidences();
			for(String className : DatasetSettings.getInstance().getClassNames()){
				if(className.equals(label))
					confidences.addConfidence(1.0);
				else
					confidences.addConfidence(0.0);
			}
			CTclassifier.addPrediction(id, confidences);
		}
		
		classifiers.addClassifier(CTclassifier);
	}
	
	/**
	 * Runs one iteration of co-training
	 * <p>
	 * For each view:<br>
	 * 1. Train a classifier on labeled data <br>
	 * 2. Allow classifier to label unlabeled data and select most confidently labeled instances<br>
	 * 3. Label most confident instances and transfer them from unlabeled to labeled set <br>
	 * Refill the pool (if used)
	 * @throws Exception if WEKA classifier for one of the views has not been generated successfully. Check whether:<ul>
	 * 	<li>class attribute is undefined</li>
	 * 	<li>testing instances are described by a different set of attributes than labeled instances. In co-training a 
	 * 		classifier is trained on one view of labeled data and applied to the same view on unlabeled (or pool) data. 
	 * 		This could happen if the {@link CoTrainingData} object is corrupt and the corresponding views of labeled and unlabeled (or pool) 
	 * 		data are mismatched</li>
	 * </ul>
	 */
	private void runOneIteration() throws Exception {		
		// train classifiers on views
		List<weka.classifiers.Classifier> viewClassifiers = new ArrayList<weka.classifiers.Classifier>();
		for(int view=0; view<data.getLabeledData().length; view++){
			weka.classifiers.Classifier classifier = dataSettings.getClassifier(view);
			viewClassifiers.add(classifier);
			try{
				viewClassifiers.get(view).buildClassifier(data.getLabeledData()[view]);
			}catch(Exception e){
				throw new Exception("ERROR: error building a classifier for view " + view + " in iteration " + currentIteration);
			}
		}
		
		// label and add data to the initial training set
		MostConfidentInstances[] mostConfidentInstances = new MostConfidentInstances[data.getLabeledData().length];
		for(int view=0; view<data.getLabeledData().length; view++){
			if(ctSettings.getPoolSize() != 0){ // classify instances from pool
				mostConfidentInstances[view] =  Evaluation.getConfidentInstances(viewClassifiers.get(view), data.getPoolData()[view]);
			}else{ // classify instances from unlabeled
				mostConfidentInstances[view] = Evaluation.getConfidentInstances(viewClassifiers.get(view), data.getUnlabeledData()[view]);
			}
		
			// label instances and move to labeled set (removes form unlabeled)
			data.labelInstances(mostConfidentInstances[view]);
			
			if(classifiers != null)
				classifiers.getEnsembles().get(0).addPredictions(mostConfidentInstances[view]);
		}
				
		// refill pool (if used)
		data.refillPool();
					
		currentIteration++;
	}
	
	/**
	 * Checks whether co-training algorithm is finished (maximal number of iterations, no more data to label, etc.)
	 * @return whether co-training is finished or not
	 */
	private boolean finished(){
		if ( data.noMoreDataToLabel() )
			return true;
		
		if (!ctSettings.isLabelAllUnlabeled()){ // stopping criteria: no. of iterations (k)
			if (currentIteration == ctSettings.getIterations())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Used for writing the co-training log: calculates all desired measures for all idividual views and the combined view.
	 * @return Line to write to co-training log file at location <code>logFileLocation<code>
	 */
	private String getMeasuesString(){
		ClassificationResult resView1 = data.testLabeled(0, false);
		ClassificationResult resView2 = data.testLabeled(1, false);
		ClassificationResult combined = data.testLabeled(false);
		
		List<MeasureIF> measures = ExperimentSettings.getInstance().getMeasures();
		String view1Str = "View1: ";
		String view2Str = "View2: ";
		String combinedStr = "Combined: ";
		DecimalFormat df = new DecimalFormat("###.##");
		for(int i=0; i<measures.size(); i++){
			view1Str += measures.get(i).getName() + ": " + df.format(measures.get(i).getMeasure(resView1)) + "; ";
			view2Str += measures.get(i).getName() + ": " + df.format(measures.get(i).getMeasure(resView2)) + "; ";
			combinedStr += measures.get(i).getName() + ": " + df.format(measures.get(i).getMeasure(combined)) + "; ";
		}
		
		return view1Str + "\n" + view2Str + "\n" + combinedStr + "\n";
	}
	
	/**
	 * Runs the co-training algorithm
	 * @return classification result of applying the trained co-training classifier on the supplied test data
	 * @throws Exception if there was an error during running one iteration of co-training see {@link #runOneIteration()} 
	 */
	@Override
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers) throws Exception{		
		try {
			super.run(data, fold, splitNo, recordClassifiers);
		} catch (JAXBException e) {
			System.out.println("WARNING: Trying to read the classifiers from file. Algorithm does not rely on the recorded classifier statistic, ignoring classifiers");
		}
		long startTime = System.currentTimeMillis();
		
		if(testEachIteration){
			String addToLog = "\nStarting co-training experiment for fold " + currentFold + " split: " + currentSplit + "\n";
			addToLog += getMeasuesString();
			addToCTlog(addToLog);
		}
		
		while(!finished()){
			runOneIteration();
			if(testEachIteration)
				addToCTlog("Classifiers after iteration: " + currentIteration + ": \n" + getMeasuesString());
		}
		
		ClassificationResult result;
		if(recordClassifiers){
			result = data.testLabeled(true);				
			classifierTestData.addPredictions(result.getPredictions());
		}else
			result = data.testLabeled(false);
		
		if(ExperimentSettings.getInstance().isWriteEnlargedCoTrainingSet()){
			String fileName = dataSettings.getResultFolder() + "/fold_" + currentFold + "/CT_enlargedTrainingSet" + "_split_" + currentSplit;
			for(int view=0; view<data.getLabeledData().length; view++){
				InstancesManipulation.writeArff(fileName + "_view_"+ view + ".arff", data.getLabeledData()[view]);
			}
		}
		
		if(testEachIteration)
			addToCTlog("End accuracy: \n" + getMeasuesString());
		
		long endTime = System.currentTimeMillis();
		runningTime = endTime - startTime;
		addToCTlog("CT running time " + ((double) runningTime)/100 + "s" );
		
		return result;
	}
		
	/**
	 * Adds the line to the co-training log file (if each iteration of co-training should be tested) at the file location {@link #logFileLocation} 
	 * (file {results_folder}/fold_{currentFold}/CTlog_split_{currentSplit}.txt).)
	 * @param line The line to be added to the co-training log.
	 */
	private void addToCTlog(String line){
		if(!testEachIteration)
			return;
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(logFileLocation), true));
			writer.println(line);
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("WARNING: error writing co-training log for fold "+ currentFold + " split " + currentSplit);
			System.out.println(e.getMessage());
		}
	}

	@Override
	public String getName() {
		return "CoTraining";
	}

	/**
	 * This algorithm does not rely on the recorded training classifier statistic, it will be ignored
	 */
	@Override
	protected void setClassifiers(ClassifierEnsembleList classifiers) {
		if(classifiers != null)
			System.out.println("WARNING: " + getName() + " algorithm does not rely on the recorded classifier statistic. Ignoring classifiers");
	}
}
