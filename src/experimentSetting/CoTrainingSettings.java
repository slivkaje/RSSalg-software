package experimentSetting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import util.PropertiesReader;

/**
 * Singleton object encapsulating all experiment settings for co-training
 */
public class CoTrainingSettings {
	private static CoTrainingSettings instance = null;
	/**
	 * Number of instances in small unlabeled pool u' (if 0 pool is not used)
	 */
	protected int poolSize = 0; 
	/**
	 * Number of examples (per each class) to label and add to initial training set in one iteration of co-training. Key:class name, 
	 * value: no. of examples to label for that class  
	 */
	Map<String, Integer> growthSize = new HashMap<String, Integer>();
	/**
	 * Number of co-training iterations (k)
	 */
	int iterations = 1;
	/**
	 * if set to true finish co-training when all unlabeled instances are labeled (and ignore iteration no.)
	 */
	boolean labelAllUnlabeled = false;  
	/**
	 * Whether or not should the co-training classifier be evaluated in each iteration (if so, the results will be logged) 
	 */
	boolean testEachIteration = false;
	
	/**
	 * Returns a singleton instance of CoTrainingSettings (co-training experiment settings)
	 * @return instance of CoTrainingSettings
	 */
	public static CoTrainingSettings getInstance() {
		if(instance == null) {
			instance = new CoTrainingSettings();
	    }
	    return instance;
	}
	private CoTrainingSettings(){}
	
	public boolean isInitiated(){
		return growthSize.size() != 0;
	}
	
	public void clear(){
		poolSize = 0;
		growthSize.clear();
		iterations = 1;
		labelAllUnlabeled = false;
		testEachIteration = false;
		System.out.println("Co-training settings cleared.");
	}
	
	/**
	 * Returns the number of instances in small unlabeled pool u' (if 0 pool is not used)
	 * @return pool size u'
	 */
	public int getPoolSize() {
		return poolSize;
	}
	private void setPoolSize(int poolSize) throws Exception {
		if(poolSize < 0)
			throw new Exception("Pool size must be equal or grater than 0 (trying to set " + poolSize + ")");
		this.poolSize = poolSize;
	}
	
	/**
	 * For a class: in each iteration of co-training, this is the number of unlabeled examples (most confidently belonging to the class) 
	 * which should be labeled and added to initial training set 
	 * @param className name of the class
	 * @return growth size for that class (p/n)
	 */
	public int getGrowthSize(String className){
		return growthSize.get(className);
	}
	/**
	 * Number of examples (per each class) to label and add to initial training set in one iteration of co-training. Key: class name, 
	 * value: no. of examples to label for that class
	 * @return the growth size for all classes
	 */
	public Map<String, Integer> getGrowthSize() {
		return growthSize;
	}
	private void setGrowthSize(List<String> classNames, int[] growthSize) throws Exception {
		if(classNames.size() != growthSize.length)
			throw new Exception("ERROR: error assigning the growth size: num classes " + classNames.size() + " num growth size per class: " + growthSize.length);
		int totalGrowth = 0;
		for(int i=0; i<classNames.size(); i++){
			totalGrowth += growthSize[i];
			this.growthSize.put(classNames.get(i), growthSize[i]);
		}
		
		if(classNames.size() == 0 || totalGrowth == 0)
			throw new Exception("ERROR: at least 1 example must be labeled in each iteration of co-training");
	}
	
	/**
	 * Returns the number of iterations of co-training. Ignored if all unlabeled data should be labeled (see {@link CoTrainingSettings#isLabelAllUnlabeled()})
	 * @return number of iterations of co-training k
	 */
	public int getIterations() {
		return iterations;
	}
	private void setIterations(int iterations) throws Exception {
		if(!labelAllUnlabeled && iterations < 1)
			throw new Exception("There must be at least 1 iteration of co-training. Trying to set " + iterations + ")");
		this.iterations = iterations;
	}
	
	/**
	 * if true finish co-training when all unlabeled instances are labeled (and ignore iteration no.)
	 * @return whether or not to label all available unlabeled data
	 */
	public boolean isLabelAllUnlabeled() {
		return labelAllUnlabeled;
	}
	private void setLabelAllUnlabeled(boolean labelAllUnlabeled) {
		this.labelAllUnlabeled = labelAllUnlabeled;
	}
	
	/**
	 * Whether or not should the co-training classifier be evaluated in each iteration (if so, the results will be logged) 
	 * @return whether to test each iteration of co-training
	 */
	public boolean isTestEachIteration() {
		return testEachIteration;
	}
	private void setTestEachIteration(boolean testEachIteration) {
		this.testEachIteration = testEachIteration;
	}

	/**
	 * Reads the Co-training settings from properties file
	 * @param propertiesFile path and file name for the properties file
	 * @throws Exception if there was an error reading the properties
	 */
	public void readProperties(String propertiesFile) throws Exception{
		Properties properties = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(propertiesFile));
		}catch (FileNotFoundException e) {
			throw new Exception("ERROR: error reading properties file: file " + propertiesFile + "does not exist", e);
		}	
		clear();
		System.out.println("Reading co-training settings from file: " + propertiesFile);
		
		
		setPoolSize(PropertiesReader.readInt(properties, "poolSize"));
		setGrowthSize(PropertiesReader.readStringListParam(properties, "className"), 
					  PropertiesReader.readIntArrayParam(properties, "growthSize"));
		try{
			setLabelAllUnlabeled(PropertiesReader.readBooleanParam(properties, "labelAllUnlabeledData"));
		}catch(Exception e){
			// if not specified, number of iterations is used
		}
		
		if(!labelAllUnlabeled)
			setIterations(PropertiesReader.readInt(properties, "coTrainingIterations"));
		else
			setIterations(-1);
		try{
			setTestEachIteration(PropertiesReader.readBooleanParam(properties, "testEachIteration"));
		}catch(Exception e){
			// if not specified, don't test each iteration
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
	 * Print co-training parameters used in the experiment
	 * @param out stream for writing   
	 */
	public void printSettings(PrintStream out){	
		out.println("CO-TRAINING SETTINGS:");
		
		if(labelAllUnlabeled)
			out.println("\tIterating co-training until all examples are labeled");
		else 
			out.println("\tNumber of co-training iterations (k): " + iterations);
		
		
		if(poolSize == 0)
			out.println("\tNot using unlabeled pool");
		else
			out.println("\tNumber of examples in unlabeled pool u': " + poolSize);
		
		out.println("\tGrowth size (examples added in each iteration): ");
		for(String key : growthSize.keySet()){
			out.println("\t\t" + key + ": " + growthSize.get(key));
		}
		if(testEachIteration)
			out.println("\tEach iteration of co-training will be evaluated");
	}
}
