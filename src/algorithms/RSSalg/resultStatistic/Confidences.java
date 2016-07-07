package algorithms.RSSalg.resultStatistic;

import java.util.List;
import java.util.Vector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import experimentSetting.DatasetSettings;

/**
 * For each classifier from one ensemble and each possible class: confidence of the classifier that the instance belongs to the class.
 * <br>The confidences are kept in the list ordered as: conf_classifier1_class1, conf_classifier1_class2,... , conf_classifier2_class1, conf_classifier2_class2,...
 * <br>The ordering of classes is determined by the classNames parameter in {@link DatasetSettings}.
 * <br>The class is annotated with JAXB annotations so that the data can easily be saved/loaded to/from an XML file.
 * <br>Note: single classifiers are treated as the ensemble of 1 classifiers
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Confidences {
	/**
	 * The list of confidences sorted as conf_classifier1_class1, conf_classifier1_class2,... , conf_classifier2_class1, conf_classifier2_class2,...
	 */
	@XmlValue
	List<Double> confidence = new Vector<Double>();   
	
	/**
	 * Returns the prediction of the ensemble about the class label. The prediction is determined by calculating a combined ensemble confidence
	 * for each class (co-training style confidence, see {@link #getCombinedConfidence(String)}) and returning the class that has the highest confidence.  
	 * <br>Note: if all classes are equally probable, assigns the instance to the first class listed in classNames parameter of data settings
	 * @return ensemble prediction
	 */
	public String getPrediction(){
		String prediction = DatasetSettings.getInstance().getClassNames().get(0);
		double maxConf = getCombinedConfidence(prediction);
		for(int i=1; i<DatasetSettings.getInstance().getClassNames().size(); i++){
			double conf = getCombinedConfidence(DatasetSettings.getInstance().getClassNames().get(i));
			if(conf > maxConf){
				maxConf = conf;
				prediction = DatasetSettings.getInstance().getClassNames().get(i);
			}
		}
		return prediction;
	}
	
	/**
	 * Returns the prediction of the i-th classifier from the ensemble
	 * @param classifierNo classifier index in the ensemble (starting from 0)
	 * @return the prediction of the classifier for the instance
	 */
	public String getPrediction(int classifierNo){
		String prediction = DatasetSettings.getInstance().getClassNames().get(0);
		double maxConf = getConfidence(prediction, classifierNo);		
		for(int i=1; i<DatasetSettings.getInstance().getClassNames().size(); i++){
			String tmpPrediction = DatasetSettings.getInstance().getClassNames().get(i);
			double conf = getConfidence(tmpPrediction, classifierNo);
			if(conf > maxConf){
				maxConf = conf;
				prediction = tmpPrediction;
			}
		}
		return prediction;
	}
	
	/**
	 * For a given class, returns the confidence of the i-th classifier from the ensemble that the instance belongs to this class. Returns null if the class
	 * is not found (class name not found in classNames parameter of the DataSettings)
	 * @param labelName class name
	 * @param classifierNo classifier index in the ensemble (starting from 0)
	 * @return the confidence of a classifier from the ensemble that the instance belongs to the class
	 */
	public Double getConfidence(String labelName, int classifierNo){
		int noClasses = DatasetSettings.getInstance().getClassNames().size();
		int startInd = classifierNo*noClasses;
		for(int i=startInd; i<startInd+noClasses; i++){
			if(DatasetSettings.getInstance().getClassNames().get(i-startInd).equals(labelName))
				return confidence.get(i);
		}
		return null;
	}
	
	/**
	 * Returns the number of classifiers in the ensemble
	 * @return number of classifiers in the ensemble
	 */
	public int getClassifierNo(){
		int noClasses = DatasetSettings.getInstance().getClassNames().size();
		int confidencesNo = confidence.size();
		return confidencesNo/noClasses;
	}
	
	/**
	 * Returns the ensemble confidence (co-training style) that the instance belongs to the class. The confidence of the ensemble for the class is determined
	 * by multiplying the probabilities output by each of the classifiers from the ensemble. Returns null if the class
	 * is not found (class name not found in classNames parameter of the DataSettings)
	 * @param labelName class name
	 * @return ensemble confidence (co-training style) that the instance belongs to the class or null if class not found  
	 */
	public Double getCombinedConfidence(String labelName){		
		int noClassifiers = getClassifierNo();
		double combinedConfidence = 1;
		for(int viewInd=0; viewInd<noClassifiers; viewInd++){
			Double classifierConfidence = getConfidence(labelName, viewInd);			
			if(classifierConfidence == null)				
				return null;			
			combinedConfidence *= classifierConfidence; 
		}
		return combinedConfidence;
	}
	
	/**
	 * Adds a confidence to the end of the confidence list, NOTE: the confidences must be added in the ordering: conf_classifier1_class1, conf_classifier1_class2,... , 
	 * conf_classifier2_class1, conf_classifier2_class2,...
	 * @param labelConfidence the confidence to add to the list 
	 */
	public void addConfidence(double labelConfidence){
		confidence.add(labelConfidence);
	}
}
