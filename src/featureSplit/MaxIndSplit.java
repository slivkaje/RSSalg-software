package featureSplit;

import java.util.Random;

import algorithms.co_training.CoTrainingData;
import featureSplit.utils.FeatureGraph;
import featureSplit.utils.FeatureSplit;

public class MaxIndSplit implements SplitterIF {

	@Override
	public FeatureSplit splitDatasets(FeatureGraph graph, CoTrainingData data, Random rand, int splitNo) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "MaxInd";
	}

}
