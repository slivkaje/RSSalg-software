/** 	
 * Name: MajorityVote.java
 * 
 * Purpose: The prediction for each instance is acquired by aggregating the votes of the trained classifiers 
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
package algorithms.RSSalg;

import util.InstancesManipulation;
import weka.core.Instance;
import algorithms.Algorithm;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.RSSalg.resultStatistic.Label;
import algorithms.RSSalg.resultStatistic.Votes;
import algorithms.RSSalg.voter.VoterIF;
import algorithms.co_training.CoTrainingData;
import classificationResult.ClassificationResult;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;

/**
 * The prediction for each instance is acquired by aggregating the votes of the trained classifiers 
 * <p> The aggregation method is specified by the algorithm implementing the {@link algorithms.RSSalg.voter.VoterIF}. 
 * For example, when applying to multiple co-trained classifiers with different splits and using {@link algorithms.RSSalg.voter.MajorityVoter}, 
 * this is the MV experiment from the  paper
 * J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language Datasets". Acta Polytechnica Hungarica 10 (2))).
 * <p>
 * Relies on reading the created statistics about testing each of the classifiers. It is mandatory to set the ClassifiersFilename parameter in 
 * ExperimentSettings to the location of statistic file
 * <p>
 * The confidence of the resulting classifier is calculated as entropy: for each label confidence = sum(-probability_i*Math.log(probability_i)), 
 * where probability_i is the confidence of the ith classifier for the given label 
 *   
 *
 */
public class MajorityVote extends Algorithm{
	
	
	/**
	 * @throws Exception loading statistics about test data failed - check whether the specified file exists
	 */
	@Override
	public ClassificationResult run(CoTrainingData data, int fold, int splitNo, boolean recordClassifiers) throws Exception {
		super.run(data, fold, splitNo, recordClassifiers);
		long startTime = System.currentTimeMillis();
		
		String filename = DatasetSettings.getInstance().getResultFolder()+"/fold_"+ currentFold + "/" + ExperimentSettings.getInstance().getClassifiersFilename();
		try{				
			ClassifierEnsembleList classifiersTest = new ClassifierEnsembleList();
			classifiersTest.fromXML(filename);
			
			if(classifiersTest.getStatisticSize() != data.getTestData()[0].size())
				throw new Exception("Test data size and recorded statistic don't match. Test data size: " + data.getTestData()[0].size() + " recorded stats: " +  classifiersTest.getStatisticSize());
			
			ClassificationResult result = null;
			if(recordClassifiers)
				result = new ClassificationResult(true);
			else
				result = new ClassificationResult(false);
				
			for(Instance inst : data.getTestData()[0]){
				double id = Double.parseDouble(InstancesManipulation.getInstanceID(inst));
				if(!classifiersTest.containsID(id))
					throw new Exception("Instance id " + id + " from test data missing in recorded statistics");
				String actualLabel = InstancesManipulation.getLabel(inst); 
				VoterIF voter = ExperimentSettings.getInstance().getVoter();
				
				Votes votesForInstance = classifiersTest.getVotes(id);
				Label assignedLabel = voter.vote(votesForInstance);
				
				if(actualLabel != null)
				if (!assignedLabel.getName().equals(actualLabel))
					result.updateFalse(id, assignedLabel.getName(), classifiersTest.getVotes(id).getEntropies());
				else{
					result.updateTrue(id, assignedLabel.getName(), classifiersTest.getVotes(id).getEntropies());
				}
				
				// confidence is calculated as the entropy for the given label
				if(recordClassifiers){
//					classifierTestData.addPrediction(id, classifiersTest.getVotes(id).getConfidences());
					classifierTestData.addPrediction(id, classifiersTest.getVotes(id).getEntropies());
				}
			}
			
			long endTime = System.currentTimeMillis();
			runningTime = endTime - startTime;
			
			return result;
		}catch(Exception e){
			throw new Exception("ERROR: error loading statistics about test data. Probably the file " + filename + " is missing", e);
		}
	}

	@Override
	public String getName() {
		return ExperimentSettings.getInstance().getVoter().getName() + "_of_Co-training_classifiers_on_test_set";
	}

	/**
	 * This algorithm does not rely on the recorded training classifier statistic, it will be ignored
	 */
	@Override
	protected void setClassifiers(ClassifierEnsembleList classifiers) {
		if(classifiers != null)
			System.out.println("WARNING: " + getName() + " algorithm does not rely on the recorded training classifier statistic. Ignoring classifiers");
	}
}
