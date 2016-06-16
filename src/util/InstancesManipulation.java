package util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import algorithms.co_training.CoTrainingData;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.AddID;
import weka.filters.unsupervised.attribute.Remove;
import experimentSetting.DatasetSettings;

/**
 * Different utility functions for manipulating WEKA Instances object   
 */
public class InstancesManipulation {
	
	
	/**
	 * Writes the dataset to an ARFF file
	 * @param fileName path to dataset file
	 * @param instances dataset to write to an ARFF file    
	 * @throws IOException if the file cannot be written to the desired location (e.g. the folder is missing)
	 */
	public static void writeArff(String fileName, Instances instances) throws IOException {
		FileWriter out = new FileWriter(fileName);
		out.write(instances.toString());
		out.flush();
		out.close();
	}
	
	/**
	 * Checks whether the instances should be tagged with id. The instances are tagged if id attribute name in data settings is empty (no id attribute is specified)
	 * or an id attribute is specified but dies not exist in the dataset
	 * @param instances dataset to check for id tagging
	 * @return whether the instances should be tagged with id 
	 */
	private static boolean shouldGenerateID(Instances instances){
		Attribute idAtt = instances.attribute(DatasetSettings.getInstance().getIdAttributeName()); 
		if(idAtt == null) // the id attribute is defined, but doesn't exist in the dataset. Can happen when loading the second view and the first view 
			              // was tagged with default id attribute
			return true;
		return false;
	}
	
	/**
	 * Reads the dataset from ARFF file and tags it with id if necessary
	 * @param filename path to dataset file
	 * @param setClassAndID if true, sets the class and id attributes  
	 * @return the loaded dataset  
	 * @throws Exception
	 * <ul>
	 * <li> the ARFF file is missing
	 * <li> class attribute is missing (there is no attribute in the dataset that matches the name of the class attribute given in the data properties)
	 * <li> adding an id attribute failed
	 * </ul>
	 */
	public static Instances readArff(String filename, boolean setClassAndID) throws Exception{		
		Instances dataset;
		try {
			dataset = new Instances(new FileReader(filename));
		} catch (Exception e) {			
			throw new IOException("Problem reading dataset: '" + filename + "' file not found.");
		}

		if(setClassAndID){
			Attribute classAttribute = dataset.attribute(DatasetSettings.getInstance().getClassAttributeName());
			if (classAttribute == null) {
				throw new Exception("Problem setting class attribute: no attribute with name '" + DatasetSettings.getInstance().getClassAttributeName() + "'");	
			}
			dataset.setClass(classAttribute);
		
			if(shouldGenerateID(dataset))
				try{
					dataset = addIdAttribute(dataset);
				}catch (Exception e) {
					throw new Exception("Problem adding 'ID' attribute: " + e.getMessage());
				}		
		}
		return dataset;
	}

	/**
	 * Tags the dataset with default id attribute (numeric, range [0, number_of_instances])
	 * @param instances the dataset    
	 * @return dataset with added id attribute
	 * @throws Exception if adding the id attribute failed
	 */
	private static Instances addIdAttribute(Instances instances) throws Exception{
		AddID addIDfilter = new AddID();
		Vector<String> options = new Vector<String>();
		options.add("-C");
		options.add("first");
		options.add("-N");
		options.add(DatasetSettings.getInstance().getIdAttributeName());
		String[] optArray = (String[]) options.toArray(new String[options.size()]);
				
		addIDfilter.setOptions(optArray);
		addIDfilter.setInputFormat(instances);		
		return Filter.useFilter(instances, addIDfilter);
	}
		
	/**
	 * Copies an instance from one dataset to another
	 * @param sourceDataset dataset to copy the instance from
	 * @param destinationDataset dataset to copy the instance to
	 * @param instanceIndex index of the instance to copy         
	 */
	public static void copyInstance(Instances sourceDataset, Instances destinationDataset, int instanceIndex){
		Instance instanceToCopy = sourceDataset.instance(instanceIndex);
		destinationDataset.add(instanceToCopy);		
	}
	
	/**
	 * Moves an instance from one dataset to another
	 * @param sourceDataset dataset to move the instance from
	 * @param destinationDataset dataset to move the instance to
	 * @param instanceIndex index of the instance to move         
	 */
	public static void moveInstance(Instances sourceDataset, Instances destinationDataset, int instanceIndex){
		Instance instanceToCopy = sourceDataset.instance(instanceIndex);
		destinationDataset.add(instanceToCopy);
		sourceDataset.delete(instanceIndex);		
	}
	
	/**
	 * Moves an instance from one dataset to another. The dataset might be represented by several different views (each element from the array is one view) 
	 * @param sourceDataset dataset to move the instance from
	 * @param destinationDataset dataset to move the instance to
	 * @param instanceIndex index of the instance to move         
	 */
	public static void moveInstance(Instances[] sourceDataset, Instances[] destinationDataset, int instanceIndex){
		for(int view=0; view < sourceDataset.length; view++)
			moveInstance(sourceDataset[view], destinationDataset[view], instanceIndex);		
	}
	
	/**
	 * Copies an instance from one dataset to another. The dataset might be represented by several different views (each element from the array is one view) 
	 * @param sourceDataset dataset to copy the instance from
	 * @param destinationDataset dataset to copy the instance to
	 * @param instanceIndex index of the instance to copy         
	 */
	public static void copyInstance(Instances[] sourceDataset, Instances[] destinationDataset, int instanceIndex){
		for(int view=0; view < sourceDataset.length; view++)
			copyInstance(sourceDataset[view], destinationDataset[view], instanceIndex);		
	}
	
	/**
	 * Moves an instance from one dataset to another
	 * @param sourceDataset dataset to move the instance from
	 * @param destinationDataset dataset to move the instance to
	 * @param idToMove id of the instance to move         
	 */
	public static void moveInstance(Instances sourceDataset, Instances destinationDataset, String idToMove){
		for(int instanceInd=0; instanceInd<sourceDataset.numInstances(); instanceInd++){
			String instId = getInstanceID(sourceDataset.instance(instanceInd));
			if(instId == idToMove){
				moveInstance(sourceDataset, destinationDataset, instanceInd);
				return;
			}
		}
	}
	
	/**
	 * Copies all instances from one dataset to another
	 * @param sourceDataset dataset to copy from
	 * @param destinationDataset dataset to copy to      
	 */
	public static void copyAllInstances(Instances sourceDataset, Instances destinationDataset){
		for(int instanceInd = 0; instanceInd < sourceDataset.numInstances(); instanceInd++){
			copyInstance(sourceDataset, destinationDataset, instanceInd);
		}		
	}
	
	/**
	 * Copies all instances from one dataset to another. The dataset might be represented by several different views (each element from the array is one view)
	 * @param sourceDataset dataset to copy from
	 * @param destinationDataset dataset to copy to      
	 */
	public static void copyAllInstances(Instances[] sourceDataset, Instances[] destinationDataset){
		for(int view = 0; view < sourceDataset.length; view++)
			copyAllInstances(sourceDataset[view], destinationDataset[view]);
	}
	
	/**
	 * Clones the dataset (deep copy)
	 * @param instances dataset to be cloned
	 * @return newly created dataset      
	 */
	public static Instances cloneDataset(Instances instances){
		Instances result = new Instances(instances);
		return result;
	}
	
	/**
	 * Clones the dataset
	 * @param instances dataset to be cloned. The dataset might be represented by several different views (each element from the array is one view)
	 * @return newly created dataset      
	 */
	public static Instances[] cloneDataset(Instances[] instances){
		Instances result[] = new Instances[instances.length];
		for(int i=0; i<instances.length; i++)
			result[i] = cloneDataset(instances[i]);				
		return result;
	}
	
	/**
	 * Creates an empty dataset with the same attribute set as the given dataset
	 * @param instances dataset whose attributes will be used for the creation of the new dataset
	 * @return newly created dataset      
	 */
	public static Instances createEmptyDataset(Instances instances){
		Instances result = new Instances(instances, 0);
		return result;
	}
	
	/**
	 * Moves all instances from one dataset to another. The dataset might be represented by several different views (each element from the array is one view)
	 * @param sourceDataset dataset to move from
	 * @param destinationDataset dataset to move to      
	 */
	public static void moveAllInstances(Instances[] sourceDataset, Instances[] destinationDataset){
		for(int view = 0; view < sourceDataset.length; view++)
			moveAllInstances(sourceDataset[view], destinationDataset[view]);	
	}
	
	/**
	 * Moves all instances from one dataset to another
	 * @param sourceDataset dataset to move from
	 * @param destinationDataset dataset to move to      
	 */
	public static void moveAllInstances(Instances sourceDataset, Instances destinationDataset){
		while(sourceDataset.numInstances() > 0){
			moveInstance(sourceDataset, destinationDataset, 0);
		}		
	}
	
	/**
	 * Moves instances with given ids from one dataset to another
	 * @param sourceDataset dataset to move from
	 * @param destinationDataset dataset to move to
	 * @param ids ids of instances to move      
	 */
	public static void moveInstances(Instances sourceDataset, Instances destinationDataset, Set<String> ids){
		for(int instanceInd = 0; instanceInd < sourceDataset.numInstances(); instanceInd++){
			String instId = getInstanceID(sourceDataset.instance(instanceInd));
			if(ids.contains(instId)){
				moveInstance(sourceDataset, destinationDataset, instanceInd);
				instanceInd--;
				ids.remove(instId);
				if(ids.size() == 0)
					return;
			}
		}
	}
	
	/**
	 * Returns instance label as String
	 * @param instance instance
	 * @return instance label 
	 */
	public static String getLabel(Instance instance){
		if(instance.classIsMissing())
			return null;
		else
			return instance.stringValue(instance.classAttribute());
	}
	
	
	/**
	 * Returns the value of instance id attribute as String
	 * @param instance instance
	 * @return instance id 
	 */
	public static String getInstanceID(Instance instance){
		Attribute idAtt = instance.dataset().attribute(DatasetSettings.getInstance().getIdAttributeName());
		return ""+ instance.value(idAtt);
	}
	
	/**
	 * Finds the instance defined by the supplied id and returns its index in the dataset
	 * @param dataset dataset to search for instance
	 * @param instanceId id of the instance to find
	 * @return instance index in the given dataset or -1 if not found 
	 */
	public static int findInstance(Instances dataset, String instanceId){
		for(int i=0; i<dataset.numInstances(); i++)
			if( getInstanceID(dataset.instance(i)).equals(instanceId))
				return i;
		return -1;
	}
	
	/**
	 * Removes the instance defined bythe supplied id from a dataset. The dataset might be represented by several different views 
	 * (each element from the array is one view)
	 * @param dataset different views of the dataset to remove the instance from 
	 * @param instanceId the id of the instance to delete
	 */
	public static void removeInstance(Instances[] dataset, String instanceId){
		int ind = findInstance(dataset[0], instanceId);
		if (ind == -1)
			return; // instance not found in dataset (might happen in co-training: instance was removed earlier because it was labeled by a different view)
		for(Instances view : dataset)
			view.delete(ind); // instance should be at the same index in all datasets 
	}
	
	/**
	 * Checks whether two datasets contain the same instances according to ids of the instances. Instances might be described by different sets of attributes 
	 * (i.e. be the two different views of the same dataset).
	 * @param dataset1 the first dataset
	 * @param dataset2 the second dataset
	 * @return whether or not the supplied datasets contain the same set of instances 
	 */
	public static boolean sameInstances(Instances dataset1, Instances dataset2){		
		if(dataset1.numInstances()!= dataset2.numInstances())
			return false;
		
		boolean same = true;
		for(int i=0; i<dataset1.numInstances(); i++){
			same = same && (findInstance(dataset2, getInstanceID(dataset1.instance(i))) != -1); 
		}
		return same;
	}
	
	/**
	 * Returns the string that contains the array of id attribute values of the instances in the dataset: "id1; id2; id3; ..."
	 * @param instances the dataset
	 * @return instances id array
	 */
	public static String getInstancesIdStr(Instances instances){
		String res = "";
		for(int i=0; i<instances.numInstances(); i++)
			res += InstancesManipulation.getInstanceID(instances.instance(i)) + "; ";
		return res;
	}
	
	/**
	 * Sets the values of class attribute to be missing
	 * @param instances dataset to remove the labels from
	 * @return the dataset with labels set to "?" (missing value)
	 */
	public static Instances removeLabel(Instances instances){
		for(Instance instance: instances)
			instance.setClassMissing();
		return instances;
	}
	
	/**
	 * Maps class indices (internal WEKA mapping) to class String values
	 * @param classIndex class index
	 * @param classAttribute class attribute
	 * @return class name (String)
	 * @throws Exception if there is no class that is mapped to a supplied class index 
	 */
	public static String getClassName(double classIndex, Attribute classAttribute) throws Exception{
		DatasetSettings dataSettings = DatasetSettings.getInstance();
		
		for(int i=0; i<dataSettings.getClassNames().size(); i++){
			String className = dataSettings.getClassNames().get(i);
			int classNameIndex = classAttribute.indexOfValue(className);			
			if (classNameIndex == classIndex)
				return className;
			
		}
		
		throw new Exception("Class with index " + classIndex + "not found");
	}

	/**
	 * Merges the attributes from two different datasets that represent different views of the data into a single view.  
	 * @param dataset1 the 1st dataset
	 * @param dataset2 the 2nd dataset
	 * @return dataset with attributes from both datasets
	 */
	public static Instances mergeAttributes(Instances dataset1, Instances dataset2){
		Instances tmpDataset1 = cloneDataset(dataset1);
		Instances tmpDataset2 = cloneDataset(dataset2);
		
		int classAttIndex = tmpDataset1.classIndex();
		tmpDataset1.setClassIndex(-1);		
		tmpDataset1.deleteAttributeAt(classAttIndex);
						
		int idAttIndex = tmpDataset2.attribute(DatasetSettings.getInstance().getIdAttributeName()).index();		
		tmpDataset2.deleteAttributeAt(idAttIndex);
		
		tmpDataset1 = Instances.mergeInstances(tmpDataset1, tmpDataset2);
		tmpDataset1.setClass(tmpDataset1.attribute(DatasetSettings.getInstance().getClassAttributeName()));
		return tmpDataset1;
	}
	
	/**
	 * Returns the total number of attributes in all views (class and id attribute are included in this count).
	 * @param instances different views of the dataset
	 * @return number of attributes
	 */
	public static int getNoAttributes(Instances[] instances){
		int res = 0;
		for(int i=0; i<instances.length; i++)
			res += instances[i].numAttributes();
		res = res-2; // do not duplicate class and id		
		return res;
	}
	
	/**
	 * Removes all attributes from the dataset except for the class and id attribute
	 * @param instances dataset to remove the attributes from
	 * @return dataset with removed attributes
	 * @throws Exception if there was an error removing the attributes
	 */
	public static Instances removeAllAttributes(Instances instances) throws Exception{
		Remove removeFilter = new Remove();
		int classAttIndex = instances.classIndex() + 1; // filter starts indexing from 1, not 0
		int idIndex = instances.attribute(DatasetSettings.getInstance().getIdAttributeName()).index() + 1;
		if(idIndex < classAttIndex)
			removeFilter.setAttributeIndices(idIndex + "," + classAttIndex);
		else
			removeFilter.setAttributeIndices(classAttIndex + "," + idIndex);
		removeFilter.setInvertSelection(true);
		try{
			removeFilter.setInputFormat(instances);
			return Filter.useFilter(instances, removeFilter);
		}catch(Exception e){
			throw new Exception("ERROR: error removing all attributes from the dataset", e);
		}
	}
	
	/**
	 * Removes attributes defined by indices from the dataset
	 * @param instances dataset to remove attributes from
	 * @param indices indices of attributes to remove from the dataset (counting from 1)
	 * @param invertSelection if true, keep only the attributes with given indices in the dataset and remove the rest 
	 * @return dataset with attributes removed
	 * @throws Exception if there was an error removing the attributes (e.g. maximum attribute index is n and one of the supplied attribute indices is greater than n)
	 */
	public static Instances removeAttributes(Instances instances, Set<Integer> indices, boolean invertSelection) throws Exception{
		Iterator<Integer> it = indices.iterator();
		if(!it.hasNext())
			return instances; // no atts to move
		
		String attIndices = "" + it.next();
		while(it.hasNext()) 			
			attIndices += "," + it.next();
			
		// skip class attribute from removal
		if(invertSelection){
			attIndices += "," + (instances.classIndex()+1); // +1 because filter counts from 1 not 0
		}
		
		Remove removeFilter = new Remove();
		removeFilter.setAttributeIndices(attIndices);
		removeFilter.setInvertSelection(invertSelection);
		
		try{
			removeFilter.setInputFormat(instances);
			return Filter.useFilter(instances, removeFilter);
		}catch(Exception e){
			throw new Exception("ERROR: error removing all attributes from the dataset", e);
		}
	}
	
	/**
	 * Copies attributes defined by given indices from one dataset to another
	 * @param origin the dataset to copy attributes from
	 * @param destination the dataset to copy attributes to
	 * @param indices inidices of attributes to copy (counting from 1)
	 * @return modified Instances object (datasest with added (copied) attributes)
	 * @throws Exception if there was an error copying the attributes (e.g. maximum attribute index is n and one of the supplied attribute indices is greater than n)
	 */
	public static Instances copyAttributes(Instances origin, Instances destination, Set<Integer> indices) throws Exception{
		Instances tmpOrigin = cloneDataset(origin);
		
		// keep the given attributes in tmpOrigin
		tmpOrigin = removeAttributes(tmpOrigin, indices, true); 
		
		// remove class from destination (will be copied from origin)
		int classAttIndex = destination.classIndex();
		destination.setClassIndex(-1);		
		destination.deleteAttributeAt(classAttIndex);
		destination = Instances.mergeInstances(destination, tmpOrigin);
		String className = DatasetSettings.getInstance().getClassAttributeName();
		destination.setClass(destination.attribute(className));
		return destination;
	}
	
	/**
	 * Labled (training) data is defined by the prediction parameter ([id, label] pair for all labeled instances ) - modify the supplied {@link CoTrainingData} accordingly.
	 * <br>For all instances belonging to the labeled data in {@link CoTrainingData} object: 
	 * <ul>
	 * <li> if the instance is not present in predictions - delete it from the labeled data
	 * <li> if the instance is present in predictions - re-label it if necessary
	 * </ul>
	 * For all instances belonging to the unlabeled data in {@link CoTrainingData} object:
	 * <ul> 
	 * <li> if present in predictions - label it and move it to the labeled data  
	 * </ul>  
	 * @param predictions class attribute value for each instance that should be in the labeled dataset. Key: instance id, value: label for that instance
	 * @param data co-training data object (labeled, unlabeled and test data)
	 * @return the modified {@link CoTrainingData} object: data is labeled according to the supplied label set (predictions parameter)
	 * @throws Exception if some of the instances defined by predictions parameter are missing in both labeled and unlabeled data
	 */
	public static CoTrainingData setTrainingSet(Map<Double, String> predictions, CoTrainingData data) throws Exception{
		Map<Double, String> tmpPredictions = new HashMap<Double, String>();
		tmpPredictions.putAll(predictions);
		
		for(int i=0; i<data.getLabeledData()[0].size(); i++){
			Instance inst = data.getLabeledData()[0].get(i);
			double instID = Double.parseDouble(getInstanceID(inst));
			
			if(!tmpPredictions.containsKey(instID)){ // instance is not found in predictions -> should be removed from the labeled data
				for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++)
					data.getLabeledData()[viewInd].remove(i);
				i--; // removal of the instance changes the length of the dataset
			}else{
				// check label and re-label if necessary
				String label = tmpPredictions.get(instID);
				String currentLabel = getLabel(inst);
				if(!currentLabel.equals(label)){
					for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++)
						data.getLabeledData()[viewInd].instance(i).setClassValue(label); // re-label instance in each view
				}
				tmpPredictions.remove(instID); // instance found an re-labeled, remove it from predictions. This is to check whether all instances for labeling are found
			}
		}
		// search for the remaining instances that should be in the training set.
		for(int i=0; i<data.getUnlabeledData()[0].size(); i++){
			Instance inst = data.getUnlabeledData()[0].get(i);
			double instID = Double.parseDouble(getInstanceID(inst));
			if(tmpPredictions.containsKey(instID)){ // label and move to labeled
				String label = tmpPredictions.get(instID);
				for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++){
					data.getUnlabeledData()[viewInd].instance(i).setClassValue(label);
					data.getLabeledData()[viewInd].add(data.getUnlabeledData()[viewInd].instance(i));
					data.getUnlabeledData()[viewInd].delete(i);
				}
				i--;
				tmpPredictions.remove(instID);
			}
		}
		
		if(!(tmpPredictions.size() == 0)){
			String missingInstances = "";
			for(Double id : tmpPredictions.keySet())
				missingInstances += id + " ";
			throw new Exception("ERROR: not all predictions found. Ids missing in data: " + missingInstances);
		}
		
		return data;
	}
	
	/**
	 * Removes all instances from the dataset
	 * @param instances dataset to remove the instances from
	 * @return the empty dataset
	 */
	public static Instances removeAllInstances(Instances instances){
		while(instances.numInstances() > 0)
				instances.delete(0);
		return instances;
	}
	
	/**
	 * Removes all instances from the dataset represented with several views
	 * @param instances dataset to remove the instances from
	 * @return the empty dataset
	 */
	public static Instances[] removeAllInstances(Instances[] instances){
		for(int viewInd= 0; viewInd < instances.length; viewInd++){
			instances[viewInd] = removeAllInstances(instances[viewInd]);
		}
		return instances;
	}
	
	/**
	 * Test data is defined by the predictions parameter ([id, label] pair for all test instances) - modify the supplied {@link CoTrainingData} 
	 * accordingly. For example, it is used in RSSalg for out-of-bag evaluation.<br>
	 * For all instances previously belonging to the test data in {@link CoTrainingData} object - remove them.<br>
	 * For all instances belonging to the labeled data in {@link CoTrainingData} object - if present in predictions, re-label if necessary and move to test data<br>
	 * For all instances belonging to the unlabeled data in {@link CoTrainingData} object - if present in predictions, label and move to test data<br>   
	 * @param predictions class attribute value for each instance that should be in the test dataset. Key: instance id, value: label for that instance
	 * @param data co-training data object (labeled, unlabeled and test data)
	 * @return the modified {@link CoTrainingData} object: test data is modified according to the supplied set (predictions parameter)
	 * @throws Exception if some of the instances defined by predictions parameter are missing in both labeled and unlabeled data
	 */
	public static CoTrainingData setTestSet(Map<Double, String> predictions, CoTrainingData data) throws Exception{
		Map<Double, String> tmpPredictions = new HashMap<Double, String>();
		tmpPredictions.putAll(predictions);
		
		// remove all test instances if any (these are the test instances for the evaluation of the final classifier that should not be used for model training/optimization)
		if(!(data.getTestData()[0].numInstances() == 0)){
			data.setTestData(removeAllInstances(data.getTestData()));
		}
		
		for(int i=0; i<data.getLabeledData()[0].size(); i++){
			Instance inst = data.getLabeledData()[0].get(i);
			double instID = Double.parseDouble(getInstanceID(inst));
			if(tmpPredictions.containsKey(instID)){				
				// check label and relabel if necessary
				String label = tmpPredictions.get(instID);
				String currentLabel = getLabel(inst);
				if(!currentLabel.equals(label)){
					for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++)
						data.getLabeledData()[viewInd].instance(i).setClassValue(label);
				}
				// move to test data
				for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++){
					data.getTestData()[viewInd].add(inst);
					data.getLabeledData()[viewInd].delete(i);
				}
			}
		}
		
		for(int i=0; i<data.getUnlabeledData()[0].size(); i++){
			Instance inst = data.getUnlabeledData()[0].get(i);
			double instID = Double.parseDouble(getInstanceID(inst));
			if(tmpPredictions.containsKey(instID)){ // label and move to test
				String label = tmpPredictions.get(instID);
				for(int viewInd= 0; viewInd < data.getLabeledData().length; viewInd++){
					data.getUnlabeledData()[viewInd].instance(i).setClassValue(label);
					data.getTestData()[viewInd].add(data.getUnlabeledData()[viewInd].instance(i));
					data.getUnlabeledData()[viewInd].delete(i);
				}
				i--;
				tmpPredictions.remove(instID);
			}
		}
		
		if(!(tmpPredictions.size() == 0)){
			String missingInstances = "";
			for(Double id : tmpPredictions.keySet())
				missingInstances += id + " ";
			throw new Exception("ERROR: not all predictions found. Ids missing in data: " + missingInstances);
		}
		return data;
	}
	
	/**
	 * Merged the views in the unique attribute set. 
	 * Each view ({@link Instances} object from the array) should contain the same number of instances, described by different attribute sets.
	 * @param dataset different views of one dataset
	 * @return dataset with a unique attribute set
	 */
	public static Instances getMerged(Instances[] dataset) {
		if(dataset.length == 1) // already just one view
			return dataset[0];
		
		Instances dest = InstancesManipulation.cloneDataset(dataset[0]);
		// move features from all other views to the first one
		for(int view=1; view < dataset.length; view++){
			try{
				dest = mergeAttributes(dest, dataset[view]);
			}catch(Exception e){
				new Exception("ERROR: error merging views.", e);
			}
		}
		return dest;
	}
	
	/**
	 * Returns the number of instances that have the certain value of an attribute
	 * @param instances the dataset
	 * @param attName the name of the attribute
	 * @param attValue the value of the attribute
	 * @return the number of instances from the dataset who have the given value for this attribute
	 */
	public static int getNumberOfInstancesWithAttValue(Instances instances, String attName, String attValue){
		int num = 0;
		Attribute att = instances.attribute(attName);
		for(Instance inst : instances){
			String instanceValSt = inst.stringValue(att);			
			if (instanceValSt.equals(attValue))
				num++;
		}
		return num;
	}
}
