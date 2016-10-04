/** 	
 * Name: MeasureForClass.java
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
package application.GUI;

public class MeasureForClass {
	private String measure;
	private String className;
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public MeasureForClass(String measure, String className) {
		super();
		this.measure = measure;
		this.className = className;
	}
	
	@Override
	public String toString() {
		return "MeasureForClass [measure=" + measure + ", className="
				+ className + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(! (obj instanceof MeasureForClass))
			return false;
		MeasureForClass otherMeas = (MeasureForClass) obj;
		boolean equal = otherMeas.className.equals(className)&&otherMeas.measure.equals(measure);
		return equal;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((measure == null) ? 0 : measure.hashCode());
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());		
		return result;
	}
	
	
	
	
}
