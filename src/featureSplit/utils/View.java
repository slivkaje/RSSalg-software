/** 	
 * Name: View.java
 * 
 * Purpose: Represents one view of the data - a subset of the complete feature set that describes the data.
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

import java.util.HashSet;
import java.util.Set;

/**
 * Represents one view of the data - a subset of the complete feature set that describes the data 
 */
public class View {
	/**
	 * View number in the feature split that contains several views (starting from 0)
	 */
	protected int viewNo;
	/**
	 * The set of features in the view
	 */
	protected Set<Feature> features = new HashSet<Feature>();

	/**
	 * Creates a new instance of the view
	 * @param viewNo view number in the feature split (one feature split contains multiple views)
	 */
	public View(int viewNo) {
		this.viewNo = viewNo;
	}
	/**
	 * Adds a feature to the view
	 * @param feature a new feature
	 */
	public void addFeature(Feature feature){
		features.add(feature);
	}
	/**
	 * Checks whether the view contains the feature
	 * @param feature the feature to look for
	 * @return true if feature belongs to the view, false otherwise
	 */
	public boolean featureInView(Feature feature){
		return features.contains(feature);
	}
	/**
	 * Returns the view number in the feature split (one feature split contains multiple views)
	 * @return view number in the feature split
	 */
	public int getViewNo(){
		return viewNo;
	}
	/**
	 * Returns the set of features in the view
	 * @return the set of features in the view
	 */
	public Set<Feature> getFeatures() {
		return features;
	}
	/**
	 * Returns the indices (original feature indices in the dataset that contains features from all views) of the features in this view
	 * @return the indices of the features from the view
	 */
	public Set<Integer> getFeatureIndices(){
		Set<Integer> res = new HashSet<Integer>();
		for(Feature feature : features){
			res.add(feature.getIndex());
		}
		return res;
	}
	@Override
	public String toString() {
		String res = "Features in view " + viewNo + ": ";
		for(Feature feature : features)
			res += feature + "; ";
		return res;
	}
	/**
	 * Generates the hashCode based on the feature set the view contains. Does not consider the number of the view - the two views that contain the exact same 
	 * feature subset are equal, although their assigned numbers may be different 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
		return result;
	}
	
	/**
	 * Compares two views based on the feature set they contain. The two views that contain the exact same feature subset are equal, 
	 * although their indexes may be different
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		View other = (View) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		return true;
	}
}
