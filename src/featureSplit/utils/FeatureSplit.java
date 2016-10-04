/** 	
 * Name: FeatureSplit.java
 * 
 * Purpose: Represents a feature split: the set of views (feature subsets) of the original dataset that has a unique feature set.
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
 * Represents a feature split: the set of views (feature subsets) of the original dataset that has a unique feature set
 */
public class FeatureSplit {
	/**
	 * The set of views of the whole feature set
	 */
	protected Set<View> views = new HashSet<View>();
	
	/**
	 * Adds a view to the feature split
	 * @param view view to be added 
	 * @throws Exception if the view with the same view number already exists in the dataset
	 */
	public void addView(View view) throws Exception{
		int viewNo = view.getViewNo();
		if(getView(viewNo)!=null)
			throw new Exception("ERROR: View " + viewNo + "already exists in the dataset.");
		views.add(view);
	}

	/**
	 * Returns the number of views in the feature split
	 * @return number of views
	 */
	public int getNoViews(){
		return views.size();
	}
	
	/**
	 * Adds a feature to the desired view defined by the view number. If the desired view does not exist, this method will create a new view with the given number and
	 * add the feature to the newly created view  
	 * @param feature the feature to be added
	 * @param viewNo the number of the view to add the feature to
	 */
	public void addFeatureToView(Feature feature, int viewNo){
		View view = getView(viewNo);
		if(view == null){ // view not found, add new view
			view = new View(viewNo);
			view.addFeature(feature);
			views.add(view);
		}else{
			views.remove(view);
			view.addFeature(feature);
			views.add(view); 
		}
	}
	
	/**
	 * Returns a view with the desired number or null if the view with that number does not exist 
	 * @param viewNo the number of the desired view 
	 * @return the desired view
	 */
	public View getView(int viewNo){
		for(View view : views){
			if(view.getViewNo() == viewNo)
				return view;
		}
		return null;
	}
		
	/**
	 * Returns the view that contains the feature defined by the method parameter or null if such a view does not exist
	 * @param feature the feature to look for
	 * @return the view that contains the feature
	 */
	public View getView(Feature feature){
		for(View view : views)
			if (view.featureInView(feature))
				return view;
		return null;
	}
	
	@Override
	public String toString() {
		String res = "Views: " + "\n";
		for(View view : views)
			res += "\t" + view + "\n";
		return res;
	}

	/**
	 * Generates a hash code based on the view set of the feature split
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((views == null) ? 0 : views.hashCode());
		return result;
	}

	/**
	 * Compares two feature splits based on the view sets they contain. Two feature splits are equal if their view sets are equal even if their ordering (view indices) 
	 * is permuted, e.g. split1[1st view: view1, 2nd view: view2], split2[1st view: view2, 2nd view: view1] =&gt; splits split1 and split2 are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeatureSplit other = (FeatureSplit) obj;
		if (views == null) {
			if (other.views != null)
				return false;
		} else if (!views.equals(other.views)){
			return false;
		}
		return true;
	}

	/**
	 * Returns the set of views that describe a feature split
	 * @return the set of views of the feature split
	 */
	public Set<View> getViews() {
		return views;
	}
	
	/**
	 * Returns the feature indices (original indices in the dataset that contains features from all views, see {@link Feature}) of the features in the desired view or
	 * null if the desired view does not exist 
	 * @param viewNo number of the desired view
	 * @return the indices of the features from the view
	 */
	public Set<Integer> getFeatureIndicesForView(int viewNo){
		View v = getView(viewNo);
		if(v == null)
			return null;
		return v.getFeatureIndices();
	}
}
