package algorithms.RSSalg.resultStatistic;

/**
 * Aggregates the votes from multiple ensembles that the instance belongs to one particular label.
 * The entropy of the given label is calculated in the following way:<br>
 * <pre>
 * for all ensembles from the list i
 * 		for all classifiers in the i-th ensemble j:
 *	 		sum(-probability_ij*Math.log(probability_ij))
 *</pre> 
 *<br>where probability_ij is the confidence of j-th classifier of the i-th ensemble that the instance belongs to this class (confidences of all classifiers from all 
 *ensembles are used).
 *<br>NOTE: for now these entropies are used only as the confidence of the final MajorityVote classifier and do not affect the results of the experiments
 */
public class Label {
	/**
	 * The label name
	 */
	String name;
	/**
	 * Entropy of ensemble votes: 
	 * for all ensembles from the list i
	 * 	for all classifiers in the i-th ensemble j:
	 * 		sum(-probability_ij*Math.log(probability_ij)) where probability_ij is the confidence of j-th classifier of the i-th ensemble that the instance 
	 * belongs to this class
	 */
	double entropy = 0;
	/**
	 * Number of ensembles that voted for the label (that predict that the instance belongs to this class)
	 */
	int noVotes = 0;
	
	/**
	 * Creates a new instance of the Label
	 * @param name class name
	 */
	public Label(String name){
		this.name = name;
	}
	
	/**
	 * Creates a new instance of the Label
	 * @param name class name
	 * @param entropy entropy of ensemble votes for the label
	 * @param noVotes number of ensembles that voted for the label (no. of ensembles that predict that the instance belongs to this class)  
	 */
	public Label(String name, double entropy, int noVotes) {
		this.name = name;
		this.entropy = entropy;
		this.noVotes = noVotes;
	}

	/**
	 * Returns the label name
	 * @return label name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Sets the label name
	 * @param name class name
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * Returns the entropy of ensemble votes for this label
	 * @return entropy of ensemble votes for this label
	 */
	public double getEntropy() {
		return entropy;
	}
	/**
	 * Sets the entropy of ensemble votes votes for this label
	 * @param entropy entropy of ensemble votes for this label
	 */
	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}

	/**
	 * Returns the number of ensembles that predict that the instance belongs to this class (the number of ensembles that render this is the most probable class 
	 * for the instance)
	 * @return number of votes for the label
	 */
	public int getNoVotes() {
		return noVotes;
	}
	/**
	 * Sets the number of ensembles that predict that the instance belongs to this class (the number of ensembles that render this is the most probable class 
	 * for the instance)
	 * @param noVotes number of votes for the label
	 */
	public void setNoVotes(int noVotes) {
		this.noVotes = noVotes;
	}
	
	/**
	 * Uses the confidence of one classifier to update the current setting of the total entropy of ensemble votes  
	 * @param probability the confidence that the instance belongs to this class
	 */
	public void updateEntropy(double probability){
		if(probability!=0)
			entropy += -probability*Math.log(probability);
	}
	/**
	 * Increases the number of ensembles that voted for this label (the number of ensembles that render this is the most probable class for the instance)
	 */
	public void increaseVotes(){
		noVotes++;
	}

	@Override
	public String toString() {
		return "Label [name=" + name + ", entropy=" + entropy + ", noVotes="
				+ noVotes + "]";
	}
}
