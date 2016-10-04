/** 	
 * Name: CandidateEvaluatorIF.java
 * 
 * Purpose: Interface that the classes for candidate evaluation in RSSalg must implement. 
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

import classificationResult.measures.MeasureIF;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;

/**
 * Interface that the classes for candidate evaluation in RSSalg must implement
 */
public interface CandidateEvaluatorIF {
	
	/**
	 * Evaluates the candidate
	 * @param data The data used in the experiment (labeled, unlabeled and test data). During evaluation, labeled 
	 * 		  and unlabeled data will be modified according to the candidate that is being evaluated. 
	 * 		  The evaluator should not modify the original data object (use the deep copy instead)  
	 * @param statistic  Statistics (instance ids, predictions and confidences) about the instances (from unlabeled set) labeled during the co-training processes.
	 * @param candidat candidate (label agreement/example occurrence threshold threshold pair) for evaluation
	 * @param measure measure used for candidate evaluation
	 * @throws Exception if there was an error during candidate evaluation
	 */
	public void evaluateCandidate(CoTrainingData data, ClassifierEnsembleList statistic, Candidate candidat, MeasureIF measure) throws Exception;
	
	/**
	 * Returns the name of the candidate evaluator
	 * @return name of the algorithm for candidate evaluation
	 */
	public String getName();
}
