/** 	
 * Name: TestSetAccuracyCandidateEvaluator.java
 * 
 * Purpose: Candidate evaluation for RSSalg_best algorithm from the paper: J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language Datasets". Acta Polytechnica Hungarica 10 (2).
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

/**
 * Candidate evaluation for RSSalg<sub>best</sub> algorithm from the paper: 
 * <p>
 * J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language 
 * Datasets". Acta Polytechnica Hungarica 10 (2)
 * <p>
 * Each individual represents the label/example threshold pair. Setting the label threshold/example threshold pair causes some of the data 
 * from co-training result sets that exceeds these thresholds to be selected as training data for the final model creation, and some of the data to be 
 * omitted from this selection. The model is trained on the instances that exceed the thresholds defined by candidate and evaluated on the test set 
 * (used for final model evaluation).
 * Because of optimization on the test set, RSSalg<sub>best</sub> can be used only as upper limit performance for RSSalg, it cannot be applied to a real-world 
 * co-training setting.
 */
public class TestSetAccuracyCandidateEvaluator implements CandidateEvaluatorIF { 
	/**
	 * A list of candidates that are already evaluated. Candidates with different example/label threshold may still have the same set of kept examples (which will result 
	 * with the same final classifier, i.e. the same final classification model). Since evaluation (training and testing a model) might potentially be expensive, we keep the 
	 * record of evaluated candidates in order to just copy the fitness value in case that the candidate for evaluation results with the same model as some already evaluated candidate
	 * (see also {@link Candidate#equals(Object)}).  
	 */
	protected List<Candidate> evaluatedCandidates = new ArrayList<Candidate>();
	
	public void evaluateCandidate(CoTrainingData data, ClassifierEnsembleList classifiers, Candidate candidate, MeasureIF measure) throws Exception {	
		int index = evaluatedCandidates.indexOf(candidate); // Finds the candidate that results with the same final training set. Returns -1 if there is no such evaluated candidate.
		if(index != -1){ // Found evaluated candidate that results with the same final training set: copy fitness values from that candidate and exit function 			
			Candidate oldEvaluatedCandidate = evaluatedCandidates.get(index);
			candidate.setCandidateEvaluation(oldEvaluatedCandidate.getFitness(), oldEvaluatedCandidate.getActualFitness());
			return;
		}
		
		CoTrainingData tmpData = new CoTrainingData(data); // copy the data (in order not to change the original data)
		Map<Double, String> predictions =  candidate.getPredictions(); // instances in the final training set: key: instance id; value: prediction for instance				
		tmpData = InstancesManipulation.setTrainingSet(predictions, tmpData);
			
		weka.classifiers.Classifier classifier = DatasetSettings.getInstance().getCombinedClassifier();
		ClassificationResult result =  Evaluation.performTest(classifier, tmpData.getLabeledData()[0], tmpData.getTestData()[0], false);
		double fitness = measure.getMeasure(result);
		candidate.setCandidateEvaluation(fitness, fitness); 
		evaluatedCandidates.add(candidate);
		return;
	}

	public String getName() {
		return "best";
	}

}
