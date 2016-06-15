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
