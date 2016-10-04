/** 	
 * Name: MeasuresTableModel.java
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

import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

public class MeasuresTableModel extends AbstractTableModel {	
	private static final long serialVersionUID = 1L;
	private String[] columnNames = {"Measure", "For class"};
	private final LinkedList<MeasureForClass> list = new LinkedList<MeasureForClass>();
	
	public void addElement(String measure, String className) {
		// Adds the element in the last position in the list
		if(!list.contains(new MeasureForClass(measure, className))){
			list.add(new MeasureForClass(measure, className));		
			fireTableRowsInserted(list.size()-1, list.size()-1);
		}
	}
	
	public void addElementMeasureImplClass(String measureImplClass, String className) {
		// Adds the element in the last position in the list
		list.add(new MeasureForClass(getMeasureFromImplClass(measureImplClass), className));		
		fireTableRowsInserted(list.size()-1, list.size()-1);
	}
	
	public void removeElement(int index) {
		if(list.size() < index)
			return;
		list.remove(index);	
		fireTableRowsDeleted(index, index);
    }
	
	public void removeAllElements() {
		int listSize = list.size();
		if(listSize == 0)
			return;
		list.clear();	
		fireTableRowsDeleted(0, listSize);
    }
	
	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {		
			case 0: return list.get(rowIndex).getMeasure();
			case 1: return list.get(rowIndex).getClassName();
		}
		return null;
	}

	@Override
	public String getColumnName(int column) {		
		return columnNames[column];
	}
	
	private String getMeasureImplementationClass(String measure){
		switch (measure) {
		case "Accuracy": return "classificationResult.measures.AccuracyMeasure";
		case "F1-measure": return "classificationResult.measures.F1Measure";
		case "Precision": return "classificationResult.measures.Precision";
		case "Recall": return "classificationResult.measures.Recall";
		default: return null;
		}
	}
	
	
	private String getMeasureFromImplClass(String measure){
		switch (measure) {
		case "classificationResult.measures.AccuracyMeasure": return "Accuracy";
		case "classificationResult.measures.F1Measure": return "F1-measure";
		case "classificationResult.measures.Precision": return "Precision";
		case "classificationResult.measures.Recall": return "Recall";
		default: return null;
		}
	}
	
	public String getMeasureImplementationClasses(){
		String res = "";
		for(int i=0; i<getRowCount(); i++){		
			String measure = list.get(i).getMeasure();
			String measureImplClass = getMeasureImplementationClass(measure);
			res += "\"" + measureImplClass + "\" ";
		}
		return res.trim();
	}
	
	public String getClassNamesForMeasures(){
		String res = "";
		for(int i=0; i<getRowCount(); i++){		
			String className = list.get(i).getClassName();
			if(className.equals("not specified"))
				className = "avg";			
			res += "\"" + className + "\" ";
		}
		return res.trim();
	}
	
}
