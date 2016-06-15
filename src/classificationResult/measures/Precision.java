package classificationResult.measures;

import java.util.List;

import classificationResult.ClassificationResult;
import experimentSetting.DatasetSettings;

/**
 * Calculates the precision for the specified class: precision = tp/(tp+fp) where tp - true positive (correctly predicted to belong to the specified class), 
 * fp - false positive (incorrectly predicted to belong to the specified class).
 * <p>
 * Average precision (for all classes) can be calculated by specifying "avg" as the class name
 *
 */
public class Precision implements MeasureIF {
	String className = null;

	public double getMeasure(ClassificationResult result) {
		if(className != null)
			return getPrecisionForName(result, className);
			
		List<String> names = DatasetSettings.getInstance().getClassNames();
		double avgPrec = 0;
		for(String name : names){
			avgPrec += getPrecisionForName(result, name);
		}			
		avgPrec /= names.size();
		
		return avgPrec;
	}

	public String getName() {
		if(className == null)
			return "precision (averaged)";
		return "precision for class " + className;
	}
	
	/**
	 * Set the class for precision evaluation. If averaged precision is required use "avg" as the class name
	 */
	public void setClassName(String name){
		this.className = name;
	}
	
	private double getPrecisionForName(ClassificationResult result, String name){
		int tp = result.getTrueForClass(name);
		int fp = result.getFalseForClass(name);
		return ((double) tp)/(tp+fp);
	}

	/**
	 * Always returns true - precision is calculated for a specific class
	 */
	public boolean dependsOnClass() {
		return true;
	}

	@Override
	public String getClassName() {
		return className;
	}

}
