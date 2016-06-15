package algorithms.co_training;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import classificationResult.ClassifiedInstance;

/**
 * Class representing the list of classified instances ({@link ClassifiedInstance} objects) sorted descending by classification confidence. 
 * The list can only contain the predefined number of most confidently classified instances. 
 * <p>
 * Used in co-training to allow each classifier to select and label a predefined number of unlabeled instances for which it is most confident about the prediction. 
 * For example, according the notation used in<br> 
 * A. Blum, T. Mitchell, "Combining labeled and unlabeled data with co-training", 
 * COLT: Proceedings of the Workshop on Computational Learning Theory, Morgan Kaufmann, 1998, p. 92-100.<br>
 * in each iteration of co-training we would use one ClassifiedInstancesQueue of capacity p for the positively labeled examples 
 * and one ClassifiedInstancesQueue of the capacity n for the negatively labeled examples  
 */
public class ClassifiedInstancesQueue {

	/**
	 * The sorted list of classified instances
	 */
	protected List<ClassifiedInstance> list;
	/**
	 * Maximal number of kept classified instances
	 */
	protected int capacity;

	/**
	 * Creates a new <code>ClassifiedInstancesQueue</code> with the given capacity.
	 * @param capacity maximal number of instances that can be in the list	 
	 * */
	public ClassifiedInstancesQueue(int capacity) {
		this.list = new LinkedList<ClassifiedInstance>();
		this.capacity = capacity;
	}

	/**
	 * Attempts to add another classified instance to the list.
	 * The classified instance is added in the fashion that maintains the ordering of the classified instances (descending, based on prediction confidence)
	 * <p> 
	 * 1. If the list is full to its capacity an instance will be added if it is more confidently labeled
	 * then the last (least confident) member of the classified instances list. The previous last member of the 
	 * list will be removed<br>
	 * 2. If the current size of the list is lower than its capacity the instance will be added to the list
	 *  
	 * @param newInstance the instance to be added
	 */
	public void add(ClassifiedInstance newInstance) {
		if (list.size() == capacity){ // list is already full to its full capacity -> check if the instance should be added
			double lastElementConfidence = list.get(list.size()-1).getCombinedConfidence();
			if(newInstance.getCombinedConfidence() <= lastElementConfidence)
				return; // instance not confident enough, do not add it to the list
		}
		
		// the instance should be added as it is more confident than the last member of the list.
		
		if (list.size() == 0){
			list.add(newInstance);
			return;
		}
		
		if (list.size() == capacity){ // list was already full before adding a new instance -> remove the last one
			list.remove(list.size()-1);
		}
		
		// Find the right position for the instance
		ListIterator<ClassifiedInstance> it = this.list.listIterator();
		while (it.hasNext()) {
			if (it.next().getCombinedConfidence() < newInstance.getCombinedConfidence()){
				list.add(it.previousIndex(), newInstance);
				return;
			}
		}
		// add at the end of queue if worse than all others
		list.add(newInstance); 
	}

	/**
	 * Returns the sorted list of classified instances
	 * @return the sorted list of classified instances (descending by classification confidence)
	 */
	public List<ClassifiedInstance> getList() {
		return list;
	}

	/**
	 * Returns the list of instances in the priority queue as String
	 * @return String that represents the list of classified instances currently in the queue  
	 */
	@Override
	public String toString() {
		String res = "";
		for(ClassifiedInstance inst : list)
			res += inst + "\n";
		return res;
	}
}