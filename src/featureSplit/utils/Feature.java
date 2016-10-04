/** 	
 * Name: Feature.java
 * 
 * Purpose:  Represents a feature: feature name and its index in the dataset that contains features from all views (index of feature in the Instances object acquired by merging all the views in the unique feature set). It is assumed that the feature name is unique in the dataset.
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

import featureSplit.RandomSplit;

/**
 * Represents a feature: feature name and its index in the dataset that contains features from all views (index of feature in the Instances object acquired by 
 * merging all the views in the unique feature set). It is assumed that the feature name is unique in the dataset.
 * <p> 
 * Note: the feature index is dependent on the ordering of the views that are being merged. Expected usage:  
 * <ol>
 * <li>create an Instances object by moving all features to the first view. Each feature is assigned an index (acquired from the Instances object)
 * <li>create a feature split - a {@link FeatureSplit} object
 * <li>use {@link FeatureSplit#getFeatureIndicesForView(int)} method to get the indices of the features in original Instances object (from the 1st step) 
 * 			    that should be moved to the desired view  
 * </ol> 
 * For example, see the implementation of {@link RandomSplit#splitDatasets} method
 */
public class Feature {
	/**
	 * The name of the feature. It is assumed that the feature name is unique in the dataset
	 */
	protected String name;
	/**
	 * Feature index in the dataset with merged views 
	 */
	protected int index; // feature index in dataset
	
	/**
	 * Create a new instance of Feature object
	 * @param name the name of the feature
	 * @param index index of the feature in the dataset that contains features from all views
	 */
	public Feature(String name, int index) {
		this.name = name;
		this.index = index;
	}
	/**
	 * Returns the name of the feature
	 * @return feature name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Returns the index of the feature in the dataset that contains features from all views
	 * @return feature index in the dataset that contains features from all views
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return name + "(index " + index + ")";
	}
	/**
	 * Generates the hashCode of the feature based on its name which should be unique in the dataset. Does not consider the feature index
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	/**
	 * Compares two features based on their names. It is assumed that the feature name is unique in the dataset. Does not consider the feature indices
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Feature other = (Feature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
