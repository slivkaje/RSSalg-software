/** 	
 * Name: SplitterIF.java
 * 
 * Purpose:  Interface that any feature split algorithm must implement 
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

import java.util.Random;

import algorithms.co_training.CoTrainingData;
import featureSplit.utils.FeatureGraph;
import featureSplit.utils.FeatureSplit;

/*
 * Interface that any feature split algorithm must implement 
 */
public interface SplitterIF {
	/**
	 * Splits the feature set into multiple views
	 * @param graph the feature graph (if needed)
	 * @param data data to split in the views
	 * @param rand an instance of random number generator to use (if needed)
	 * @param splitNo the number of split (if multiple splits are created)
	 * @return the feature split
	 * @throws Exception if there was an error creating the split
	 */
	public FeatureSplit splitDatasets(FeatureGraph graph, CoTrainingData data, Random rand, int splitNo) throws Exception;
	/**
	 * Returns the name of the algorithm that generates a feature split
	 * @return the name of the algorithm
	 */
	public String getName();
}
