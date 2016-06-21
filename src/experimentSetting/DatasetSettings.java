package experimentSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import util.PropertiesReader;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.unsupervised.attribute.RemoveByName;

/**
 * Description of the loaded data
 * Singleton object   
 */
public class DatasetSettings {
	public static String DEFAULT_ID_ATT_NAME = "ID"; // default name for the ID attribute that will be generated
	
	private static DatasetSettings instance = null; // Singleton
	protected String resultFolder = null; // folder in which the results will be recorded
	protected int randSeed = 42; // seed for random number generator used in the experiment
	protected Random rand = null; // rundom number generator
	protected int noViews; // number of views
	/**
	 * If true: read the preset cross-validation experiment. Locations of data files ({@link #fileNames}) will be ignored; if false: create a new cross-validation experiment according to the settings
	 */
	protected boolean loadPresetExperiment = false;
	/**
	 * Data files - locations of files that contain the data. Each file corresponds to one view of the data
	 */
	protected List<String> fileNames = new ArrayList<String>(); 
	protected List<String> classNames = new ArrayList<String>(); // name of each existing class
	protected List<String> classifierClassNames = new ArrayList<String>(); // classification models used for each view (defined as name of the classes that implement WEKA Classifier interface)
	protected String combinedClassiffierClassName = null; // classificatiom model used for the combined classifier (obtained by merging features from individual views)
	protected String classAttributeName = null; // name of the class attribute
	protected String idAttributeName = null; // name of the id attribute (null if the id attribute does not exist)

	private int callsToRandom = 0;
	
	public static DatasetSettings getInstance() {
		if(instance == null) {
			instance = new DatasetSettings();
	    }
	    return instance;
	}
	
	public boolean isInitiated(){
		return classAttributeName != null;
	}
	
	public void claerSettings(){
		resultFolder = null;
		randSeed = 42;
		rand = null;
		noViews = 0;
		loadPresetExperiment = false;
		fileNames.clear();
		classNames.clear();
		classifierClassNames.clear();
		combinedClassiffierClassName = null;
		classAttributeName = null;
		idAttributeName = null;
		callsToRandom = 0;
		System.out.println("Data settings cleared.");
	}
	
	public int getRandSeed() {
		return randSeed;
	}

	public int getNoViews() {
		return noViews;
	}
	private void setNoViews(int noViews) {
		this.noViews = noViews;
	}
	public List<String> getFileNames() {
		return fileNames;
	}
	private void setFileNames(List<String> fileNames) throws Exception {
		this.fileNames = fileNames;
		noViews = fileNames.size();
		if (noViews > 2)
			throw new Exception("ERROR: read data for " + noViews + " views. Currently supports just 2 views");
		
		for(int i=0; i<fileNames.size(); i++){
			String fileName = fileNames.get(i);
			File tmp = new File(fileName);
			if(!tmp.exists())
				throw new Exception("ERROR: file '" + fileName + "' set for view " + i + " does not exist");
		}
	}
	public List<String> getClassNames() {
		return classNames;
	}
	private void setClassNames(List<String> classNames) throws Exception {
		for(int i=0; i<classNames.size(); i++)
			if (classNames.get(i) == null)
				throw new Exception("ERROR: class name for class " + i + " set to be null");
		this.classNames = classNames;
	}
	public String getClassAttributeName() {
		return classAttributeName;
	}
	private void setClassAttributeName(String classAttributeName) throws Exception {
		if (classAttributeName == null)
			throw new Exception("ERROR: class attribute name is null");
		this.classAttributeName = classAttributeName;
	}
	public String getIdAttributeName() {
		return idAttributeName;
	}
	public void setIdAttributeName(String idAttributeName) {
		if(idAttributeName == null)
			this.idAttributeName = DEFAULT_ID_ATT_NAME;
		else
			this.idAttributeName = idAttributeName;
	}
	private void setRandSeed(int randSeed) {
		this.randSeed = randSeed;
		this.rand = new Random(randSeed);
		callsToRandom = 0;
	}
	
	public int getNextRandom(){
		int number = rand.nextInt();		
		callsToRandom++;		
		return number;
	}
	
	public void restartRandom(){
		this.rand = new Random(randSeed);
		callsToRandom = 0;
	}
	
	public Random cloneRandom(){
		Random tmpRand = new Random(randSeed);
		for(int i=0; i<callsToRandom; i++){
			tmpRand.nextInt();
		}
		return tmpRand;
	}
	
	public Random getRestartedRandom(){
		Random tmpRandom = new Random(randSeed);
		return tmpRandom;
	}
	
	public String getResultFolder() {
		return resultFolder;
	}
	
	private void setResultFolder(String resultFolder) throws Exception {
		if(resultFolder == null)
			throw new Exception("ERROR: missing path to result folder");
		File folder = new File(resultFolder);
		if(!folder.exists()){
			System.out.println("Creating directory \"" + resultFolder + "\" for recording results.");
			boolean created = folder.mkdir();
			if(!created){
				throw new Exception("ERROR: Directory for recording results \"" + resultFolder + "\" could not be created.");
			}
		}
		this.resultFolder = resultFolder;
	}
	
	
	/**
	 * If true: read the preset cross-validation experiment from the data. Ignore the locations of data files ({@link #getFileNames()}) for new experiment creation.
	 * <br>if false: create a new cross-validation experiment using {@link #getFileNames()} as data source
	 * @return whether to load a preset experiment
	 */
	public boolean isLoadPresetExperiment() {
		return loadPresetExperiment;
	}

	/**
	 * Note: set results folder first!
	 * @param loadPresetExperiment if true, should load a previously created experiment (not create a new one) 
	 */
	private void setLoadPresetExperiment(boolean loadPresetExperiment) {
		this.loadPresetExperiment = loadPresetExperiment;
		File subfolder = new File(getResultFolder() + "/fold_0"); // at least 1 fold must exist
		if(subfolder.exists()){
			noViews = 0;
			while(true){
				File labeledViewFile = new File(subfolder + "/labeled_view" + noViews + ".arff");
				if(!labeledViewFile.exists())
					break;
				noViews++;
			}
		}
	}

	/**
	 * Reads all data settings from properties file	 
	 * 
	 * @param PropertiesFile Path to the properties file to read from	 
	 * @throws Exception loading properties failed, e.g. properties file not found or error reading a property (missing or wrong type)
	 */
	public void readProperties(String PropertiesFile) throws Exception {
		Properties properties = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(PropertiesFile));
		}catch (FileNotFoundException e) {
			throw new Exception("ERROR: error reading properties file: file " + PropertiesFile + " does not exist.", e);
		}	
		claerSettings();
		System.out.println("Reading the dataset properties from file: " + PropertiesFile);
		
		
		setResultFolder(PropertiesReader.readStringParam(properties, "resultFolder"));
		try{
			setRandSeed(PropertiesReader.readInt(properties, "randomGeneratorSeed"));
		}catch(Exception e){
			// use default (42)
		}
		
		setLoadPresetExperiment(PropertiesReader.readBooleanParam(properties, "loadPresetExperiment"));
		if(!isLoadPresetExperiment()){
			setFileNames(PropertiesReader.readStringListParam(properties, "dataFiles"));
			setNoViews(PropertiesReader.readInt(properties, "noViews"));
		}
		
		setClassNames(PropertiesReader.readStringListParam(properties, "classNames"));
		setClassAttributeName(PropertiesReader.readStringParam(properties, "classAttributeName"));
		setIdAttributeName(PropertiesReader.readStringParam(properties, "idAttributeName"));		
		setClassifierClassNames(PropertiesReader.readStringListParam(properties, "classifiers"));
		setCombinedClassiffierClassName(PropertiesReader.readStringParam(properties, "combinedClassifier"));
			
		if (classifierClassNames.size() > 1 && classifierClassNames.size() != noViews)
			throw new Exception("Number of classifiers (" + classifierClassNames.size() + ") differs from the number of views (" + noViews + "). Specify a classifier for each view or just one that will be used for all views.");
			
		try{
			PrintStream writer = new PrintStream(new FileOutputStream(resultFolder + "/Experiment.txt", true)); 
			printSettings(writer);
			writer.close();
		}catch(Exception e){
			System.out.println("WARNING: could not write the data settings for the experiment in file " + resultFolder + "/Experiment.txt");
		}
	}
	
	public List<String> getClassifierClassNames() {
		return classifierClassNames;
	}

	private void setClassifierClassNames(List<String> classifierClassNames) throws Exception {
		this.classifierClassNames = classifierClassNames;
		
		for(int view=0; view<classifierClassNames.size(); view++)
			getClassifier(view); // checking if class names are valid
	}
	public String getCombinedClassiffierClassName() {
		return combinedClassiffierClassName;
	}
	private void setCombinedClassiffierClassName(String combinedClassiffierClassName) throws Exception {
		this.combinedClassiffierClassName = combinedClassiffierClassName;
		getCombinedClassifier();
	}
	
	private Classifier getClassifier(String name) throws Exception{
		try {			
			ClassLoader loader = ClassLoader.getSystemClassLoader();			
			@SuppressWarnings("rawtypes")
			Class c = loader.loadClass(name);
			Object returnObj = c.newInstance();
			
			// Do not use ID attribute in classification
			FilteredClassifier result = new FilteredClassifier();			
			RemoveByName remove = new RemoveByName();
			remove.setExpression(idAttributeName);
			result.setFilter(remove);			
			result.setClassifier((Classifier) returnObj);
									
			if (returnObj == null) {
				throw new Exception("ERROR: error creating object of class " + name);
			}else
				return result;
		} catch (Exception e) {
			throw new Exception("ERROR: error creating object of class " + name, e);
		}
	}

	public Classifier getCombinedClassifier() throws Exception{
		return getClassifier(combinedClassiffierClassName);
	}
	public Classifier getClassifier(int view) throws Exception{
		if (classifierClassNames.size() < 1)
			throw new Exception("Classifiers not specified");
		
		if (view > noViews)
			throw new Exception("Reading classifier for non-existant view " + view);
		
		int classifierNo = view;
		if(classifierClassNames.size() == 1) // the same classifier specified for all views
			classifierNo = 0;
	
		return getClassifier(classifierClassNames.get(classifierNo));
	}

	/**
	 * Print dataset properties 
	 * @param out : PrintStream for writing   
	 */
	public void printSettings(PrintStream out){
		out.println("DATA SETTINGS: ");
		out.println("\tResult folder: " + resultFolder);
		out.println("\tRandom seed: " + randSeed);
		out.println("\tNumber of views: " + noViews);
		
		if(isLoadPresetExperiment())
			out.println("\tLoading experiment from: " + getResultFolder());
		else{		
			String dataFiles = "";
			for(String fileName : fileNames)
				dataFiles += fileName + "; ";		
			out.println("\tData files: " + dataFiles);
		}
		
		String classes = "";
		for(String className : classNames){
			classes += "'" + className + "'; ";			
		}
		out.println("\tClass names: " + classes);
				
		out.println("\tClass attribute name: " + classAttributeName);
		out.println("\tID attribute name: " + idAttributeName + " (data will be automatically tagged with numeric attribute \"ID\")");		
		
		if (classifierClassNames.size() == 1)
			out.println("\tUsing " + classifierClassNames.get(0) + " for all views");
		else{
			out.println("\tClassifiers used:");
			for(int i=0; i<classifierClassNames.size(); i++){
				out.println("\t\tView " + i + ": " + classifierClassNames.get(i));
			}
		}
	}
}
