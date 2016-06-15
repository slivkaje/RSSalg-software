package classificationResult.measures;

import classificationResult.ClassificationResult;

/**
 * An interface representing a measure used for evaluating the classification result
 */
public interface MeasureIF {
	
	/**
	 * Returns the value of the measure
	 * @param result the result of applying a trained classifier to the supplied test set
	 * @return measure value
	 */
	public double getMeasure(ClassificationResult result);
	/**
	 * Returns the name of the algorithm that implements the measure calculation
	 * @return measure name
	 */
	public String getName();
	/**
	 * Whether the measure calculation is dependent on the class.
	 * For example, precision measure can be calculated for the positive class, negative class or as the average of those two values
	 * @return whether the class should be specified in order to calculate the measure
	 */
	public boolean dependsOnClass();
	/**
	 * If {@link #dependsOnClass()}, set the class for measure evaluation
	 * @param name name of the class to evaluate the measure for
	 */
	public void setClassName(String name);
	
	/**
	 * Returns the class that the measure is calculated for
	 * @return name of the class
	 */
	public String getClassName();
}
