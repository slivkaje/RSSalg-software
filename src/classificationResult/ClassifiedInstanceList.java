package classificationResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The list of instances classified by a single classifier or the ensemble of classifiers
 */
public class ClassifiedInstanceList {
	/**
	 * The list of classified instances
	 */
	protected List<ClassifiedInstance> classifiedInstances = new ArrayList<ClassifiedInstance>();
	
	/**
	 * Create a new instance of ClassifiedInstanceList 
	 */
	public ClassifiedInstanceList() {}
	
	/**
	 * Create a new instance of ClassifiedInstanceList
	 * @param classifiedInstances the list of classified instances
	 */
	public ClassifiedInstanceList(List<ClassifiedInstance> classifiedInstances) {
		this.classifiedInstances = classifiedInstances;
	}
	
	/**
	 * Returns an iterator over a collection of classified instances
	 * @return iterator over a collection of classified instances
	 */
	public Iterator<ClassifiedInstance> getIterator(){
		return classifiedInstances.iterator();
	}
	
	/**
	 * Adds the classified instance to the list
	 * @param inst a new classified instance to add
	 */
	public void addInstance(ClassifiedInstance inst){
		classifiedInstances.add(inst);
	}
	
	@Override
	public String toString() {
		String res = "";		
		for(ClassifiedInstance inst : classifiedInstances)
			res += inst + "\n";
		return res;
	}
	
	/**
	 * Generates the ClassificationResult from the classified instances list. Goes through classified instances and checks whether they are correctly classified or not  
	 * @param recordPredictions whether to record the predictions (confidences) of the classifier/ensemble for each classified instance
	 * @return the classification result of applying the classifier (or ensemble) on the set of test instances 
	 */
	public ClassificationResult getClassificationResult(boolean recordPredictions){
		ClassificationResult result = new ClassificationResult(recordPredictions);
		for (ClassifiedInstance inst : classifiedInstances) {
//			if(inst.actualLabel == null)
//				System.out.println("WARNING: instance " + inst.getInstanceId() + " not labeled and thus not included in accuracy calculation");
			double id = inst.getInstanceId();
			
			if(inst.correctlyLabeled() == 1)				
				result.updateTrue(id, inst.getPrediction(), inst.getConfidences());
			else if(inst.correctlyLabeled() == -1)
				result.updateFalse(id, inst.getPrediction(), inst.getConfidences());																			
		}
		
		return result;
	}

	/**
	 * Returns the number of classified instances
	 * @return number of classified instances in the list
	 */
	public int size(){
		return classifiedInstances.size();
	}
}
