package resultsToXML;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import experimentSetting.CoTrainingSettings;
import experimentSetting.DatasetSettings;
import experimentSetting.ExperimentSettings;

/**
 * JAXB annotated class for writing the obtained results to XML
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="")
@XmlRootElement(name = "Results")
public class ExperimentResults {
	@XmlElement(name="Experiments")
	protected List<Experiments> experiments = new ArrayList<Experiments>();

	public List<Experiments> getExperiment() {
		return experiments;
	}

	public void setExperiment(List<Experiments> experiments) {
		this.experiments = experiments;
	}
	
	public void addExperiments(Experiments newexp){
		experiments.add(newexp);
	}
	
	public void toXML(OutputStream stream) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(ExperimentResults.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(this, stream);
	}
	
	public void fromXML(String filename) throws JAXBException{
		try{
			JAXBContext context = JAXBContext.newInstance(ExperimentResults.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			ExperimentResults er = (ExperimentResults) unmarshaller.unmarshal(new File(filename));
			for(Experiments e : er.experiments){
				addExperiments(e);
			}
		}catch(Exception e){
			
		}
	}
	
	public Experiments findExperimentsByProperties(){
		Experiments newExp = new Experiments();
		newExp.setNoIterations(CoTrainingSettings.getInstance().getIterations());
		newExp.setNoSplits(ExperimentSettings.getInstance().getNoSplits());
		newExp.setClassNames(DatasetSettings.getInstance().getClassNames());
		List<Integer> growthSize = new ArrayList<Integer>();
		for(String className : DatasetSettings.getInstance().getClassNames())
			growthSize.add(CoTrainingSettings.getInstance().getGrowthSize(className));
		newExp.setGrowthSize(growthSize);
		
		for(Experiments currExp : this.experiments){
			if(currExp.equals(newExp))
				return currExp;
		}
		experiments.add(newExp);
		return experiments.get(experiments.size()-1);
	}

	@Override
	public String toString() {
		String retStr = "Experimental results:\n";
		for(Experiments exp : experiments)
			retStr += "\t" + exp + "\n";
		return retStr;
	}
}
