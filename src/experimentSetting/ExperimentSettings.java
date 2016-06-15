package experimentSetting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import util.PropertiesReader;
import algorithms.Algorithm;
import algorithms.SupervisedAlgorithm_All;
import algorithms.SupervisedAlgorithm_L;
import algorithms.RSSalg.MajorityVote;
import algorithms.RSSalg.RSSalg;
import algorithms.RSSalg.GA.CandidateEvaluatorIF;
import algorithms.RSSalg.voter.VoterIF;
import classificationResult.measures.MeasureIF;
import featureSplit.SplitterIF;

/**
 * Class encapsulating all experiment settings: algorithm which is run (e.g. co-training or RSSalg), 
 * feature splitting algorithm, etc.
 * Singleton object   
 */
public class ExperimentSettings {
	private static ExperimentSettings instance = null;
	protected Algorithm algorithm = null; // Algorithm that will be run (currently supports RSSalg and co-training)
	
	// Class implementing SplitterIF used for splitting features. If not specified, split is not applied.
	// Can be used for natural split: views are defined by fileNames in dataSettings
	// This setting does not apply to RSSalg which always uses DifferentRandomSplitsSplitter
	protected SplitterIF splitter = null;
	
	protected boolean balancedSplit = true; // for now, only ballanced split is supported
	
	// Number of different splits used with co-training (parameter m in RSSalg) or number of Random Splits. This parameter is specified
	// only if RSSalg or Co-training wRandom is run (otherwise it is ignored) 
	protected int noSplits = 1;
	// The measures to be calculated in the experiment
	protected List<MeasureIF> measures = new ArrayList<MeasureIF>();
//	protected String finalClassifierClassName;
	protected boolean loadClassifierStatistic = false;
	protected boolean writeClassifiers = true;
	protected String ClassifiersFilename = null;
	protected boolean writeEnlargedCoTrainingSet = false;
	// read only for RSSalg
	protected CandidateEvaluatorIF evaluator = null;
	protected VoterIF voter = null;
	
	public static ExperimentSettings getInstance() {
		if(instance == null) {
			instance = new ExperimentSettings();
	    }
	    return instance;
	}          
	
	public boolean isInitiated(){
		return algorithm != null;
	}
	
	public void clear(){
		algorithm = null;
		splitter = null;
		balancedSplit = true;
		noSplits = 1;
		measures.clear();
		loadClassifierStatistic = false;
		writeClassifiers = true;
		ClassifiersFilename = null;
		writeEnlargedCoTrainingSet = false;
		evaluator = null;
		voter = null;
		System.out.println("Experiment settings cleared.");
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}
	private void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	public SplitterIF getSplitter() {
		return splitter;
	}
	private void setSplitter(SplitterIF splitter) {
		this.splitter = splitter;
	}

	public boolean isBalancedSplit() {
		return balancedSplit;
	}
	public int getNoSplits() {
		return noSplits;
	}
	private void setNoSplits(int noSplits) throws Exception {
		if(noSplits < 1)
			throw new Exception("There must be at least 1 feature split for co-training. Trying to set " + noSplits + ")");
		this.noSplits = noSplits;
	}
	public List<MeasureIF> getMeasures() {
		return measures;
	}
	private void addMeasure(MeasureIF measure) {
		this.measures.add(measure);
	}
//	public String getFinalClassifierClassName() {
//		return finalClassifierClassName;
//	}
//	private void setFinalClassifierClassName(String finalClassifierClassName) throws Exception {
//		this.finalClassifierClassName = finalClassifierClassName;
//		getFinalClassifier(); // check whether it is a valid classifier name 
//	}
	public boolean isLoadClassifierStatistic() {
		return loadClassifierStatistic;
	}
	private void setLoadClassifierStatistic(boolean loadClassifierStatistic) {
		this.loadClassifierStatistic = loadClassifierStatistic;
	}
	public String getClassifiersFilename() {
		return ClassifiersFilename;
	}
	private void setClassifiersFilename(String classifiersFilename) {
		ClassifiersFilename = classifiersFilename;
	}
	public boolean isWriteClassifiers() {
		return writeClassifiers;
	}
	private void setWriteClassifiers(boolean writeClassifiers) {
		this.writeClassifiers = writeClassifiers;
	}
	public CandidateEvaluatorIF getEvaluator() {
		return evaluator;
	}
	private void setEvaluator(CandidateEvaluatorIF evaluator) {
		this.evaluator = evaluator;
	}
	public boolean isWriteEnlargedCoTrainingSet() {
		return writeEnlargedCoTrainingSet;
	}
	private void setWriteEnlargedCoTrainingSet(boolean writeEnlargedCoTrainingSet) {
		this.writeEnlargedCoTrainingSet = writeEnlargedCoTrainingSet;
	}
	public VoterIF getVoter() {
		return voter;
	}
	private void setVoter(VoterIF voter) {
		this.voter = voter;
	}

	public void readProperties(String propertiesFile) throws Exception{
		Properties properties = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(propertiesFile));
		}catch (FileNotFoundException e) {
			throw new Exception("ERROR: error reading properties file: file " + propertiesFile + "does not exist", e);
		}
		clear();
		System.out.println("Reading the experiment settings from file: " + propertiesFile);
		
		
		setAlgorithm((Algorithm) PropertiesReader.readObjectParam(properties, "algorithm"));
		
		List<String> measureClassNames = PropertiesReader.readStringListParam(properties, "measures");
		List<String> measuresForClasses = PropertiesReader.readStringListParam(properties, "measuresForClass");
		if(measureClassNames.size() != measuresForClasses.size()){
			throw new Exception("ERROR: a class needs to be specified for each measure (\"avg\" if the measure does not depend on a class).");
		}
		for(int i=0; i<measureClassNames.size(); i++){
			MeasureIF measure = (MeasureIF) PropertiesReader.getObject(measureClassNames.get(i));
			if(!measure.dependsOnClass()){
				addMeasure(measure);
			}else{
				measure.setClassName(measuresForClasses.get(i));
				addMeasure(measure);
			}
		}
		
		setLoadClassifierStatistic(PropertiesReader.readBooleanParam(properties, "loadClassifiers"));
		setWriteClassifiers(PropertiesReader.readBooleanParam(properties, "writeClassifiers"));
		try{
			setClassifiersFilename(PropertiesReader.readStringParam(properties, "ClassifiersFilename"));
		}catch(Exception e){
			if (loadClassifierStatistic || (algorithm instanceof MajorityVote))
				throw new Exception("Classifiers file name must be specified for loading classifiers", e);
		}
		setWriteEnlargedCoTrainingSet(PropertiesReader.readBooleanParam(properties, "writeEnlargedTrainingSet"));
		
		try{
			setVoter((VoterIF) PropertiesReader.readObjectParam(properties, "voter"));
		}catch(Exception e){ // voter not specified
			if(algorithm instanceof MajorityVote )
				throw new Exception("Voter interface must be specified for " + algorithm.getName() + " algorithm");
		}
		
		if (!(algorithm instanceof SupervisedAlgorithm_L || algorithm instanceof SupervisedAlgorithm_All)){  // if so, no need for other parameters
			String splitterClassName = PropertiesReader.readStringParam(properties, "featureSpliter");
			if (splitterClassName == null){			
				setSplitter(null); // run natural split
			}else{
				setSplitter((SplitterIF) PropertiesReader.readObjectParam(properties, "featureSpliter"));
			}

			try{
				setNoSplits(PropertiesReader.readInt(properties, "noSplits"));
			}catch(Exception e){
				// if not specified, a single split is used 
			}
		}
		
		if(algorithm instanceof RSSalg){
			setEvaluator((CandidateEvaluatorIF) PropertiesReader.readObjectParam(properties, "candidateEvaluator"));
		}
		
		String resultFolder = "";
		try{
			resultFolder = DatasetSettings.getInstance().getResultFolder();
			PrintStream writer = new PrintStream(new FileOutputStream(resultFolder + "/Experiment.txt", true)); 
			printSettings(writer);
			writer.close();
		}catch(Exception e){
			System.out.println("WARNING: could not write the data settings for the experiment in file " + resultFolder + "/Experiment.txt");
		}
	} 
	
	/**
	 * Print the experiment setting
	 * @param out : PrintStream for writing   
	 */
	public void printSettings(PrintStream out){	
		out.println("EXPERIMENT SETTINGS:");
		out.println("\tRunning " + algorithm.getClass().getName() + " algorithm");
		if(algorithm instanceof SupervisedAlgorithm_L || algorithm instanceof SupervisedAlgorithm_All)
			return; 
		if(algorithm instanceof RSSalg){
			out.println("\tUsing DifferentRandomSplitsSplitter for feature splits");
			out.println("\tCandidate evaluator for GA: " + evaluator.getName());
			out.println("\tFinal classifier: " + /*finalClassifierClassName*/ DatasetSettings.getInstance().getCombinedClassiffierClassName());			
		}else if (splitter == null)
			out.println("\tUsing a natural feature split (" + DatasetSettings.getInstance().getNoViews() + " views, ballanced: " + balancedSplit + ")");
		else
			out.println("\tUsing " + splitter.getClass().getName() + " for feature splitting (" + DatasetSettings.getInstance().getNoViews() + " views, ballanced: " + balancedSplit + ")");
		out.println("\tRunning " + noSplits + " splits with co-training");	
		if(loadClassifierStatistic)
			out.println("\tLoading classifier statitics from file " + ClassifiersFilename);
		if(writeClassifiers)
			out.println("\tWriting classifier statistics");
		
	}
}
