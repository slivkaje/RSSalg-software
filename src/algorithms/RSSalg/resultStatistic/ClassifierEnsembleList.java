/** 	
 * Name: ClassifierEnsembleList.java
 * 
 * Purpose:  Represents the list of created classifier ensembles.
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

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import experimentSetting.DatasetSettings;
import algorithms.RSSalg.voter.MajorityVoter;

/**
 * Represents the list of created classifier ensembles.
 * <br>For example, in RSSalg multiple co-training classifiers are created. Each co-training classifier is an ensemble of two classifiers (one for each view of the data). 
 * <br>The class is annotated with JAXB annotations so that it can be easily written/read to/from an XML file.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassifierEnsembleList {
	/**
	 * List of all example ids in the statistic (instances classified by any of the ensembles from the list, e.g. labeled by at least one co-training classifier trained
	 * in RSSalg)
	 */
	@XmlTransient
	Set<Double> exampleIDs = new HashSet<Double>();
	/**
	 * List of classifier ensembles
	 */
	@XmlElement(name = "ensemble")
	List<ClassifierEnsemble> ensembles = new Vector<ClassifierEnsemble>();

	/**
	 * Aggregated votes of all ensembles from the ensembles list for each instance that occurs
	 * key: instance id, value: votes for the instance
	 */
	@XmlTransient
	Map<Double, Votes> votes = new HashMap<Double, Votes>(); 
	
	/**
	 * Returns all classifier ensembles from the list
	 * @return all ensembles 
	 */
	public List<ClassifierEnsemble> getEnsembles() {
		return ensembles;
	}
	/**
	 * Sets the classifier ensemble list
	 * @param ensembles classifier ensembles
	 */
	public void setClassifiers(List<ClassifierEnsemble> ensembles) {
		this.ensembles = ensembles;
		for(int i=0; i<ensembles.size(); i++){
			List<Double> classifierExampleIDs = ensembles.get(i).getInstanceId();
			exampleIDs.addAll(classifierExampleIDs);
		}			
	}
	/**
	 * Adds an ensemble to the classifier ensemble list
	 * @param ensemble a new ensemble to add to the list
	 */
	public void addClassifier(ClassifierEnsemble ensemble){
		this.ensembles.add(ensemble);
		List<Double> classifierExampleIDs = ensemble.getInstanceId();
		exampleIDs.addAll(classifierExampleIDs);
	}
	
	/**
	 * Add multiple ensembles to the classifier ensemble list
	 * @param otherEnsembles list of ensembles to be added to this list
	 */
	public void addClassifiers(ClassifierEnsembleList otherEnsembles){
		if(otherEnsembles != null)
			if(otherEnsembles.getEnsembles() != null)
				for(ClassifierEnsemble ensemble : otherEnsembles.getEnsembles())
					ensembles.add(ensemble);
	}
	
	/**
	 * For the instance defined by the supplied id, aggregates the votes from all ensembles 
	 * @param instanceID instance id
	 * @return aggregated ensemble votes for the instance
	 */
	public List<Label> predictions(double instanceID){
		
		// initiate the predictions for the instance by adding all possible label names in the way they are listed in the className parameter of DataSettings
		List<Label> predictions = new ArrayList<Label>();
		for(String label : DatasetSettings.getInstance().getClassNames())
			predictions.add(new Label(label));
		
		for(ClassifierEnsemble ensemble : ensembles){ // for each ensemble from the list			
			String prediction = ensemble.getPrediction(instanceID); // get the prediction of that ensemble
			if(prediction == null) // classifier did not vote for the instance, skip classifier
				continue;
			
			// for each label
			for(Label label : predictions){				
				
				// if the given ensemble predicts that the instance belongs to this class increase the ensemble votes count for that prediction 
				if(label.getName().equals(prediction)){					
					label.increaseVotes();
				}
				
				// update the entropy by the confidence of this ensemble that the instance belongs to that class:
				int classifierNo = getEnsembles().get(0).getClassifierNo(); // number of classifiers in this ensemble (e.g. 2 for co-training ore 1 for single classifier)
				// for all classifiers in the given ensemble: use its confidence to update the entropy  
				for(int classifierInd=0; classifierInd<classifierNo; classifierInd++){
					Double classifierConfForLabel = ensemble.getConfidence(instanceID, label.getName(), classifierInd);
					label.updateEntropy(classifierConfForLabel);
				}
				// end updating the entropy
				// NOTE: for now these entropies are used only as the confidence of the final MajorityVote classifier and do not affect the results of the experiments				
			}			
		}
		
		return predictions;
	}
	
	/**
	 * Writes the data to XML
	 * @param stream output stream for writing
	 * @throws JAXBException if there was an error writing the data (most probably the folder the output file should be in is missing)
	 */
	public void toXML(OutputStream stream) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(ClassifierEnsembleList.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(this, stream);
	}
	
	/**
	 * Load the data from the XML file
	 * @param filename the name of the XML file (includes path)
	 * @throws JAXBException if there was an error reading the file. Check if the file is missing or XML element structure does not match.
	 */
	public void fromXML(String filename) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(ClassifierEnsembleList.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		ClassifierEnsembleList cl = (ClassifierEnsembleList) unmarshaller.unmarshal(new File(filename));
		for(ClassifierEnsemble classifier : cl.ensembles){
			addClassifier(classifier);
		}
	}
	
	/**
	 * Returns the instances (and their majority-vote assigned labels) that exceed the label agreement and example occurrence thresholds
	 * @param labelAgreementTS the label agreement threshold
	 * @param exampleOccurenceTS the example occurrence  threshold
	 * @return the instances and their assigned labels (by majority voting of the ensembles). Key: instance id, value: the predicted label 
	 */
	public Map<Double, String> getExamplesThatExceedThresholds(double labelAgreementTS, double exampleOccurenceTS){
		// aggregate the votes from all ensembles from the ensemble list for all instances
		aggregateVotes();		
		
		Map<Double, String> result = new HashMap<Double, String>();		
		Iterator<Double> it = exampleIDs.iterator();
		MajorityVoter voter = new MajorityVoter();
		while(it.hasNext()){
			double id = it.next();
			if( getExampleOccurencePercent(id) >= exampleOccurenceTS && getLabelAgreementPercent(id) >= labelAgreementTS){
				Label assignedLabel = voter.vote(votes.get(id));
				result.put(id, assignedLabel.getName());
			}							
		}
		return result;
	}
	
	/**
	 * Returns the instances (and their majority-vote assigned labels) that do not exceed the label agreement and example occurrence thresholds
	 * @param labelAgreementTS the label agreement threshold
	 * @param exampleOccurenceTS the example occurrence  threshold
	 * @return the instances and their assigned labels (by majority voting of the ensembles). Key: instance id, value: the predicted label 
	 */
	public Map<Double, String> getExamplesThatDontExceedThresholds(double labelAgreementTS, double exampleOccurenceTS){
		// aggregate the votes from all ensembles from the ensemble list for all instances
		aggregateVotes();		
		Map<Double, String> result = new HashMap<Double, String>();		
		Iterator<Double> it = exampleIDs.iterator();
		MajorityVoter voter = new MajorityVoter();
		while(it.hasNext()){
			double id = it.next();
			if( getExampleOccurencePercent(id) < exampleOccurenceTS || getLabelAgreementPercent(id) < labelAgreementTS){
				Label assignedLabel = voter.vote(votes.get(id));
				result.put(id, assignedLabel.getName());
			}							
		}
		return result;
	}
	
	/**
	 * Get n least confident instances from the set of predictions.
	 * Confidence score for one instance is calculated as example_occurence_percent(instance) + label_agreement_percent(instance)
	 * @param number the desired number of least confident instances
	 * @param predictions the predictions to choose from 
	 * @return the sorted list (from lowest confidence to highest) of least confident instances
	 */
	public List<Double> getLeastConfident(int number, Map<Double, String> predictions){
		// aggregate the votes from all ensembles from the ensemble list for all instances
		aggregateVotes();
		Set<Double> ids = predictions.keySet();
		Iterator<Double> it = ids.iterator();
		List<Double> leastConfident = new ArrayList<Double>();		
		
		while(it.hasNext()){
			double id = it.next();
			double score = getExampleOccurencePercent(id) + getLabelAgreementPercent(id);
			
			if(leastConfident.size() == 0){
				leastConfident.add(id);
			}else{ 
				for(int i=0; i<leastConfident.size(); i++){
					double currId = leastConfident.get(i);
					double currScore = getExampleOccurencePercent(currId) + getLabelAgreementPercent(currId);
					if(currScore > score){
						leastConfident.add(i, id);
						break;
					}
					
				}
				if(leastConfident.size() > number)
					leastConfident.remove(leastConfident.size()-1);
			}
		}
		return leastConfident;
	}
	
	/**
	 * Return the total number of instances in the statistics. This is the number of instances classified by at least one of the ensembles from the ensemble list, e.g. 
	 * by at least one co-training classifier in RSSalg
	 * @return the total number of instances 
	 */
	public int getStatisticSize(){
		return exampleIDs.size();
	}
	
	/**
	 * Aggregates the votes from all ensembles
	 */
	public void aggregateVotes(){
		if (exampleIDs.size() == votes.size())
			return; // votes already aggregated, would be empty otherwise

		// for all instances that were labeled by at least one of the ensembles from the ensemble list (e.g. by at least one co-training classifier in RSSalg) 
		for(double id: exampleIDs ){
			List<Label> predictions = predictions(id); // get aggregated predictions (all ensembles from the list) for that instance
			Votes ensembleVotes = new Votes(predictions);
			votes.put(id, ensembleVotes);
		}
	}
	
	/**
	 * Returns the label agreement percent for the instance
	 * @param instanceID instance id
	 * @return label agreement percent for the instance
	 */
	public double getLabelAgreementPercent(double instanceID){
		aggregateVotes();
		Votes vote = votes.get(instanceID);
		MajorityVoter voter = new MajorityVoter();		
		Label assignedLabel = voter.vote(vote);		
		return ( (double) assignedLabel.getNoVotes())/vote.getNumberOfVotes();	
	}
	
	/**
	 * Returns the example occurrence percent for the instance
	 * @param instanceID instance id
	 * @return example occurrence percent for the instance
	 */
	public double getExampleOccurencePercent(double instanceID){
		aggregateVotes();
		Votes vote = votes.get(instanceID);				
		return ((double) vote.getNumberOfVotes()) / ensembles.size();
	}
	
	/**
	 * Returns the minimal recorded example occurrence percent. I.E. exampleOccurencePercent of the example (instance) that was labeled by the 
	 * least number of classifiers of all examples 
	 * @return minimal example occurrence percent of all recorded instances
	 */
	public double getMinExampleOccurencePercent(){		
		aggregateVotes();
		double minExamplePerc = Double.MAX_VALUE;
		for(double id : votes.keySet()){
			if (getExampleOccurencePercent(id) < minExamplePerc)
				minExamplePerc = getExampleOccurencePercent(id);
		}
		return minExamplePerc;
	}
	
	/**
	 * Returns the minimal recorded label agreement percent. I.E. labelAgreementPercent of the example (instance) that the classifiers that labeled it most 
	 * disagree on the label  
	 * @return minimal label agreement percent of all recorded instances
	 */
	public double getMinLabelAgreementPercent(){		
		aggregateVotes();
		double minLabelPerc = Double.MAX_VALUE;
		for(double id : votes.keySet()){
			if (getLabelAgreementPercent(id) < minLabelPerc)
				minLabelPerc = getLabelAgreementPercent(id);
		}
		return minLabelPerc;
		
	}
	
	/**
	 * Returns the aggregated votes (from all ensembles in the ensemble list) for an instance
	 * @param instanceID instance id
	 * @return aggregated votes for the instance
	 */
	public Votes getVotes(double instanceID){
		aggregateVotes();
		return votes.get(instanceID);
	}
	
	/**
	 * Checks whether the statistics contains the instance with the given id 
	 * @param id instance id
	 * @return true if the instance is recorded in the statistics, false otherwise
	 */
	public boolean containsID(double id){
		return exampleIDs.contains(id);		
	}
	
	/**
	 * Returns the ids of all instances recorded in the statistics
	 * @return ids of all instances recorded in the statistics
	 */
	public Set<Double> getExampleIDs(){
		return exampleIDs;
	}
}
