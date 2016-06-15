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
