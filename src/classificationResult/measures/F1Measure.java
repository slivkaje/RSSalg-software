package classificationResult.measures;

import java.util.List;

import classificationResult.ClassificationResult;
import experimentSetting.DatasetSettings;

/**
 * Calculates the f1-measure for the specified class: f1-measure = 2*precision(class)*recall(class)/( precision(class)+recall(class) ).
 * <p>
 * Average f1-measure (for all classes) can be calculated by specifying "avg" as the class name
 */
public class F1Measure implements MeasureIF {
	String className = null;

	public double getMeasure(ClassificationResult result) {
		if(className != null)
			return getFMeasForName(result, className);
		
		List<String> names = DatasetSettings.getInstance().getClassNames();
		double avgF = 0;
		for(String name : names){
			avgF += getFMeasForName(result, name);
		}			
		avgF /= names.size();
		return avgF;
	}

	public String getName() {
		if(className == null)
			return "f1-measure (averaged)";
		return "f1-measure for class " + className;
	}
	
	/**
	 * Set the class for measure evaluation. If averaged f1-measure is required use "avg" as the class name
	 */
	public void setClassName(String name){
		this.className = name;
	}

	private double getFMeasForName(ClassificationResult result, String name){
		Precision prec = new Precision();
		prec.setClassName(name);
		Recall recall = new Recall();
		recall.setClassName(name);
		
		double p = prec.getMeasure(result);
		double r = recall.getMeasure(result);
		return 100*2*p*r/(p+r);
	}

	/**
	 * Always returns true - f1 measure is calculated for a specific class
	 */
	public boolean dependsOnClass() {
		return true;
	}

	@Override
	public String getClassName() {
		return className;
	}
}
