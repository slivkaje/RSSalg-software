/** 	
 * Name: DifferentRandomSplitsSplitter.java
 * 
 * Purpose:  Creates several balanced random splits of features in two views. Balanced split - views have the same number of features.
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
package featureSplit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import algorithms.co_training.CoTrainingData;
import experimentSetting.ExperimentSettings;
import featureSplit.utils.FeatureGraph;
import featureSplit.utils.FeatureSplit;

/*
 * Creates several balanced random splits of features in two views. Balanced split - views have the same number of features.  
 * Keeps track of created splits so that each time a unique feature split is created. 
 *  
 */
public class DifferentRandomSplitsSplitter implements SplitterIF {
	/**
	 * A list of already created splits
	 */
	List<FeatureSplit> splits = new ArrayList<FeatureSplit>();
		
	/**
	 * Generates a new (unique) random split of features in the two balanced views
	 * @param data data to split into views
	 * @param rand instance of random number generator to use
	 * @return the new random feature split
	 */
	private FeatureSplit generateNewSplit(CoTrainingData data, Random rand){
		while (true){
			RandomSplit splitter = new RandomSplit();
			FeatureSplit newSplit = splitter.generateNewSplit(data, rand);
			boolean splitExists = splits.contains(newSplit);
			if(!splitExists){
				splits.add(newSplit);				
				return newSplit;
			}
//			else{
//				System.out.println("New split: ");
//				System.out.println(newSplit);
//				System.out.println("Existing same split: ");
//				for(FeatureSplit fs :splits){
//					if(fs.equals(newSplit))
//						System.out.println(fs);
//				}
//			}
		}
	}
	
	/**
	 * Creates a balanced random split of features in two views. Balanced split - views have the same number of features.  
	 * Keeps track of already created splits so that each time a unique feature split is returned
	 * @param graph this parameter is ignored - random feature split does not rely on a certain measure so it does not need a graph
	 * @param data data to split in the views
	 * @param rand an instance of random number generator to use
	 * @param splitNo the number of split
	 * @return the feature split
	 * @throws Exception if there was an error creating the split
	 */
	public FeatureSplit splitDatasets(FeatureGraph graph, CoTrainingData data, Random rand, int splitNo) throws Exception {
		data.mergeViews();
		
		FeatureSplit split = null;
		if(splits.size() < ExperimentSettings.getInstance().getNoSplits()){ 
			split = generateNewSplit(data, rand);
		}else{
			split = splits.get(splitNo);
		}
		Set<Integer> indicesOfFeaturesToMove = split.getFeatureIndicesForView(1);
		data.moveAttributes(0, 1, indicesOfFeaturesToMove);
		return split;
	}

	public String getName() {
		return "DifferentRandomSplits";
	}
}
