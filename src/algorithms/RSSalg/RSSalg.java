package algorithms.RSSalg;

import java.util.List;
import java.util.Random;

import util.Evaluation;
import util.InstancesManipulation;
import algorithms.Algorithm;
import algorithms.RSSalg.GA.Candidate;
import algorithms.RSSalg.GA.CandidateEvaluatorIF;
import algorithms.RSSalg.GA.GAThresholdOptimiser;
import algorithms.RSSalg.GA.RSSalgCandidateEvaluator;
import algorithms.RSSalg.GA.TestSetAccuracyCandidateEvaluator;
import algorithms.RSSalg.resultStatistic.ClassifierEnsemble;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTraining;
import algorithms.co_training.CoTrainingData;
import classificationResult.ClassificationResult;
import classificationResult.measures.MeasureIF;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;
import featureSplit.DifferentRandomSplitsSplitter;

/**
 * Implementation of RSSalg from the paper:<p>
 * J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language 
 * Datasets". Acta Polytechnica Hungarica 10 (2).
 * <p>
 * RSSalg runs the co-training algorithm for the predefined number of times, each time using a different feature split (no. of splits is defined through noSplits in
 * experiment properties). A statistics about building each of the co-training classifiers {@link algorithms.RSSalg.resultStatistic.ClassifierEnsembleList} is recorded
 * (this statistics can also be loaded from a previously run experiment that recorded the statistics, e.g. Random from the paper). The recorded statistics is used 
 * to build a final classifier.
 * <p>
 * The method of aggregating the statistics for the final classifier is defined by the class implementing the {@link CandidateEvaluatorIF} specified in the experiment 
 * properties. For example, RSSalg experiment is run by specifying  {@link RSSalgCandidateEvaluator} in the experiment properties, while RSSalg<sub>best</sub> experiment 
 * is run by using {@link TestSetAccuracyCandidateEvaluator}. Supply the AccuracyMeasure to the CandidateEvaluatorIF to run the RSSalg and RSSalg<sub>best</sub>
 * experiments from the paper (property optimizationMeasure in genetic algorithm properties).
 * <p> 
 */
public class RSSalg extends Algorithm{
	/**
	 * SplitterIF class used for splitting the features is always DifferentRandomSplitsSplitter for RSSalg
	 */
	protected DifferentRandomSplitsSplitter splitter = new DifferentRandomSplitsSplitter();
	/**
	 * Whether the statistics should be created i.e. perform co-training with predefined number of random splits (if no statistics file is specified)
	 */
	protected boolean createStatistics = true;	
	
	@Override
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers) throws Exception{
		super.run(data, fold, splitNo, recordClassifiers);
		long startTime = System.currentTimeMillis();
	
		if(createStatistics){// if classifiers are not set, create the statistics		
			classifiers = new ClassifierEnsembleList();
			System.out.println("----------------------------");
			int noSplits = ExperimentSettings.getInstance().getNoSplits();
			CoTraining ct = new CoTraining();
			for(int i=0; i<noSplits; i++){
				CoTrainingData tmpData = new CoTrainingData(data);
		
				try{
					Random rand = DatasetSettings.getInstance().cloneRandom();
					splitter.splitDatasets(null, tmpData, rand, i);
				}catch(Exception e){
					throw new Exception("ERROR: error creating a random split", e);
				}
				
				ClassificationResult result = ct.run(tmpData, fold, i, true);
				ClassifierEnsemble CTclassifier = ct.getClassifiers().getEnsembles().get(0);
				classifiers.addClassifier(CTclassifier); 
							
				List<MeasureIF> measures = ExperimentSettings.getInstance().getMeasures();
				System.out.println("Split " + i + ": ");
				for(MeasureIF measure : measures){
					System.out.println("\t" + measure.getName() + ": " + measure.getMeasure(result));
				}
				System.out.println("----------------------------");
			}
		} 
		
		// check whether training on wrong classifiers file (test data should not be labeled by any of the classifiers)
		for(int i=0; i<data.getTestData()[0].size(); i++){
			double id = Double.parseDouble(InstancesManipulation.getInstanceID(data.getTestData()[0].get(i)));
			if(classifiers.containsID(id))
				throw new Exception("Training on test data id " + id);
		}
		
		try{
			GAThresholdOptimiser optimizer = new GAThresholdOptimiser(classifiers, data, currentFold);
			Candidate solution = optimizer.run();
			data.mergeViews();
			data = InstancesManipulation.setTrainingSet(solution.getPredictions(), data);
		}catch(Exception e){
			Exception ex = new Exception("ERROR: error running threshold optimizer for RSSalg fold " + currentFold + ":\n");
			ex.addSuppressed(e);
			throw ex;
		}
		
		if(ExperimentSettings.getInstance().isWriteEnlargedCoTrainingSet()){
			String fileName = DatasetSettings.getInstance().getResultFolder() + "/fold_" + currentFold + "/" + getName() + "_enlargedTrainingSet.arff";
			InstancesManipulation.writeArff(fileName, data.getLabeledData()[0]);			
		}
		
		ClassificationResult result = null;
		if(recordClassifiers){
			result = Evaluation.performTest(DatasetSettings.getInstance().getCombinedClassifier(), data.getLabeledData()[0], data.getTestData()[0], true);
			classifierTestData.addPredictions(result.getPredictions());
		}else
			result = Evaluation.performTest(DatasetSettings.getInstance().getCombinedClassifier(), data.getLabeledData()[0], data.getTestData()[0], false);
		
		long endTime = System.currentTimeMillis();
		runningTime = endTime - startTime;
		return result;
	}

	// splitter is not restarted in setData in order to use the same splits for all folds (see DifferentRandomSplitsSplitter)

	public String getName() {
		return "RSSalg_" + ExperimentSettings.getInstance().getEvaluator().getName();
	}

	@Override
	public void setClassifiers(ClassifierEnsembleList classifiers) {
		super.setClassifiers(classifiers);
		if(classifiers!=null)
			createStatistics = false;
	}
}
