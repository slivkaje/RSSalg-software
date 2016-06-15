package algorithms.RSSalg.voter;

import algorithms.RSSalg.resultStatistic.Label;
import algorithms.RSSalg.resultStatistic.Votes;

/**
 * Interface for the classes that aggregate the predictions of multiple classifiers in order to produce a final prediction
 *
 */
public interface VoterIF {
	
	/**
	 * Aggregates the predictions of multiple classifiers in order to assign a final label
	 * @param predictions predictions of multiple classifiers
	 * @return the assigned label
	 */
	public Label vote(Votes predictions);

	/**
	 * Returns the name of the algorithm
	 * @return name of the voter algorithm
	 */
	public String getName();
}
