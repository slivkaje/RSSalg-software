/** 	
 * Name: MeasureIF.java
 * 
 * Purpose: An interface representing a measure used for evaluating the classification result.
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
