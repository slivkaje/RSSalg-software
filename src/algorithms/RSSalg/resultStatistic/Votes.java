/** 	
 * Name: Votes.java
 * 
 * Purpose: Represents the votes of multiple ensembles for one particular instance.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the votes of multiple ensembles for one particular instance:
 * <ul> 
 * <li>the list of all possible labels and their assigned measures (see {@link Label})   
 * <li>the total number of ensembles that voted for this instance. For example, in RSSalg multiple co-training classifiers are trained (each co-training 
 * classifier is an ensemble). For one particular instance i, some co-training classifiers might have voted for the label (picked it from unlabeled set for labeling) 
 * and some might have not. This is the number of co-training classifiers that labeled the instance
 * </ul>
 *
 */
public class Votes {
	/**
	 * List of assigned labels
	 */
	protected List<Label> labels = new ArrayList<Label>();
	/**
	 * The total number of votes (sum of the number of votes for each of the labels)
	 */
	protected int numberOfVotes = 0;
	
	/**
	 * Creates a new instance of Votes object. Note: assumes that the labels are added to the list in the order of class names defined in DataSettings   
	 * @param labels the list of the assigned labels
	 */
	public Votes(List<Label> labels){
		this.labels = labels;
		numberOfVotes = 0;
		for(Label label : labels){
			numberOfVotes += label.getNoVotes();
		}
	}

	/**
	 * Returns the list of the assigned labels
	 * @return list of assigned labels
	 */
	public List<Label> getLabels() {
		return labels;
	}
	/**
	 * Returns the total number of votes (number of ensembles that voted for this instance)
	 * @return total number of votes
	 */
	public int getNumberOfVotes() {
		return numberOfVotes;
	}

	@Override
	public String toString() {
		String retStr = "";
		for(Label label : labels)
			retStr += label + "\n";
		return retStr;
	}
	
	/**
	 * Returns the list of entropies of votes for the labels 
	 * @return list of label entropies (in the same ordering as labels)
	 */
	public Confidences getEntropies(){
		Confidences confidences = new Confidences();
//		for(String labelName : DatasetSettings.getInstance().getClassNames()){ // in order to ensure that confidences are in the same order of labels for each example
		// later: votes object is created in Classifiers aggregateVotes() method. Labels are always in the same order (defined by the order of class names in experiment settings)
			for(Label label : labels)
//				if(label.getName().equals(labelName)){ 
					confidences.addConfidence(label.getEntropy());
//					break;
//				}
//		}
		return confidences;
	}
}
