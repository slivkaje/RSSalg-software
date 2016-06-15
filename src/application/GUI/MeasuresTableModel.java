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
