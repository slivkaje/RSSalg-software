/** 	
 * Name: Measure.java
 * 
 * Purpose: JAXB annotated class for writing the obtained results to XML
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
