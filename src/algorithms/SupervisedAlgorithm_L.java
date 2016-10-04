/** 	
 * Name: SupervisedAlgorithm_L.java
 * 
 * Purpose: A supervised algorithm trained on labeled data and evaluated on test data. Unlabeled data is ignored.
 * 
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

import classificationResult.ClassificationResult;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;

/**
 * A supervised algorithm trained on labeled data and evaluated on test data. Unlabeled data is ignored. Labeled and test data are supplied through 
 * {@link algorithms.co_training.CoTrainingData} object.
 * <p>
 * This is the L<sub>acc</sub> algorithm described in paper J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural
 * Language Datasets". Acta Polytechnica Hungarica 10 (2)).
 *</p>
 */
public class SupervisedAlgorithm_L extends Algorithm{	

	/**
	 * Run the supervised Lacc experiment and return the classification result.
	 * <p>
	 * Generally, in the experiment:<br>
	 * 1. In the case of multiple view setting, all views are merged to form a unique attribute set<br>
	 * 2. Algorithm is trained in the supervised fashion on the labeled set and applied on the test set
	 */
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers){
		try {
			super.run(data, fold, splitNo, recordClassifiers);
		} catch (Exception e) {
			System.out.println("WARNING: Trying to read the classifiers from file. Algorithm does not rely on the recorded classifier statistic, ignoring classifiers");
		}
		long startTime = System.currentTimeMillis();
		
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
		return "Supervised_experiment_L";
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
