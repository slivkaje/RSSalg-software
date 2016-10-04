/** 	
 * Name: Precision.java
 * 
 * Purpose: Calculates the precision for the specified class: precision = tp/(tp+fp) where tp - true positive (correctly predicted to belong to the specified class), fp - false positive (incorrectly predicted to belong to the specified class).
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
