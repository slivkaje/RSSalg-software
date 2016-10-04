/** 	
 * Name: ClassifiedInstanceList.java
 * 
 * Purpose: The list of instances classified by a single classifier or the ensemble of classifiers.
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
package classificationResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The list of instances classified by a single classifier or the ensemble of classifiers
 */
public class ClassifiedInstanceList {
	/**
	 * The list of classified instances
	 */
	protected List<ClassifiedInstance> classifiedInstances = new ArrayList<ClassifiedInstance>();
	
	/**
	 * Create a new instance of ClassifiedInstanceList 
	 */
	public ClassifiedInstanceList() {}
	
	/**
	 * Create a new instance of ClassifiedInstanceList
	 * @param classifiedInstances the list of classified instances
	 */
	public ClassifiedInstanceList(List<ClassifiedInstance> classifiedInstances) {
		this.classifiedInstances = classifiedInstances;
	}
	
	/**
	 * Returns an iterator over a collection of classified instances
	 * @return iterator over a collection of classified instances
	 */
	public Iterator<ClassifiedInstance> getIterator(){
		return classifiedInstances.iterator();
	}
	
	/**
	 * Adds the classified instance to the list
	 * @param inst a new classified instance to add
	 */
	public void addInstance(ClassifiedInstance inst){
		classifiedInstances.add(inst);
	}
	
	@Override
	public String toString() {
		String res = "";		
		for(ClassifiedInstance inst : classifiedInstances)
			res += inst + "\n";
		return res;
	}
	
	/**
	 * Generates the ClassificationResult from the classified instances list. Goes through classified instances and checks whether they are correctly classified or not  
	 * @param recordPredictions whether to record the predictions (confidences) of the classifier/ensemble for each classified instance
	 * @return the classification result of applying the classifier (or ensemble) on the set of test instances 
	 */
	public ClassificationResult getClassificationResult(boolean recordPredictions){
		ClassificationResult result = new ClassificationResult(recordPredictions);
		for (ClassifiedInstance inst : classifiedInstances) {
//			if(inst.actualLabel == null)
//				System.out.println("WARNING: instance " + inst.getInstanceId() + " not labeled and thus not included in accuracy calculation");
			double id = inst.getInstanceId();
			
			if(inst.correctlyLabeled() == 1)				
				result.updateTrue(id, inst.getPrediction(), inst.getConfidences());
			else if(inst.correctlyLabeled() == -1)
				result.updateFalse(id, inst.getPrediction(), inst.getConfidences());																			
		}
		
		return result;
	}

	/**
	 * Returns the number of classified instances
	 * @return number of classified instances in the list
	 */
	public int size(){
		return classifiedInstances.size();
	}
}
