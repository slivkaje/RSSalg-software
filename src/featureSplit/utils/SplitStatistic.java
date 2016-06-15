package featureSplit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Object that aggregates the measures of cutting the dataset in multiple views. A full attribute set can be represented as the feature graph, see 
 * {@link FeatureGraph}. When dividing the feature set in multiple views, relationships between the features belonging to the separate views are ignored, 
 * i.e. some of the edges of the graph are cut. This object aggregates the measures about performing a certain feature split (division in multiple views):
 * <ul>
 * <li> total sum of edge weights for all edges that were discarded
 * <li> for each view pair: the sum of cut edges between the views
 * <li> for each view: sum of remaining edge weights within the view 
 * </ul>    
 * Note: This class is not tested!
 */
public class SplitStatistic {
	/**
	 * Total sum of weights on edges that were cut by a split
	 */
	protected double sumOfCutEdges = 0;
	/**
	 * For each view pair: the sum of cut edges between the views
	 */
	protected double sumOfCutEdges2Views[][];
	/**
	 * For each view: sum of remaining edge weights within the view
	 */
	protected double sumOfEdgesView[];  
	
	/**
	 * View number
	 */
	protected int[] viewNumbers;

	/**
	 * Creates the new instance of SplitSatistic: given the feature graph and the feature split calculates the measures of applying the feature split
	 * @param graph the feature graph
	 * @param split the feature split to be applied on the graph
	 */
	public SplitStatistic(FeatureGraph graph, FeatureSplit split){
		
		// copy the list of the views (in order not to modify the feature split)
		List<View> views = new ArrayList<View>();
		views.addAll(split.getViews());
		
		sumOfCutEdges2Views = new double[views.size()][views.size()];
		sumOfEdgesView = new double[views.size()];
		viewNumbers = new int[views.size()];
		
		for(int i=0; i<views.size(); i++){
			viewNumbers[i] = views.get(i).getViewNo();
			// features from the ith view
			Set<Feature> features_i = views.get(i).features;
			// sum of cut edges between the ith and the ith view (same view) is 0  
			sumOfCutEdges2Views[i][i] = 0;
			// calculate the sum of edges within the ith view 
			calcInterEdgesViewSum(graph, features_i, i);
			//calculate the sum of cut edges between the ith view and all other views
			for(int j=i+1; j<views.size(); j++){
				Set<Feature> features_j = views.get(j).features; // features of the second view
				calcIntraEdgesViewSum(graph, features_i, features_j, i, j);
			}
		}
		
	}
	
	/**
	 * Calculates the sum of cut edges between the two views 
	 * @param graph the feature graph
	 * @param features_v1 features of the first view
	 * @param features_v2 features of the second view
	 * @param v1 index of the first view
	 * @param v2 index of the second view
	 */
	private void calcIntraEdgesViewSum(FeatureGraph graph, Set<Feature> features_v1, Set<Feature> features_v2, int v1, int v2){		
		double sumCutEdges12 = 0; // the total sum of edges between the views
		for(Feature feature1 : features_v1)
			for(Feature feature2 : features_v2){
				sumCutEdges12 += graph.getEdge(feature1.getName(), feature2.getName());
			}
		sumOfCutEdges2Views[v1][v2] = sumCutEdges12;
		sumOfCutEdges2Views[v2][v1] = sumCutEdges12;
		sumOfCutEdges += sumCutEdges12; // enlarge the total sum of cut edges
	}
	
	/**
	 * Calculates the sum of edges within the view
	 * @param graph the feature graph
	 * @param features features in the view
	 * @param view view index in sumOfEdgesView array
	 */
	private void calcInterEdgesViewSum(FeatureGraph graph, Set<Feature> features, int view){
		for(Feature feature1 : features)
			for(Feature feature2 : features){
				sumOfEdgesView[view] += graph.getEdge(feature1.getName(), feature2.getName());
			}
	}

	/**
	 * Returns the total sum of edge weights for all edges that were discarded
	 * @return total sum of edge weights for all edges that were discarded
	 */
	public double getSumOfCutEdges() {
		return sumOfCutEdges;
	}
	
	/**
	 * Mapping view number (from the feature split) to the view index in this representation (arrays)
	 * @param viewNo the number of the view in the feature split
	 * @return view index in arrays
	 * @throws Exception if the desired view does not exist 
	 */
	private int findViewInd(int viewNo) throws Exception{
		for(int i=0; i<viewNumbers.length; i++)
			if(viewNumbers[i] == viewNo)
				return i;
		throw new Exception("View " + viewNo + " does not exist");
	}
	
	/**
	 * Returns the sum of cut edges between the two views
	 * @param view1No the 1st view number (from the feature split)
	 * @param view2No the 2nd view number (from the feature split)
	 * @return sum of cut edges between the 1st and the 2nd view
	 * @throws Exception if one of the views does not exist
	 */
	public double getSumOfCutEdges2Views(int view1No, int view2No) throws Exception{
		int i = findViewInd(view1No);
		int j = findViewInd(view2No);
		return sumOfCutEdges2Views[i][j];
	}
	
	/**
	 * Returns the sum of edge weights within the view
	 * @param viewNo  the view number (from the feature split)
	 * @return sum of edge weights within the view
	 * @throws Exception if the desired view does not exist 
	 */
	public double getSumOfEdgesView(int viewNo) throws Exception{
		int i = findViewInd(viewNo);
		return sumOfEdgesView[i];
	}
}
