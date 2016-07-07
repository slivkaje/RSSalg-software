package setExperiment;

import java.util.Random;
import algorithms.co_training.CoTrainingData;
import util.InstancesManipulation;
import weka.core.Instance;
import weka.core.Instances;
import experimentSetting.CVSettings;
import experimentSetting.DatasetSettings;

/**
 * Loads the dataset and prepares the data for the n-fold-cross validation experiment described in:
 * <p>
 * F. Feger and I. Koprinska. Co–training Using RBF Nets and Different Feature Splits. In Proceedings of 2006 International Joint Conference on Neural Network, pp. 1878–1885, 2006.  
 */
public class CrossValidationSeparator {	
	/**
	 * Loaded original files. Each Instances object form the array corresponds to one view
	 */
	protected Instances[] originalFiles; 
	/**
	 * Restarted random number generator
	 */
	private Random rand = DatasetSettings.getInstance().getRestartedRandom();
	/**
	 * The instance of cross-validation experiment settings
	 */
	CVSettings CVsettings = CVSettings.getInstance();
	/**
	 * The instance of data settings
	 */
	DatasetSettings dataSettings = DatasetSettings.getInstance();
	
	/**
	 * Divides the data in n parts of roughly equal size.
	 * 
	 * @return : data divided in folds. The first dimension corresponds to fold and the second dimension corresponds to different views of that fold.
	 */
	private Instances[][] createFolds(){		
		Instances[][] folds = new Instances[CVsettings.getNoFolds()][dataSettings.getNoViews()];
				
		for (int view = 0; view < this.originalFiles.length; view++) {
			// Initialize random number generator. The random generator is restarted with the same random seed for each of the views so that the sequence of 
			// generated random numbers would be same for each view (same instances are selected from each view) 
			rand = dataSettings.getRestartedRandom();  
			// Shuffle and stratify original arffs. All views are randomized the same way, since both use the same sequence of random numbers.
			this.originalFiles[view].randomize(rand);
			this.originalFiles[view].stratify(CVsettings.getNoFolds());
			
			for (int currFold = 0; currFold < CVsettings.getNoFolds(); currFold++) {
				folds[currFold][view] = this.originalFiles[view].testCV(CVsettings.getNoFolds(), currFold);
			}
		}
		
		return folds;
	}

	/**
	 * Chooses the predefined number of labeled examples per each class from the given dataset
	 *  
	 * @param data data to choose labeled examples from. Elements of the the array (each Instance object) correspond to different view of the data 
	 * 
	 * @return chosen labeled instances. Each Instance object (element from the returned array) corresponds to a different view of the data
	 */
	private Instances[] chooseLabeled(Instances[] data){
		Instances[] labeledData = new Instances[data.length]; 
		for(int view=0; view<data.length; view++){
			labeledData[view] = InstancesManipulation.createEmptyDataset(data[view]);
		}
		
		for(String className: dataSettings.getClassNames()){
			int noInstancesToChoose = CVsettings.getNoLabeled().get(className); // number of instances to choose for the given class
			int chosen = 0; // number of instances already chosen
			while(true){	
				int instInd = rand.nextInt(data[0].numInstances()); // random instance from the first view
				Instance instance = data[0].instance(instInd);
				String instanceLabel = InstancesManipulation.getLabel(instance);
				
				if(instanceLabel != null)
				if(instanceLabel.equals(className)){ // if randomly chosen instance belongs to the desired class
					// moves the same instance for all views from data to labeled. It is assumed that instances in different views have same ordering
					InstancesManipulation.moveInstance(data, labeledData, instInd); 
					chosen++;
					if(chosen == noInstancesToChoose){							
						break;
					}
				}
			}			
		}		
		return labeledData;
	}
	
	/**
	 * Loads the data files that will be divided for n-fold-cross validation 
	 * @throws Exception 
	 * <ul>
	 * <li> the ARFF file is missing
	 * <li> class attribute is missing (there is no attribute in the dataset that matches the name of the class attribute given in the data properties)
	 * <li> adding an id attribute failed
	 * </ul>
	 */
	private void loadOriginalFiles() throws Exception{		
		this.originalFiles = new Instances[dataSettings.getNoViews()];
		
		if(dataSettings.getFileNames().size() == this.originalFiles.length){
			for(int view=0; view < this.originalFiles.length; view++){
				this.originalFiles[view] = InstancesManipulation.readArff(dataSettings.getFileNames().get(view), true);
			}
		}else{
			System.out.println("WARNING: specified no. of data files different from the specified number of views. "
					+ "All features will be moved to the first view. Other views will contain only class and id attribute. Perform a split later "
					+ "to move features to these views.");
			originalFiles[0] =  InstancesManipulation.readArff(dataSettings.getFileNames().get(0), true);
			for(int view=1; view < this.dataSettings.getFileNames().size(); view++){
				Instances data = InstancesManipulation.readArff(dataSettings.getFileNames().get(view), true);
				originalFiles[0] = InstancesManipulation.mergeAttributes(originalFiles[0], data);
			}
			for(int view=1; view < this.originalFiles.length; view++){
				originalFiles[view] = InstancesManipulation.cloneDataset(originalFiles[0]);
				originalFiles[view] = InstancesManipulation.removeAllAttributes(originalFiles[view]);
			}
			
		}
	}
	
	/**
	 * Loads the data and prepares the n-fold-cross validation experiment 
	 * 
	 * @return array of prepared CoTrainingData objects, each corresponding to one labeled/unlabeled/test split in n-fold-cross validation experiment 
	 * @throws Exception if the dataset files are missing or corrupt
	 */
	public CoTrainingData[] prepareCrossValidationExperiment() throws Exception{
		try{
			loadOriginalFiles();
		}catch(Exception e){
			throw new Exception("ERROR: failed loading data files: " + e.getMessage(), e);			
		}
		
		CoTrainingData[] data = new CoTrainingData[CVsettings.getNoFolds()];
		
		Instances[][] folds = createFolds();
		
		for(int currentFold = 0; currentFold < CVsettings.getNoFolds(); currentFold++){			
			
			// the n equal parts are copied so they remain intact when moving instances  
			Instances[][] tmpfolds = new Instances[CVsettings.getNoFolds()][dataSettings.getNoViews()]; 
			for(int k = 0; k < CVsettings.getNoFolds(); k++){
				tmpfolds[k] = InstancesManipulation.cloneDataset(folds[k]); 				
			}
			
			int foldForLabeled = currentFold; // part from which to choose labeled data			
			int lowerBoundaryUnlabeled = currentFold;
			int upperBoundaryUnlabeled = (currentFold+ CVsettings.getNoFoldsUnlabeled()- 1) % CVsettings.getNoFolds();
		
			// Initiate labeled, unlabeled and test data: create empty datasets
			Instances[] labeled = new Instances[dataSettings.getNoViews()];
			Instances[] unlabeled = new Instances[dataSettings.getNoViews()];
			Instances[] test = new Instances[dataSettings.getNoViews()];
			for(int view = 0; view < dataSettings.getNoViews(); view++){
				labeled[view] = InstancesManipulation.createEmptyDataset(this.originalFiles[view]);
				unlabeled[view] = InstancesManipulation.createEmptyDataset(this.originalFiles[view]);
				test[view] = InstancesManipulation.createEmptyDataset(this.originalFiles[view]);
			}
			
			labeled = chooseLabeled(tmpfolds[foldForLabeled]); // choose labeled instances from labeled fold
	
			// copy rest of the folds where they belong
			for(int foldInd=0; foldInd<CVsettings.getNoFolds(); foldInd++){
				boolean belongsToUnlabeled = true;
				if(lowerBoundaryUnlabeled < upperBoundaryUnlabeled){
					belongsToUnlabeled = (foldInd >= lowerBoundaryUnlabeled) && (foldInd <= upperBoundaryUnlabeled);
				}else{ 
					belongsToUnlabeled = (foldInd >= lowerBoundaryUnlabeled) || (foldInd <= upperBoundaryUnlabeled);
				}
				
				if(belongsToUnlabeled)
					InstancesManipulation.moveAllInstances(tmpfolds[foldInd], unlabeled);
				else // belongs to the test
					InstancesManipulation.moveAllInstances(tmpfolds[foldInd], test);									
			}
			
			// remove labels from unlabeled data if needed
			if(CVSettings.getInstance().isRemoveLabelsFromUnlabeled()) 
				for(int view=0; view<dataSettings.getNoViews(); view++)
					unlabeled[view] = InstancesManipulation.removeLabel(unlabeled[view]);
			
			data[currentFold] = new CoTrainingData(labeled, unlabeled, test);
		}
	
		return data;
	}	
}
