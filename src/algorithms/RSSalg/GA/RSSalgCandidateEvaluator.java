/** 	
 * Name: RSSalgCandidateEvaluator.java
 * 
 * Purpose: Candidate evaluation for RSSalg in paper: J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language Datasets". Acta Polytechnica Hungarica 10 (2)
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
package algorithms.RSSalg.GA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import util.Evaluation;
import util.InstancesManipulation;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;
import classificationResult.ClassificationResult;
import classificationResult.measures.MeasureIF;
import experimentSetting.DatasetSettings;
import experimentSetting.GASettings;

/**
 * Candidate evaluation for RSSalg in paper: 
 * <p>
 * J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language Datasets". Acta Polytechnica Hungarica 10 (2)
 * <p>
 * The label of each instance is determined by the majority vote of the obtained ensemble of co-traning classifiers.
 * Intuition: the instances that are labeled by more classifiers and for which those classifiers have higher label agreement are more reliably labeled. The
 * goal is to eliminate unreliably labeled instances from the training set in order to eliminate the noise. The elimination is executed by setting an example
 * occurrence/label agreement threshold par: if the instance is labeled by equal or more classifiers than the set example occurrence threshold and if the label 
 * agreement for the instance is equal or higher than the label agreement threshold the instance is kept in the training set, otherwise it is eliminated.
 * Each candidate represents the label/example threshold pair. The goal is to find a candidate that gives the best results for the given dataset.
 * <p>
 * Setting the label threshold/example threshold pair causes some of the data from co-training result sets that exceeds these thresholds to be selected as 
 * training data for the final model creation, and some of the data to be omitted from this selection. In RSSalg, the created final training set is evaluated
 * on the test set created from the omitted data (the instances whose label agreement percent and example occurrence percent did not exceed the thresholds).
 * <p>
 * However, some label threshold/example threshold pairs might cause all of the examples to be selected for the final training set, thus leaving no examples 
 * left for evaluation of the individual. Also, too small test sets with high possibility of noise, could impose a bad estimation of the performance of the created model. 
 * Thus, a testing threshold is defined - a minimal number of examples needed in the test set for the evaluation of an individual. 
 * The testing threshold is defined dependent on the size of the total number of examples in statistic S. For example, if we use a testing threshold of  20%, each individual 
 * that uses more then 80% examples from the statistic S for the model, and less then 20% of the examples form S for testing the model, is considered as poorly estimated.
 * In such cases, the examples are transfered from the training to the test set until there are enough examples in the test set for estimation. The examples transferred from 
 * the training set are those estimated to be the least confident ones - based on the label agreement percent and 
 * example agreement percent: those examples that have the smallest sum of these two values are considered to be the least confident ones.
 * 
 */
public class RSSalgCandidateEvaluator implements CandidateEvaluatorIF { 
	/**
	 * A list of candidates that are already evaluated. Candidates with different example/label threshold may still have the same set of kept examples (which will result 
	 * with the same final classifier). Since evaluation (training and testing a model) might potentially be expensive, we keep the 
	 * record of evaluated candidates in order to just copy the fitness value in case that the candidate for evaluation results with the same model as some 
	 * already evaluated candidate (see also {@link Candidate#equals(Object)})  
	 */
	protected List<Candidate> evaluatedCandidates = new ArrayList<Candidate>(); 
	
	public void evaluateCandidate(CoTrainingData data, ClassifierEnsembleList classifiers, Candidate candidate, MeasureIF measure) throws Exception {	
		Map<Double, String> predictions =  candidate.getPredictions(); // instances in the final training set: key: instance id; value: prediction for instance
		Map<Double, String> leftOut = candidate.getLeftOut(); // instances left out from the final training set: key: instance id; value: prediction for instance
		
		// Left out examples (used for testing) should be at least TestingTS % of the statistics 
		int minTest = (int) Math.round(GASettings.getInstance().getTestingTS() * (classifiers.getStatisticSize()));
		if(leftOut.size() < minTest ){ // transfer from predictions to left out
			int num = minTest - leftOut.size();		
			List<Double> leastConfident = classifiers.getLeastConfident(num, predictions);
			for(double id: leastConfident){
				String pred = predictions.get(id);
				leftOut.put(id, pred);
				predictions.remove(id);				
			}
			
			// redefine the thresholds accordingly
			double minLabel = Double.MAX_VALUE;
			double minExample = Double.MAX_VALUE;
			for(double id : predictions.keySet()){
				double labelAgg = classifiers.getLabelAgreementPercent(id);
				double exOcc = classifiers.getExampleOccurencePercent(id);
				if(labelAgg < minLabel)
					minLabel = labelAgg;
				if(exOcc < minExample)
					minExample = exOcc;
			}
			candidate.setExampleThreshold(minExample);
			candidate.setLabelThreshold(minLabel);
		}
		
		int index = evaluatedCandidates.indexOf(candidate); // Finds the candidate that results with the same final training set. Returns -1 if there is no such evaluated candidate.
		if(index != -1){ // Found evaluated candidate that results with the same final training set: copy fitness values from that candidate and exit function 
			Candidate oldEvaluatedCandidate = evaluatedCandidates.get(index);
			candidate.setCandidateEvaluation(oldEvaluatedCandidate.getFitness(), oldEvaluatedCandidate.getActualFitness());
			return;
		}
		
		CoTrainingData tmpData = new CoTrainingData(data); // copy the data (in order not to modify the original data)
		tmpData = InstancesManipulation.setTrainingSet(predictions, tmpData); // set kept instances as the training set
		weka.classifiers.Classifier classifier = DatasetSettings.getInstance().getCombinedClassifier();
		
		double actualFitness = -1;
		if(GASettings.getInstance().isLogGA()){ // logging GA -> should evaluate actual fitness 
			ClassificationResult result =  Evaluation.performTest(classifier, tmpData.getLabeledData()[0], tmpData.getTestData()[0], false);
			actualFitness = measure.getMeasure(result);
		}
		
		tmpData = InstancesManipulation.setTestSet(leftOut, tmpData); // set left out instances as the test set
		tmpData.mergeViews();

		ClassificationResult result =  Evaluation.performTest(classifier, tmpData.getLabeledData()[0], tmpData.getTestData()[0], false);
		double fitness = measure.getMeasure(result);
		candidate.setCandidateEvaluation(fitness, actualFitness);
		evaluatedCandidates.add(candidate);
		return;
	}

	public String getName() {
		return "left_out_instances";
	}

}
