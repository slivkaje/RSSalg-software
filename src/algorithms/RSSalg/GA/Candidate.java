/** 	
 * Name: Candidate.java
 * 
 * Purpose: Class representing the candidate for GA optimization in RSSalg. 
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

import java.text.DecimalFormat;
import java.util.Map;

import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;

/**
 * Class representing the candidate for GA optimization in RSSalg.
 * Each candidate is a label agreement threshold and example occurrence threshold pair. The thresholds are used for elimination of "unreliably labeled" 
 * examples from the final training set in RSSalg 
 */
public class Candidate implements Comparable<Candidate>{
	/**
	 * Label agreement threshold
	 */
	protected double labelThreshold;
	/**
	 * Example occurrence threshold
	 */
	protected double exampleThreshold;
	/** 
	 * Evaluated fitness of the candidate. -1 if not evaluated
	 */
	protected double fitness = -1;
	/**
	 * Actual fitness of the candidate (evaluated on the supplied test set). -1 if not evaluated
	 */
	protected double actualFitness;
	/**
	 * Instance ids and their predictions for instances kept in the training set (defined by the candidate thresholds). Null if not evaluated.
	 */
	protected Map<Double, String> predictions = null;
	/**
	 * Instance ids and their predictions for the instances left out from the final training set (defined by the candidate thresholds). Null if not evaluated.
	 */
	protected Map<Double, String> leftOut = null;
	
	/**
	 * Creates a new instance of the Candidate
	 * @param labelThreshold Label agreement threshold
	 * @param exampleThreshold Example occurrence threshold
	 * @param classifiers Statistics (instance ids, predictions and confidences) about the instances (from unlabeled set) labeled during the co-training processes
	 */
	public Candidate(double labelThreshold, double exampleThreshold, ClassifierEnsembleList classifiers){
		this.labelThreshold = labelThreshold;
		this.exampleThreshold = exampleThreshold;
		setPredictions(classifiers.getExamplesThatExceedThresholds(labelThreshold, exampleThreshold));
		setLeftOut(classifiers.getExamplesThatDontExceedThresholds(labelThreshold, exampleThreshold));
	}
	
	/**
	 * Returns label agreement threshold 
	 * @return label agreement threshold
	 */
	public double getLabelThreshold() {
		return labelThreshold;
	}
	/**
	 * Sets the label agreement threshold
	 * @param labelThreshold the new label agreement threshold
	 */
	public void setLabelThreshold(double labelThreshold) {
		this.labelThreshold = labelThreshold;
	}
	/**
	 * Returns the example occurrence threshold 
	 * @return example occurrence threshold
	 */
	public double getExampleThreshold() {
		return exampleThreshold;
	}
	/**
	 * Sets the example occurrence threshold 
	 * @param exampleThreshold new example occurrence threshold
	 */
	public void setExampleThreshold(double exampleThreshold) {
		this.exampleThreshold = exampleThreshold;
	}
	/**
	 * Sets the fitness measure of the candidate
	 * @param fitness the assessed fitness 
	 * @param actualFitness the actual (true) fitness evaluated on the test set
	 */
	public void setCandidateEvaluation(double fitness, double actualFitness){
		this.fitness = fitness;
		this.actualFitness = actualFitness;
	}
	/**
	 * Returns the assessed fitness of the candidate or -1 if the candidates fitness is not assessed yet
	 * @return assessed fitness of the candidate
	 */
	public double getFitness() {
		return fitness;
	}
	/**
	 * Returns the actual (true) fitness of the candidate evaluated on the supplied test set or -1 if the true fitness of the candidate is not evaluated
	 * @return true fitness of the candidate 
	 */
	public double getActualFitness() {
		return actualFitness;
	}
	/**
	 * Returns ids and predictions for instances kept in the final training set (ones that are not eliminated by candidate thresholds). Returns null if 
	 * candidate is not evaluated yet
	 * @return map (key: id, value: the predicted class) that represents predictions for instances kept in the final training set
	 */
	public Map<Double, String> getPredictions() {
		return predictions;
	}
	/**
	 * Sets the ids and predictions for instances kept in the final training set (ones that are not eliminated by candidate thresholds).
	 * @param predictions map (key: id, value: the predicted class) that represents the predictions for instances kept in the final training set 
	 */
	private void setPredictions(Map<Double, String> predictions) {
		this.predictions = predictions;
	}
	/**
	 * Returns ids and predictions for instances eliminated from the final training set (by candidate thresholds). Returns null if 
	 * candidate is not evaluated yet
	 * @return  map (key: id, value: the predicted class) that represents predictions for instances left out of the final training set
	 */	
	public Map<Double, String> getLeftOut() {
		return leftOut;
	}
	/**
	 * Sets the ids and predictions for instances eliminated from the final training set (by candidate thresholds)
	 * @param leftOut predictions map (key: id, value: the predicted class) that represents predictions eliminated from the final training set
	 */
	private void setLeftOut(Map<Double, String> leftOut) {
		this.leftOut = leftOut;
	}

	/**
	 * Returns the String that represents the candidate: thresholds, candidates fitness (assessed and true) and number of instances that are kept in the final
	 * training set for this candidate of thresholds pair
	 * @return the candidate as string
	 */
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("###.##");
		String retStr = "Thresholds: label " + df.format(labelThreshold * 100) + "% of classifiers" + " example: " + df.format(exampleThreshold *100) + "% of the instances.";
		if(fitness != -1){
			retStr += " Fitness: " + df.format(fitness) + "; ";
			if (actualFitness != -1)
				retStr += "Actual fitness " + df.format(actualFitness) + ";";
			else
				retStr += "Actual fitness not evaluated; ";
		}else
			retStr += " Candidate not evaluated. ";
		retStr += " Instances in the final dataset: " + predictions.size() + ".";
		return retStr;
	}
	
	/** 
	 * Checks whether two candidates result with the same final training set (i.e. the same final classifier)
	 * <p>  
	 * Candidates with different example/label threshold may still have the same set of examples which will be kept in the final training set.
	 * For example, consider the statistics
	 * <ul>
	 * <li>example 1 is labeled by 100% of classifiers and label agreement is 80%</li>
	 * <li>example 2 is labeled by 80% of classifiers and label agreement is 70%</li>
	 * <li>example 3 is labeled by 50% of classifiers and label agreement is 70%</li>   
	 * </ul><br> 
	 * Two candidates: candidate1 (example occurrence: 80%, label agreement 70%) and candidate2 (example occurrence: 60%, label agreement 70%) both leave
	 * examples 1 and 2 in the final training set and thus result with the same final classifier.
	 * <p> 
	 * This method is convenient to use if classifier training/application on the test set is expensive in order to skip the expensice evaluation of the 
	 * same classifiers 
	 * @param otherCandidate candidate to compare with
	 * @return whether the candidates result with the same final classifier
	 */
	private boolean sameInstances(Candidate otherCandidate){
		if (this.predictions.size() != otherCandidate.predictions.size())
			return false;
		
		for(double instanceId : this.predictions.keySet()){
			if (!otherCandidate.predictions.containsKey(instanceId))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((predictions == null) ? 0 : predictions.keySet().hashCode());
		return result;
	}


	/** 
	 * Checks whether two candidates result with the same final training set (and accordingly the same final classifier)
	 * <p>  
	 * Candidates with different example/label threshold may still have the same set of examples which will be kept in the final training set.
	 * For example, consider the statistics: 
	 * <ul> 
	 * <li>example 1 is labeled by 100% of classifiers and label agreement is 80%</li>
	 * <li>example 2 is labeled by 80% of classifiers and label agreement is 70%</li>
	 * <li>example 3 is labeled by 50% of classifiers and label agreement is 70%</li>   
	 * </ul> 
	 * Two candidates: candidate1 (example occurrence: 80%, label agreement 70%) and candidate2 (example occurrence: 60%, label agreement 70%) both leave only
	 * examples 1 and 2 for the final training set and thus result with the same final classifier.
	 * <p> 
	 * This method is convenient to use if classifier training/application on the test set is expensive in order to skip the expensice evaluation of the 
	 * same classifiers 
	 * @param obj candidate to compare with
	 * @return whether the candidates result with the same final classifier
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Candidate other = (Candidate) obj;
		if (predictions == null) {
			if (other.predictions != null)
				return false;
		} else if (!sameInstances(other))
			return false;
		return true;
	}

	/**
	 * Compares the candidates according to their assessed fitness
	 */
	@Override
	public int compareTo(Candidate otherCandidate) {
		if(otherCandidate.fitness > fitness)
			return 1;
		else if(otherCandidate.fitness < fitness)
			return -1;
		else
			return 0;
	}
}
