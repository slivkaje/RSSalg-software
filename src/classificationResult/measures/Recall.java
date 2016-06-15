package classificationResult.measures;

import java.util.List;

import classificationResult.ClassificationResult;
import experimentSetting.DatasetSettings;

/**
 * Calculates the recall for the specified class: recall = tp/(tp+fn) where tp - true positive (correctly predicted to belong to the specified class), 
 * fn - false negative (incorrectly predicted as not belonging to the specified class).
 * <p>
 * Average recall (for all classes) can be calculated by specifying "avg" as the class name
 *
 */
public class Recall implements MeasureIF {
	String className = null;

	public double getMeasure(ClassificationResult result) {
		if(className != null)
			return getRecallForName(result, className);
			
		List<String> names = DatasetSettings.getInstance().getClassNames();
		double avgRecall = 0;
		for(String name : names){
			avgRecall += getRecallForName(result, name);
		}			
		avgRecall /= names.size();
		
		return avgRecall;
	}

	public String getName() {
		if(className == null)
			return "recall (averaged)";
		return "recall for class " + className;
	}
	
	/**
	 * Set the class for recall evaluation. If averaged recall is required use "avg" as the class name
	 */
	public void setClassName(String name){
		this.className = name;
	}
	
	private double getRecallForName(ClassificationResult result, String name){
		int tp = result.getTrueForClass(name);
		int fn = result.getFalseNegativeForClass(name);
		return ((double) tp)/(tp+fn);
	}

	/**
	 * Always returns true - recall is calculated for a specific class
	 */
	public boolean dependsOnClass() {
		return true;
	}

	@Override
	public String getClassName() {
		return className;
	}

}
