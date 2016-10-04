/** 	
 * Name: FeatureGraph.java
 * 
 * Purpose: Graph representing the measures between the features in the dataset.
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
package featureSplit.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Graph representing the measures between the features in the dataset. A full attribute set can be represented as the feature graph where the 
 * nodes of the graph are the features. The relationship between the feature pair can be calculated by a given measure (e.g. mutual information) 
 * and coded as the edge between the two features where the weight of the edge corresponds to the measure value
 */
public class FeatureGraph {
	/**
	 * feature graph is represented as the map - key: featureName1_featureName2 (it is assumed that the feature names are unique), value: weight of the edge between 
	 * the two features
	 */
	protected Map<String, Double> graph = new HashMap<String, Double>();
	
	/**
	 * Add the weighted edge between the features
	 * @param feature1 the name of the first feature
	 * @param feature2 the name of the second feature
	 * @param weight the weight of the edge in the graph
	 */
	public void addEdge(String feature1, String feature2, double weight){
		graph.put(feature1 + "_" + feature2, weight);
	}
	
	/**
	 * Returns the weight of the edge of the graph between the two features
	 * @param feature1 the name of the first feature
	 * @param feature2 the name of the second feature
	 * @return weight of the edge between the features
	 */
	public double getEdge(String feature1, String feature2){
		return graph.get(feature1 + "_" + feature2);
	}
}
