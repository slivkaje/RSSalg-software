package util;

import java.util.List;

import algorithms.RSSalg.resultStatistic.Confidences;
import algorithms.co_training.MostConfidentInstances;
import classificationResult.ClassificationResult;
import classificationResult.ClassifiedInstance;
import classificationResult.ClassifiedInstanceList;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import experimentSetting.DatasetSettings;

public class Evaluation {
	
	/**
	 * Classifies the instance. 
	 * @param classifier trained classifier
	 * @param instanceToClassify instance for classification
	 * @return the classified instance as {@link ClassifiedInstance} object 
	 * @throws Exception if there was an error applying the trained WEKA classifier on the given instance. Possible reason: classifier is trained on the different
	 * 		   attribute set than the one that describes the instance 
	 */
	public static ClassifiedInstance classifyInstance(Classifier classifier, Instance instanceToClassify) throws Exception{
		String instanceId = InstancesManipulation.getInstanceID(instanceToClassify);
		double actualLabel = instanceToClassify.classValue(); // index of the actual class			
		
		String actualLabelStr = null;
		if(!instanceToClassify.classIsMissing())
		try{
			actualLabelStr = InstancesManipulation.getClassName(actualLabel, instanceToClassify.classAttribute());
		}catch (Exception e){
//			throw new Exception("ERROR: error obtaining the actual label string for instance " + instanceId, e);
			System.out.println("WARNING: instance " + instanceId + " is missing the actual label. It will be ignored when evaluating the classifier");
		}
		
		Confidences confidences = new Confidences();
		try{
			double[] distribution = classifier.distributionForInstance(instanceToClassify);
			for(String className : DatasetSettings.getInstance().getClassNames()){
				double dist = distribution[instanceToClassify.classAttribute().indexOfValue(className)];
				confidences.addConfidence(dist);
			}
		}catch(Exception e){
			throw new Exception("ERROR: error while classifying instance " + instanceId, e);
		}
				
		return new ClassifiedInstance(Double.parseDouble(instanceId), confidences, actualLabelStr);
	}
	
	/**
	 * Classifies the instance by multiplying the probability output by different classifiers (co-training style classification). 
	 * Note: separate classifier is expected for each of the views
	 * 
	 * @param classifiers array of trained WEKA classifiers
	 * @param instanceToClassify different views of the instance for classification 
	 * @return the classified instance as {@link ClassifiedInstance} object
	 * @throws Exception if:<br>
	 * 
	 * 			(1)there was an error applying one of the trained WEKA classifiers on the given instance. Possible reason: classifier is trained on the 
	 * 			   different attribute set than the one that describes the instance<br>
	 * 			(2)the number of trained classifiers is different from the number of views
	 */
	public static ClassifiedInstance classifyInstance(List<Classifier> classifiers, Instance[] instanceToClassify) throws Exception{
		if (classifiers.size() != instanceToClassify.length)
			throw new Exception("ERROR: error classifying the instance: the number of views and classifiers trained on those views must be the same (classifiers: " 
									+ classifiers.size() + " views: " + instanceToClassify.length + ")");
			
		String instanceId = InstancesManipulation.getInstanceID(instanceToClassify[0]);
		double actualLabel = instanceToClassify[0].classValue(); // index of the actual class			
		
		String actualLabelStr = "?";
		try{
			actualLabelStr = InstancesManipulation.getClassName(actualLabel, instanceToClassify[0].classAttribute());
		}catch (Exception e){
//			throw new Exception("ERROR: error obtaining the actual label string for instance " + instanceId, e);
			System.out.println("WARNING: instance " + instanceId + " is missing the actual label. It will be ignored when evaluating the classifier");
		}
		
		Confidences confidences = new Confidences();
		try{
			double[] distribution = new double[instanceToClassify[0].numClasses()*classifiers.size()];
			double[] combinedDistribution = new double[instanceToClassify[0].numClasses()];
			for(int i=0; i<combinedDistribution.length; i++)
				combinedDistribution[i] = 1;
			
			for(int classifierInd = 0; classifierInd < classifiers.size(); classifierInd++){				
				double[] tmpDistribution = classifiers.get(classifierInd).distributionForInstance(instanceToClassify[classifierInd]);
				for(int i=0; i<tmpDistribution.length; i++)
					combinedDistribution[i] *= tmpDistribution[i];
				for(int i=0; i<tmpDistribution.length; i++)
					distribution[classifierInd*tmpDistribution.length+i] = tmpDistribution[i];
			}
			
			// re-normalizing the probabilities output by final classifier
			double sumProbabilites = 0;
			for(int i=0; i<combinedDistribution.length; i++)
				sumProbabilites += combinedDistribution[i];
			for(int i=0; i<combinedDistribution.length; i++){
				combinedDistribution[i] *= 1/sumProbabilites;				
			}
						
			for(int classifierInd = 0; classifierInd < classifiers.size(); classifierInd++)
				for(String className : DatasetSettings.getInstance().getClassNames()){
					double dist = distribution[classifierInd*combinedDistribution.length + instanceToClassify[0].classAttribute().indexOfValue(className)];
					confidences.addConfidence(dist);
				}
		
		}catch(Exception e){
			throw new Exception("ERROR: error while classifying instance " + instanceId, e);
		}
	
		return new ClassifiedInstance(Double.parseDouble(instanceId), confidences, actualLabelStr);
	}
	
	/**
	 * Classifies instances by multiplying the probability output by different classifiers (co-training style classification).
	 * Note: separate classifier is expected for each of the views
	 * 
	 * @param classifiers array of trained WEKA classifiers
	 * @param unlabeledDataset different views of instances to apply classifier on
	 * @return the list of classified instances
	 * @throws Exception if:
	 * <ul>
	 * 			<li>there was an error applying one of the trained WEKA classifiers on one of the the given instances. Possible reason: classifier is trained on the 
	 * 			   different attribute set than the one that describes the instance<br>
	 * 			<li>the number of trained classifiers is different from the number of views
	 * </ul>
	 */
	public static ClassifiedInstanceList classifyInstances(List<Classifier> classifiers, Instances[] unlabeledDataset) throws Exception{
		ClassifiedInstanceList result = new ClassifiedInstanceList();
		for(int i=0; i<unlabeledDataset[0].size(); i++){
			Instance[] instanceToClassify = new Instance[unlabeledDataset.length];
			for(int j=0; j<unlabeledDataset.length; j++)
				instanceToClassify[j] = unlabeledDataset[j].get(i);
			ClassifiedInstance classifiedInstance = classifyInstance(classifiers, instanceToClassify);
			result.addInstance(classifiedInstance);
		}
		return result;
	}
	
	/**
	 * Classifies instances 
	 * @param classifier trained WEKA classifier
	 * @param unlabeledDataset data to apply classifier on
	 * @return the list of classified instances
	 * @throws Exception if there was an error applying the trained WEKA classifier on the given instance. Possible reason: classifier is trained on the different
	 * 		   attribute set than the one that describes the instance 
	 */
	public static ClassifiedInstanceList classifyInstances(Classifier classifier, Instances unlabeledDataset) throws Exception{
		ClassifiedInstanceList result = new ClassifiedInstanceList();
		for(int i=0; i<unlabeledDataset.size(); i++){
			Instance instanceToClassify = unlabeledDataset.get(i);
			ClassifiedInstance classifiedInstance = classifyInstance(classifier, instanceToClassify);
			result.addInstance(classifiedInstance);
		}
		return result;
	}

	/**
	 * Classify instances and return the most confidently labeled ones. The number of most confidently labeled instances per class is defined by 
	 * the growthSize parameter in co-training.properties.
	 * @param classifier trained WEKA classifier
	 * @param unlabeledDataset data to apply classifier on
	 * @return the list of classified instances
	 * @throws Exception if there was an error applying the trained WEKA classifier on the given instance. Possible reason: classifier is trained on the different
	 * 		   attribute set than the one that describes the instance 
	 */
	public static MostConfidentInstances getConfidentInstances(Classifier classifier, Instances unlabeledDataset) throws Exception{
		MostConfidentInstances result = new MostConfidentInstances();
		for(int i=0; i<unlabeledDataset.size(); i++){
			Instance instanceToClassify = unlabeledDataset.get(i);
			ClassifiedInstance classifiedInstance = classifyInstance(classifier, instanceToClassify);
			result.addInstance(classifiedInstance);
		}
		return result;
	}
	
	/**
	 * Tests the classifier: trains the given model on a supplied training set and evaluates the performance on the suppled test set. 	 
	 * 
	 * @param classificationModel WEKA classification model (untrained classifier)
	 * @param trainingSet data to train the model on 
	 * @param testSet test set for the evaluation of the trained model
	 * @param recordPredictions whether to record the classifier prediction (confidences) for each instance
	 * @return classification results
	 * @throws Exception
	 * <ul>
	 * <li>there was an error training the WEKA classification model on the supplied training set<br>
	 * <li>there was an error applying the trained WEKA classifier on the supplied test set. Possible reason: classifier is trained on the different
	 * 		   attribute set than the one that describes the test set
	 * </ul> 
	 */
	public static ClassificationResult performTest(Classifier classificationModel, Instances trainingSet, Instances testSet, boolean recordPredictions) throws Exception{
		try {
			classificationModel.buildClassifier(trainingSet);
		} catch (Exception e) {			
			throw new Exception("ERROR: error classifying the test set: could not build the classifiers", e);			
		}
		
		ClassifiedInstanceList classifiedInstances = classifyInstances(classificationModel, testSet);
		return classifiedInstances.getClassificationResult(recordPredictions);			
	}
		
	/**
	 * Tests the co-training style combined classifier.
	 * <br>
	 * <b>Training:</b> trains different supervised models on the same supplied labeled set (same instances) but using different views (attribute sets)<br>
	 * <b>Testing:</b>  each instance from the supplied test set is classified in the following way: for each class the probability that the instance 
	 * 					belongs to that class is calculated by multiplying the probabilities output by each of the trained models; instance is then assigned 
	 * 					the class that has the highest probability 
	 * @param classifiers array of training models to be used (untrained classifiers)
	 * @param trainingSet the different views of the training (labeled) set
	 * @param testSet the different views of the test set to be used for the trained classifier evaluation
	 * @param recordPredictions whether to record the classifier prediction (confidences) for each instance
	 * @return classification result
	 * @throws Exception
	 * <ul>
	 * <li>the number of training models is different from the number of views
	 * <li>there was an error training one of the WEKA classification models on the corresponding view of labeled data<br>
	 * <li>there was an error applying one the trained WEKA classifiers on the corresponding view of the test set. Possible reason: classifier is trained 
	 *     on the different attribute set than the one that describes the test set
	 * </ul> 
	 * 
	 */
	public static ClassificationResult performTest(List<Classifier> classifiers, Instances[] trainingSet, Instances[] testSet, boolean recordPredictions) throws Exception{
		if (classifiers.size() != trainingSet.length || classifiers.size() != testSet.length)
			throw new Exception("ERROR: error classifying the instance: the number of views and classifiers trained on those views must be the same (classifiers: " 
									+ classifiers.size() + " training views: " + trainingSet.length + " testing views" + testSet.length + ")");
		
		try {
			for(int i=0; i<classifiers.size(); i++)
				classifiers.get(i).buildClassifier(trainingSet[i]);
		} catch (Exception e) {			
			throw new Exception("ERROR: error classifying the test set: could not build the classifiers", e);			
		}
		
		ClassifiedInstanceList classifiedInstances = classifyInstances(classifiers, testSet);
		return classifiedInstances.getClassificationResult(recordPredictions);			
	}

}
