package resultsToXML;

import java.text.DecimalFormat;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB annotated class for writing the obtained results to XML
 */

@XmlType(name="TMeasure")
@XmlAccessorType(XmlAccessType.FIELD)
public class Measure {
	@XmlAttribute(required=true)
	protected String name;
	@XmlAttribute(required=true)
	protected double microAveraged;
	@XmlAttribute(required=true)
	protected double macroAveraged;
	@XmlAttribute(required=true)
	protected double stdDev;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getMicroAveraged() {
		return microAveraged;
	}
	public void setMicroAveraged(double microAveraged) {
		this.microAveraged = microAveraged;
	}
	public double getMacroAveraged() {
		return macroAveraged;
	}
	public void setMacroAveraged(double macroAveraged) {
		this.macroAveraged = macroAveraged;
	}
	public double getStdDev() {
		return stdDev;
	}
	public void setStdDev(double stdDev) {
		this.stdDev = stdDev;
	}
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("###.##");
		return name + ": microAveraged=" + df.format(microAveraged) + ", macroAveraged=" + df.format(macroAveraged) + "+/-" + df.format(stdDev);
	}
}
