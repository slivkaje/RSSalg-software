package resultsToXML;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
/**
 * JAXB annotated class for writing the obtained results to XML
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="TExperiment")
public class Experiments {
	@XmlAttribute(required=true)
	protected int noIterations;
	@XmlAttribute(required=true)
	protected List<String> classNames = new ArrayList<String>();
	@XmlAttribute(required=true)
	protected List<Integer> growthSize = new ArrayList<Integer>();
	@XmlAttribute(required=true)
	protected int noSplits;
	@XmlElement(name="experiment")
	protected List<Experiments.Experiment> experiment = new ArrayList<Experiments.Experiment>();
	
	public int getNoIterations() {
		return noIterations;
	}
	public void setNoIterations(int noIterations) {
		this.noIterations = noIterations;
	}
	public List<String> getClassNames() {
		return classNames;
	}
	public void setClassNames(List<String> classNames) {
		this.classNames = classNames;
	}
	public List<Integer> getGrowthSize() {
		return growthSize;
	}
	public void setGrowthSize(List<Integer> growthSize) {
		this.growthSize = growthSize;
	}
	public int getNoSplits() {
		return noSplits;
	}
	public void setNoSplits(int noSplits) {
		this.noSplits = noSplits;
	}
	public List<Experiments.Experiment> getExperiment() {
		return experiment;
	}
	public void setExperiment(List<Experiments.Experiment> experiment) {
		this.experiment = experiment;
	}
	public void addExperiment(Experiments.Experiment newExperiment){
		this.experiment.add(newExperiment);
	}
	
	public Experiments.Experiment findExperiment(String name){
		for(Experiments.Experiment e : this.experiment)
			if(e.name.equals(name))
				return e;
		Experiments.Experiment newE = new Experiments.Experiment();
		newE.setName(name);
		this.experiment.add(newE);
		return this.experiment.get(this.experiment.size()-1);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classNames == null) ? 0 : classNames.hashCode());
		result = prime * result
				+ ((growthSize == null) ? 0 : growthSize.hashCode());
		result = prime * result + noIterations;
		result = prime * result + noSplits;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Experiments other = (Experiments) obj;
		if (classNames == null) {
			if (other.classNames != null)
				return false;
		} else if (!classNames.equals(other.classNames))
			return false;
		if (growthSize == null) {
			if (other.growthSize != null)
				return false;
		} else if (!growthSize.equals(other.growthSize))
			return false;
		if (noIterations != other.noIterations)
			return false;
		if (noSplits != other.noSplits)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String retStr = "Experiments [noSplits= " + noSplits + ", noIterations=" + noIterations + ", growth size=";
		for(int i=0; i<growthSize.size(); i++)
			retStr += " " + growthSize.get(i) + "(" + classNames.get(i) + ")";
		retStr += "]\n";
		for(Experiments.Experiment exp : experiment){
			retStr += exp + "\n";
		}
		return retStr;
	}


	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name="")
	public static class Experiment{
		@XmlAttribute(name="name", required=true)
		protected String name; 
		@XmlElement(name="measure")
		protected List<Measure> measures = new ArrayList<Measure>();

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Measure> getMeasures() {
			return measures;
		}
		public void setMeasures(List<Measure> measures) {
			this.measures = measures;
		}	
		public void addMeasure(Measure measure){
			measures.add(measure);
		}
		public Measure findMeasure(String name){
			for(Measure meas : measures)
				if(meas.getName().equals(name))
					return meas;
			
			Measure meas = new Measure();
			meas.setName(name);
			measures.add(meas);
			return measures.get(measures.size()-1);
		}
		@Override
		public String toString() {
			String retStr = "\t\t" + name + "\n";
			for(Measure measure : measures)
				retStr += "\t\t\t" + measure + "\n";
			return retStr;
		}
	}
	
}
