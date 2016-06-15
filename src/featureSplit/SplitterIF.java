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
