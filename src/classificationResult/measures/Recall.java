/** 	
 * Name: Recall.java
 * 
 * Purpose: Calculates the recall for the specified class: recall = tp/(tp+fn) where tp - true positive (correctly predicted to belong to the specified class), fn - false negative (incorrectly predicted as not belonging to the specified class).
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
