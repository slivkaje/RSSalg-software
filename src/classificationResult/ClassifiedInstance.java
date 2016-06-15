package classificationResult;

import algorithms.RSSalg.resultStatistic.Confidences;

/**
 * Object that represents an instance classified by a single classifier or by ensamble of classifiers (id, classifier confidence for each label and actual label)
 */
public class ClassifiedInstance implements Comparable<ClassifiedInstance>{
	/**
	 * Instance id
	 */
	protected double instanceId;
	/**
	 * Classifier confidence for each of the possible labels Note: assumes the ordering of the confidences (see {@link Confidences})
	 */
	protected Confidences confidences;
	/**
	 * Actual label of the instance. Null if not available
	 */
	protected String actualLabel;
	
	/**
	 * Creates a new instance of ClassifiedInstance object
	 * @param instanceId instance id
	 * @param confidences classifier confidence for each possible label
	 * @param actualLabel actual label of the instance (can be null if the actual label is missing)
	 */
	public ClassifiedInstance(double instanceId, Confidences confidences, String actualLabel) {
		this.instanceId = instanceId;
		this.confidences = confidences;
		this.actualLabel = actualLabel;
	}
	/**
	 * Returns the instance id
	 * @return instance id
	 */
	public double getInstanceId() {
		return instanceId;
	}
	/**
	 * Sets the instance id as the specified value
	 * @param instanceId instance id
	 */
	public void setInstanceId(double instanceId) {
		this.instanceId = instanceId;
	}
	/**
	 * Returns the classifier/ensemble confidences for each of the possible labels. Note: assumes the ordering of the confidences - see {@link Confidences}
	 * @return classifiers ensemble confidences for each of the possible labels
	 */
	public Confidences getConfidences() {
		return confidences;
	}
	/**
	 * Sets the classifier/ensemble confidences for each of the possible labels to the value of the given parameter. Note: assumes the ordering of the 
	 * confidences - see {@link Confidences}
	 * @param confidence ensemble confidences for each of the possible labels
	 */
	public void setConfidences(Confidences confidence) {
		this.confidences = confidence;
	}
	/**
	 * Get the actual label of the instance
	 * @return actual label or null if the actual label is not available
	 */
	public String getActualLabel() {
		return actualLabel;
	}
	/**
	 * Set the actual label of the instance. The label can be set to null if not available.
	 * @param actualLabel actual label of the instance
	 */
	public void setActualLabel(String actualLabel) {
		this.actualLabel = actualLabel;
	}
	/**
	 * Get the prediction for the instance
	 * @return prediction for the instance
	 */
	public String getPrediction(){
		return confidences.getPrediction();
	}
	/**
	 * Get confidence for a classifier that the instance is correctly classified (as belonging to {@link #getPrediction()} class) 
	 * @param classifierNo number of classifier in the ensemble (starting form 0)  
	 * @return confidence of the classifier that the prediction for the instance is correct
	 */
	public double getConfidence(int classifierNo){
		return confidences.getConfidence(getPrediction(), classifierNo);
	}
	/**
	 * Get the combined confidence (confidence of the co-training style ensemble) that the instance is correctly classified 
	 * (as belonging to {@link #getPrediction()} class). See {@link Confidences#getCombinedConfidence(String)}. 
	 * If there is only one classifier, the confidence of that classifier will be returned.
	 * @return the co-training style ensemble confidence that the prediction for the instance is correct. 
	 */
	public double getCombinedConfidence(){
		return confidences.getCombinedConfidence(getPrediction());
	}
	/**
	 * Returns the confidence of a specified classifier that the instance belongs to the specified class
	 * @param labelName the name of the class to get the confidence for 
	 * @param classifierNo classifier number
	 * @return the confidence of a specified classifier that the instance belongs to the specified class
	 */
	public double getConfidence(String labelName, int classifierNo) {
		return confidences.getConfidence(labelName, classifierNo);
	}
	/**
	 * Get the combined confidence (confidence of the co-training style ensemble) that the instance belongs to the specified class, see {@link Confidences#getCombinedConfidence(String)}.
	 * If there is only one classifier in the ensemble, the method will return the confidence of that classifier.
	 * @param labelName the name of the class to get the confidence for
	 * @return the co-training style ensemble confidence confidence of the classifiers that the instance belongs to the specified class 
	 */
	public double getCombinedConfidence(String labelName) {
		return confidences.getCombinedConfidence(labelName);
	}
	@Override
	public String toString() {
		if (actualLabel != null)
			return "ID: " + instanceId + " predicted: " + getPrediction() + "(confidence: " + getCombinedConfidence() + ") actual label: " + actualLabel + " correctly classified: " + correctlyLabeled();
		else
			return "ID: " + instanceId + " predicted: " + getPrediction() + "(confidence: " + getCombinedConfidence() + ") actual label is unknown";
	}
	
	/**
	 * Compares instances according to the combined classifier confidence (see {@link #getCombinedConfidence()})
	 */
	@Override
	public int compareTo(ClassifiedInstance otherClassifiedInstance) {
		if (this.getCombinedConfidence() > otherClassifiedInstance.getCombinedConfidence())
			return -1;
		else if (this.getCombinedConfidence() < otherClassifiedInstance.getCombinedConfidence())
			return 1;
		else
			return 0;
	}
	
	/**
	 * Returns 1 if the instance is correctly classified, -1 if the instance is misslabeled and 0 if the actual label is missing
	 * @return 1 if the instance is correctly classified, -1 if the instance is misslabeled and 0 if the actual label is missing
	 */
	public int correctlyLabeled(){
		if (actualLabel == null)
			return 0;
		
		String prediction = getPrediction();
		if (actualLabel.equals(prediction))
			return 1;
		else
			return -1;	
	}
}
