package classificationResult.measures;

import classificationResult.ClassificationResult;

/**
 * Accuracy measure: accuracy = num_of_correctly_labeled_instances / total_number_of_labeled_instances 
 */
public class AccuracyMeasure implements MeasureIF{

	public String getName() {
		return "accuracy";
	}

	public double getMeasure(ClassificationResult result) {
		return (double) result.getNoCorrectlyLabeled()*100 / ( result.getNoCorrectlyLabeled() + result.getNoMisslabeled());
	}

	/**
	 * Always returns false - accuracy is not calculated for a specific class
	 */
	public boolean dependsOnClass() {
		return false;
	}

	/**
	 * Ignores the class name - accuracy is not calculated for a specific class
	 */
	public void setClassName(String name) {
		if(!name.equals("avg"))
			System.out.println("WARNING: trying to calculate the accuracy for a specific class. Accuracy is not class-dependent, ignoring class name.");
	}

	/**
	 * Returns "avg" - accuracy is not calculated for a specific class
	 */
	@Override
	public String getClassName() {
		return "avg";
	}

}
