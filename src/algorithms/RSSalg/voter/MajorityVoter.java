package algorithms.RSSalg.voter;

import algorithms.RSSalg.resultStatistic.Label;
import algorithms.RSSalg.resultStatistic.Votes;

/**
 * Uses a simple majority vote of multiple trained classifiers in order to produce a final prediction 
 */
public class MajorityVoter implements VoterIF {

	/**
	 * Predicts the label of the instance using the simple majority vote of classifiers
	 * @see algorithms.RSSalg.voter.VoterIF#vote(algorithms.RSSalg.resultStatistic.Votes)
	 */
	public Label vote(Votes predictions) {
		Label mostVoted = null;
		int mostVotes = 0;
		for(Label label : predictions.getLabels())
			if(label.getNoVotes() > mostVotes){
				mostVoted = label;
				mostVotes = label.getNoVotes();
			}			
		return mostVoted;
	}

	public String getName() {
		return "Majority_vote";
	}
}
