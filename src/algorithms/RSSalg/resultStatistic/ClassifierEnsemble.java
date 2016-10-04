/** 	
 * Name: ClassifierEnsemble.java
 * 
 * Purpose:  Object that represents the statistics (instance ids, predictions and confidences for each prediction) about the instances labeled by the ensemble of classifiers.
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
package algorithms.RSSalg.resultStatistic;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import algorithms.co_training.MostConfidentInstances;
import classificationResult.ClassifiedInstance;
import classificationResult.ClassifiedInstanceList;
import experimentSetting.DatasetSettings;

/**
 * Object that represents the statistics (instance ids, predictions and confidences for each prediction) about the instances labeled by the ensemble of classifiers.
 * In this implementation a single classifier is treated as an ensemble of 1 classifiers.<br>
 * The class is JAXB annotated so that this statistics can easily be saved to an XML file. 
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassifierEnsemble {
	/**
	 * Classifier id
	 */
	@XmlAttribute
	int id;
	/**
	 * The list of classified instances
	 */
	@XmlList
	List<Double> instanceId = new Vector<Double>();

	/**
	 * The list of classifier confidences for classifying each of the instances. The ordering of confidences is the same as ordering of the instances, i.e. 
	 * k-th Confidences object from the {@link #confidence} list corresponds to the k-th object from the {@link #instanceId} list 
	 * 
	 */
	List<Confidences> confidence = new Vector<Confidences>();
	
	/**
	 * Adds a prediction for the instance
	 * @param instanceId the id of the instance
	 * @param confidences for each possible class, the confidences of each classifier from the ensemble that the instance belongs to that class 
	 */
	public void addPrediction(double instanceId, Confidences confidences){
		this.instanceId.add(instanceId);
		this.confidence.add(confidences);
	}
	
	/**
	 * Adds predictions for multiple instances from the {@link MostConfidentInstances} object
	 * @param mostConfidentInst classified instances that should be added to the statistics of the ensemble
	 */
	public void addPredictions(MostConfidentInstances mostConfidentInst){
		List<String> classNames = DatasetSettings.getInstance().getClassNames();
		for(String className : classNames){
			ClassifiedInstanceList instances =  mostConfidentInst.getMostConfidentInstances(className);			
			Iterator<ClassifiedInstance> it = instances.getIterator();
			while (it.hasNext()) {
				ClassifiedInstance inst = it.next();
				addPrediction(inst.getInstanceId(), inst.getConfidences());		
			}
		}
	}
	
	/**
	 * Adds predictions for multiple instances
	 * @param predictions instance ids and their predictions key: instance id, value: for each possible class, the confidence of 
	 *        each possible classifier from the ensemble that the instance belongs to that class
	 */
	public void addPredictions(Map<Double, Confidences> predictions){
		for(double id : predictions.keySet()){
			addPrediction(id, predictions.get(id));
		}
		
	}
	
	/**
	 * Returns the id of the classifier
	 * @return classifier id
	 */
	public int getId() {
		return id;
	}
	/**
	 * Sets the id of the classifier
	 * @param id the new id value
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Returns the list of ids of the instances ensemble was applied on
	 * @return the list of ids of the instances from the ensemble statistics
	 */
	public List<Double> getInstanceId() {
		return instanceId;
	}

	/**
	 * Returns classifier confidences for the instance (for each possible class, the confidence of each possible classifier from the ensemble that 
	 * the instance belongs to that class). Returns null if the instance hasn't been classified by the ensemble (is not present in ensemble statistics)
	 * @param instanceId instance id
	 * @return classifiers confidences when classifying the instance with the given id or null if the instance hasn't been classified by this ensemble
	 */
	public Confidences getConfidences(double instanceId){
		for(int i=0; i<this.instanceId.size(); i++){
			if(instanceId == this.instanceId.get(i))
				return this.confidence.get(i);
		}
		return null; // instance not labeled by the ensamble
	}
	
	/**
	 * Returns the confidence of one of the classifiers from the ensemble that the instance belongs to the given class
	 * @param instanceId instance id
	 * @param label the name of the class to get the confidence for
	 * @param classifierNo the index of the classifier from the ensemble (starting from 0) 
	 * @return the confidence of the classifier from the ensemble that the instance belongs to this class
	 */
	public Double getConfidence(double instanceId, String label, int classifierNo){
		for(int i=0; i<this.instanceId.size(); i++){
			if(instanceId == this.instanceId.get(i))
				return this.confidence.get(i).getConfidence(label, classifierNo);
		}
		return null; // instance not labeled by classifier
	}
	
	/**
	 * Returns the co-training style ensemble confidence that the instance belongs to the specified class. Co-training style confidence for a class is calculated
	 * by multiplying the confidence of each of the classifiers from the ensemble that the instance belongs to the specified class. 
	 * If there is only one classifier in the ensemble this method returns the confidence of the single classifier. Returns null if instance is not 
	 * labeled by the ensemble   
	 * @param instanceId the id of the instance
	 * @param label the name of the class to get the confidence for 
	 * @return ensemble confidence for the class (co-training style)
	 */
	public Double getCombinedConfidence(double instanceId, String label){
		for(int i=0; i<this.instanceId.size(); i++){
			if(instanceId == this.instanceId.get(i))
				return this.confidence.get(i).getCombinedConfidence(label);
		}
		return null; // instance not labeled by classifier
	}
	
	/**
	 * Returns the ensemble prediction for the instance. The ensemble prediction is based on co-training style ensemble confidence, see 
	 * {@link #getCombinedConfidence(double, String)}. The prediction is determined as the class label with the highest ensemble confidence. If all classes are 
	 * equally probable, ensemble will vote in the favor of the 1st class listed in the classNames parameter of the {@link DatasetSettings}. Returns null if the
	 * instance was not classified by the ensemble 
	 * @param instanceId instance id
	 * @return ensemble prediction for the instance
	 */
	public String getPrediction(double instanceId){
		for(int i=0; i<this.instanceId.size(); i++){
			if(instanceId == this.instanceId.get(i))
				return this.confidence.get(i).getPrediction();
		}
		return null; // instance not labeled by the ensemble
	}
	
	/**
	 * Returns the number of classifiers in the ensamble
	 * @return number of classifiers in the ensemble
	 */
	public int getClassifierNo(){
		return confidence.get(0).getClassifierNo();  
	}
}
