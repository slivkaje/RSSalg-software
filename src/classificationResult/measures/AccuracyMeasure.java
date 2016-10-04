/** 	
 * Name: AccuracyMeasure.java
 * 
 * Purpose: Calculates the accuracy measure: accuracy = num_of_correctly_labeled_instances / total_number_of_labeled_instances
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
