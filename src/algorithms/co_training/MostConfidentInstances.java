/** 	
 * Name: MostConfidentInstances.java
 * 
 * Purpose: Class representing the most confidently labeled instances for each of the classes.
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
package algorithms.co_training;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classificationResult.ClassifiedInstance;
import classificationResult.ClassifiedInstanceList;
import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;

/**
 * Class representing the most confidently labeled instances for each of the classes (predicted to belong to the class). 
 *
 */
public class MostConfidentInstances {
	/**
	 * Most confidently classified instances per each class. Key: class name (String); Value: most confident instances for the class ({@link ClassifiedInstancesQueue} object) 
	 */
	protected Map<String, ClassifiedInstancesQueue> topInstances; 

	/**
	 * Creates an instance of most confident instances. Initiates the {@link #topInstances} map (key: class name; value: most confident instances for the class)
	 * by adding all existing classes in the dataset and initializing {@link ClassifiedInstancesQueue} object for each class. Each 
	 * {@link ClassifiedInstancesQueue} object for the class is created with the capacity of the growthSize for that class (no. of examples
	 * from that class to be added in each iteration of co-training, growthSize in co-training.properties).
	 */
	public MostConfidentInstances() {		
		topInstances = new HashMap<String, ClassifiedInstancesQueue>();
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(int i=0; i<classNames.size(); i++){
			topInstances.put(classNames.get(i), new ClassifiedInstancesQueue(CoTrainingSettings.getInstance().getGrowthSize(classNames.get(i))));
		}
	}
	
	/**
	 * Try to add an instance to the most confident instance list. Finds the list of classified instances to add to (dependent on the class) and tries
	 * to add an instance to that list (see {@link ClassifiedInstancesQueue#add(ClassifiedInstance)})   
	 * @param inst classified instance to be added (instance id, actual label, prediction and confidence)
	 */
	public void addInstance(ClassifiedInstance inst){
		String prediction = inst.getPrediction();
		ClassifiedInstancesQueue listForClass = topInstances.get(prediction);
		listForClass.add(inst);	
	}

	/**
	 * Returns the String representing the list of most confident instances for each class
	 * @return the String representing the list of most confident instances for each class
	 */
	@Override
	public String toString() {
		String res = "";
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(int i=0; i<classNames.size(); i++){
			res += "for Class '" + classNames.get(i) + "':\n";
			res += topInstances.get(classNames.get(i));
			res += "\n";
		}
		return res;
	}
	
	/**
	 * Returns the most confident instances for the class
	 * @param className name of the class
	 * @return most confidently labeled instances predicted to belong to the class defined by the className parameter
	 */
	public ClassifiedInstanceList getMostConfidentInstances(String className){
		return new ClassifiedInstanceList(topInstances.get(className).getList());
	}
}
