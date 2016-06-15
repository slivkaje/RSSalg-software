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
