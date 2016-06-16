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
