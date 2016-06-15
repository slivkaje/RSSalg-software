package algorithms;

import util.InstancesManipulation;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;
import classificationResult.ClassificationResult;

/**
 * Supervised experiment - training set: labeled data and unlabeled data (with correct labels assigned); test set: test data.
 * Labeled, unlabeled and test data are supplied through {@link algorithms.co_training.CoTrainingData} object.
 * This experiment assumes that unlabeled data in {@link algorithms.co_training.CoTrainingData} object has the correct label assigned. 
 * <p>
 * This is the ALL<sub>acc</sub> algorithm described in the paper J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View 
 * Natural Language Datasets". Acta Polytechnica Hungarica 10 (2))
 */
public class SupervisedAlgorithm_All extends Algorithm {

	/**
	 * Run the supervised Allacc experiment and return the classification result.
	 * <p>
	 * Generally, in the experiment:<br>
	 *  1. All the data is moved from unlabeled (and unlabeled pool) set to the labeled set. It is assumed that the unlabeled data has the correct label assigned.<br>
	 *  2. In the case of multiple view setting, all views are merged to form a unique attribute set<br>
	 *  3. Algorithm is trained in the supervised fashion and applied on the test set
	 */
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers){
		try {
			super.run(data, fold, splitNo, recordClassifiers);
		} catch (Exception e) {
			System.out.println("WARNING: Trying to read the classifiers from file. Algorithm does not rely on the recorded classifier statistic, ignoring classifiers");
		}
		
		long startTime = System.currentTimeMillis();
		
		// check whether all unlabeled have assigned correct label (no missing values for the label attribute)					
		int noMissingValues = data.getUnlabeledData()[0].attributeStats(data.getUnlabeledData()[0].classIndex()).missingCount;
		if(data.getPoolData() != null){
			noMissingValues += data.getPoolData()[0].attributeStats(data.getPoolData()[0].classIndex()).missingCount;
		}
		if(noMissingValues > 0){
			System.out.println("WARNING: MISSING " + noMissingValues + " IN UNLABELED DATA");
			System.out.println("\tLabels for unlabeled data might have been removed. If so, re-run the experiment by using loadFromFiles=false and removeLabels=false in "
					+ "cv.properties. This will recreate the n-fold-cross validation expeiment while preserving the labels in unlabeled data.");
		}
		
		// 1. Move all unlabeled instances to labeled set
		InstancesManipulation.moveAllInstances(data.getUnlabeledData(), data.getLabeledData());
		if(data.getPoolData() != null){
			InstancesManipulation.moveAllInstances(data.getPoolData(), data.getLabeledData());
		}
		
		// 3. Test the model trained on labeled data on the test data by merging views 
		ClassificationResult result = null;
		if(recordClassifiers)
			result = data.testLabeledMergedViews(true);
		else
			result = data.testLabeledMergedViews(false);
		if(classifierTestData !=null)
			classifierTestData.addPredictions(result.getPredictions());
		
		long endTime = System.currentTimeMillis();
		runningTime = endTime - startTime;
		return result;
	}

	public String getName() {
		return "Supervised_experiment_All";
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
