package classificationResult;

import java.util.HashMap;
import java.util.Map;
import algorithms.RSSalg.resultStatistic.Confidences;
import experimentSetting.DatasetSettings;

/**
 * Results of the classifier evaluation on the supplied test set
 */
public class ClassificationResult {
	/**
	 * The instance of specified data settings
	 */
	protected DatasetSettings dataSettings = DatasetSettings.getInstance();	
	/**
	 * Number of correctly labeled instances for each of the classes. Key: class name, value: no. of correctly labeled instances for that class 
	 */
	protected Map<String, Integer> trueClass = new HashMap<String, Integer>();
	/**
	 * Number of falsely labeled instances for each of the classes. Key: class name, value: no. of instances falsely labeled to belong to that class 
	 */
	protected Map<String, Integer> falseClass = new HashMap<String, Integer>();
	/**
	 * Whether to record the classifier prediction for ech instance
	 */
	protected boolean recordPredictions;
	/**
	 * Classifier (or ensemble) prediction for each of the instances - key: instance id, value: classifier/classifiers confidence/confidences for each of the possible 
	 * label. Note: assumes the ordering of the confidences - see {@link Confidences}
	 */
	protected Map<Double, Confidences> predictions = new HashMap<Double, Confidences>();
	
	/**
	 * Creates a new instance of ClassificationResult object
	 * @param recordPredictions whether to record the classifier prediction (confidences) for each instance
	 */
	public ClassificationResult(boolean recordPredictions){
		for (int i=0; i<dataSettings.getClassNames().size(); i++){
			trueClass.put(dataSettings.getClassNames().get(i), 0);
			falseClass.put(dataSettings.getClassNames().get(i), 0);
			this.recordPredictions = recordPredictions;
		}
			
	}
	/**
	 * Returns the number of instances correctly predicted to belong to the specified class
	 * @param classname the name of the class
	 * @return number of true positives for the specified class
	 */
	public int getTrueForClass(String classname){
		return trueClass.get(classname);
	}
	/**
	 * Returns the number of instances falsely predicted to belong to the specified class (classifier prediction: the specified class, true label: some other class)
	 * @param classname the name of the class
	 * @return number of instances falsely predicted as belonging to the class 
	 */
	public int getFalseForClass(String classname){
		return falseClass.get(classname);
	}
	
	/**
	 * Returns the number of false negatives for the class (actual label: the specified class, prediction: some other class)
	 * @param classname the name of the class
	 * @return number of false negatives for the class
	 */
	public int getFalseNegativeForClass(String classname){
		int fn = 0;
		for(String name : falseClass.keySet()){
			if(!name.equals(classname))
				fn+=falseClass.get(name);
		}
		return fn;
	}
	
	/**
	 * Updates the results by adding new classification results 
	 * @param resultsToAdd new classification results
	 */
	public void updateResults(ClassificationResult resultsToAdd){
		for (String className : resultsToAdd.getTrueClass().keySet()) {
			this.trueClass.put(className, this.trueClass.get(className) + resultsToAdd.getTrueClass().get(className));
		}
		for (String className : resultsToAdd.getFalseClass().keySet()) {
			this.falseClass.put(className, this.falseClass.get(className) + resultsToAdd.getFalseClass().get(className));
		}
	
		if(recordPredictions)
			for(double id : resultsToAdd.predictions.keySet())
				predictions.put(id, resultsToAdd.predictions.get(id));
	}
	
	/**
	 * Returns the total number of correctly labeled instances 
	 * @return number of correctly labeled instances
	 */
	public int getNoCorrectlyLabeled(){
		int correctlyLabeled = 0;		
		for(String key:trueClass.keySet())
			correctlyLabeled += trueClass.get(key);		
		return correctlyLabeled;
	}
	
	/**
	 * Returns the total number of mislabeled instances 
	 * @return number of mislabeled instances
	 */
	public int getNoMisslabeled(){
		int misslabeled = 0;		
		for(String key:trueClass.keySet())
			misslabeled += falseClass.get(key);
		return misslabeled;
	}
		
	/**
	 * Instance is correctly classified, update the results
	 * @param id instance id
	 * @param className instance correctly classified as belonging to this class
	 * @param confidences classifier/ensemble confidences for each of the possible labels
	 */
	public void updateTrue(double id, String className, Confidences confidences){
		trueClass.put(className, trueClass.get(className) + 1);	
		if(recordPredictions)
			this.predictions.put(id, confidences);
	}
	
	/**
	 * Instance is mislabeled, update the results
	 * @param id instance id
	 * @param className instance falsely classified as belonging to this class (prediction: className, true label: some other class)
	 * @param confidences classifier/ensemble confidences for each of the possible labels
	 */
	public void updateFalse(double id, String className, Confidences confidences){
		falseClass.put(className, falseClass.get(className) + 1);
		if(recordPredictions)
			this.predictions.put(id, confidences);
	}

	/**
	 * Returns the number of correctly labeled instances for each of the classes. Key: class name, value: no. of correctly labeled instances for that class
	 * @return number of correctly labeled instances for each of the classes
	 */
	public Map<String, Integer> getTrueClass() {
		return trueClass;
	}

	/**
	 * Returns the number of falsely labeled instances for each of the classes. Key: class name, value: no. of instances falsely labeled to belong to that class (they actually 
	 * belong to some other class)
	 * @return number of falsely labeled instances for each of the classes
	 */
	public Map<String, Integer> getFalseClass() {
		return falseClass;
	}

	@Override
	public String toString() {		
		String res = "Correctly classified: ";
		for(String className : trueClass.keySet())
			res += className + ": " + trueClass.get(className) + " ";
		res +="\nMissclassified: ";
		for(String className : falseClass.keySet())
			res += className + ": " + falseClass.get(className) + " ";
		return res;
	}

	/**
	 * Returns classifier (or ensemble) confidences for each of the classified instances - key: instance id, value: classifier/classifiers confidence/confidences 
	 * for each of the possible labels. Note: assumes the ordering of the confidences {@link Confidences} 
	 * @return classifier prediction for each of the instances 
	 */
	public Map<Double, Confidences> getPredictions() {
		if(recordPredictions)
			return predictions;
		return null;
	}
}
