/** 	
 * Name: GAThresholdOptimiser.java
 * 
 * Purpose: Implementation of genetic algorithm used for threshold optimization of RSSalg and RSSalg_best from the paper J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language Datasets". Acta Polytechnica Hungarica 10 (2). (chapter 3.1) 
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
package algorithms.RSSalg.GA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.InstancesManipulation;
import algorithms.RSSalg.resultStatistic.ClassifierEnsembleList;
import algorithms.co_training.CoTrainingData;
import classificationResult.measures.MeasureIF;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;
import experimentSetting.GASettings;

/**
 * Implementation of genetic algorithm used for threshold optimization of RSSalg and RSSalg<sub>best</sub> from the paper (chapter 3.1):<p>
 * J. Slivka, A. Kovacevic, Z. Konjovic: "Combining Co-Training with Ensemble Learning for Application on Single-View Natural Language 
 * Datasets". Acta Polytechnica Hungarica 10 (2)
 */
public class GAThresholdOptimiser {
	/**
	 * Data (labeled, unlabeled and test instances) used in the experiment
	 */
	protected CoTrainingData data;
	/**
	 * Statistics (instance ids, predictions and confidences for each prediction) about the instances labeled during the algorithm execution 
	 * (instances formally belonging to the unlabeled set, labeled and added to train data by the algorithm)
	 */
	protected ClassifierEnsembleList classifierStatistics;
	/**
	 * Random number generator cloned from DataSettings
	 */
	protected Random rand;
	/**
	 * Algorithm used for candidate evaluation
	 */
	protected CandidateEvaluatorIF evaluator;
	/**
	 * Measure used for candidate evaluation
	 */
	protected MeasureIF measure;
	/**
	 * Current generation of candidates
	 */
	protected List<Candidate> currentGeneration = new ArrayList<Candidate>();
	/**
	 * The next generation of candidates
	 */
	protected List<Candidate> nextGeneration = new ArrayList<Candidate>();
	
	
	/**
	 * Minimal label agreement threshold to explore. E.g. if the minimal label agreement in the obtained statistics is 70%, there is no need to explore
	 * candidates that have lower label threshold than 70%  
	 */
	// label threshold would generally be in the interval [0-100] %. The label thresholds of candidates are generated in the interval [0-100%] 
	// and then scaled to [minimal_label_precent, maximal_label_percent]
	// where minimal_label_precent - label agreement percent of the instance that classifiers most disagree on
	//       maximal_label_percent - label agreement percent of the instance that classifiers most agree on	
	protected int minLabelAgreementThreshold = 0;
	
	/**
	 * Minimal example occurrence threshold to explore. E.g. if the minimal example occurence in the obtained statistics is 30%, there is no need to explore
	 * candidates that have lower label threshold than 30%  
	 */
	// example threshold would generally be in the interval [0-100] %. The example thresholds of candidates are generated in the interval [0-100%] 
	// and then scaled to [minimal_example_precent, maximal_example_percent]
	// where minimal_example_precent - occurrence percent of the instance that is labeled by the least number of classifiers of all instances in the statistic
	//       maximal_example_percent - occurrence percent of the instance that is labeled by the greatest number of classifiers of all instances in the statistic
	protected int minExampleOccuranceThreshold = 0;
		
	/**
	 *  Number of bits that encode the candidates chromosome
	 */
	protected static int noBits = 8;
	
	/**
	 * Sum of candidates fitnesses 
	 */
	private double totalFitnessSum;
	/**
	 * Best candidate found so far
	 */
	protected Candidate bestSoFar = null;
	/**
	 * Current fold in fold-cross validation experiment
	 */
	protected int fold; 
	/**
	 * If a log file for GA should be kept, write it in {results_folder}/fold_{currentFold}/ThresholdOptimiserlog.txt
	 */
	protected String logFileLocation;
	/**
	 * Number of generations in which there was no improval of the result
	 */
	private int noImproval = 0;
	
	/**
	 * Creates the new instance of GAThresholdOptimiser
	 * @param classifierStatistics Statistics (instance ids, predictions and confidences for each prediction) about the instances labeled during the algorithm execution 
	 * @param data Data (labeled, unlabeled and test instances) used in the experiment. Makes a deep copy of the data, so the original object is not modified
	 * @param fold Current fold in fold-cross validation experiment
	 * @throws Exception if there was an error during merging of the views (the final classifier in RSSalg uses a unique attribute set)
	 */
	public GAThresholdOptimiser(ClassifierEnsembleList classifierStatistics, CoTrainingData data, int fold) throws Exception {
		this.evaluator = ExperimentSettings.getInstance().getEvaluator();
		this.rand = DatasetSettings.getInstance().cloneRandom();
		this.data = new CoTrainingData(data);
		 
		if(this.evaluator instanceof RSSalgCandidateEvaluator && !(GASettings.getInstance().isLogGA()) )
			this.data.setTestData(InstancesManipulation.removeAllInstances(this.data.getTestData()));
		
		this.data.mergeViews();
		this.classifierStatistics = classifierStatistics;		
		this.measure = GASettings.getInstance().getOptMeasure();

		setMinLabelAgreementPercent();
		setMinExampleOccurancePercent();
		System.out.println("Starting GA fold "+ fold);
		System.out.println("Label agreement is in the range [" + minLabelAgreementThreshold + ", 100] %");
		System.out.println("Example agreement is in the range [" + minExampleOccuranceThreshold + ", 100] %");
		
		this.fold = fold;
		
		logFileLocation = DatasetSettings.getInstance().getResultFolder() + "/fold_" + fold + "/ThresholdOptimiserlog.txt";
		if(GASettings.getInstance().isLogGA()){
			File logFile = new File(logFileLocation);
			if(logFile.exists())
				logFile.delete();
		}
	}
	
	private void setMinLabelAgreementPercent(){
		this.minLabelAgreementThreshold = (int) Math.floor(classifierStatistics.getMinLabelAgreementPercent() * 100);		
	}
	
	private void setMinExampleOccurancePercent(){
		this.minExampleOccuranceThreshold = (int) Math.floor(classifierStatistics.getMinExampleOccurencePercent() *100);
	}
	
	/**
	 * Scale value from the range [odlMin, oldMax] to the rangde [newMin, newMax]
	 * @param oldMin old minimum
	 * @param oldMax old maximum
	 * @param newMin new minimum
	 * @param newMax new maximum
	 * @param oldValue value to scale in the new range
	 * @return the value scaled in the new range
	 */
	private double scale(double oldMin, double oldMax,  double newMin, double newMax, double oldValue){
		double oldRangeSize = (oldMax - oldMin);  
		double newRangeSize = (newMax - newMin);  
		return (oldValue - oldMin) * newRangeSize / oldRangeSize + newMin;
	}
	
	/**
	 * Scale label agreement from the range [0, 100]% to range [minLabelAgreementThreshold, 100] %
	 * @param labelAgreement label agreement value
	 * @return label agreement scaled to the new range [minLabelAgreementThreshold, 100] %
	 */
	private double scaleToLabelAgreementRange(double labelAgreement){
		double scaledVal = scale(0, 100, minLabelAgreementThreshold, 100, labelAgreement);		
		return scaledVal / 100; // percent to [0, 1]
	}
	
	/**
	 * Scale example occurrence from the range [0, 100]% to range [minExampleOccuranceThreshold, 100] %
	 * @param exampleOccurence example occurrence value
	 * @return label example occurrence scaled to the new range [minExampleOccuranceThreshold, 100] %
	 */
	private double scaleToExampleOccurenceRange(double exampleOccurence){
		double scaledVal = scale(0, 100, minExampleOccuranceThreshold, 100, exampleOccurence);		
		return scaledVal / 100; // percent to [0, 1]
	}
	
	/**
	 * Initialize the first generation for GA by generating the predefined number of random candidates
	 */
	private void initFirstGeneration(){
		int generationSize = GASettings.getInstance().getGenerationSize();
		
		while(currentGeneration.size() < generationSize){						
			int randLabelTS = rand.nextInt(100);
			int randExampleTS = rand.nextInt(100);					
			Candidate newCandidate = new Candidate(scaleToLabelAgreementRange(randLabelTS), scaleToExampleOccurenceRange(randExampleTS), classifierStatistics);
//			if(!currentGeneration.contains(newCandidate)) - uncomment if all candidates in the first generation should be different
				currentGeneration.add(newCandidate);
		}
	}
	
	/**
	 * Evaluate all the candidates from the current generation
	 * @throws Exception if there was an error in evaluating the candidate
	 */
	private void evaluateCurrentGeneration() throws Exception{
		totalFitnessSum = 0;
		
		Candidate lastBest = bestSoFar;		
		bestSoFar = currentGeneration.get(0);
		for(Candidate candidate : currentGeneration){
			evaluator.evaluateCandidate(data, classifierStatistics, candidate, measure);
			totalFitnessSum += candidate.getFitness();
			if(candidate.getFitness() > bestSoFar.getFitness()){
				bestSoFar = candidate;
			}
		}
		if(lastBest!=null && bestSoFar.getFitness() == lastBest.getFitness()) // lastBest will be null in 1. generation
			noImproval++;
		else
			noImproval=0;
	}
	
	/**
	 * Returns the string that represents the current generation of the candidate
	 */
	@Override
	public String toString() {
		String res = "";
		for(Candidate candidate : currentGeneration)
			res += candidate + "\n";		
		return res;
	}

	/**
	 * A convenience method for converting an int value to binary code
	 * @param val int value for conversion
	 * @param min minimal possible value for the int (min where val belongs to range [min, 100], see chapter 3.1 from the paper)
	 * @return binary code for int value
	 */
	private int[] int2bin(double val, int min){
		int max = 100;
		
		int b = (int) Math.round(((val - min) * ((Math.pow(2, noBits)-1)/(max-min))));
		int [] retVal = new int[noBits];
	    for(int i=0; i<noBits; i++) {
	    	int o = (int)b%2;
		    b = b/2;
		    retVal[i]=o;
	    }	    
	    return retVal;
    }
	
	/**
	 * A convenience method for converting the binary code to an int value
	 * @param bin binary code for conversion
	 * @param min minimal possible value for the int (min where val belongs to range [min, 100], see chapter 3.1 from the paper)
	 * @return corresponding int value
	 */
    private int bin2int(int[] bin, int min){
    	int max = 100;
    	
        int ret = 0;
        int b = 1;
        for(int i=0; i<bin.length; i++) {
	        ret += bin[i]*b;
	        b = b*2;
        }
        ret = (int) Math.round(min + ret*(max-min)/(Math.pow(2, noBits)-1));
        return  ret;
    }
    
    /**
     * Select the candidate by performing roulette wheel [Back 1991] selection - the probability of selecting the candidate for reproduction is proportional to its fitness
     * <p>
     * Back and F. Hoffmeister. Extended Selection Mechanisms in Genetic Algorithm. In: Belew, R. K. and Brooker, L. B., editors, Proc. 4th International Conference on Genetic Algorithm, pp. 92-99, 1991.
     * @return the selected candidate
     */
    private Candidate rouletteSelection(){		
		int candidateIndex = 0;		
		double t = totalFitnessSum*((double)rand.nextInt(1000)/1000);
		double s = 0.0;
		double sp = 0.0;
		
		for (int i=0; i<GASettings.getInstance().getGenerationSize(); i++) {
			sp = s;
			s += currentGeneration.get(i).getFitness();
			if((sp<=t)&&(t<s)){
				candidateIndex = i;
				break;
			}
		}
		return currentGeneration.get(candidateIndex);
	}
	
    /**
     * Perform the bi-parental uniform crossover [Syswerda 1989] between the pair of candidates<p>
     * Syswerda. Uniform crossover in genetic algorithms. Proceedings of the 3rd International Conference on Genetic Algorithms, pp. 2-9, 1989.
     * @param candidate1 the 1st candidate
     * @param candidate2 the 2nd candidate
     */
	private void crossover(Candidate candidate1, Candidate candidate2){				
		// crossover for labelChromosome
		int[] labelChromosome1 = int2bin(candidate1.getLabelThreshold()*100, minLabelAgreementThreshold);
		int[] labelChromosome2 = int2bin(candidate2.getLabelThreshold()*100, minLabelAgreementThreshold);
		for (int i = 0; i < noBits; i++) {	
			double d = ((double)rand.nextInt(1000))/1000;
			if(d <= GASettings.getInstance().getCrossoverTS()){
				int temp = labelChromosome1[i];
				labelChromosome1[i] = labelChromosome2[i];
				labelChromosome2[i] = temp;
			}
		}
		candidate1.setLabelThreshold( ((double) bin2int(labelChromosome1, minLabelAgreementThreshold)) / 100);
		candidate2.setLabelThreshold( ((double) bin2int(labelChromosome2, minLabelAgreementThreshold)) / 100);
		
		// crossover ExampleChromosome
		int[] exampleChromosome1 = int2bin(candidate1.getExampleThreshold()*100, minExampleOccuranceThreshold);
		int[] exampleChromosome2 = int2bin(candidate2.getExampleThreshold()*100, minExampleOccuranceThreshold);
		for (int i = 0; i < noBits; i++) {	
			double d = ((double)rand.nextInt(1000))/1000;
			if(d <= GASettings.getInstance().getCrossoverTS()){
				int temp = exampleChromosome1[i];
				exampleChromosome1[i] = exampleChromosome2[i];
				exampleChromosome2[i] = temp;
			}
		} 
		candidate1.setExampleThreshold( ((double) bin2int(exampleChromosome1, minExampleOccuranceThreshold)) / 100);
		candidate2.setExampleThreshold( ((double) bin2int(exampleChromosome2, minExampleOccuranceThreshold)) / 100);
	}
	
	/**
	 * Perform the single-point mutation [Goldberg 1989]<p>
	 * D. Goldberg. Genetic algorithms in search, optimization and machine learning. Addison–Wesley, 1989.
	 * @param candidate candidate to perform the mutation on
	 */
	private void mutation(Candidate candidate){
		// mutate label threshold chromosome
		int[] labelChromosome = int2bin(candidate.getLabelThreshold()*100, minLabelAgreementThreshold);
		for (int i = 0; i < noBits; i++) {	
			double d = ((double)rand.nextInt(1000))/1000;
			if( d <= GASettings.getInstance().getMutationTS()) {
				if (labelChromosome[i] == 0)
					labelChromosome[i] = 1;
				else
					labelChromosome[i] = 0;
			}
		}
		candidate.setLabelThreshold( ((double) bin2int(labelChromosome, minLabelAgreementThreshold)) / 100);
		
		// mutate example threshold chromosome
		int[] exampleChromosome = int2bin(candidate.getExampleThreshold()*100, minExampleOccuranceThreshold);
		for (int i = 0; i < noBits; i++) {	
			double d = ((double)rand.nextInt(1000))/1000;
			if( d <= GASettings.getInstance().getMutationTS()) {
				if (exampleChromosome[i] == 0)
					exampleChromosome[i] = 1;
				else
					exampleChromosome[i] = 0;
			}
		}
		candidate.setExampleThreshold( ((double) bin2int(exampleChromosome, minExampleOccuranceThreshold)) / 100);
	}
	
	/**
	 * Generate the next generation from the current one
	 */
	protected void generateNextGeneration(){
		nextGeneration.clear();
		if(GASettings.getInstance().isElitism()){
			nextGeneration.add(bestSoFar);
		}
		while(nextGeneration.size() < GASettings.getInstance().getGenerationSize()){
			Candidate parent1 = rouletteSelection();
			Candidate parent2 = rouletteSelection();
			crossover(parent1, parent2);
			mutation(parent1);
			mutation(parent2);
			nextGeneration.add(parent1);
			nextGeneration.add(parent2);
		}
	}
	
	/**
	 * Whether or not the GA should stop in this iteration (max number of generations reached or there was no improvement in last x generations)
	 * @param currentIteration the current iteration of GA
	 * @return true if the stopping criteria has been reached, false otherwise
	 */
	private boolean shouldStop(int currentIteration){
		if(currentIteration == GASettings.getInstance().getIterationNo())
			return true; // maximal number of generations reached
		
		if(GASettings.getInstance().getNoImprovalgenerations() > 0)
			if(noImproval == GASettings.getInstance().getNoImprovalgenerations())
				return true; // there was no improvement in last  GASettings.getInstance().getNoImprovalgenerations() generations
		return false;
	}
	
	/**
	 * Run the genetic algorithm for threshold pair optimization in RSSalg
	 * @return the best candidate (found optimum)
	 * @throws Exception if there was an error during evaluation of the threshold pair candidates
	 */
	public Candidate run() throws Exception{
		initFirstGeneration();
		int currentIteration = 0;
		while(!shouldStop(currentIteration)){
			System.out.println("GA iteration " + currentIteration + " of " + GASettings.getInstance().getIterationNo());
			evaluateCurrentGeneration();	
			logGeneration(currentIteration);
			System.out.println("Best candidate: " + bestSoFar);
			System.out.println("------------------------------------------------------------------------------");
			generateNextGeneration();
			currentGeneration.clear();
			currentGeneration.addAll(nextGeneration);
			currentIteration++;
		}
		return bestSoFar;
	}
	
	/**
	 * Used for GA logging
	 * @param generationNo number of generation that is logged
	 */
	private void logGeneration(int generationNo){
		if(!GASettings.getInstance().isLogGA())	
			return;
		Collections.sort(currentGeneration);
			
		try {
			PrintWriter writer = new PrintWriter(new FileOutputStream(new File(logFileLocation), true));
			writer.println("Generation " + generationNo);
			writer.println(toString());
			writer.println();
			writer.println();
			writer.close();
		} catch (FileNotFoundException e) {
			System.out.println("WARNING: error writing threshold optimiser log for fold "+ fold);
			System.out.println(e.getMessage());
		}
	}
}
