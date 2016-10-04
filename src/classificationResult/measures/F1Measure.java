/** 	
 * Name: F1Measure.java
 * 
 * Purpose: Calculates the f1-measure for the specified class: f1-measure = 2*precision(class)*recall(class)/( precision(class)+recall(class) ).
 * 
 * Author: Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * Copyright: (c) 2016 Jelena Slivka <slivkaje AT uns DOT ac DOT rs>
 * 
 * This file is a part of RSSalg software, a flexible, highly configurable tool for experimenting 
 * with co-training based techniques. RSSalg Software encompasses the implementation of 
 * co-training and RSSalg, a co-training based technique that can be applied to single-view 
 * datasets published in the paper: 
 * 
 * Slivka, J., Kovacevic, A. and Konjovic, Z., 2013. 
 * Combining Co-Training with Ensemble Learning for Application on Single-View Natural 
 * Language Datasets. Acta Polytechnica Hungarica, 10(2).
 *   
 * RSSalg software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RSSalg software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
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
