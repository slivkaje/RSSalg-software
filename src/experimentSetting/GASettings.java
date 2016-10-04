/** 	
 * Name: GASettings.java
 * 
 * Purpose:  Class encapsulating all genetic algorithm settings.
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
package experimentSetting;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import classificationResult.measures.MeasureIF;
import util.PropertiesReader;

public class GASettings {
	private static GASettings instance = null; // Singleton
	
	protected int generationSize = 0;
	protected int iterationNo = 0;
	protected double crossoverTS = 0;
	protected double mutationTS = 0;
	protected boolean elitism = true;
	protected double testingTS = 0;  
	protected boolean logGA = true;
	protected int noImprovalgenerations = -1;
	protected MeasureIF optMeasure = null;
	
	public static GASettings getInstance() {
		if(instance == null) {
			instance = new GASettings();
	    }
	    return instance;
	}
	
	public boolean isInitiated(){
		return optMeasure != null;
	}
	
	public void clear(){
		generationSize = 0;
		iterationNo = 0;
		crossoverTS = 0;
		mutationTS = 0;
		elitism = true;
		testingTS = 0;
		logGA = true;
		noImprovalgenerations = -1;
		optMeasure = null;
		System.out.println("GA settings cleared.");
	}
	
	public int getGenerationSize() {
		return generationSize;
	}
	private void setGenerationSize(int generationSize) throws Exception {
		if (generationSize < 2)
			throw new Exception("The generation size in GA should be at least 2. Trying to set " + generationSize);
		this.generationSize = generationSize;
	}
	public int getIterationNo() {
		return iterationNo;
	}
	private void setIterationNo(int iterationNo) throws Exception {
		if (iterationNo < 1)
			throw new Exception("The number of iterations in GA should be at least 1. Trying to set " + iterationNo);
		this.iterationNo = iterationNo;
	}
	public double getCrossoverTS() {
		return crossoverTS;
	}
	private void setCrossoverTS(double crossoverTS) throws Exception {
		if (crossoverTS <= 0  || crossoverTS >1)
			throw new Exception("The crossover threshold in GA should be between 0 and 1. Trying to set " + crossoverTS);
		this.crossoverTS = crossoverTS;
	}
	public double getMutationTS() {
		return mutationTS;
	}
	private void setMutationTS(double mutationTS) throws Exception {
		if (mutationTS <= 0 || mutationTS >1)
			throw new Exception("The mutation threshold in GA should be between 0 and 1. Trying to set " + mutationTS);
		this.mutationTS = mutationTS;
	}
	public boolean isElitism() {
		return elitism;
	}
	private void setElitism(boolean elitism) {
		this.elitism = elitism;
	}
	public double getTestingTS() {
		return testingTS;
	}
	private void setTestingTS(double testingTS) throws Exception {
		if (testingTS < 0 || testingTS > 1)
			throw new Exception("The testing threshold should be between 0 and 1. Trying to set " + testingTS);
		this.testingTS = testingTS;
	}
	public boolean isLogGA() {
		return logGA;
	}
	private void setLogGA(boolean logGA) {
		this.logGA = logGA;
	}
	public MeasureIF getOptMeasure() {
		return optMeasure;
	}
	private void setOptMeasure(MeasureIF optMeasure) {
		this.optMeasure = optMeasure;
	}
	public int getNoImprovalgenerations() {
		return noImprovalgenerations;
	}
	private void setNoImprovalgenerations(int noImprovalgenerations) throws Exception {
		if (noImprovalgenerations < 0 && noImprovalgenerations != -1)
			throw new Exception("The number of generations without improval should be positive or -1 (ignore this value). Trying to set " + noImprovalgenerations);
		this.noImprovalgenerations = noImprovalgenerations;
	}

	public void readProperties(String propertiesFile) throws Exception{
		Properties properties = null;
		try {
			properties = new Properties();
			properties.load(new FileInputStream(propertiesFile));
		}catch (FileNotFoundException e) {
			throw new Exception("ERROR: error reading properties file: file '" + propertiesFile + "' does not exist", e);
		}	
		clear();
		System.out.println("Reading GA properties from file: " + propertiesFile);
		
		setGenerationSize(PropertiesReader.readInt(properties, "generationSize"));
		setIterationNo(PropertiesReader.readInt(properties, "iterations"));
		setCrossoverTS(PropertiesReader.readDoubleParam(properties, "crossoverTS"));
		setMutationTS(PropertiesReader.readDoubleParam(properties, "mutationTS"));
		setElitism(PropertiesReader.readBooleanParam(properties, "elitism"));
		setTestingTS(PropertiesReader.readDoubleParam(properties, "testingTS"));
		setLogGA(PropertiesReader.readBooleanParam(properties, "logGA"));
		setNoImprovalgenerations(PropertiesReader.readInt(properties, "noImprovalGenerations"));
		
		MeasureIF measure = (MeasureIF) PropertiesReader.readObjectParam(properties, "optimizationMeasure");
		if(measure.dependsOnClass()){
			String  optimizationMeasureClass = PropertiesReader.readStringParam(properties, "optimizationMeasureClass");
			if(!optimizationMeasureClass.equals("avg"))				
				measure.setClassName(optimizationMeasureClass);
		}		
		setOptMeasure(measure);
		
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
	 * Print the settings of genetic algorithm used for threshold optimization in RSSalg
	 * @param out : PrintStream for writing   
	 */
	public void printSettings(PrintStream out){
		out.println("GA SETTINGS:");
		
		out.println("\tGeneration size: " + generationSize);		
		out.println("\tCrossover threshold: " + crossoverTS);		
		out.println("\tMutation threshold: " + mutationTS);
		out.println("\tElitism used: " + elitism);
		out.println("\tTesting threshold: " + (testingTS*100) + "%");
		out.println("\tLogging on: " + isLogGA());
		if(noImprovalgenerations > 0){
			out.println("\tStopping criteria: no improval in " + noImprovalgenerations + " or reached the maximum of " + iterationNo + " generations");
		}else
			out.println("\tNumber of GA iterations: " + iterationNo);
	}
}
